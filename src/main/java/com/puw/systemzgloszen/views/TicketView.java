package com.puw.systemzgloszen.views;

import com.puw.systemzgloszen.entity.AppUser;
import com.puw.systemzgloszen.entity.Ticket;
import com.puw.systemzgloszen.model.TicketDto;
import com.puw.systemzgloszen.entity.TicketState;
import com.puw.systemzgloszen.repository.AppUserRepository;
import com.puw.systemzgloszen.service.SecurityService;
import com.puw.systemzgloszen.service.TicketService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@PermitAll
@Route("")
public class TicketView extends VerticalLayout {

    private final TicketService ticketService;
    private final AppUserRepository appUserRepository;
    private final SecurityService securityService;
    private final Grid<TicketDto> grid = new Grid<>(TicketDto.class);

    public TicketView(TicketService ticketService, AppUserRepository appUserRepository, SecurityService securityService) {
        this.ticketService = ticketService;
        this.appUserRepository = appUserRepository;
        this.securityService = securityService;
        setSizeFull();
        setSpacing(true);
        setPadding(true);
        Button logout = new Button("Wyloguj", click ->
                securityService.logout());

        Button openDialogButton = new Button("Nowe zgłoszenie", event -> openAddTicketDialog());
        openDialogButton.getStyle().set("margin-bottom", "1rem");
        HorizontalLayout header = new HorizontalLayout(logout);
        header.setWidthFull();
        header.add(openDialogButton, logout);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        add(header);

        grid.removeAllColumns();
        grid.addColumn(TicketDto::getId).setHeader("Id");
        grid.addColumn(TicketDto::getTitle).setHeader("Tytuł");
        grid.addColumn(TicketDto::getDescription).setHeader("Opis");
        grid.addColumn(ticket -> ticket.getState().toString()).setHeader("Status");
        grid.addColumn(TicketDto::getAssignee).setHeader("Przypisana osoba");

        List<TicketDto> tickets = mapToTicketDtos(ticketService.findAll());
        grid.setItems(tickets);

        grid.addComponentColumn(ticket -> new Button("Wyświetl", e ->
                UI.getCurrent().navigate("ticket/" + ticket.getId())
        )).setHeader("Akcje");

        add(grid);
    }

    private List<TicketDto> mapToTicketDtos(List<Ticket> tickets) {
        return tickets.stream().map(this::mapToTicketDto).toList();
    }

    private TicketDto mapToTicketDto(Ticket ticket) {
        TicketDto ticketDto = new TicketDto();
        ticketDto.setId(ticket.getId());
        ticketDto.setTitle(ticket.getTitle());
        ticketDto.setDescription(ticket.getDescription());
        ticketDto.setState(ticket.getState());
        String assignee = ticket.getAssignee() != null ? ticket.getAssignee().getUsername() : "";
        ticketDto.setAssignee(assignee);
        return ticketDto;
    }

    private void openAddTicketDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Utwórz nowe zgłoszenie");

        TextField titleField = new TextField("Tytuł");
        TextArea descriptionField = new TextArea("Opis");
        String fieldsWidth = "500px";
        descriptionField.setWidth(fieldsWidth);
        descriptionField.setHeight("200px");
        descriptionField.getStyle().set("resize", "vertical");
        ComboBox<String> assigneeField = new ComboBox<>("Przypisana osoba");
        List<String> usernames = appUserRepository.findAll().stream().map(AppUser::getUsername).toList();
        assigneeField.setItems(usernames);
        assigneeField.setPlaceholder("Wybierz uzytkownika");
        assigneeField.setWidth(fieldsWidth);
        titleField.setWidth(fieldsWidth);
        descriptionField.setWidth(fieldsWidth);
        assigneeField.setWidth(fieldsWidth);

        VerticalLayout formLayout = new VerticalLayout(
                titleField, descriptionField, assigneeField
        );
        formLayout.setSpacing(true);

        Button saveButton = new Button("Zapisz", event -> {
            if (!titleField.isEmpty()) {
                Ticket ticket = new Ticket();
                ticket.setTitle(titleField.getValue());
                ticket.setDescription(descriptionField.getValue());
                ticket.setState(TicketState.TODO);
                ticket.setAssignee(appUserRepository.findByUsername((assigneeField.getValue())));
                ticketService.save(ticket);
                refreshGrid();
                dialog.close();
            }
        });

        Button cancelButton = new Button("Anuluj", e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, saveButton);
        dialog.getFooter().add(buttonLayout);

        dialog.add(formLayout);
        dialog.open();
    }

    private void refreshGrid() {
        grid.setItems(mapToTicketDtos(ticketService.findAll()));
    }
}
