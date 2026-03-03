package com.hopzone.voteverify.controller;

import com.hopzone.voteverify.config.AppProperties;
import com.hopzone.voteverify.entity.VoteCase;
import com.hopzone.voteverify.service.CaseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ReportController {

    private final CaseService caseService;
    private final AppProperties appProperties;

    public ReportController(CaseService caseService, AppProperties appProperties) {
        this.caseService = caseService;
        this.appProperties = appProperties;
    }

    @GetMapping("/report/{hash}")
    public String report(@PathVariable("hash") String hash, Model model) {
        VoteCase voteCase = caseService.getByReportHash(hash);
        model.addAttribute("case", voteCase);
        model.addAttribute("reportFailIntro", appProperties.getReportFailIntro());
        return "report";
    }
}
