package com.jimu.monitor.collect;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.jimu.monitor.collect.bean.Domain;
import com.jimu.monitor.collect.bean.Group;
import com.jimu.monitor.collect.bean.GroupMetric;
import com.jimu.monitor.collect.bean.Packet;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.List;

/**
 * Created by zhenbao.zhou on 16/5/27.
 */
public class HttpCombinerTest extends TestCase {

    @Test
    public void testCombine() {
        Group  group = new Group();
        group.setName("test");

        Domain domain1 = new Domain();
        domain1.setHost("domain1");
        domain1.setUrl("http://www.jimu.com/aa");

        Domain domain2 = new Domain();
        domain2.setHost("domain2");
        domain1.setUrl("http://www.jimu.com/domain2");

        List<Domain> domains = Lists.newArrayList(domain1, domain2);

        group.setDomainList(domains);

        Packet packet1 = Packet.builder().domain(domain1).
                rawMeasurements(ImmutableMap.of("AA_Time", 111111000000.0, "BB", 4.2, "CC_count", 420.0,
                        "JVM_MarkSweepCompact_Count", 120.0, "aaa", -1.2))
                .build();

        Packet packet2 = Packet.builder().domain(domain2).
                rawMeasurements(ImmutableMap.of("AA_Time", 211111000000.0,
                        "BB", 3.1, "CC_count", 480.0, "aaa", 5.2))
                .build();


        List<Packet> packets = Lists.newArrayList(packet1, packet2);

        GroupMetric groupMetric = new HttpCombiner().combine(group, packets);

        assertEquals(groupMetric.getDomainMeasurements().get("domain1", "BB"), 4.2);
        assertEquals(groupMetric.getGroupMeasurements().get("BB"), 7.3);

        assertEquals(groupMetric.getDomainMeasurements().get("domain2", "CC_count"), 8.0);
        assertEquals(groupMetric.getGroupMeasurements().get("CC_count"), 15.0);

        assertEquals(groupMetric.getDomainMeasurements().get("domain1", "JVM_MarkSweepCompact_Count"), 2.0);
        assertEquals(groupMetric.getGroupMeasurements().get("JVM_MarkSweepCompact_Count"), 2.0);
    }
}
