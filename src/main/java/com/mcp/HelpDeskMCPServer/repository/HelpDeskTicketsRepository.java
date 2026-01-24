package com.mcp.HelpDeskMCPServer.repository;

import com.mcp.HelpDeskMCPServer.entity.HelpDeskTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HelpDeskTicketsRepository extends JpaRepository<HelpDeskTicket, Long> {
    List<HelpDeskTicket> findByCustomerName(String customerName);

}