package com.puw.systemzgloszen.entity;

public enum TicketState {
    TODO,
    IN_PROGRESS,
    WAITING,
    COMPLETED,
    REJECTED;

    public String toPolish() {
        return switch (this) {
            case TODO         -> "Do realizacji";
            case IN_PROGRESS  -> "W trakcie";
            case WAITING      -> "Oczekuje";
            case COMPLETED    -> "Rozwiązane";
            case REJECTED     -> "Odrzucone";
        };
    }
}
