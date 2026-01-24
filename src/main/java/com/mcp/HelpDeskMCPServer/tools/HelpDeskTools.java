package com.mcp.HelpDeskMCPServer.tools;

import com.mcp.HelpDeskMCPServer.entity.ClaimDecision;
import com.mcp.HelpDeskMCPServer.entity.HelpDeskTicket;
import com.mcp.HelpDeskMCPServer.service.HelpDeskTicketsService;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class HelpDeskTools {

    private static final Logger logger = LoggerFactory.getLogger(HelpDeskTools.class);
    private final HelpDeskTicketsService helpDeskTicketsService;

    private final Map<String, String> ragCache = new HashMap<>();

    @Tool(name = "helpUser", description = "Provide assistance to the user regarding their claim issue by reading the claim decision details")
    public ClaimDecision helpUser(@JsonProperty(required = true) @JsonPropertyDescription("The claim ID to look up") String claimId) {

        List<ClaimDecision> claimDecision = helpDeskTicketsService.findClaimDecisionByClaimId(Long.valueOf(claimId));
        if (claimDecision.isEmpty()) {
            return null;
        }
        return claimDecision.get(0);
    }

    @Tool(name = "createTicket", description = "Create a help desk ticket for the user's issue or problem")
    public String createTicket(@JsonProperty(required = true) @JsonPropertyDescription("The name of the user creating the ticket") String userName, @JsonProperty(required = true) @JsonPropertyDescription("Description of the issue or problem") String issueDescription) {

        HelpDeskTicket helpDeskTicket = helpDeskTicketsService.createHelpDeskTicket(issueDescription, userName);
        return "Ticket created with ID: " + helpDeskTicket.getId();
    }

    @Tool(name = "getAllTickets", description = "Get all help desk tickets for the user")
    public List<HelpDeskTicket> getAllTickets(@JsonProperty(required = true) @JsonPropertyDescription("The name of the user to retrieve tickets for") String userName) {

        return helpDeskTicketsService.findAllTickets(userName);
    }

    /*@Tool(name = "readUserPolicy", description = "Read the user's policy document based on policy number and customer ID")
    public String readUserPolicy(ToolContext toolContext) {

        String policyNumber = (String) toolContext.getContext().get("policyNumber");
        String patientName = (String) toolContext.getContext().get("userName");
        String filerStr = "policyNumber == '" + policyNumber + "' && customerId == '" + patientName.toUpperCase() + "'";

        if (ragCache.isEmpty() || !ragCache.entrySet().contains(policyNumber)) {
            if (ragCache.isEmpty() || !ragCache.containsKey(policyNumber)) {
                SearchRequest request = SearchRequest.builder().query(policyNumber).filterExpression(filerStr).topK(3) // reduce results to speed up similarity search
                        .build();

                List<Document> matches = runBlockingSimilaritySearch(request, 30);

                String policyText = matches.stream().map(Document::getText).collect(Collectors.joining("\n"));

                ragCache.put(policyNumber, policyText);
            }
        }

        return ragCache.get(policyNumber);
    }*/
}
