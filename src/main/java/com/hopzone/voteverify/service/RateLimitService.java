package com.hopzone.voteverify.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    private static final int LIMIT_PER_MINUTE = 10;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final Duration STALE_ENTRY = Duration.ofMinutes(10);

    private final Map<String, Deque<Instant>> requests = new ConcurrentHashMap<>();

    public boolean isAllowed(String ip) {
        Deque<Instant> deque = requests.computeIfAbsent(ip, key -> new ArrayDeque<>());
        Instant now = Instant.now();
        synchronized (deque) {
            removeOld(deque, now.minus(WINDOW));
            if (deque.size() >= LIMIT_PER_MINUTE) {
                return false;
            }
            deque.addLast(now);
            return true;
        }
    }

    @Scheduled(fixedDelay = 300000)
    public void cleanup() {
        Instant staleThreshold = Instant.now().minus(STALE_ENTRY);
        requests.entrySet().removeIf(entry -> {
            Deque<Instant> deque = entry.getValue();
            synchronized (deque) {
                removeOld(deque, staleThreshold);
                return deque.isEmpty();
            }
        });
    }

    private void removeOld(Deque<Instant> deque, Instant threshold) {
        while (!deque.isEmpty() && deque.peekFirst().isBefore(threshold)) {
            deque.pollFirst();
        }
    }
}
