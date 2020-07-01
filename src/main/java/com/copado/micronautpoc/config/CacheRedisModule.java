package com.copado.micronautpoc.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@EnableRedisRepositories
public class CacheRedisModule {

  private static String getPassword(String userInfo) {
    return Optional.ofNullable(userInfo)
        .map(value -> value.split(":", 2)[1])
        .orElse("");
  }

  private static JedisPoolConfig buildPoolConfig() {
    final JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(10);
    poolConfig.setMaxIdle(5);
    poolConfig.setMinIdle(1);
    poolConfig.setTestOnBorrow(true);
    poolConfig.setTestOnReturn(true);
    poolConfig.setTestOnCreate(true);
    poolConfig.setBlockWhenExhausted(true);
    return poolConfig;
  }

  @Bean
  public RedisStandaloneConfiguration redisStandaloneConfiguration(@Value("${redis.url}") String redisUrl) throws URISyntaxException {
    URI redisUri = new URI(redisUrl);
    RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisUri.getHost(), redisUri.getPort());
    redisStandaloneConfiguration.setPassword(getPassword(redisUri.getUserInfo()));
    return redisStandaloneConfiguration;
  }

  @Bean
  public JedisClientConfiguration redisClientConfiguration() {
    return JedisClientConfiguration.builder()
        .usePooling()
        .poolConfig(buildPoolConfig())
        .build();

  }

  @Bean
  public JedisConnectionFactory redisConnectionFactory(@Autowired RedisStandaloneConfiguration redisStandaloneConfiguration,
                                                       @Autowired JedisClientConfiguration redisClientConfiguration) {
    return new JedisConnectionFactory(redisStandaloneConfiguration, redisClientConfiguration);
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(@Autowired RedisStandaloneConfiguration redisStandaloneConfiguration,
                                                     @Autowired JedisClientConfiguration redisClientConfiguration) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(redisConnectionFactory(redisStandaloneConfiguration, redisClientConfiguration));
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new GenericToStringSerializer<>(Object.class));
    template.afterPropertiesSet();
    return template;
  }
}

