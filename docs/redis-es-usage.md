# Redis / Elasticsearch 使用说明

## 1. 目的

本文档用于说明 `anzo-insurance-backend` 中已经封装好的 `Redis` 与 `Elasticsearch` 基础设施，包括：

- 依赖位置
- 配置文件位置
- 配置示例
- 工具类使用方式
- 常见使用场景

当前项目已经提供以下通用封装：

- `RedisConfig`
- `RedisUtil`
- `RedisLockUtil`
- `ElasticsearchConfig`
- `ElasticsearchUtil`

## 2. 依赖位置

依赖已添加到：

- `pom.xml`

关键依赖包括：

- `spring-boot-starter-cache`
- `spring-boot-starter-data-redis`
- `org.redisson:redisson`
- `spring-boot-starter-data-elasticsearch`
- `co.elastic.clients:elasticsearch-java`

## 3. 配置文件位置

### 3.1 当前有效配置

当前默认配置位置：

- `src/main/resources/application.yml`
- `src/main/resources/application-dev.yml`
- `src/main/resources/application-dev.properties`

### 3.2 注释模板

为了方便切换环境，Redis / ES 的配置模板已经写入相应配置文件，并保留为注释形式或参考模板。

如果你需要切到自己的环境，直接取消注释并修改值即可。

## 4. Redis 配置示例

### 4.1 YAML 示例

```yaml
spring:
  redis:
    host: 127.0.0.1
    port: 6379
    password: your-redis-password
    database: 0
    timeout: 3000ms
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
        max-wait: 3000ms
```

### 4.2 Properties 示例

```properties
spring.redis.host=127.0.0.1
spring.redis.port=6379
spring.redis.password=your-redis-password
spring.redis.database=0
spring.redis.timeout=3000ms
spring.redis.lettuce.pool.max-active=20
spring.redis.lettuce.pool.max-idle=10
spring.redis.lettuce.pool.min-idle=5
spring.redis.lettuce.pool.max-wait=3000ms
```

## 5. Elasticsearch 配置示例

### 5.1 单节点 YAML 示例

```yaml
spring:
  elasticsearch:
    uris: http://127.0.0.1:9200
    username: elastic
    password: your-es-password
    connection-timeout: 3s
    socket-timeout: 10s
```

### 5.2 集群 YAML 示例

```yaml
spring:
  elasticsearch:
    uris: http://127.0.0.1:9200,http://127.0.0.1:9201
    username: elastic
    password: your-es-password
    connection-timeout: 3s
    socket-timeout: 10s
```

### 5.3 Properties 示例

```properties
spring.elasticsearch.uris=http://127.0.0.1:9200
spring.elasticsearch.username=elastic
spring.elasticsearch.password=your-es-password
spring.elasticsearch.connection-timeout=3s
spring.elasticsearch.socket-timeout=10s
```

## 6. Redis 使用方式

### 6.1 注入方式

```java
import com.anzo.insurance.common.util.RedisLockUtil;
import com.anzo.insurance.common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DemoService {

    private final RedisUtil redisUtil;
    private final RedisLockUtil redisLockUtil;
}
```

### 6.2 基础缓存

```java
redisUtil.set("enterprise:detail:" + enterpriseId, detailDto);

redisUtil.set("enterprise:detail:" + enterpriseId, detailDto, 10, TimeUnit.MINUTES);

EnterpriseDetailDTO cached = redisUtil.get(
        "enterprise:detail:" + enterpriseId,
        EnterpriseDetailDTO.class
);
```

### 6.3 计数器

```java
Long count = redisUtil.increment("notification:unread:" + userId);
Long dailyCount = redisUtil.increment("insurance:daily:" + enterpriseId, 1L);
```

### 6.4 Hash 场景

```java
redisUtil.hashPut("enterprise:profile:" + enterpriseId, "name", enterprise.getName());
redisUtil.hashPut("enterprise:profile:" + enterpriseId, "status", enterprise.getStatus());

String name = redisUtil.hashGet(
        "enterprise:profile:" + enterpriseId,
        "name",
        String.class
);
```

### 6.5 List / Set 场景

```java
redisUtil.listRightPush("export:tasks", taskId);

redisUtil.setAdd("online:users", userId);
boolean online = redisUtil.setIsMember("online:users", userId);
```

### 6.6 分布式锁

```java
redisLockUtil.executeWithLock(
        "lock:wallet:" + enterpriseId,
        1,
        10,
        TimeUnit.SECONDS,
        () -> {
            // 余额扣减、冻结、退款等需要串行保护的逻辑
            return null;
        }
);
```

### 6.7 推荐场景

- 企业详情缓存
- 客户统计缓存
- 理赔统计缓存
- 通知统计缓存
- 财务统计缓存
- 分布式锁保护余额、扣款、批量任务等关键写操作

## 7. Elasticsearch 使用方式

### 7.1 注入方式

```java
import com.anzo.insurance.common.util.ElasticsearchUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchUtil elasticsearchUtil;
}
```

### 7.2 创建索引

```java
boolean ok = elasticsearchUtil.createIndex("policy-index");
```

### 7.3 保存文档

```java
PolicySearchDocument doc = new PolicySearchDocument();
doc.setPolicyNo("POL202406180001");
doc.setApplicantName("ABC进出口有限公司");

elasticsearchUtil.save("policy-index", "policy-1", doc);
```

### 7.4 按 ID 查询

```java
PolicySearchDocument doc = elasticsearchUtil.getById(
        "policy-index",
        "policy-1",
        PolicySearchDocument.class
);
```

### 7.5 更新文档

```java
PolicySearchDocument updateDoc = new PolicySearchDocument();
updateDoc.setStatus("ACTIVE");

elasticsearchUtil.update(
        "policy-index",
        "policy-1",
        updateDoc,
        PolicySearchDocument.class
);
```

### 7.6 删除文档

```java
elasticsearchUtil.deleteById("policy-index", "policy-1");
```

### 7.7 批量写入

```java
Map<String, PolicySearchDocument> docs = Map.of(
        "policy-1", doc1,
        "policy-2", doc2
);

elasticsearchUtil.bulkIndex("policy-index", docs);
```

### 7.8 搜索示例

```java
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

Query query = Query.of(q -> q
        .match(m -> m
                .field("policyNo")
                .query("POL202406180001")
        )
);

List<PolicySearchDocument> result = elasticsearchUtil.search(
        "policy-index",
        query,
        PolicySearchDocument.class
);
```

### 7.9 计数示例

```java
long total = elasticsearchUtil.count("policy-index", null);
```

### 7.10 推荐场景

- 保单全文检索
- 客户名称模糊搜索
- 理赔编号 / 保单号联合检索
- 通知内容检索
- 管理端全局搜索

## 8. 当前注意事项

- 当前只完成了 `Redis` 与 `Elasticsearch` 的基础设施封装，还没有把具体业务模块接入缓存或搜索索引。
- `RedisUtil` 适合通用 KV、Hash、List、Set、计数器场景。
- `RedisLockUtil` 适合需要串行保护的关键写操作。
- `ElasticsearchUtil` 适合搜索型业务，不建议替代 MySQL 的主数据事务能力。
- 正式环境建议使用真实密码、最小权限账号和独立索引命名规范。

## 9. 推荐后续接入顺序

建议后续按以下顺序接入：

1. Redis 接入企业详情、统计类接口缓存
2. Redis 接入余额扣减、冻结、退款等分布式锁保护
3. Elasticsearch 先接保单搜索
4. Elasticsearch 再接客户搜索、通知检索、管理端全局搜索

## 10. 命名规范

### 10.1 Redis Key 命名规范

统一建议使用如下格式：

```text
业务模块:对象:维度[:附加条件]
```

示例：

- `enterprise:detail:{enterpriseId}`
- `enterprise:stats:{enterpriseId}`
- `customer:list:{enterpriseId}`
- `claim:stats:{enterpriseId}`
- `notification:stats:user:{userId}`
- `notification:stats:enterprise:{enterpriseId}`
- `lock:wallet:{enterpriseId}`
- `lock:policy:issue:{policyId}`
- `export:task:{taskId}`

命名建议：

- 全部使用小写英文
- 层级之间使用 `:`
- 主键、企业 ID、用户 ID 直接放在末尾
- 锁统一使用 `lock:` 前缀
- 计数器建议单独使用 `counter:` 或业务前缀明确标识

不推荐的写法：

- `cache1`
- `policy123`
- `userInfo`

### 10.2 Redis TTL 建议

建议按业务类型区分缓存时间：

- 企业详情、客户详情：`5-10 分钟`
- 统计类接口：`30 秒 - 2 分钟`
- 下拉列表、字典类：`30 分钟 - 12 小时`
- 一次性验证码、短期令牌：`1-10 分钟`
- 分布式锁：按业务处理时间设置，一般 `5-30 秒`

### 10.3 Elasticsearch 索引命名规范

统一建议使用如下格式：

```text
系统名_业务名[_环境]
```

推荐示例：

- `anzo_policy`
- `anzo_customer`
- `anzo_claim`
- `anzo_notification`
- `anzo_policy_dev`
- `anzo_policy_prod`

命名建议：

- 全部小写
- 使用下划线 `_`
- 不要使用驼峰
- 不要直接把日期写进主索引名，除非是日志或归档索引

### 10.4 Elasticsearch 文档 ID 建议

如果数据库主表已有稳定主键，优先直接复用主键：

- 保单索引文档 ID 直接使用 `policyId`
- 客户索引文档 ID 直接使用 `customerId`
- 理赔索引文档 ID 直接使用 `claimId`

这样方便：

- 增量同步
- 按 ID 覆盖更新
- 删除时精准定位

## 11. 生产环境建议

### 11.1 Redis 建议

- 开启密码或 ACL，不要裸奔
- 不要在生产环境使用默认空密码
- 区分开发、测试、生产库或实例
- 大 key 和热 key 需要监控
- 锁类 key 必须设置过期时间，避免死锁
- 缓存数据不要存放强事务主数据的唯一真值

### 11.2 Elasticsearch 建议

- 生产环境建议单独账号，不要直接使用超级管理员账号
- 至少区分 `dev / test / prod` 索引
- 搜索索引只用于检索和筛选，不替代 MySQL 主事务库
- 建议为主要检索字段提前定义 mapping，而不是完全依赖动态映射
- 大批量导入时使用批量写入并控制批次大小
- 正式环境建议启用快照备份和集群监控

### 11.3 配置管理建议

- 敏感信息尽量走环境变量，不直接写入仓库
- 本地开发可保留注释模板，正式环境通过部署平台注入
- 推荐环境变量示例：

```bash
export REDIS_HOST=127.0.0.1
export REDIS_PORT=6379
export REDIS_PASSWORD=your-redis-password

export ES_URIS=http://127.0.0.1:9200
export ES_USERNAME=elastic
export ES_PASSWORD=your-es-password
```

### 11.4 接入策略建议

建议后续接入时遵循以下原则：

1. 先接读多写少接口
2. 先接缓存，再接搜索
3. 先接单索引、单场景，再扩展到多模块
4. 所有缓存和索引接入都要有失效/同步策略

## 12. 常见问题排查

### 12.1 Redis 连接失败

常见表现：

- 启动时报 `RedisConnectionFailureException`
- 接口调用时报连接超时

排查顺序：

1. 确认 Redis 服务是否启动
2. 确认 `host / port / password / database` 是否正确
3. 确认本机或服务器防火墙是否放通端口
4. 确认配置文件和环境变量是否有覆盖关系

### 12.2 Redisson 锁无法释放或业务卡住

常见原因：

- 没有设置合理的 `leaseTime`
- 锁 key 设计过粗，多个无关请求争同一把锁
- 在锁内执行了耗时过长的远程调用

建议：

- 锁 key 尽量细粒度
- 不要把导出、上传、外部接口调用长期放在锁内
- 优先把锁用在余额扣减、状态流转等关键写场景

### 12.3 Elasticsearch 启动连接失败

常见表现：

- 应用启动时报 ES client 连接错误
- 查询时报连接超时或认证失败

排查顺序：

1. 确认 ES 服务是否启动
2. 确认 `spring.elasticsearch.uris` 是否正确
3. 确认用户名密码是否正确
4. 确认 ES 版本和当前依赖是否兼容
5. 确认是否启用了 HTTPS，如启用则地址应使用 `https://`

### 12.4 搜索不到数据

常见原因：

- 文档根本没有写入 ES
- 写入索引名和查询索引名不一致
- 查询字段名写错
- 只写入了 MySQL，没有同步写入 ES

建议检查：

- 是否执行了 `save / bulkIndex`
- 索引名是否一致
- 文档 ID 是否与数据库主键一致
- 查询 DSL 中字段名是否与 mapping 对齐

### 12.5 缓存不生效

常见原因：

- key 写错
- 读取和写入不是同一套 key 规则
- TTL 太短导致刚写入就过期
- 写操作后没有及时删除旧缓存

建议：

- 统一使用本文档的 key 命名规范
- 每个缓存场景都明确写出“谁来写、谁来删、多久过期”
- 写接口落地时同步实现缓存失效逻辑

## 13. 推荐交付要求

如果后续把 Redis / ES 接入到具体业务模块，建议交付时至少补齐：

1. 配置说明
2. Key / 索引命名说明
3. 缓存失效策略
4. MySQL 与 ES 的同步策略
5. 本地联调步骤
6. 故障排查说明
