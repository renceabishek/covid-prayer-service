package com.covid.covidprayerrequest.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "firebase.database")
public class FirebaseDbProperties {

  private String url;
  private String name;
  private String configcredentials;


}
