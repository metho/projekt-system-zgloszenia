package com.puw.systemzgloszen.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    @OneToMany(mappedBy = "ticket", fetch = FetchType.EAGER)
    List<UserComment> comments;
    @Enumerated(EnumType.STRING)
    private TicketState state;
    private LocalDateTime creationDate;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser assignee;
}
