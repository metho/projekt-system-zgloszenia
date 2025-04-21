package com.puw.systemzgloszen.service;

import com.puw.systemzgloszen.entity.Ticket;
import com.puw.systemzgloszen.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

    public List<Ticket> findAll() {
        return ticketRepository.findAll();
    }

    public Ticket findById(int id) {
        return ticketRepository.findById(id).orElse(null);
    }

    public void save(Ticket ticket) {
        ticketRepository.save(ticket);
    }

    public void delete(Ticket ticket) {
        ticketRepository.delete(ticket);
    }
}
