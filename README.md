# common-distributed-lock
基于redis redisson的分布式锁spring-boot starter组件

支持Spring Boot 1.x 和 2.x

已发布到maven中央仓库，可直接引入依赖使用。


# 使用说明

> spring boot项目接入


1.添加lock starter组件依赖
```
# maven项目引入依赖 版本号请查看中央仓库最新版本
<dependency>
  <groupId>com.mafgwo.common.distributed-lock</groupId>
  <artifactId>common-distributed-lock-starter</artifactId>
  <version>1.0.0</version>
</dependency>

# gradle项目引入依赖
compile('com.mafgwo.common.distributed-lock:common-distributed-lock-starter:1.0')
```

2.application.yaml的redis配置（可以单机、可以集群）与spring boot redis starter的配置保持一致如下
```$yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: xxxx
```


3.在需要加分布式锁的方法上，添加注解@DistributedLock，如：
```java
@Slf4j
@Service
public class TestServiceImpl implements TestService {

    @DistributedLock(name = "test-lock", keys = {"#id"}, leaseTime = -1)
    @Override
    public void testLock(String id) {
        try {
            Thread.sleep(300000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("test-lock finished");
    }
}

```

4. 支持锁指定的业务key，如同一个方法ID入参相同的加锁，其他的放行。业务key的获取支持Spel，具体使用方式如以上代码中的keys，也可以通过@DistributedLockKey注解指定key，如下
```java
@Slf4j
@Service
public class TestServiceImpl implements TestService {

    @DistributedLock(name = "test-lock", keys = "#id", leaseTime = -1)
    @Override
    public void testLock(String id, @DistributedLockKey("name") UserVO user) {
        try {
            Thread.sleep(300000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("test-lock finished");
    }
}
```

# 使用参数说明

> 配置参数说明

```properties
spring.redis.host  : redis的host
spring.redis.port  : redis的端口 默认是 6379
spring.redis.password : redis密码
spring.redis.database : redis数据索引 默认是0
spring.redis.cluster.nodes : redis集群配置 如 127.0.0.1:7000,127.0.0.1:7001,127.0.0.1:7002
spring.redis.host 和 spring.redis.cluster.nodes 选其一即可

# 为锁扩展的配置
spring.redis.waitTime : 获取锁最长阻塞时间（默认：60，单位：秒）
spring.redis.leaseTime: 已获取锁后自动释放时间（默认：60，单位：秒）
spring.redis.lockPrefix: 锁的前缀（默认：spring.application.name的值）

```
> @DistributedLock注解参数说明
```
@DistributedLock可以标注四个参数，作用分别如下

name：lock的name，对应redis的key值。可不填，默认为：类名+方法名

keys：指定的业务key，只针对指定的业务keys加锁 可指定多个具体参考上面的示例

lockType：锁的类型，支持（可重入锁，公平锁，读写锁）。默认为：可重入锁

waitTime：获取锁最长等待时间。默认为：60s。同时也可通过spring.redis.waitTime统一配置

leaseTime：获得锁后，自动释放锁的时间。默认为：60s。同时也可通过spring.redis.leaseTime统一配置 当值为 -1 则会自动续租 直到业务完成 如果代码死锁则永远不会释放锁

lockTimeoutStrategy: 加锁超时的处理策略，可配置为不做处理、快速失败、阻塞等待的处理策略，默认策略为不做处理

customLockTimeoutStrategy: 自定义加锁超时的处理策略，需指定自定义处理的方法的方法名，并保持入参一致。

releaseTimeoutStrategy: 释放锁时，持有的锁已超时的处理策略，可配置为不做处理、快速失败的处理策略，默认策略为不做处理

customReleaseTimeoutStrategy: 自定义释放锁时，需指定自定义处理的方法的方法名，并保持入参一致。
```

> 关于redis锁的key的拼接说明

\[前缀(默认为服务名)]:lock:[锁名]-[keys的拼接，通过-拼接]

- 示例：group-buying-service:lock:group-finished-xxxxgroupid
- 前缀：默认为  spring.application.name 如果该配置没有则默认是 distributed-lock 前缀，另外可以通过配置spring.redis.lockPrefix覆盖前缀
- 锁名：则有注解@DistributedLock中的name值
- keys拼接：包含两部分，一部分的注解@DistributedLock里的keys获取参数值，另一部分是通过参数注解@DistributedLockKey获取的参数值


> 锁的几个使用示例如下
```java
@Slf4j
@Service
public class TestServiceImpl implements TestService {


    @DistributedLock(name = "test-lock", keys = {"#id"}, leaseTime = -1)
    @Override
    public void testLock(String id, UserVO user) {
        try {
            Thread.sleep(30000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("test-lock finished");
    }

    /**
     * 此方法的锁 与 上面 testLock方法的锁的key是同一个
     * @param id
     * @param user
     */
    @DistributedLock(name = "test-lock", waitTime = 2, lockTimeoutStrategy = LockTimeoutStrategy.FAIL_FAST)
    @Override
    public void testLock2(@DistributedLockKey String id, UserVO user) {
        this.testLock(id, user);
    }

    @DistributedLock(name = "test-lock3")
    @Override
    public void testLock3(String id, @DistributedLockKey("deptVO.id") UserVO user) {
        this.testLock(id, user);
    }

    @DistributedLock(name = "test-lock4")
    @Override
    public void testLock4(String id, @DistributedLockKey("id") UserVO user) {
        this.testLock(id, user);
    }
}

```

加锁超时处理策略(**LockTimeoutStrategy**)：
- **NO_OPERATION** 不做处理，继续执行业务逻辑
- **FAIL_FAST** 快速失败，会抛出DistributeLockTimeoutException
- **KEEP_ACQUIRE** 阻塞等待，一直阻塞，直到获得锁，但在太多的尝试后，会停止获取锁并报错，此时很有可能是发生了死锁。
- **自定义(customLockTimeoutStrategy)** 需指定自定义处理的方法的方法名，并保持入参一致，指定自定义处理方法后，会覆盖上述三种策略，且会拦截业务逻辑的运行。

释放锁时超时处理策略(**ReleaseTimeoutStrategy**)：
- **NO_OPERATION** 不做处理，继续执行业务逻辑
- **FAIL_FAST** 快速失败，会抛出DistributeLockTimeoutException
- **自定义(customReleaseTimeoutStrategy)** 需指定自定义处理的方法的方法名，并保持入参一致，指定自定义处理方法后，会覆盖上述两种策略, 执行自定义处理方法时，业务逻辑已经执行完毕，会在方法返回前和throw异常前执行。

