package com.hopzone.voteverify.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class IpResolverService {

    public String getClientIp(HttpServletRequest request) {
        String cfIp = request.getHeader("CF-Connecting-IP");
        if (StringUtils.hasText(cfIp)) {
            return cfIp.trim();
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            String[] parts = forwardedFor.split(",");
            if (parts.length > 0 && StringUtils.hasText(parts[0])) {
                return parts[0].trim();
            }
        }

        return request.getRemoteAddr();
    }
}
