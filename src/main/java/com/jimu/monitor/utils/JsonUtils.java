package com.jimu.monitor.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by yue.liu on 16/4/15.
 */
public class JsonUtils {

    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);

    public static final ObjectMapper OM = new ObjectMapper();

    static {
        OM.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OM.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static String writeValueAsString(Object obj) {
        String str = StringUtils.EMPTY;

        if (obj == null) {
            return str;
        }

        try {
            str = OM.writeValueAsString(obj);
        } catch (IOException e) {
            log.warn("{}, {}", e.getMessage(), e);
        }
        return str;
    }

    public static <T> T readValueAsFile(File file, TypeReference<T> typeReference) {
        try {
            return OM.readValue(file, typeReference);
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
            return null;
        }
    }

    public static <T> T readValue(String json, TypeReference<T> typeReference) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            return OM.readValue(json, typeReference);
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
            return null;
        }
    }

    public static <T> T readValue(String jsonStr, Class<T> clazz) {
        return readValue(jsonStr, clazz, StringUtils.EMPTY);
    }

    public static <T> T readValue(String jsonStr, Class<T> clazz, String additionalReadErrLog) {
        if (StringUtils.isEmpty(jsonStr)) {
            return null;
        }
        T t = null;
        try {
            t = OM.readValue(jsonStr, clazz);
        } catch (IOException e) {
            log.warn("{}, {}", e.getMessage(), additionalReadErrLog, e);
        }
        return t;
    }

}
