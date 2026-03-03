package com.hopzone.voteverify.controller;

import com.hopzone.voteverify.entity.VoteCase;
import com.hopzone.voteverify.service.CaseService;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HistoryController {

    private final CaseService caseService;

    public HistoryController(CaseService caseService) {
        this.caseService = caseService;
    }

    @GetMapping("/history")
    public String history(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size,
        @RequestParam(required = false) String ip,
        @RequestParam(defaultValue = "all") String voted,
        @RequestParam(name = "dateFrom", required = false) String dateFromRaw,
        @RequestParam(name = "dateTo", required = false) String dateToRaw,
        Model model
    ) {
        LocalDate dateFrom = parseDate(dateFromRaw);
        LocalDate dateTo = parseDate(dateToRaw);

        Page<VoteCase> result = caseService.getHistory(ip, voted, dateFrom, dateTo, page, size);
        List<VoteCaseRow> rows = result.getContent().stream()
            .map(c -> new VoteCaseRow(
                c.getCreatedAtUtc(),
                c.getIp(),
                caseService.maskIp(c.getIp()),
                c.isVoted(),
                c.getVoteTime(),
                c.getHopzoneServerTime(),
                c.getHopzoneHttpStatus(),
                c.getHopzoneStatusCode(),
                c.getHopzoneResponseTimeMs(),
                c.getHopzoneApiver(),
                c.getHopzoneServerId(),
                c.getGameNickname(),
                c.getTelegram(),
                c.getDiscord(),
                c.getEmail(),
                c.getReportHash()
            ))
            .toList();

        model.addAttribute("rows", rows);
        model.addAttribute("pageData", result);
        model.addAttribute("ip", ip);
        model.addAttribute("voted", StringUtils.hasText(voted) ? voted : "all");
        model.addAttribute("dateFrom", dateFromRaw);
        model.addAttribute("dateTo", dateToRaw);
        model.addAttribute("size", size);
        return "history";
    }

    private LocalDate parseDate(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    public record VoteCaseRow(
        java.time.LocalDateTime createdAtUtc,
        String ip,
        String maskedIp,
        boolean voted,
        String voteTime,
        String hopzoneServerTime,
        Integer hopzoneHttpStatus,
        Integer hopzoneStatusCode,
        Long hopzoneResponseTimeMs,
        String hopzoneApiver,
        String hopzoneServerId,
        String gameNickname,
        String telegram,
        String discord,
        String email,
        String reportHash
    ) {
    }
}
