package co.id.middleware.mock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class MockServerApplication {
    public static ApplicationContext ctx;

    public static void main(String[] args) {
        ctx = SpringApplication.run(MockServerApplication.class, args);

    }
}
