# 监控

[TOC]

## 为什么需要监控

1. 事前即时预警,发现故障
2. 事中定位问题,尽快解决故障
3. 事后提供数据,分析事情原因

## 监控什么

一. 功能/性能相关的时间和次数
- 下单量, 下单响应时间
- 页面访问次数, 页面平均响应时间
- 支付量
- 缓存命中率

二. 资源使用情况
- 服务器(CPU, IO,内存,磁盘)
- 网络(连接数, 流量)
- 数据库(耗时, 慢查询)

三. 系统间的边界
- 接口调用次数
- 接口响应时间
- 接口成功/失败/异常统计

## 怎么监控

### 监控系统框架图
![grafana.png-45.1kB][1]

#### Applications
需要监控的应用.  两种用法
1. 用Jmonitor_client记录数据, 提供![image_1al6g0b088l821s1uqg98h1m3i1s.png-161.1kB][2] 数据
2. 应用直接往Graphite写数据. (不推荐)

#### Monitor collector
收集应用提供的数据, 汇总数据, 并上报给Graphite

#### Graphite
Graphite是一个开源项目，基于Python，专注做两个事情：存储数字时间序列，并支持以丰富的函数获取数据，以图片或者json的格式。由三个组件组成:

1. Carbon - 负责接收数据
2. Whisper - 将收到的数据存储成whisper文件（类似RRD文件）
3. graphite-api - 支持Restful的URL获取图片或JSON数据，URL里可以带各种有用的函数

#### Grafana Dashboard
Grafana 是一款开源的图形控制台, 展示 Graphite 的数据和图片

### 记录数据
用户需要做的是, 利用Jmonitor_client 包提供的api, 写入数据. 开发同学需要做4步

#### 一 引用jar包
```
在pom.xml里 增加
<dependency>
    <groupId>com.jimu.common</groupId>
    <artifactId>jmonitor</artifactId>
    <version>${新的版本}</version>
</dependency>
```
#### 二 在web.xml里增加配置
```
<servlet>
    <servlet-name>DisplayMetricServlet</servlet-name>
    <servlet-class>com.jimu.common.jmonitor.support.http.DisplayMetricServlet</servlet-class>
</servlet>
<servlet-mapping>
    <servlet-name>DisplayMetricServlet</servlet-name>
    <url-pattern>/_metrics/*</url-pattern>
</servlet-mapping>
```
可以用 http://机器名:端口号/_metrics/metrics.do 看到机器的监控数据

#### 三 写一个配置文件(json格式)
把配置文件 提交给zhenbao.zhou@jimu.com
```
{
  "name": "groupname2",   // 服务组的名字
  "department" : "departmentName2",   // 部门的名字
  "domainList": [          // 服务所包含的机器数量
    {
      "host": "collect7",   // 这台机器的名字
      "url": "http://127.0.0.1:8080/random1.jsp"  // 上一步配置的监控数据url
    },
    {
      "host": "collect8",
      "url": "http://127.0.0.1:8080/random1.jsp"
    }
  ],
  "type": "HTTP"   // 监控类型. 不用变
}
```
注意, groupName + departmentName要是唯一的, 不能重复

我们处理完之后, 2分钟之后可以生成新的面板(dashboard)

#### 在代码里计数

- 监控次数

```
JMonitor.recordOne("monitor_timer_task_success");

JMonitor.incrRecord("monitor_timer_task_success2", 10); // 也可以用这个api, 每次增加10个count

```

一分钟之后, 会在dashboard里看到这个数据.
![image_1al6n61kt1ohn1feiejo8u9pjj3g.png-121.9kB][3]

在metric url里,  Count 结果是一分钟内的总和，每分钟会清0。

在grafana的图上, count的值的单位是qps.

对于单机来说, qps = 1min内的次数 / 60.
对于集群来说, qps等于各台机器的qps之和

- 监控时间

```
 Stopwatch stopwatch = Stopwatch.createStarted();
  // 其他操作
 stopwatch.stop();
 JMonitor.recordOne("carbon_write", stopwatch.elapsed(TimeUnit.MILLISECONDS));  // 记录数据
```

会生成两个指标  `carbon_write_count` 和 `carbon_write_Time`. 也会在grafana里生成两张图

![carbon_write_time][4]

carbon_write_time的时间是, 这一分钟里,每次操作的*平均*时间.

对于单机来说, 是每台机器的平均操作时间.
对于集群来说, 是整个group的平均操作时间

![carbon_write_count][5]

- 记录绝对值

```
JMonitor.recordSize("Thread_num",200);  // 底层记录为 Thread_num_value.
```

值即为每分钟最后出现的一次

#### 最佳实践
1. 在加监控时Key末尾最好不要加入Count，Time，SuccessRate等后缀，Jmonitor会根据不同的注册方法来自动加入后缀
2. 所有的监控指标key, 放在同一个类下面, 统一管理
3. 监控的名字只能是 [0-9a-zA-Z-_]
4. 代码里记录时间时, 单位是毫秒

## 如何看图
- grafana的url地址:
   http://monitor.jimubox.com/.

- dashboard的名字
![dashboard的名字][6]

取决于你的配置文件.

```
{
  "name": "groupname2",   // 服务组的名字
  "department" : "departmentName2",   // 部门的名字
  "domainList": [          // 服务所包含的机器数量
    {
      "host": "collect7",   // 这台机器的名字
      "url": "http://127.0.0.1:8080/random1.jsp"  // 上一步配置的监控数据url
    },
    {
      "host": "collect8",
      "url": "http://127.0.0.1:8080/random1.jsp"
    }
  ],
  "type": "HTTP"   // 监控类型. 不用变
}
```

dashboard名字为 : s.部门名字.服务组名字

- 看图的时机
a. 新增一个dashboard的时间为2分钟
b. 指标延迟1分钟,可以在grafana的图上展示出来
c. 每次发布完之后, 相关同学观察系统的相应指标.  值班同学市场观察系统重要指标

## 参考资料
1. [grafana官方介绍](http://docs.grafana.org/guides/basic_concepts/)
2. [graphite官方介绍](http://graphite.wikidot.com/)

  [1]: http://static.zybuluo.com/gambol/i4a7xuzu8m7pj1owxuw8dxst/grafana.png
  [2]: http://static.zybuluo.com/gambol/xxj88xcyfpv3b9lj5kv7mu55/image_1al6g0b088l821s1uqg98h1m3i1s.png
  [3]: http://static.zybuluo.com/gambol/bsff0w4z611su534ot89twfq/image_1al6n61kt1ohn1feiejo8u9pjj3g.png
  [4]: http://static.zybuluo.com/gambol/sad1stsbmm40pbsbi50bq44y/image_1al6nd1pgki01n8k1kt514h6qke3t.png
  [5]: http://static.zybuluo.com/gambol/5ezurow7kcaggquyr0f1952z/image_1al6neavof431ui4u2btoj1gtg4a.png
  [6]: http://static.zybuluo.com/gambol/7b4kmb5qg54n6to0c3yu6qlu/image_1al6piaf41u8pe5s3ag19vr6fg54.png
