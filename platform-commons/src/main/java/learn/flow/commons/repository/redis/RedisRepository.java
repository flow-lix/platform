package learn.flow.commons.repository.redis;

import learn.flow.commons.repository.Repository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Objects;

/**
 * 封装Redis基本操作
 */
@Slf4j
public class RedisRepository<T> implements Repository {

    // 3只
    private RedisTemplate<String, Object> redisTemplate;

    /** key 序列化 */
    private static final StringRedisSerializer keySerializer = new StringRedisSerializer();
    /** value 序列化 */
    private static final JdkSerializationRedisSerializer valueSerializer = new JdkSerializationRedisSerializer();

    /**
     * 添加缓存对象
     * @param key redis 缓存key
     * @param value 缓存对象
     */
    public void set(final String key, final Object value) {
        redisTemplate.execute((RedisCallback<Long>)(connection) -> {
            byte[] keyBytes = keySerializer.serialize(key);
            Objects.requireNonNull(keyBytes);
            byte[] valueBytes = valueSerializer.serialize(value);
            Objects.requireNonNull(valueBytes);
            connection.set(keyBytes, valueBytes);
            log.debug("[redisTemplate redis]放入 缓存  key:{}, value: {}", key, value);
            return 1L;
        });
    }

    /**
     * 添加时间限制对象
     * @param key redis 缓存key
     * @param value 缓存对象
     * @param time 超时时间
     */
    public void setExpire(final String key, final Object value, final long time) {
        redisTemplate.execute((RedisCallback<Long>)(connection) -> {
            byte[] keyBytes = keySerializer.serialize(key);
            Objects.requireNonNull(keyBytes);
            byte[] valueBytes = valueSerializer.serialize(value);
            Objects.requireNonNull(valueBytes);
            connection.setEx(keyBytes, time, valueBytes);
            return 1L;
        });
    }

    /**
     * 根据key获取对象
     * @param key 缓存key
     * @return the value object
     */
    public Object get(final String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 清除Redis持久化的数据
     * @return result
     */
    public String flushDb() {
        return redisTemplate.execute((RedisCallback<String>)(connection) -> {
            connection.flushDb();
            return "OK";
        });
    }
}
