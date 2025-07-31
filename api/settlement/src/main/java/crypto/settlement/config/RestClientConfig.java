package crypto.settlement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;


@Configuration
public class RestClientConfig {

    @Value("${endpoints.crypto-exchange-settlement.url}")
    private String walletServiceUrl;

    @Bean
    public RestClient walletServiceClient() {
        return RestClient.builder()
                .baseUrl(walletServiceUrl)
                .build();
    }
}
