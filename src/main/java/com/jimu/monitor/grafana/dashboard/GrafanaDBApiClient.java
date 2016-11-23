package com.jimu.monitor.grafana.dashboard;

import com.google.common.collect.ImmutableMap;
import com.jimu.monitor.utils.HttpClientHelper;
import com.jimu.monitor.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import static com.jimu.monitor.Configs.config;

/**
 * Created by zhenbao.zhou on 16/6/4.
 *
 * 和Grafana交互的api
 */
@Slf4j
public class GrafanaDBApiClient {


    final static  Map<String, String> headerMap = ImmutableMap.of("Content-Type", "application/json", "Accept", "application/json",
            "Authorization", config.getGrafanaAuthorization());

    /**
     * 生成dashboard的json push给grafana
     */
    public enum Pusher {

        pusher;


        /**
         * 同步post
         *
         * @param json
         * @return
         */
        public String syncPost(String json) {
            return HttpClientHelper.post(url(), json, headerMap);
        }

        /**
         * 检查post返回的结果 是否表示成功
         *
         * @param result
         * @return
         */
        public boolean checkPostResult(String result) {
            try {
                Map<String, String> map = JsonUtils.readValue(result, Map.class);
                if (map != null && StringUtils.equals(map.get("status"), "success")) {
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
    }


    /**
     * 抓取grafana里现有的所有的dashboard
     */
    public enum DBFetcher{

        fetcher;

        public String fetch() {
            log.info("start fetch grafana now dashboard:{}", url());

            return HttpClientHelper.get(url(), headerMap);
        }

        private String url() {
            return config.getGrafanaApiUri() + "/api/search";
        }
    }
}
