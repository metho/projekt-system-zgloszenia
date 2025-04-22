package com.puw.systemzgloszen.views;

import com.puw.systemzgloszen.entity.AppUser;
import com.puw.systemzgloszen.entity.Ticket;
import com.puw.systemzgloszen.entity.TicketState;
import com.puw.systemzgloszen.entity.UserComment;
import com.puw.systemzgloszen.repository.AppUserRepository;
import com.puw.systemzgloszen.repository.UserCommentRepository;
import com.puw.systemzgloszen.service.TicketService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDateTime;
import java.util.List;

@Route("ticket")
@PermitAll
public class TicketDetailsView extends VerticalLayout implements HasUrlParameter<String> {

    private final TicketService ticketService;
    private final AppUserRepository appUserRepository;
    private final UserCommentRepository userCommentRepository;
    private Ticket ticket;

    private final VerticalLayout commentsLayout = new VerticalLayout();
    private final TextArea commentInput = new TextArea("Dodaj komentarz");
    private final Button addCommentButton = new Button("Dodaj");
    private final H2 title = new H2();
    private final TextArea descriptionField = new TextArea("Opis");
    private final ComboBox<TicketState> stateField = new ComboBox<>("Status");
    private final ComboBox<String> assigneeField = new ComboBox<>("Przypisana osoba");

    private final Button saveButton = new Button("Zapisz zmiany");

    public TicketDetailsView(TicketService ticketService, AppUserRepository appUserRepository, UserCommentRepository userCommentRepository) {
        this.ticketService = ticketService;
        this.userCommentRepository = userCommentRepository;

        setWidthFull();
        setAlignItems(Alignment.CENTER);
        getStyle().set("padding", "2rem");

        Div card = new Div();
        card.getStyle()
                .set("maxWidth", "600px")
                .set("width", "100%")
                .set("padding", "2rem")
                .set("boxShadow", "0 4px 12px rgba(0,0,0,0.1)")
                .set("borderRadius", "12px")
                .set("backgroundColor", "#fff");

        title.getStyle().set("margin-bottom", "1.5rem");

        descriptionField.setWidthFull();
        descriptionField.setHeight("120px");

        stateField.setItems(TicketState.values());
        stateField.setWidthFull();

        List<AppUser> users = appUserRepository.findAll();
        assigneeField.setItems(users.stream().map(AppUser::getUsername).toList());
        assigneeField.setWidthFull();

        saveButton.addClickListener(e -> saveChanges());

        addCommentButton.addClickListener(e -> addComment());

        commentInput.setWidthFull();
        commentInput.setHeight("100px");
        addCommentButton.getStyle().set("margin-top", "0.5rem");
        commentsLayout.setWidthFull();
        commentsLayout.setSpacing(true);

        VerticalLayout commentSection = new VerticalLayout(
                new H2("Komentarze"),
                commentInput,
                addCommentButton,
                commentsLayout
        );
        commentSection.setWidthFull();
        commentSection.setPadding(false);
        commentSection.getStyle().set("margin-top", "2rem");

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1)
        );
        formLayout.add(descriptionField, stateField, assigneeField);

        VerticalLayout layout = new VerticalLayout(title, formLayout, saveButton);
        layout.setSpacing(true);
        layout.setAlignItems(Alignment.STRETCH);

        card.add(layout);
        add(card);
        add(card, commentSection);
        this.appUserRepository = appUserRepository;
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        if (parameter != null) {
            try {
                int id = Integer.parseInt(parameter);
                ticket = ticketService.findById(id);

                if (ticket != null) {
                    showTicketDetails();
                } else {
                    add("Zgloszenie nie znalezione.");
                }
            } catch (NumberFormatException e) {
                add("Bledne ID zgloszenia.");
            }
        }
    }

    private void addComment() {
        String text = commentInput.getValue().trim();
        if (!text.isEmpty()) {
            AppUser author = appUserRepository.findByUsername("jkowalski");
            UserComment comment = new UserComment();
            comment.setComment(text);
            comment.setUser(author);
            comment.setCommentDate(LocalDateTime.now());
            comment.setTicket(ticket);
            userCommentRepository.save(comment);
            commentInput.clear();
            loadComments();
        }
    }

    private void loadComments() {
        commentsLayout.removeAll();
        List<UserComment> comments = userCommentRepository.findByTicket(ticket);

        for (UserComment comment : comments) {
            Span author = new Span(comment.getUser().getUsername());
            author.getStyle().set("font-weight", "bold");

            Span timestamp = new Span(comment.getCommentDate().toString());
            timestamp.getStyle().set("color", "#888").set("font-size", "12px");

            Paragraph text = new Paragraph(comment.getComment());

            VerticalLayout commentBox = new VerticalLayout(
                    new HorizontalLayout(author, timestamp),
                    text
            );
            commentBox.getStyle()
                    .set("padding", "1rem")
                    .set("border", "1px solid #ddd")
                    .set("border-radius", "8px")
                    .set("background-color", "#f9f9f9");

            commentsLayout.add(commentBox);
        }
    }

    private void showTicketDetails() {
        title.setText("Ticket #" + ticket.getId() + ": " + ticket.getTitle());
        descriptionField.setValue(ticket.getDescription());
        stateField.setValue(ticket.getState());
        assigneeField.setValue(ticket.getAssignee().getUsername());
        loadComments();
    }

    private void saveChanges() {
        ticket.setDescription(descriptionField.getValue());
        ticket.setState(stateField.getValue());
        AppUser appUser = appUserRepository.findByUsername(ticket.getAssignee().getUsername());
        ticket.setAssignee(appUser);

        ticketService.save(ticket);
        saveButton.setText("Zapisano");
    }
}
