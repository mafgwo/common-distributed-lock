package com.mafgwo.common.distributed.lock.starter;

import com.mafgwo.common.distributed.lock.core.BusinessKeyProvider;
import com.mafgwo.common.distributed.lock.core.DistributedLockAspectHandler;
import com.mafgwo.common.distributed.lock.core.LockInfoProvider;
import com.mafgwo.common.distributed.lock.core.config.DistributedLockProperties;
import com.mafgwo.common.distributed.lock.core.lock.LockFactory;
import io.netty.channel.nio.NioEventLoopGroup;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * JKLock自动装配
 *
 * @author chenxiaoqi
 * @since 2019/07/29
 */
@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties(DistributedLockProperties.class)
@Import({DistributedLockAspectHandler.class})
public class DistributedLockAutoConfiguration {

    @Autowired
    private DistributedLockProperties distributedLockProperties;

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    RedissonClient redisson() throws Exception {
        Config config = new Config();
        if (distributedLockProperties.getCluster() != null) {
            config.useClusterServers().setPassword(distributedLockProperties.getPassword())
                .addNodeAddress(distributedLockProperties.getCluster().getNodes());
        }
        else if (StringUtils.hasText(distributedLockProperties.getUrl())) {
            config.useSingleServer().setAddress(distributedLockProperties.getUrl())
                .setDatabase(distributedLockProperties.getDatabase())
                .setPassword(distributedLockProperties.getPassword());
        }
        else {
            config.useSingleServer().setAddress(distributedLockProperties.getHost() + ":" + distributedLockProperties.getPort())
                .setDatabase(distributedLockProperties.getDatabase())
                .setPassword(distributedLockProperties.getPassword());
        }
        Codec codec = (Codec) ClassUtils.forName(distributedLockProperties.getCodec(), ClassUtils.getDefaultClassLoader()).newInstance();
        config.setCodec(codec);
        config.setEventLoopGroup(new NioEventLoopGroup());
        return Redisson.create(config);
    }

    @Bean
    public LockInfoProvider lockInfoProvider() {
        return new LockInfoProvider();
    }

    @Bean
    public BusinessKeyProvider businessKeyProvider() {
        return new BusinessKeyProvider();
    }

    @Bean
    public LockFactory lockFactory() {
        return new LockFactory();
    }
}
