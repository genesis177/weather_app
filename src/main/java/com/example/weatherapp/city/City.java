package com.example.weatherapp.city;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record City(
        @JsonProperty("name") String name,
        @JsonProperty("country") String country,
        @JsonProperty("lat") Double lat,
        @JsonAlias({"lng", "lon"})
        @JsonProperty("lon") Double lon,
        @JsonProperty("state") String state) {

    public String toSuggestionString() {
        String n = name != null ? name : "";
        String c = country != null ? country : "";
        String la = lat != null ? String.valueOf(trimDouble(lat)) : "";
        String lo = lon != null ? String.valueOf(trimDouble(lon)) : "";
        return String.format("%s %s (%s,%s)", n, c, la, lo).trim();
    }

    private static double trimDouble(Double d) {
        return Math.round(d * 1_000_000d) / 1_000_000d;
    }
}