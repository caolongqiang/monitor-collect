package com.jimu.monitor.collect.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Created by yue.liu on 16/4/15.
 */

public class HttpClientHelper {
    private final static Logger logger = LoggerFactory.getLogger(HttpClientHelper.class);

    private static void consume(HttpEntity entity) {
        try {
            EntityUtils.consume(entity);
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public static String getString(String url) {
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

}
