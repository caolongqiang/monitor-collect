<%@ page import="java.util.List" %><%@ page import="com.google.common.collect.Lists" %><%@ page import="java.util.Random" %><%@ page contentType="text/plain;charset=UTF-8" language="java" %><%    List<String> key1 = Lists.newArrayList(            "qconfig_get",            "rebuild_price_collect_room_price_type_name_dynamic_cash_back",            "rebuild_price_handler_qhstats_full_protobuf",            "push_guidePrice",            "rebuild_price_collect_hotel_price_type_name_nearby_discount",            "rebuild_price_handler_qhstats_full_protobuf_serialize",            "KRYO_INIT",            "rebuild_price_collect_price_type_name_dynamic_reduce_with_add_price",            "DetailPT_Size",            "rebuild_price_collect_price_type_dynamic_reduce_hotel",            "rebuild_price_collect_lose_qta_ratio",            "guideHotelPriceDetail_redis_hdel",            "redisService_hdel_result",            "rebuild_price_collect_room_price_type_name_dynamic_reduce_with_add_price",            "rebuild_price_collect_hotel_price_type_name_spring_festival_redbag",            "rebuild_price_collect_lose_tuan",            "rebuild_price_collect_price_type_name_dynamic_reduce",            "qhstats_collect_log_full_protobuf",            "guideHotelPriceDetail_redis_hget",            "rebuild_price_collect_lose_bnb"    );    List<String> key2 = Lists.newArrayList(            "ThreadPool_rebuild_price_collect_completedTaskCount",            "ThreadPool_rebuild_price_collect_currentQueueSize",            "ThreadPool_rebuild_price_handler_qhstats_full_taskCount",            "JVM_ParNew_Count",            "TOMCAT_\"http-bio-8080\"_bytesSent_Count",            "TOMCAT_\"http-bio-8080\"_request_Count",            "JVM_ConcurrentMarkSweep_Count"    );    for(String s1 : key1) {        out.println(s1+"_Count=" + new Random().nextInt(100)+"\n");        out.println(s1+"_Time=" + new Random().nextInt(10000)+"\n");    }    for(String s2 : key2) {        out.println(s2+"=" + new Random().nextInt(100)+"\n");    }%>