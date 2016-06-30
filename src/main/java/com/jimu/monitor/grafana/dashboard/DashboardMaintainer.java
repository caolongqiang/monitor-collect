package com.jimu.monitor.grafana.dashboard;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.jimu.monitor.grafana.dashboard.bean.Option;
import com.jimu.monitor.grafana.dashboard.bean.RenderDbParam;
import com.jimu.monitor.utils.HttpClientHelper;
import com.jimu.monitor.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

import static com.jimu.monitor.Configs.config;
import static com.jimu.monitor.grafana.dashboard.DashboardJsonGenerator.generator;
import static com.jimu.monitor.grafana.dashboard.Pusher.pusher;

/**
 * 维护线上可以使用的Dashboard, 总是包含最新数据的.
 *
 * 总体来说, 做三件事情
 * <p>
 * 1. 从某个graphite-api里, 取出所有的metric. 选出所有符合我们条件的metrics,
 * </p>
 * <p>
 * 2. 将这些metric 按照规则, 封装成第三步所需要的格式
 * </p>
 * <p>
 * 3. 将第二步的结果生成json, post给grafana
 * </p>
 *
 * Created by zhenbao.zhou on 16/6/4.
 */
@Slf4j
public class DashboardMaintainer implements Runnable {

    private final static String DELIMITER = ".";

    @Override
    public void run() {
        log.info("DashboardMaintainer starts running");
        List<RenderDbParam> paramList = fillRenderParam(fetchSuitableMetric());

        for (RenderDbParam param : paramList) {
            try {
                String json = generator.render(param);
                String result = pusher.syncPost(json);
                boolean ret = pusher.checkPostResult(result);
                if (ret) {
                    log.info("post success. result:{}", result);
                } else {
                    log.warn("post failed. result{},  json:{}", result, json);
                }
            } catch (Throwable t) {
                log.error("error in render or post", t);
            }
        }
    }

    /**
     * 从 api的返回结果里, 筛选出符合我们要求的metrics
     * 
     * @return List<String></String>
     * @throws RuntimeException
     */
    private List<String[]> fetchSuitableMetric() {

        String graphiteApi = config.getGraphiteApi();
        Preconditions.checkNotNull(graphiteApi);

        log.info("start fetch graphite url : {}", graphiteApi);

        String content = HttpClientHelper.get(graphiteApi);
        List<String> originMetrics = JsonUtils.readValue(content,
                new TypeReference<List<String>>() {
                });

        log.info(" end fetch graphite url:{}.", graphiteApi);
        if (originMetrics == null) {
            log.error("error in parse content. url is {}, content is {}", graphiteApi, content);
            return Lists.newArrayList();
        }

        return originMetrics.parallelStream()
                .map(metric -> metric.split("\\."))
                .filter(metrics -> metrics.length == 5)
                // .filter(metrics -> metrics[0].equalsIgnoreCase("s"))
                .collect(Collectors.toList());
    }

    /**
     * 合并已有metrics, 生成这些metrics所需要的dashboard
     * 
     * @param metrics metric数组的列表. metric 的格式是  a.b.c.d.e
     * @return 聚合之后的RenderDbParam结果
     */
    private List<RenderDbParam> fillRenderParam(List<String[]> metrics) {
        Preconditions.checkNotNull(metrics);
        metrics.forEach(metric -> Preconditions.checkArgument(metric.length == 5, "每个指标的长度必须为5"));

        log.info("start fill render param. metrics size:{}", metrics.size());

        // 存template的query 与 domain关系的multimap
        Multimap<String, String> queryToDomainsMap = HashMultimap.create();

        // 存template的query 与 metric关系的multimap
        Multimap<String, String> queryToMetricsMap = HashMultimap.create();

        // 前面已经检查过metrics的长度了,所以这里不担心数组越界
        metrics.forEach(metricArray -> {
            String query = metricArray[0] + DELIMITER + metricArray[1] + DELIMITER + metricArray[2];
            queryToDomainsMap.put(query, metricArray[3]);
            queryToMetricsMap.put(query, metricArray[4]);
        });

        List<RenderDbParam> paramList = Lists.newArrayListWithExpectedSize(queryToDomainsMap.keySet().size());

        for (String query : queryToDomainsMap.keySet()) {
            List<Option> domainOptions = queryToDomainsMap.get(query).stream()
                    .map(domainName -> new Option(domainName, false)).collect(Collectors.toList());
            List<Option> metricOptions = queryToMetricsMap.get(query).stream()
                    .map(metricName -> new Option(metricName, false)).collect(Collectors.toList());
            metricOptions.add(Option.ALL_OPTION());

            RenderDbParam param = RenderDbParam.builder().title(query).domainNameQuery(query)
                    .domainOptions(domainOptions).metricOptions(metricOptions).build();
            paramList.add(param);
        }

        log.info("end fill render param. renderParam size:{}", paramList.size());

        return paramList;
    }

    public static void main(String[] args) {
        DashboardMaintainer m = new DashboardMaintainer();
        m.run();
        System.out.printf("lall");
    }
}
