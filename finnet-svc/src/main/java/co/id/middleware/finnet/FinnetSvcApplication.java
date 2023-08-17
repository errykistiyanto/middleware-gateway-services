package co.id.middleware.finnet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
//@EnableEurekaClient
public class FinnetSvcApplication {
	public static ApplicationContext ctx;

	public static void main(String[] args) {
		ctx = SpringApplication.run(FinnetSvcApplication.class, args);

//		RedisTemplate<String, String> redisTemplate = (RedisTemplate<String, String>) ctx.getBean("redisTemplate");
//		NameRegistrar.register("redisTemplate", redisTemplate);

//		HistoryService historyService = (HistoryService) ctx.getBean("historyImpl");
//		NameRegistrar.register("historyImpl", historyService);

	}

}