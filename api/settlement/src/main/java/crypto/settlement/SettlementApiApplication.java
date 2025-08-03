package crypto.settlement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackages = {"crypto"})
@EntityScan(basePackages = {"crypto"})
public class SettlementApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(SettlementApiApplication.class, args);
    }
}
