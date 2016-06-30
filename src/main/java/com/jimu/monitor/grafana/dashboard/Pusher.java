package com.jimu.monitor.grafana.dashboard;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.jimu.monitor.utils.HttpClientFactory;
import com.jimu.monitor.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.jimu.monitor.Configs.config;

/**
 * Created by zhenbao.zhou on 16/6/4.
 *
 * 把结果异步push给grafana.
 */
@Slf4j
public enum Pusher {

    pusher;

    private final static Executor executor = Executors.newFixedThreadPool(5);

    Map<String, String> headerMap = ImmutableMap.of("Content-Type", "application/json", "Accept", "application/json",
            "Authorization", config.getGrafanaAuthorization());

    /**
     * 异步post
     * @param json
     * @return
     */
    public CompletableFuture<String> asyncPost(String json) {
        return CompletableFuture.supplyAsync(() -> post(url(), json, headerMap), executor);
    }

    /**
     * 同步post
     * @param json
     * @return
     */
    public String syncPost(String json) {
        return post(url(), json, headerMap);
    }

    /**
     * 检查post返回的结果 是否表示成功
     * @param result
     * @return
     */
    public boolean checkPostResult(String result) {
        try {
            Map<String, String> map = JsonUtils.readValue(result, Map.class);
            if (map != null && StringUtils.equals(map.get("status"),"success")) {
                return true;
            }
        } catch (Exception e) {
            log.warn("error in parse json value. result:{}", result, e);
        }
        return false;
    }

    private String url() {
        return config.getGrafanaApiUri() + "/api/dashboards/db";
    }

    /**
     * 这个post方法封装的很垃圾, 但是我没想好怎么封装成一个公有的逻辑(理论上来说,应该都在httpclienthelper里).
     * 先放在pusher类里, 设置为private
     *
     * @param url
     * @param content
     * @param headerMap
     * @return
     */
    private String post(String url, String content, Map<String, String> headerMap) {
        Preconditions.checkNotNull(url, "url不能为空");
        Preconditions.checkNotNull(content, "content不能为空");

        log.debug("url is:{},  content is {}, headerMap is :{}", url, content, headerMap.size());

        HttpEntity entity;
        HttpPost post = new HttpPost(url);
        CloseableHttpClient httpClient = HttpClientFactory.getHttpClient();
        String result = StringUtils.EMPTY;
        try {
            post.setEntity(new StringEntity(content));
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                post.setHeader(entry.getKey(), entry.getValue());
            }
            entity = httpClient.execute(post).getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
            }
        } catch (Exception e) {
            log.info("error url is :{}", url, e.getMessage(), e);
        }

        return result;
    }

}
