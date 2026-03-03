package com.hopzone.voteverify.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "vote_cases")
public class VoteCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_hash", nullable = false, unique = true, length = 64)
    private String reportHash;

    @Column(name = "created_at_utc", nullable = false)
    private LocalDateTime createdAtUtc;

    @Column(nullable = false, length = 45)
    private String ip;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "recaptcha_ok", nullable = false)
    private boolean recaptchaOk;

    @Column(name = "hopzone_http_status")
    private Integer hopzoneHttpStatus;

    @Column(name = "hopzone_raw_json", columnDefinition = "TEXT")
    private String hopzoneRawJson;

    @Column(nullable = false)
    private boolean voted;

    @Column(name = "vote_time", length = 64)
    private String voteTime;

    @Column(name = "hopzone_server_time", length = 64)
    private String hopzoneServerTime;

    @Column(name = "screenshot_ip_path", nullable = false, length = 255)
    private String screenshotIpPath;

    @Column(name = "screenshot_vote_path", nullable = false, length = 255)
    private String screenshotVotePath;

    @Column(length = 255)
    private String note;

    @Column(name = "hopzone_account_id", length = 64)
    private String hopzoneAccountId;

    @Column(name = "hopzone_server_id", length = 32)
    private String hopzoneServerId;

    @Column(name = "user_contact", length = 512)
    private String userContact;

    @Column(name = "game_nickname", length = 64)
    private String gameNickname;

    @Column(name = "telegram", length = 128)
    private String telegram;

    @Column(name = "discord", length = 128)
    private String discord;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "hopzone_response_time_ms")
    private Long hopzoneResponseTimeMs;

    @Column(name = "hopzone_status_code")
    private Integer hopzoneStatusCode;

    @Column(name = "hopzone_apiver", length = 32)
    private String hopzoneApiver;

    public Long getId() {
        return id;
    }

    public String getReportHash() {
        return reportHash;
    }

    public void setReportHash(String reportHash) {
        this.reportHash = reportHash;
    }

    public LocalDateTime getCreatedAtUtc() {
        return createdAtUtc;
    }

    public void setCreatedAtUtc(LocalDateTime createdAtUtc) {
        this.createdAtUtc = createdAtUtc;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public boolean isRecaptchaOk() {
        return recaptchaOk;
    }

    public void setRecaptchaOk(boolean recaptchaOk) {
        this.recaptchaOk = recaptchaOk;
    }

    public Integer getHopzoneHttpStatus() {
        return hopzoneHttpStatus;
    }

    public void setHopzoneHttpStatus(Integer hopzoneHttpStatus) {
        this.hopzoneHttpStatus = hopzoneHttpStatus;
    }

    public String getHopzoneRawJson() {
        return hopzoneRawJson;
    }

    public void setHopzoneRawJson(String hopzoneRawJson) {
        this.hopzoneRawJson = hopzoneRawJson;
    }

    public boolean isVoted() {
        return voted;
    }

    public void setVoted(boolean voted) {
        this.voted = voted;
    }

    public String getVoteTime() {
        return voteTime;
    }

    public void setVoteTime(String voteTime) {
        this.voteTime = voteTime;
    }

    public String getHopzoneServerTime() {
        return hopzoneServerTime;
    }

    public void setHopzoneServerTime(String hopzoneServerTime) {
        this.hopzoneServerTime = hopzoneServerTime;
    }

    public String getScreenshotIpPath() {
        return screenshotIpPath;
    }

    public void setScreenshotIpPath(String screenshotIpPath) {
        this.screenshotIpPath = screenshotIpPath;
    }

    public String getScreenshotVotePath() {
        return screenshotVotePath;
    }

    public void setScreenshotVotePath(String screenshotVotePath) {
        this.screenshotVotePath = screenshotVotePath;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getHopzoneAccountId() {
        return hopzoneAccountId;
    }

    public void setHopzoneAccountId(String hopzoneAccountId) {
        this.hopzoneAccountId = hopzoneAccountId;
    }

    public String getHopzoneServerId() {
        return hopzoneServerId;
    }

    public void setHopzoneServerId(String hopzoneServerId) {
        this.hopzoneServerId = hopzoneServerId;
    }

    public String getUserContact() {
        return userContact;
    }

    public void setUserContact(String userContact) {
        this.userContact = userContact;
    }

    public String getGameNickname() {
        return gameNickname;
    }

    public void setGameNickname(String gameNickname) {
        this.gameNickname = gameNickname;
    }

    public String getTelegram() {
        return telegram;
    }

    public void setTelegram(String telegram) {
        this.telegram = telegram;
    }

    public String getDiscord() {
        return discord;
    }

    public void setDiscord(String discord) {
        this.discord = discord;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getHopzoneResponseTimeMs() {
        return hopzoneResponseTimeMs;
    }

    public void setHopzoneResponseTimeMs(Long hopzoneResponseTimeMs) {
        this.hopzoneResponseTimeMs = hopzoneResponseTimeMs;
    }

    public Integer getHopzoneStatusCode() {
        return hopzoneStatusCode;
    }

    public void setHopzoneStatusCode(Integer hopzoneStatusCode) {
        this.hopzoneStatusCode = hopzoneStatusCode;
    }

    public String getHopzoneApiver() {
        return hopzoneApiver;
    }

    public void setHopzoneApiver(String hopzoneApiver) {
        this.hopzoneApiver = hopzoneApiver;
    }
}
