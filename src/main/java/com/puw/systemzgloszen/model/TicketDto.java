package com.puw.systemzgloszen.model;

import com.puw.systemzgloszen.entity.TicketState;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class TicketDto {
    private Long id;
    private String title;
    private String description;
    private TicketState state;
    private String assignee;
    private LocalDateTime creationDate;
}
