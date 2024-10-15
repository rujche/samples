package rujche.sample;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootApplication
public class SampleApplication {
	private final RedisTemplate<Object, Object> redisTemplate;

	public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class, args);
	}

	public SampleApplication (RedisTemplate<Object, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Bean
	public ApplicationRunner applicationRunner() {
		return (args) -> {
			System.out.println("hello world");
			redisTemplate.opsForValue().set("key", "value");
			System.out.println("Get key from redis: key = " + redisTemplate.opsForValue().get("key"));
		};
	}

}
