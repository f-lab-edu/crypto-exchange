package crypto.settlement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@ComponentScan(basePackages = {"crypto"})
@EntityScan(basePackages = {"crypto"})
@EnableJpaRepositories(basePackages = {"crypto.settlementdata.repository"})
public class SettlementApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(SettlementApiApplication.class, args);
    }
}
