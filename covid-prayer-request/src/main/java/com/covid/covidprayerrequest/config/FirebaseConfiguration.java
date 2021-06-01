package com.covid.covidprayerrequest.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfiguration {

  private final FirebaseDbProperties firebaseDbProperties;

  @Value(value = "classpath:google-services.json")
  private Resource gservicesConfig;

  public FirebaseConfiguration(FirebaseDbProperties firebaseDbProperties) {
    this.firebaseDbProperties = firebaseDbProperties;
  }

  @Bean
  public FirebaseApp initializeFirebaseApp() throws IOException {

    FirebaseOptions options = new FirebaseOptions.Builder()
        .setCredentials(GoogleCredentials.fromStream((getInputStream())))
        .setDatabaseUrl(firebaseDbProperties.getUrl())
        .build();

    return FirebaseApp.initializeApp(options);
  }


  @Bean
  @Qualifier("fbPersist")
  public DatabaseReference provideDatabaseReference(FirebaseApp firebaseApp) {
    FirebaseDatabase
            .getInstance(firebaseApp)
            .setPersistenceEnabled(false);
    return FirebaseDatabase
            .getInstance(firebaseApp)
            .getReference();
  }

  private InputStream getInputStream() throws IOException {
    InputStream inputStream;

    if (firebaseDbProperties.getConfigcredentials() == null) {
      inputStream = gservicesConfig.getInputStream();
    } else {
      JSONObject jsonObject = new JSONObject(firebaseDbProperties.getConfigcredentials());
      inputStream = new ByteArrayInputStream(jsonObject.toString().getBytes());
    }
    return inputStream;
  }


}
