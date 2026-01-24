package com.mcp.HelpDeskMCPServer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@Table(name = "claim_decision_evidence")
public class ClaimDecisionEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // decision_id BIGINT NOT NULL REFERENCES claim_decisions(id)
    @Column(name = "decision_id", nullable = false, insertable = false, updatable = false)
    private Long decisionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_id", nullable = false)
    private ClaimDecision claimDecision;

    // chunk_text TEXT NOT NULL
    @Column(name = "chunk_text", columnDefinition = "text", nullable = false)
    private String chunkText;

    // score NUMERIC(10,6)
    @Column(name = "score", precision = 10, scale = 6)
    private BigDecimal score;

    // created_at TIMESTAMPTZ DEFAULT now()
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    public ClaimDecisionEvidence() {
        this.createdAt = OffsetDateTime.now();
    }

}
