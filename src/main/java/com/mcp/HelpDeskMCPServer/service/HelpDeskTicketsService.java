package com.mcp.HelpDeskMCPServer.service;

import com.mcp.HelpDeskMCPServer.entity.ClaimDecision;
import com.mcp.HelpDeskMCPServer.entity.HelpDeskTicket;
import com.mcp.HelpDeskMCPServer.model.ClaimDecisionDB;
import com.mcp.HelpDeskMCPServer.repository.HelpDeskTicketsRepository;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Data
public class HelpDeskTicketsService {
    private final HelpDeskTicketsRepository helpDeskTicketsRepository;
    private final ClaimDecisionDB claimDecisionDB;

    public HelpDeskTicket createHelpDeskTicket(String issue, String userName) {
          HelpDeskTicket ticket =  HelpDeskTicket.builder()
                    .issueDescription(issue)
                    .customerName(userName)
                    .priority("high")
                    .status("OPEN")
                    .created(LocalDateTime.now())
                    .eta(LocalDateTime.now().plusDays(3))
                    .build();

        return helpDeskTicketsRepository.save(ticket);
    }

    public List<ClaimDecision> findClaimDecisionByClaimId(Long claimId) {
        return claimDecisionDB.findLatestByClaimId(claimId).orElse(null);
    }

    public List<HelpDeskTicket> findAllTickets(String userName) {
        return helpDeskTicketsRepository.findByCustomerName(userName);
    }
}
