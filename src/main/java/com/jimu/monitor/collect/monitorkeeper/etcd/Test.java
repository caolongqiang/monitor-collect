package com.jimu.monitor.collect.monitorkeeper.etcd;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;

import java.util.concurrent.Future;

/**
 * 这个代码本来是想来测试 events接口的. 但是取不到数据. 尴尬
 * refer to http://git.jimubox.com/snippets/24
 * Created by zhenbao.zhou on 16/7/1.
 */
public class Test {

    public static void main(String[] args) {
        AsyncHttpClient c = new AsyncHttpClient();

        // We are just interested to retrieve the status code.
        c.prepareGet("https://daikon.jimubox.com/api/events").setRequestTimeout(-1).execute(new AsyncHandler() {

            public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
                System.out.println("body part:" + bodyPart);
                return STATE.CONTINUE;
            }

            @Override
            public STATE onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
                System.out.println("onStatusReceived" + responseStatus);
                return STATE.CONTINUE;
            }

            @Override
            public STATE onHeadersReceived(HttpResponseHeaders headers) throws Exception {
                System.out.println("onHeadersReceived" + headers);
                return STATE.CONTINUE;
            }

            @Override
            public Object onCompleted() throws Exception {
                System.out.println("onCompleted");
                return null;
            }

            @Override
            public void onThrowable(Throwable t) {
                System.out.println("onThrowable");
            }
        });
    }
}
