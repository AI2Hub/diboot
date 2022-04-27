/*
 * Copyright (c) 2015-2021, www.dibo.ltd (service@dibo.ltd).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.diboot.iam.starter;

import com.diboot.core.cache.BaseCacheManager;
import com.diboot.core.cache.DynamicRedisCacheManager;
import com.diboot.iam.redis.ShiroRedisCacheManager;
import org.apache.shiro.cache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

/**
 * Shiro の Redis 缓存自动配置
 *
 * @author wind
 * @version v2.3.0
 * @date 2021/7/20
 * Copyright © diboot.com
 */
@Order(921)
@Configuration
@ConditionalOnBean(RedisTemplate.class)
@ConditionalOnClass(RedisOperations.class)
@ConditionalOnResource(resources = "org/springframework/data/redis")
@AutoConfigureBefore({IamAutoConfig.class})
public class IamRedisAutoConfig {
    @Autowired
    private IamProperties iamProperties;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    /**
     * 启用RedisCacheManager定义
     * @return
     */
     @Bean(name = "shiroCacheManager")
     @ConditionalOnMissingBean(CacheManager.class)
     public CacheManager shiroCacheManager(RedisTemplate<String, Object> redisTemplate) {
        return new ShiroRedisCacheManager(redisTemplate, iamProperties.getJwtTokenExpiresMinutes());
     }

    /**
     * 验证码的缓存管理
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public BaseCacheManager baseCacheManager(){
        // redis配置参数
        RedisCacheConfiguration defaultCacheConfiguration = RedisCacheConfiguration
                        .defaultCacheConfig()
                            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(redisTemplate.getStringSerializer()))
                            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisTemplate.getValueSerializer()))
                            .disableCachingNullValues()
                            .entryTtl(Duration.ofMinutes(iamProperties.getTokenExpiresMinutes()));
        // 初始化redisCacheManager
        RedisCacheManager redisCacheManager =
                RedisCacheManager.RedisCacheManagerBuilder
                        .fromConnectionFactory(redisTemplate.getConnectionFactory())
                        .cacheDefaults(defaultCacheConfiguration)
                        .transactionAware()
                        .build();
        return new DynamicRedisCacheManager(redisCacheManager);
    }

}