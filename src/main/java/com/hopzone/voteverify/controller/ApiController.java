package com.hopzone.voteverify.controller;

import com.hopzone.voteverify.config.AppProperties;
import com.hopzone.voteverify.dto.CheckResponse;
import com.hopzone.voteverify.service.CaseService;
import com.hopzone.voteverify.service.IpResolverService;
import com.hopzone.voteverify.service.RateLimitService;
import com.hopzone.voteverify.service.RecaptchaService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final RecaptchaService recaptchaService;
    private final IpResolverService ipResolverService;
    private final RateLimitService rateLimitService;
    private final CaseService caseService;
    private final AppProperties appProperties;

    public ApiController(
        RecaptchaService recaptchaService,
        IpResolverService ipResolverService,
        RateLimitService rateLimitService,
        CaseService caseService,
        AppProperties appProperties
    ) {
        this.recaptchaService = recaptchaService;
        this.ipResolverService = ipResolverService;
        this.rateLimitService = rateLimitService;
        this.caseService = caseService;
        this.appProperties = appProperties;
    }

    @PostMapping(value = "/check", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> check(
        @RequestParam("screenshotIp") MultipartFile screenshotIp,
        @RequestParam("screenshotVote") MultipartFile screenshotVote,
        @RequestParam("hopzoneAccountId") String hopzoneAccountId,
        @RequestParam("gameNickname") String gameNickname,
        @RequestParam(name = "telegram", required = false) String telegram,
        @RequestParam(name = "discord", required = false) String discord,
        @RequestParam(name = "email", required = false) String email,
        @RequestParam(name = "g-recaptcha-response", required = false) String recaptchaResponse,
        HttpServletRequest request
    ) throws Exception {
        String realIp = ipResolverService.getClientIp(request);
        String ip = appProperties.isDebugIpOverrideEnabled()
            ? appProperties.getDebugIpOverride().trim()
            : realIp;
        if (!rateLimitService.isAllowed(realIp)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("error", "Too many requests. Limit: 10 per minute."));
        }

        if (!StringUtils.hasText(hopzoneAccountId) || hopzoneAccountId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Hopzone Account ID is required. Authorize in Hopzone cabinet, Account tab, copy ID."));
        }
        if (!StringUtils.hasText(gameNickname) || gameNickname.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ник в игре обязателен."));
        }
        boolean recaptchaOk = true;
        if (recaptchaService.isEnabled()) {
            if (!StringUtils.hasText(recaptchaResponse)) {
                return ResponseEntity.badRequest().body(Map.of("error", "reCAPTCHA token is required."));
            }
            recaptchaOk = recaptchaService.verifyToken(recaptchaResponse, realIp);
            if (!recaptchaOk) {
                return ResponseEntity.badRequest().body(Map.of("error", "reCAPTCHA verification failed."));
            }
        }

        String userAgent = request.getHeader("User-Agent");
        CheckResponse response = caseService.processCheck(ip, userAgent, hopzoneAccountId.trim(),
            gameNickname.trim(),
            StringUtils.hasText(telegram) ? telegram.trim() : null,
            StringUtils.hasText(discord) ? discord.trim() : null,
            StringUtils.hasText(email) ? email.trim() : null,
            screenshotIp, screenshotVote, recaptchaOk);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
