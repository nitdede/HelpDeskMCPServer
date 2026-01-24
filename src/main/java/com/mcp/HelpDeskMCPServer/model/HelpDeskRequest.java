package com.mcp.HelpDeskMCPServer.model;

public record HelpDeskRequest(String claimId, String issueDescription, String customerName, String policyNumber) {
}
