package com.hopzone.voteverify.controller;

import com.hopzone.voteverify.config.AppProperties;
import com.hopzone.voteverify.service.IpResolverService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VerifyController {

    private final IpResolverService ipResolverService;
    private final AppProperties appProperties;

    public VerifyController(IpResolverService ipResolverService, AppProperties appProperties) {
        this.ipResolverService = ipResolverService;
        this.appProperties = appProperties;
    }

    @GetMapping({"/", "/verify"})
    public String verifyPage(HttpServletRequest request, Model model) {
        String realIp = ipResolverService.getClientIp(request);
        model.addAttribute("hopzoneVoteUrl", appProperties.getHopzoneVoteUrl());
        model.addAttribute("clientIp", appProperties.isDebugIpOverrideEnabled()
            ? appProperties.getDebugIpOverride().trim()
            : realIp);
        model.addAttribute("debugIpOverride", appProperties.isDebugIpOverrideEnabled());
        model.addAttribute("debugIpOverrideValue", appProperties.getDebugIpOverride());
        model.addAttribute("realIp", realIp);
        model.addAttribute("recaptchaSiteKey", appProperties.getRecaptchaSiteKey());
        model.addAttribute("recaptchaEnabled", appProperties.isRecaptchaEnabled());
        model.addAttribute("baseUrl", appProperties.getBaseUrl());
        return "verify";
    }
}
