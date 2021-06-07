package com.covid.covidprayerrequest.model;

import lombok.*;

@Getter
@Setter
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Request {

    private String _for;
    private String date;
    private String description;
    private String by;
    private String status;
}
