package com.puw.systemzgloszen.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
public class UserComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String comment;
    private LocalDateTime commentDate;
    @ManyToOne
    private Ticket ticket;
    @ManyToOne
    private AppUser user;

}
