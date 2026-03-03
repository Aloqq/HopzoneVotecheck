package com.hopzone.voteverify.service;

import com.hopzone.voteverify.config.AppProperties;
import com.hopzone.voteverify.dto.CheckResponse;
import com.hopzone.voteverify.service.TelegramService;
import com.hopzone.voteverify.dto.HopzoneCheckResult;
import com.hopzone.voteverify.entity.VoteCase;
import com.hopzone.voteverify.repository.VoteCaseRepository;
import jakarta.persistence.criteria.Predicate;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CaseService {

    private final VoteCaseRepository voteCaseRepository;
    private final StorageService storageService;
    private final HopzoneClient hopzoneClient;
    private final AppProperties appProperties;
    private final TelegramService telegramService;
    private final Clock clock;

    public CaseService(
        VoteCaseRepository voteCaseRepository,
        StorageService storageService,
        HopzoneClient hopzoneClient,
        AppProperties appProperties,
        TelegramService telegramService
    ) {
        this.voteCaseRepository = voteCaseRepository;
        this.storageService = storageService;
        this.hopzoneClient = hopzoneClient;
        this.appProperties = appProperties;
        this.telegramService = telegramService;
        this.clock = Clock.systemUTC();
    }

    @Transactional
    public CheckResponse processCheck(
        String ip,
        String userAgent,
        String hopzoneAccountId,
        String gameNickname,
        String telegram,
        String discord,
        String email,
        MultipartFile screenshotIp,
        MultipartFile screenshotVote,
        boolean recaptchaOk
    ) throws Exception {
        HopzoneCheckResult hopzone = hopzoneClient.checkVote(ip);

        if (hopzone.voted()) {
            return new CheckResponse(
                true,
                emptyToDash(hopzone.voteTime()),
                emptyToDash(hopzone.hopzoneServerTime()),
                hopzone.rawJson(),
                null
            );
        }

        String screenshotIpPath = storageService.store(screenshotIp, "Screenshot #1");
        String screenshotVotePath = storageService.store(screenshotVote, "Screenshot #2");

        VoteCase voteCase = new VoteCase();
        voteCase.setReportHash(generateReportHash(ip));
        voteCase.setCreatedAtUtc(LocalDateTime.now(clock));
        voteCase.setIp(ip);
        voteCase.setUserAgent(truncate(userAgent, 255));
        voteCase.setRecaptchaOk(recaptchaOk);
        voteCase.setHopzoneHttpStatus(hopzone.httpStatus());
        voteCase.setHopzoneRawJson(hopzone.rawJson());
        voteCase.setVoted(false);
        voteCase.setVoteTime(truncate(hopzone.voteTime(), 64));
        voteCase.setHopzoneServerTime(truncate(hopzone.hopzoneServerTime(), 64));
        voteCase.setScreenshotIpPath(truncate(screenshotIpPath, 255));
        voteCase.setScreenshotVotePath(truncate(screenshotVotePath, 255));
        voteCase.setHopzoneAccountId(truncate(hopzoneAccountId, 64));
        voteCase.setHopzoneServerId(truncate(appProperties.getHopzoneServerId(), 32));
        voteCase.setGameNickname(truncate(gameNickname, 64));
        voteCase.setTelegram(truncate(telegram, 128));
        voteCase.setDiscord(truncate(discord, 128));
        voteCase.setEmail(truncate(email, 255));
        voteCase.setHopzoneResponseTimeMs(hopzone.responseTimeMs());
        voteCase.setHopzoneStatusCode(hopzone.statusCode());
        voteCase.setHopzoneApiver(truncate(hopzone.apiver() != null ? hopzone.apiver() : "", 32));
        voteCase = voteCaseRepository.save(voteCase);
        telegramService.notifyNewCase(voteCase);

        return new CheckResponse(
            false,
            emptyToDash(voteCase.getVoteTime()),
            emptyToDash(voteCase.getHopzoneServerTime()),
            voteCase.getHopzoneRawJson(),
            "/report/" + voteCase.getReportHash()
        );
    }

    @Transactional(readOnly = true)
    public VoteCase getByReportHash(String hash) {
        return voteCaseRepository.findByReportHash(hash)
            .orElseThrow(() -> new IllegalArgumentException("Report case not found."));
    }

    @Transactional(readOnly = true)
    public Page<VoteCase> getHistory(String ip, String voted, LocalDate from, LocalDate to, int page, int size) {
        int safeSize = Math.max(1, Math.min(size, 500));
        Pageable pageable = PageRequest.of(Math.max(page, 0), safeSize, Sort.by(Sort.Direction.DESC, "createdAtUtc"));
        Specification<VoteCase> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(ip)) {
                predicates.add(cb.equal(root.get("ip"), ip.trim()));
            }
            if (StringUtils.hasText(voted) && !"all".equalsIgnoreCase(voted)) {
                predicates.add(cb.equal(root.get("voted"), Boolean.parseBoolean(voted)));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAtUtc"), from.atStartOfDay()));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAtUtc"), to.atTime(23, 59, 59)));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return voteCaseRepository.findAll(spec, pageable);
    }

    public String maskIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return "";
        }
        if (ip.contains(".")) {
            String[] parts = ip.split("\\.");
            if (parts.length == 4) {
                return parts[0] + "." + parts[1] + ".***.***";
            }
        }
        return ip.length() <= 6 ? "***" : ip.substring(0, 4) + "***";
    }

    public String buildReportMessage(VoteCase voteCase) {
        String reportUrl = appProperties.getBaseUrl() + "/report/" + voteCase.getReportHash();
        String screenshot1 = appProperties.getBaseUrl() + voteCase.getScreenshotIpPath();
        String screenshot2 = appProperties.getBaseUrl() + voteCase.getScreenshotVotePath();

        return """
            Hello Hopzone team,
                        
            Please check a possible selective vote recognition issue for Lineage2.
            For this IP, your API returns voted=false, but the screenshot shows “You voted”.
            It does not look like a generic delay because other IPs are confirmed almost instantly.
                        
            Hopzone Account ID: %s
            Hopzone Server ID (from vote URL): %s
            Ник: %s | TG: %s | Discord: %s | Email: %s
            IP: %s
            UTC time: %s
            API voted: %s
            API vote_time: %s
            API hopzone_server_time: %s
            API status_code: %s
            API response time (ms): %s
            API apiver: %s
            Raw JSON: %s
                        
            Report page: %s
            Screenshot #1 (in-game IP): %s
            Screenshot #2 (Hopzone “You voted”): %s
                        
            Could you please check logs / antifraud / vote write records for this IP and timestamp?
            Thank you.
            """
            .formatted(
                emptyToDash(voteCase.getHopzoneAccountId()),
                emptyToDash(voteCase.getHopzoneServerId()),
                emptyToDash(voteCase.getGameNickname()),
                emptyToDash(voteCase.getTelegram()),
                emptyToDash(voteCase.getDiscord()),
                emptyToDash(voteCase.getEmail()),
                voteCase.getIp(),
                voteCase.getCreatedAtUtc() + " UTC",
                voteCase.isVoted(),
                emptyToDash(voteCase.getVoteTime()),
                emptyToDash(voteCase.getHopzoneServerTime()),
                voteCase.getHopzoneStatusCode() != null ? voteCase.getHopzoneStatusCode() : voteCase.getHopzoneHttpStatus(),
                voteCase.getHopzoneResponseTimeMs() != null ? voteCase.getHopzoneResponseTimeMs() : "-",
                emptyToDash(voteCase.getHopzoneApiver()),
                voteCase.getHopzoneRawJson(),
                reportUrl,
                screenshot1,
                screenshot2
            );
    }

    @Transactional
    @Scheduled(cron = "0 0 */6 * * *")
    public void cleanupOldCases() {
        LocalDateTime cutoff = LocalDateTime.now(clock).minusDays(14);
        List<VoteCase> oldCases = voteCaseRepository.findByCreatedAtUtcBefore(cutoff);
        for (VoteCase voteCase : oldCases) {
            storageService.deleteIfExists(voteCase.getScreenshotIpPath());
            storageService.deleteIfExists(voteCase.getScreenshotVotePath());
        }
        if (!oldCases.isEmpty()) {
            voteCaseRepository.deleteAllInBatch(oldCases);
        }
    }

    private String generateReportHash(String ip) {
        try {
            String source = UUID.randomUUID() + ":" + System.nanoTime() + ":" + ip + ":" + InstantSource.now(clock);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(source.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception e) {
            return UUID.randomUUID().toString().replace("-", "");
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private String emptyToDash(String value) {
        return StringUtils.hasText(value) ? value : "-";
    }

    private static final class InstantSource {
        private InstantSource() {
        }

        static String now(Clock clock) {
            return java.time.Instant.now(clock).atOffset(ZoneOffset.UTC).toString();
        }
    }
}
