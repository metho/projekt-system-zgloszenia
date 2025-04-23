package com.puw.systemzgloszen.views;

import com.puw.systemzgloszen.entity.AppUser;
import com.puw.systemzgloszen.entity.Ticket;
import com.puw.systemzgloszen.entity.TicketState;
import com.puw.systemzgloszen.model.TicketDto;
import com.puw.systemzgloszen.repository.AppUserRepository;
import com.puw.systemzgloszen.service.SecurityService;
import com.puw.systemzgloszen.service.TicketService;
import com.puw.systemzgloszen.service.UserRoleUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@PermitAll
@Route("")
public class TicketView extends VerticalLayout {

    private static final DateTimeFormatter UI_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", new Locale("pl", "PL"));

    private final TicketService ticketService;
    private final AppUserRepository appUserRepository;
    private final SecurityService securityService;
    private final Grid<TicketDto> grid = new Grid<>(TicketDto.class);
    private final TextField keywordFilter     = new TextField();
    private final ComboBox<TicketState> stateFilter = new ComboBox<>();
    private final ComboBox<String> assigneeFilter  = new ComboBox<>();

    public TicketView(TicketService ticketService, AppUserRepository appUserRepository, SecurityService securityService) {
        this.ticketService = ticketService;
        this.appUserRepository = appUserRepository;
        this.securityService = securityService;

        UserDetails authenticatedUser = securityService.getAuthenticatedUser();
        boolean isElevatedRoleUser = UserRoleUtils.hasElevatedRole(authenticatedUser);
        createHeader(isElevatedRoleUser);

        //filtry
        keywordFilter.setPlaceholder("Tytuł, opis lub numer zgłoszenia");
        keywordFilter.setClearButtonVisible(true);
        keywordFilter.setWidth("270px");

        stateFilter.setPlaceholder("Status");
        stateFilter.setItems(TicketState.values());
        stateFilter.setItemLabelGenerator(TicketState::toPolish);
        stateFilter.setClearButtonVisible(true);

        assigneeFilter.setPlaceholder("Przypisana osoba");
        assigneeFilter.setItems(appUserRepository.findAll().stream().map(AppUser::getUsername).toList());
        assigneeFilter.setClearButtonVisible(true);

        HorizontalLayout filters = new HorizontalLayout(keywordFilter, stateFilter, assigneeFilter);
        filters.setWidthFull();
        filters.setDefaultVerticalComponentAlignment(Alignment.END);
        add(filters);

        setSizeFull();
        setSpacing(true);
        setPadding(true);

        grid.removeAllColumns();
        grid.addColumn(TicketDto::getId).setHeader("Numer zgłoszenia").setWidth("10%").setTextAlign(ColumnTextAlign.CENTER);
        grid.addColumn(TicketDto::getTitle).setHeader("Tytuł").setWidth("20%");
        grid.addColumn(TicketDto::getDescription).setHeader("Opis").setWidth("30%");
        grid.addColumn(ticket -> ticket.getState().toPolish()).setHeader("Status").setWidth("10%");
        grid.addColumn(TicketDto::getAssignee).setHeader("Przypisana osoba").setWidth("10%");
        grid.addColumn(ticket -> ticket.getCreationDate().format(UI_FORMAT)).setHeader("Data utworzenia").setWidth("12%");

        List<TicketDto> tickets = mapToTicketDtos(ticketService.findAll());
        if (!isElevatedRoleUser) {
            tickets = tickets.stream()
                    .filter(t -> t.getAssignee().equals(authenticatedUser.getUsername()))
                    .toList();
        }
        ListDataProvider<TicketDto> dataProvider = new ListDataProvider<>(tickets);
        grid.setDataProvider(dataProvider);
        dataProvider.addFilter(ticket -> {
            String kw = keywordFilter.getValue().trim().toLowerCase();
            boolean keywordOk = kw.isEmpty() ||
                    ticket.getTitle().toLowerCase().contains(kw) ||
                    ticket.getId().toString().contains(kw) ||
                    ticket.getDescription().toLowerCase().contains(kw);

            boolean stateOk = stateFilter.isEmpty() ||
                    ticket.getState() == stateFilter.getValue();

            boolean assigneeOk = assigneeFilter.isEmpty() ||
                    Objects.equals(ticket.getAssignee(), assigneeFilter.getValue());

            return keywordOk && stateOk && assigneeOk;
        });
        keywordFilter.addValueChangeListener(e -> dataProvider.refreshAll());
        stateFilter.addValueChangeListener(e -> dataProvider.refreshAll());
        assigneeFilter.addValueChangeListener(e -> dataProvider.refreshAll());

        grid.addComponentColumn(ticket -> new Button("Wyświetl", e ->
                UI.getCurrent().navigate("ticket/" + ticket.getId())
        )).setHeader("Akcje");

        add(grid);
    }

    private void createHeader(boolean isElevatedRoleUser) {
        Button logout = new Button("Wyloguj", click ->
                securityService.logout());

        Button openDialogButton = new Button("Nowe zgłoszenie", event -> openAddTicketDialog());
        openDialogButton.setEnabled(isElevatedRoleUser);
        openDialogButton.getStyle().set("margin-bottom", "1rem");
        HorizontalLayout header = new HorizontalLayout(logout);
        header.setWidthFull();
        header.add(openDialogButton, logout);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        add(header);
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
        ticketDto.setCreationDate(ticket.getCreationDate());
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
                ticket.setCreationDate(LocalDateTime.now());
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
