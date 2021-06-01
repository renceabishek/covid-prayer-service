package com.covid.covidprayerrequest.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {

  private final FirebaseDbProperties firebaseDbProperties;


  public WebClientConfiguration(FirebaseDbProperties firebaseDbProperties) {
    this.firebaseDbProperties = firebaseDbProperties;
  }

  @Bean(name = "webClientFirebase")
  public WebClient webClientConfig(WebClient.Builder webClient) {
    return webClient.baseUrl(firebaseDbProperties.getUrl())
        .build();
  }

  @Bean(name = "restTemplateFirebase")
  public RestTemplate restTemplateConfig(RestTemplateBuilder restTemplateBuilder) {
    return restTemplateBuilder.rootUri(firebaseDbProperties.getUrl()).build();
  }

}
