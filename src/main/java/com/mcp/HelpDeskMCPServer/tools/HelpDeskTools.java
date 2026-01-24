package com.mcp.HelpDeskMCPServer.tools;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.mcp.HelpDeskMCPServer.entity.ClaimDecision;
import com.mcp.HelpDeskMCPServer.entity.HelpDeskTicket;
import com.mcp.HelpDeskMCPServer.service.HelpDeskTicketsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class HelpDeskTools {

    private static final Logger logger = LoggerFactory.getLogger(HelpDeskTools.class);
    private final HelpDeskTicketsService helpDeskTicketsService;
    private final VectorStore vectorStore;
    private final Executor vectorTaskExecutor;

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

    @Tool(name = "readUserPolicy", description = "Read the user's policy document based on policy number and customer ID")
    public String readUserPolicy(@JsonProperty(required = true) @JsonPropertyDescription("The policyNumber to look up") String policyNumber, @JsonProperty(required = true) @JsonPropertyDescription("The userName") String patientName) {

        logger.info("RAG lookup for policyNumber: {} customerId: {}", policyNumber, patientName.toUpperCase());

        if (ragCache.isEmpty() || !ragCache.containsKey(policyNumber)) {
            // Simple search without filter - filter results in-memory instead
            SearchRequest request = SearchRequest.builder()
                    .query(policyNumber + " " + patientName.toUpperCase())
                    .topK(10) // get more results and filter in memory
                    .build();

            List<Document> matches = runBlockingSimilaritySearch(request, 30);

            // Filter results in-memory based on metadata
            List<Document> filtered = matches.stream()
                    .filter(doc -> {
                        Map<String, Object> metadata = doc.getMetadata();
                        if (metadata == null) return false;

                        String docPolicy = (String) metadata.get("policyNumber");
                        String docCustomer = (String) metadata.get("customerId");

                        return policyNumber.equals(docPolicy) &&
                                patientName.toUpperCase().equals(docCustomer);
                    })
                    .limit(3)
                    .toList();

            String policyText = filtered.stream()
                    .map(Document::getText)
                    .collect(Collectors.joining("\n"));

            ragCache.put(policyNumber, policyText);
        }

        return ragCache.get(policyNumber);
    }

    /**
     * Run the vector store similarity search on the shared blocking executor with a timeout.
     * If the search times out, returns an empty list (caller may use cached results as fallback).
     */
    private List<Document> runBlockingSimilaritySearch(SearchRequest request, int timeoutSeconds) {
        CompletableFuture<List<Document>> future = CompletableFuture.supplyAsync(() -> vectorStore.similaritySearch(request), vectorTaskExecutor);
        try {
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException te) {
            future.cancel(true);
            logger.info("Vector similaritySearch timed out after {}s", timeoutSeconds);
            return List.of();
        } catch (Exception e) {
            logger.info("Vector similaritySearch failed: {}", e.getMessage());
            return List.of();
        }
    }
}
