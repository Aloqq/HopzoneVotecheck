package com.hopzone.voteverify.dto;

public record HopzoneCheckResult(
    int httpStatus,
    String rawJson,
    boolean voted,
    String voteTime,
    String hopzoneServerTime,
    Integer statusCode,
    String apiver,
    long responseTimeMs
) {
}
