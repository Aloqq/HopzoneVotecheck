package com.hopzone.voteverify.dto;

public record CheckResponse(
    boolean voted,
    String voteTime,
    String hopzoneServerTime,
    String rawJson,
    String reportUrl
) {
}
