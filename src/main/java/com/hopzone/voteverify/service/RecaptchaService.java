package com.hopzone.voteverify.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.hopzone.voteverify.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class RecaptchaService {

    private static final Logger log = LoggerFactory.getLogger(RecaptchaService.class);
    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    private final AppProperties appProperties;
    private final RestClient restClient;

    public RecaptchaService(AppProperties appProperties) {
        this.appProperties = appProperties;
        this.restClient = RestClient.builder().build();
    }

    public boolean isEnabled() {
        return appProperties.isRecaptchaEnabled();
    }

    public boolean verifyToken(String recaptchaToken, String ip) {
        if (recaptchaToken == null || recaptchaToken.isBlank()) {
            return false;
        }
        if (appProperties.getRecaptchaSecretKey() == null || appProperties.getRecaptchaSecretKey().isBlank()) {
            log.warn("reCAPTCHA secret key is empty. Verification rejected.");
            return false;
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("secret", appProperties.getRecaptchaSecretKey());
        form.add("response", recaptchaToken);
        form.add("remoteip", ip);

        try {
            JsonNode response = restClient.post()
                .uri(VERIFY_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(JsonNode.class);

            return response != null && response.path("success").asBoolean(false);
        } catch (Exception e) {
            log.error("reCAPTCHA verification failed: {}", e.getMessage());
            return false;
        }
    }
}
