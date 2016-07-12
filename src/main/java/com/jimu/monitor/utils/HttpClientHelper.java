package com.jimu.monitor.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

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

        return get(url, Maps.newHashMap());
    }

    public static String get(String url, Map<String, String> headerMap) {
        Preconditions.checkNotNull(url, "url不能为空");

        HttpEntity entity = null;
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpClient httpClient = HttpClientFactory.getHttpClient();
        String content = StringUtils.EMPTY;
        if (MapUtils.isNotEmpty(headerMap)) {
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                httpGet.setHeader(entry.getKey(), entry.getValue());
            }
        }

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

    public static String post(String url, String content) {
        return post(url, content, Maps.newHashMap());
    }

    public static String post(String url, String content, Map<String, String> headerMap) {
        Preconditions.checkNotNull(url, "url不能为空");
        Preconditions.checkNotNull(content, "content不能为空");

        logger.debug("url is:{},  content is {}, headerMap is :{}", url, content, headerMap.size());

        HttpEntity entity = null;
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
            logger.info("error url is :{}", url, e.getMessage(), e);
        } finally {
            consume(entity);
        }

        return result;
    }

    private static void consume(HttpEntity entity) {
        try {
            EntityUtils.consume(entity);
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }
    }
}
