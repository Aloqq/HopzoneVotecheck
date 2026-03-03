package com.hopzone.voteverify.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hopzone.voteverify.config.AppProperties;
import com.hopzone.voteverify.dto.HopzoneCheckResult;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class HopzoneClient {

    private static final Logger log = LoggerFactory.getLogger(HopzoneClient.class);
    private static final Pattern VOTED_TRUE_PATTERN = Pattern.compile("\"voted\"\\s*:\\s*(true|1)", Pattern.CASE_INSENSITIVE);

    private final AppProperties appProperties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public HopzoneClient(AppProperties appProperties, ObjectMapper objectMapper) {
        this.appProperties = appProperties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
    }

    public HopzoneCheckResult checkVote(String ip) {
        String endpoint = appProperties.getHopzoneEndpoint()
            .replace("{TOKEN}", appProperties.getHopzoneToken())
            .replace("{IP}", ip);

        long startMs = System.currentTimeMillis();
        try {
            String body = restClient.get()
                .uri(endpoint)
                .retrieve()
                .body(String.class);
            long responseTimeMs = System.currentTimeMillis() - startMs;
            return parse(200, body, responseTimeMs);
        } catch (RestClientResponseException e) {
            long responseTimeMs = System.currentTimeMillis() - startMs;
            String body = e.getResponseBodyAsString();
            log.warn("Hopzone API returned status {} for URL {}", e.getStatusCode().value(), maskToken(endpoint));
            return parse(e.getStatusCode().value(), body, responseTimeMs);
        } catch (Exception e) {
            long responseTimeMs = System.currentTimeMillis() - startMs;
            log.error("Hopzone API call failed for URL {}: {}", maskToken(endpoint), e.getMessage());
            return new HopzoneCheckResult(500, "{\"error\":\"" + sanitize(e.getMessage()) + "\"}", false, "", "", null, null, responseTimeMs);
        }
    }

    private HopzoneCheckResult parse(int httpStatus, String body, long responseTimeMs) {
        String payload = (body == null || body.isBlank()) ? "{}" : body;

        try {
            JsonNode node = objectMapper.readTree(payload);
            boolean voted = node.path("voted").asBoolean(false);
            String voteTime = firstNonEmpty(node, "voteTime", "vote_time");
            String serverTime = firstNonEmpty(node, "hopzoneServerTime", "server_time");
            Integer statusCode = node.has("status_code") ? node.get("status_code").asInt() : httpStatus;
            String apiver = node.has("apiver") ? node.get("apiver").asText("") : "";
            return new HopzoneCheckResult(httpStatus, payload, voted, voteTime, serverTime, statusCode, apiver, responseTimeMs);
        } catch (Exception ignored) {
            boolean voted = VOTED_TRUE_PATTERN.matcher(payload).find();
            return new HopzoneCheckResult(httpStatus, payload, voted, "", "", null, null, responseTimeMs);
        }
    }

    private String firstNonEmpty(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.has(key)) {
                String v = node.get(key).asText("");
                if (v != null && !v.isBlank()) return v;
            }
        }
        return "";
    }

    private String maskToken(String value) {
        String token = appProperties.getHopzoneToken();
        if (token == null || token.isBlank()) {
            return value;
        }
        return value.replace(token, "***");
    }

    private String sanitize(String value) {
        if (value == null) {
            return "unknown";
        }
        return value.replace("\"", "'");
    }
}
