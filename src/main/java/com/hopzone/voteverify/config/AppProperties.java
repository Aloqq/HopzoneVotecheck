package com.hopzone.voteverify.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String baseUrl = "http://localhost:8080";
    private String hopzoneToken = "";
    private String hopzoneEndpoint = "https://api.hopzone.net/lineage2/vote?token={TOKEN}&ip_address={IP}";
    private boolean recaptchaEnabled = true;
    private String recaptchaSiteKey = "";
    private String recaptchaSecretKey = "";
    private String storageDir = "uploads";
    private String hopzoneSiteUrl = "https://www.hopzone.net";
    private String hopzoneServerId = "";
    private String hopzoneVoteUrl = "https://l2.hopzone.net/ru/site/vote/106168/1";
    private String reportFailIntro = "";
    private String debugIpOverride = "";
    private String telegramBotToken = "";
    private String telegramChatId = "";
    private String telegramMessageThreadId = "";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getHopzoneToken() {
        return hopzoneToken;
    }

    public void setHopzoneToken(String hopzoneToken) {
        this.hopzoneToken = hopzoneToken;
    }

    public String getHopzoneEndpoint() {
        return hopzoneEndpoint;
    }

    public void setHopzoneEndpoint(String hopzoneEndpoint) {
        this.hopzoneEndpoint = hopzoneEndpoint;
    }

    public String getRecaptchaSiteKey() {
        return recaptchaSiteKey;
    }

    public boolean isRecaptchaEnabled() {
        return recaptchaEnabled;
    }

    public void setRecaptchaEnabled(boolean recaptchaEnabled) {
        this.recaptchaEnabled = recaptchaEnabled;
    }

    public void setRecaptchaSiteKey(String recaptchaSiteKey) {
        this.recaptchaSiteKey = recaptchaSiteKey;
    }

    public String getRecaptchaSecretKey() {
        return recaptchaSecretKey;
    }

    public void setRecaptchaSecretKey(String recaptchaSecretKey) {
        this.recaptchaSecretKey = recaptchaSecretKey;
    }

    public String getStorageDir() {
        return storageDir;
    }

    public void setStorageDir(String storageDir) {
        this.storageDir = storageDir;
    }

    public String getHopzoneSiteUrl() {
        return hopzoneSiteUrl;
    }

    public void setHopzoneSiteUrl(String hopzoneSiteUrl) {
        this.hopzoneSiteUrl = hopzoneSiteUrl;
    }

    public String getHopzoneServerId() {
        return hopzoneServerId;
    }

    public void setHopzoneServerId(String hopzoneServerId) {
        this.hopzoneServerId = hopzoneServerId;
    }

    public String getHopzoneVoteUrl() {
        return hopzoneVoteUrl;
    }

    public void setHopzoneVoteUrl(String hopzoneVoteUrl) {
        this.hopzoneVoteUrl = hopzoneVoteUrl;
    }

    public String getReportFailIntro() {
        return reportFailIntro;
    }

    public void setReportFailIntro(String reportFailIntro) {
        this.reportFailIntro = reportFailIntro;
    }

    public String getDebugIpOverride() {
        return debugIpOverride;
    }

    public void setDebugIpOverride(String debugIpOverride) {
        this.debugIpOverride = debugIpOverride;
    }

    public boolean isDebugIpOverrideEnabled() {
        return debugIpOverride != null && !debugIpOverride.isBlank();
    }

    public String getTelegramBotToken() {
        return telegramBotToken;
    }

    public void setTelegramBotToken(String telegramBotToken) {
        this.telegramBotToken = telegramBotToken;
    }

    public String getTelegramChatId() {
        return telegramChatId;
    }

    public void setTelegramChatId(String telegramChatId) {
        this.telegramChatId = telegramChatId;
    }

    public boolean isTelegramEnabled() {
        return telegramBotToken != null && !telegramBotToken.isBlank()
            && telegramChatId != null && !telegramChatId.isBlank();
    }

    public Integer getTelegramMessageThreadId() {
        if (telegramMessageThreadId == null || telegramMessageThreadId.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(telegramMessageThreadId.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void setTelegramMessageThreadId(String telegramMessageThreadId) {
        this.telegramMessageThreadId = telegramMessageThreadId != null ? telegramMessageThreadId : "";
    }
}
