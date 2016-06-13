package com.jimu.monitor.collect;

import com.google.common.collect.ImmutableList;
import com.jimu.monitor.collect.bean.Domain;
import com.jimu.monitor.collect.bean.Group;
import com.jimu.monitor.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.List;

/**
 * Created by zhenbao.zhou on 16/5/28.
 * 用来生成group的. 忽略这个文件
 */
@Slf4j
public class GroupTest {

    @Test
    public void testGenerate(){
        Group group = new Group();
        Domain domain1 = new Domain();
        domain1.setHost("collect7");
        domain1.setUrl("http://l-hpricecollect7.h.cn5.qunar.com:8080/qmonitor.jsp");
        Domain domain2 = new Domain();
        domain2.setHost("collect8");;
        domain2.setUrl("http://l-hpricecollect8.h.cn5.qunar.com:8080/qmonitor.jsp");

        List<Domain> domainList = ImmutableList.of(domain1, domain2);

        group.setDomainList(domainList);
        group.setName("collect group");
        group.setDepartment("d1");
        group.setType(Group.Type.HTTP);


        Group group2 = new Group();
        Domain domain3 = new Domain();
        domain3.setHost("admin4");
        domain3.setUrl("http://l-qtaspa4.h.cn8.qunar.com:8080/monitor.jsp");

        // 这台机器不存在
        Domain domain4 = new Domain();
        domain4.setHost("admin1");;
        domain4.setUrl("http://l-qtaspa1.h.cn8.qunar.com:8080/monitor.jsp");

        Domain domain5 = new Domain();
        domain5.setHost("admin14");;
        domain5.setUrl("http://l-qtaspa2.h.cn8.qunar.com:8080/monitor.jsp");

        List<Domain> domainList2 = ImmutableList.of(domain3, domain4, domain5);

        group2.setDomainList(domainList2);
        group2.setName("hotel_rt_linkage");
        group2.setType(Group.Type.HTTP);
        group.setDepartment("d2");

        log.info("group1:{}", JsonUtils.writeValueAsString(group));
        log.info("group2:{}", JsonUtils.writeValueAsString(group2));

    }
}
