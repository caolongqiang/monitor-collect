package com.jimu.monitor.utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yue.liu on 16/4/15.
 */
public class HttpClientFactory {
    private static final Logger log = LoggerFactory.getLogger(HttpClientFactory.class);

    private volatile static CloseableHttpClient httpClient = null;

    static {
        init();
    }

    private synchronized static void init() {
        if (httpClient != null) {
            return;
        }

        RequestConfig requestConfig = getRequestConfig();
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = getPoolingHttpClientConnectionManager();
        ConnectionKeepAliveStrategy connectionKeepAliveStrategy = getConnectionKeepAliveStrategy();

        httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(poolingHttpClientConnectionManager)
                .setUserAgent("Nestia Monitor Server")
                .setKeepAliveStrategy(connectionKeepAliveStrategy)
                .build();
    }


    private static PoolingHttpClientConnectionManager getPoolingHttpClientConnectionManager() {
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
        poolingHttpClientConnectionManager.setMaxTotal(200);
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(100);
        return poolingHttpClientConnectionManager;
    }

    private static RequestConfig getRequestConfig() {
        return RequestConfig.custom()
                .setSocketTimeout(20000)
                .setConnectTimeout(10000)
                .setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY)
                .build();
    }

    private static ConnectionKeepAliveStrategy getConnectionKeepAliveStrategy() {
        return new DefaultConnectionKeepAliveStrategy() {
            @Override
            public long getKeepAliveDuration(
                    HttpResponse response,
                    HttpContext context) {
                long keepAlive = super.getKeepAliveDuration(response, context);
                if (keepAlive == -1) {
                    //如果服务器没有设置keep-alive这个参数，我们就把它设置成5秒
                    keepAlive = 5000;
                }
                return keepAlive;
            }
        };
    }

    public static CloseableHttpClient getHttpClient() {
        if (httpClient == null) {
            init();
        }
        return httpClient;
    }

}
