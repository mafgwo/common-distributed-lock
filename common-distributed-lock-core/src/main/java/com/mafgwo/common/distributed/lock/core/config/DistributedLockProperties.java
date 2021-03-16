package com.mafgwo.common.distributed.lock.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author chenxiaoqi
 * @since 2019/07/29
 */
@Data
@ConfigurationProperties(prefix = DistributedLockProperties.PREFIX)
public class DistributedLockProperties {

    public static final String PREFIX = "spring.redis";

    private String url;
    private String host = "localhost";
    private Integer port = 6379;
    private String password;
    private int database = 0;
    private ClusterServer cluster;

    private String codec = "org.redisson.codec.JsonJacksonCodec";

    private long waitTime = 60;
    private long leaseTime = 60;

    /**
     * 锁前缀 默认为服务名 可通过此方式覆盖
     */
    private String lockPrefix;

    @Data
    public static class ClusterServer {
        private String[] nodes;
    }
}
