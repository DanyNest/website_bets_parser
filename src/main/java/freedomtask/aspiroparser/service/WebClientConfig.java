package freedomtask.aspiroparser.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @Author danynest
 * @CreateAt 09.04.25
 */

@Configuration
public class WebClientConfig {
  @Value("${leon.base.url}")
  private String BASE_URL;
  @Value("${leon.default-header}")
  private String DEFAULT_HEADER;
  @Value("${leon.default.encoding-group}")
  String DEFAULT_ENCODING_GROUP;

  @Bean
  public WebClient webClient() {
    return WebClient.builder()
        .baseUrl(BASE_URL)
        .defaultHeader(DEFAULT_HEADER, DEFAULT_ENCODING_GROUP)
        .build();
  }
}
