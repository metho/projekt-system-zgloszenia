package com.puw.systemzgloszen.repository;

import com.puw.systemzgloszen.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {
}
