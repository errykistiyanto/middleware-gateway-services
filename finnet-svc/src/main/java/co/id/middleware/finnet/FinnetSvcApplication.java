package co.id.middleware.finnet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class FinnetSvcApplication {
	public static ApplicationContext ctx;

	public static void main(String[] args) {
		ctx = SpringApplication.run(FinnetSvcApplication.class, args);

	}
}
