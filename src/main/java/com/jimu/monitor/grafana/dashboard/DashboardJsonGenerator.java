package com.jimu.monitor.grafana.dashboard;

import com.google.common.base.Preconditions;
import com.google.common.io.Resources;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

import com.jimu.monitor.grafana.dashboard.bean.RenderDbParam;
import com.jimu.monitor.utils.JsonUtils;

import java.io.StringWriter;
import java.util.Properties;

import static com.jimu.monitor.Configs.config;

/**
 * Db = DashBoard. 意思是grafana里的面板
 *
 * 这是一个单例
 *
 * Created by zhenbao.zhou on 16/6/4.
 */
public enum DashboardJsonGenerator {

    generator;

    // template是线程安全的
    Template template;

    DashboardJsonGenerator() {
        Properties p = new Properties();
        p.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, Resources.getResource(config.getVmPath()).getPath());

        VelocityEngine ve = new VelocityEngine();
        ve.init(p);

        template = ve.getTemplate(config.getNewDbTemplateName(), "utf-8");
    }


    /**
     * 渲染模板,
     * @param param refer to #RenderDbParam
     * @return 生成dashboard所需要的json
     */
    public String render(RenderDbParam param) {
        Preconditions.checkNotNull(param);

        VelocityContext context = new VelocityContext();

        context.put("title", param.getTitle());
        context.put("domain_name_query", param.getDomainNameQuery());
        context.put("version", System.currentTimeMillis());
        context.put("metric_selector_option", JsonUtils.writeValueAsString(param.getMetricOptions()));
        context.put("domain_selector_option", JsonUtils.writeValueAsString(param.getDomainOptions()));

        /* 渲染模板 */
        StringWriter w = new StringWriter();

        template.merge(context, w);
        return w.toString();
    }

    public static void main(String[] args) {
        System.out.println("ll:" + DashboardJsonGenerator.generator.render(null));
    }
}
