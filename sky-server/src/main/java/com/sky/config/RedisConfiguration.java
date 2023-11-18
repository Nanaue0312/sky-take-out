package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {
	@Bean
	public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		log.info("开始创建redisTemplate");
		RedisTemplate<String, String> redisTemplate = new RedisTemplate();
		// 设置redis连接工厂
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		// 设置redis key序列化器
		GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
		// 设置Key的序列化
		redisTemplate.setKeySerializer(RedisSerializer.string());
		redisTemplate.setHashKeySerializer(RedisSerializer.string());
		// 设置Value的序列化
		redisTemplate.setValueSerializer(jsonRedisSerializer);
		redisTemplate.setHashValueSerializer(jsonRedisSerializer);
		return redisTemplate;
	}
}
