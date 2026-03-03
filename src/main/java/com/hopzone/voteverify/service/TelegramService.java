package com.hopzone.voteverify.service;

import com.hopzone.voteverify.config.AppProperties;
import com.hopzone.voteverify.entity.VoteCase;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class TelegramService {

    private static final Logger log = LoggerFactory.getLogger(TelegramService.class);
    private static final String API_URL = "https://api.telegram.org/bot%s/sendMessage";

    private final AppProperties appProperties;
    private final RestClient restClient;

    public TelegramService(AppProperties appProperties) {
        this.appProperties = appProperties;
        this.restClient = RestClient.builder().build();
    }

    public void notifyNewCase(VoteCase voteCase) {
        if (!appProperties.isTelegramEnabled()) {
            return;
        }
        String reportUrl = appProperties.getBaseUrl() + "/report/" + voteCase.getReportHash();
        String status = voteCase.isVoted() ? "✅ voted" : "❌ not voted";
        String contact = formatContact(voteCase);
        String nick = resolveNick(voteCase);
        String cmdEscaped = escapeHtml("/compensation-send " + nick + " vote");
        String text = String.format("""
            🎮 Hopzone Vote Verify — новый кейс

            %s
            IP: %s
            Account ID: %s
            %s
            Time: %s
            voteTime: %s
            hopzoneServerTime: %s

            Report: %s

            %s
            """,
            status,
            voteCase.getIp(),
            voteCase.getHopzoneAccountId() != null ? voteCase.getHopzoneAccountId() : "-",
            contact,
            voteCase.getCreatedAtUtc(),
            voteCase.getVoteTime() != null ? voteCase.getVoteTime() : "-",
            voteCase.getHopzoneServerTime() != null ? voteCase.getHopzoneServerTime() : "-",
            reportUrl,
            "<code>" + cmdEscaped + "</code>"
        );
        send(text);
    }

    private String resolveNick(VoteCase c) {
        if (c.getGameNickname() != null && !c.getGameNickname().isBlank()) {
            return c.getGameNickname().trim();
        }
        if (c.getUserContact() != null && !c.getUserContact().isBlank()) {
            return c.getUserContact().trim();
        }
        return "nick";
    }

    private String formatContact(VoteCase c) {
        StringBuilder sb = new StringBuilder();
        if (c.getGameNickname() != null && !c.getGameNickname().isBlank()) {
            sb.append("Ник: ").append(c.getGameNickname());
        }
        if (c.getTelegram() != null && !c.getTelegram().isBlank()) {
            if (sb.length() > 0) sb.append("\n");
            sb.append("TG: ").append(c.getTelegram());
        }
        if (c.getDiscord() != null && !c.getDiscord().isBlank()) {
            if (sb.length() > 0) sb.append("\n");
            sb.append("Discord: ").append(c.getDiscord());
        }
        if (c.getEmail() != null && !c.getEmail().isBlank()) {
            if (sb.length() > 0) sb.append("\n");
            sb.append("Email: ").append(c.getEmail());
        }
        if (c.getUserContact() != null && !c.getUserContact().isBlank() && sb.length() == 0) {
            sb.append("Contact: ").append(c.getUserContact());
        }
        return sb.length() > 0 ? sb.toString() : "Contact: -";
    }

    private void send(String text) {
        try {
            String url = String.format(API_URL, appProperties.getTelegramBotToken());
            StringBuilder body = new StringBuilder();
            body.append("chat_id=").append(URLEncoder.encode(appProperties.getTelegramChatId(), StandardCharsets.UTF_8));
            if (appProperties.getTelegramMessageThreadId() != null) {
                body.append("&message_thread_id=").append(appProperties.getTelegramMessageThreadId());
            }
            body.append("&parse_mode=HTML");
            body.append("&text=").append(URLEncoder.encode(text, StandardCharsets.UTF_8));
            restClient.post()
                .uri(URI.create(url))
                .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                .body(body.toString())
                .retrieve()
                .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Telegram send failed: {}", e.getMessage());
        }
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

}
