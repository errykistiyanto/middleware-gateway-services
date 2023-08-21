package co.id.middleware.finnet.jpos;

import org.jpos.q2.Q2;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Created by bankdki on 3/30/15.
 */

@Configuration
public class SpringQ2 extends Q2 {
    private SpringQ2 q2;

    @PostConstruct
    public void init() {
        q2 = new SpringQ2();
        q2.start();
    }

    @PreDestroy
    public void halt() {
        q2.shutdown();
    }

}