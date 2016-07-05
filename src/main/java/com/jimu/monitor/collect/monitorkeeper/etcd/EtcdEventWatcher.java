package com.jimu.monitor.collect.monitorkeeper.etcd;

import com.jimu.common.jmonitor.JMonitor;
import com.jimu.monitor.utils.JsonUtils;
import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.jimu.monitor.Configs.config;

/**
 * 监听 etcd实时推送接口的信息, 如果发生变化, 则重新抓取一次etcd全量信息 Created by zhenbao.zhou on 16/7/5.
 */
@Slf4j
@Service
public class EtcdEventWatcher {

    /**
     * 启动watcher的线程池
     */
    private final static ScheduledExecutorService workerExecutor = Executors.newSingleThreadScheduledExecutor();

    /**
     * 负责重新刷新etcd内容
     */
    private final static ExecutorService refreshExecutor = Executors.newSingleThreadExecutor();

    private final static int CONNECTION_TIMEOUT_IN_MS = 1000; // 1s
    private final static int REQUEST_TIMEOUT_IN_MS = 3 * 60 * 1000; // 3min
    private final static int WORKER_DELAY_TIME_IN_SECOND = 20; // 20s
    private final static int INIT_DELAY_IN_MS = 1000; // 1s

    private final static int MAX_CONNECTION = 2;

    private final static AsyncHttpClientConfig asyncConfig = new AsyncHttpClientConfig.Builder()
            .setMaxConnections(MAX_CONNECTION).setRequestTimeout(REQUEST_TIMEOUT_IN_MS)
            .setConnectTimeout(CONNECTION_TIMEOUT_IN_MS).build();

    @Autowired
    EtcdResultContainer etcdResultContainer;

    private final static AsyncHttpClient client = new AsyncHttpClient(asyncConfig);

    // 单例
    private static WatchWorker watchWorker = new EtcdEventWatcher().new WatchWorker();

    public void watch() {
        workerExecutor.schedule(watchWorker, INIT_DELAY_IN_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * 监控url 内容的worker 如果push过来的content满足要求时, 会开一个线程, 在另外一个线程里,reload 全量etcd数据
     */
    class WatchWorker implements Runnable {
        private volatile boolean running = false;

        @Override
        public void run() {
            if (running) {
                log.debug("task is running. exists");
                workerExecutor.schedule(watchWorker, WORKER_DELAY_TIME_IN_SECOND, TimeUnit.SECONDS);
                return;
            }

            running = true;

            client.prepareGet(config.getEtcdEventApi()).execute(new AsyncHandler() {
                @Override
                public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
                    dealContent(bodyPart);
                    return STATE.CONTINUE;
                }

                @Override
                public STATE onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
                    log.debug("on status received :" + responseStatus.getStatusText());
                    return STATE.CONTINUE;
                }

                @Override
                public STATE onHeadersReceived(HttpResponseHeaders headers) throws Exception {
                    return STATE.CONTINUE;
                }

                @Override
                public Object onCompleted() throws Exception {
                    running = false;
                    JMonitor.recordOne("etcd_watcher_on_complete");
                    workerExecutor.schedule(watchWorker, WORKER_DELAY_TIME_IN_SECOND, TimeUnit.SECONDS);
                    return null;
                }

                // 如果连接中断, 这里会抛出一个ConnectionException.
                @Override
                public void onThrowable(Throwable t) {
                    running = false;
                    log.warn("on throwable. change running to false", t);
                    JMonitor.recordOne("etcd_watcher_on_throwable");
                    workerExecutor.schedule(watchWorker, WORKER_DELAY_TIME_IN_SECOND, TimeUnit.SECONDS);
                }
            });
        }

        /**
         * content的信息请参考 http://git.jimubox.com/snippets/24 目前看到的信息是
         * 
         * <pre>
         *     {
         *         "action":"takeover", //动作 takeover 为发布
         *         "app":"merak-in-auth", //应用
         *         "env":"production", //环境
         *         "gen":"26"  //发布次数
         *     }
         * </pre>
         * 
         * @param responseBodyPart
         */
        void dealContent(HttpResponseBodyPart responseBodyPart) {
            final int MIN_LENGTH = 4;
            if (responseBodyPart.length() < MIN_LENGTH) {
                log.debug("got message. length:{}", responseBodyPart.length());
                return;
            }

            String content = new String(responseBodyPart.getBodyPartBytes());
            log.info("deal content. content:{}", content);
            EventBody eventBody = JsonUtils.readValue(content, EventBody.class);
            if (eventBody == null) {
                JMonitor.recordOne("error event body.");
                log.warn("error in parse event body. content is:{}", content);
                return;
            }

            if (!eventBody.shouldReload()) {
                log.info("正确收到eventbody. 但是不需要执行refresh操作, exists, content:{}", content);
                return;
            }

            JMonitor.recordOne("refresh ectd by watcher.");
            log.info("refresh etcd content by watcher. go");

            // TODO 这个地方将来性能可能也有问题. 其实最好的方法是, 只reload这一个发生了变化的job的相关属性.
            refreshExecutor.execute(() -> {
                etcdResultContainer.refreshJob();
            });

        }
    }

    @Getter
    @Setter
    static class EventBody {
        private final static String TAKEOVER = "takeover"; // takeover 为发布
        String action;
        String app;
        String env;
        String gen;

        public boolean shouldReload() {
            return StringUtils.equals(TAKEOVER, action);
        }
    }

    public static void main(String[] args) {
        EtcdEventWatcher watcher = new EtcdEventWatcher();
        watcher.watch();
    }
}
