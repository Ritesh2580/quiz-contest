package com.quizcontest.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Cache Configuration for the Quiz Contest Application.
 * <p>
 * This configuration class sets up Redis as the caching provider with custom
 * serialization for Java 8 date/time types. It defines multiple cache regions
 * with different TTL (Time To Live) configurations based on data volatility.
 * </p>
 *
 * <p><b>Cache Regions:</b></p>
 * <ul>
 *   <li>{@value #CACHE_USERS} - User data (1 hour TTL)</li>
 *   <li>{@value #CACHE_QUIZZES} - Quiz information (30 minutes TTL)</li>
 *   <li>{@value #CACHE_QUESTIONS} - Question data (30 minutes TTL)</li>
 *   <li>{@value #CACHE_QUESTION_OPTIONS} - Question options (30 minutes TTL)</li>
 *   <li>{@value #CACHE_QUIZ_PARTICIPANTS} - Quiz participation data (15 minutes TTL)</li>
 *   <li>{@value #CACHE_PLAYER_ANSWERS} - Player answer data (15 minutes TTL)</li>
 *   <li>{@value #CACHE_LEADERBOARD} - Leaderboard rankings (10 minutes TTL)</li>
 *   <li>{@value #CACHE_REPORTS} - Generated reports (2 hours TTL)</li>
 * </ul>
 *
 * @author Quiz Contest Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

    /** Cache name for user data. */
    public static final String CACHE_USERS = "users";

    /** Cache name for quiz data. */
    public static final String CACHE_QUIZZES = "quizzes";

    /** Cache name for question data. */
    public static final String CACHE_QUESTIONS = "questions";

    /** Cache name for question option data. */
    public static final String CACHE_QUESTION_OPTIONS = "questionOptions";

    /** Cache name for quiz participant data. */
    public static final String CACHE_QUIZ_PARTICIPANTS = "quizParticipants";

    /** Cache name for player answer data. */
    public static final String CACHE_PLAYER_ANSWERS = "playerAnswers";

    /** Cache name for leaderboard data. */
    public static final String CACHE_LEADERBOARD = "leaderboard";

    /** Cache name for report data. */
    public static final String CACHE_REPORTS = "reports";

    /**
     * Creates and configures the primary {@link CacheManager} using Redis.
     * <p>
     * This method configures:
     * <ul>
     *   <li>Custom ObjectMapper with JavaTimeModule for proper date/time serialization</li>
     *   <li>GenericJackson2JsonRedisSerializer for JSON serialization of cache values</li>
     *   <li>StringRedisSerializer for cache keys</li>
     *   <li>Individual TTL configurations for each cache region</li>
     *   <li>Transaction-aware cache manager</li>
     * </ul>
     *
     * @param redisConnectionFactory the Redis connection factory to use for connections
     * @return the configured RedisCacheManager instance
     */
    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        // Configure specific cache TTLs
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put(CACHE_USERS, defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put(CACHE_QUIZZES, defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put(CACHE_QUESTIONS, defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put(CACHE_QUESTION_OPTIONS, defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put(CACHE_QUIZ_PARTICIPANTS, defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put(CACHE_PLAYER_ANSWERS, defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put(CACHE_LEADERBOARD, defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put(CACHE_REPORTS, defaultConfig.entryTtl(Duration.ofHours(2)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}
