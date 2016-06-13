package com.jimu.monitor.utils;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by yue.liu on 16/4/15.
 */
public class HttpClientHelper {
    private final static Logger logger = LoggerFactory.getLogger(HttpClientHelper.class);

    /**
     * Get某个url的内容. 超时时间在HttpClientFactory设置的. 具体设置是 SocketTimeout(2000) ConnectTimeout(1000)
     * 
     * @param url
     * @return
     */
    public static String get(String url) {
        Preconditions.checkNotNull(url, "url不能为空");

        HttpEntity entity = null;
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpClient httpClient = HttpClientFactory.getHttpClient();
        String content = StringUtils.EMPTY;
        try {
            entity = httpClient.execute(httpGet).getEntity();
            if (entity != null) {
                content = EntityUtils.toString(entity);
            }
        } catch (Exception e) {
            logger.info("error url is :{}", url, e.getMessage(), e);
        } finally {
            consume(entity);
        }

        return content;
    }


    private static void consume(HttpEntity entity) {
        try {
            EntityUtils.consume(entity);
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }
    }
}
