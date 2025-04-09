package freedomtask.aspiroparser.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @Author danynest
 * @CreateAt 09.04.25
 */

@Configuration
public class WebClientConfig {
  @Bean
  public WebClient webClient() {
    return WebClient.builder()
        .baseUrl("https://leonbets.com")
        .defaultHeader("Accept-Encoding", "gzip, deflate, br, zstd")
        .build();
  }
}
