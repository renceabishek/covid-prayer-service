package com.covid.covidprayerrequest.controller;

import com.covid.covidprayerrequest.config.FirebaseJsonProperties;
import com.covid.covidprayerrequest.model.Request;
import com.covid.covidprayerrequest.model.Status;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.DatabaseReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    String datePattern = "MM/dd/yyyy HH:mm:ss";

    @PostMapping("/request")
    public void CreateRequest(@RequestBody Request request) {
            DatabaseReference postsRef = mainDatabaseReference.child(firebaseJsonProperties.getRequest());
            request.setStatus("wait");
            request.setDate(new SimpleDateFormat(datePattern).format(Calendar.getInstance().getTime()));
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
                            return new Request(request.get_for(), request.getDate(), request.getDescription(), request.getBy(), request.getStatus());
                        })
                        .sorted(Comparator.comparing((Request status)->convert_enum_status(status.getStatus())).reversed()
                                        .thenComparing((Request r)->LocalDate.parse(r.getDate(),
                                DateTimeFormatter.ofPattern(datePattern, Locale.ENGLISH))).reversed())
                        .collect(Collectors.toList()));
    }

    private Status convert_enum_status(String status) {
        if(status.equalsIgnoreCase("wait")) {
            return Status.WAIT;
        }
        else if(status.equalsIgnoreCase("inprogress")) {
            return Status.INPROGRESS;
        } else if(status.equalsIgnoreCase("yes")) {
            return Status.YES;
        } else {
            return null;
        }
    }
}
