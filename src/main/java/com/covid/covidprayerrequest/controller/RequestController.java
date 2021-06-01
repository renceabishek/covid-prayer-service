package com.covid.covidprayerrequest.controller;

import com.covid.covidprayerrequest.config.FirebaseJsonProperties;
import com.covid.covidprayerrequest.model.Request;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.DatabaseReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/prayer")
public class RequestController {

    @Autowired
    @Qualifier("fbPersist")
    private DatabaseReference mainDatabaseReference;

    private final WebClient webClientFirebase;
    private final FirebaseJsonProperties firebaseJsonProperties;

    @Autowired
    ObjectMapper objectMapper;

    public RequestController(WebClient webClientFirebase,
                             FirebaseJsonProperties firebaseJsonProperties) {
        this.webClientFirebase = webClientFirebase;
        this.firebaseJsonProperties = firebaseJsonProperties;
    }

    @PostMapping("/request")
    public void CreateRequest(@RequestBody Request request) {
            DatabaseReference postsRef = mainDatabaseReference.child(firebaseJsonProperties.getRequest());
            DatabaseReference newPostRef = postsRef.push();
            newPostRef.setValueAsync(request);
    }

    @GetMapping("/request")
    public Mono<List<Request>> getRequests() {
        return webClientFirebase.get()
                .uri(uriBuilder -> uriBuilder.path(firebaseJsonProperties.getRequest() + ".json")
                        .build())
                .retrieve()
                .onStatus(HttpStatus::isError, resp -> resp.createException()
                        .map(Exception::new)
                        .flatMap(Mono::error))
                .bodyToMono(new ParameterizedTypeReference<HashMap<String, Request>>() {
                }).map(monoProfile -> monoProfile.entrySet().stream()
                        .map(k -> {
                            Request request = objectMapper.convertValue(k.getValue(), Request.class);
                            return new Request(request.get_for(), request.getDate(), request.getDescription(), request.getBy());
                        })
                        .sorted(Comparator.comparing(Request::getBy)).collect(Collectors.toList()));
    }
}
