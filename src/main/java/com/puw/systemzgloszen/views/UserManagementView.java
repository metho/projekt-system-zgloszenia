package com.puw.systemzgloszen.views;

import com.puw.systemzgloszen.entity.AppUser;
import com.puw.systemzgloszen.entity.UserRole;
import com.puw.systemzgloszen.repository.AppUserRepository;
import com.puw.systemzgloszen.service.SecurityService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.userdetails.UserDetails;

@Route("admin/users")
@RolesAllowed("ADMIN")
public class UserManagementView extends VerticalLayout {

    private final AppUserRepository userRepository;
    private final SecurityService securityService;

    private final Grid<AppUser> userGrid = new Grid<>(AppUser.class, false);
    private final Dialog userDialog = new Dialog();
    private final TextField usernameField = new TextField("Username");
    private final PasswordField passwordField = new PasswordField("Password");
    private final ComboBox<String> roleField = new ComboBox<>("Role");
    private final Button saveButton = new Button("Zapisz");
    private final Button closeButton = new Button("Zamknij");

    public UserManagementView(AppUserRepository userRepository, SecurityService securityService) {
        this.userRepository = userRepository;
        this.securityService = securityService;
        setSizeFull();
        setPadding(true);

        H2 header = new H2("User Management");
        Button addButton = new Button("Add User", e -> openUserDialog(null, false));

        configureGrid();
        configureDialog();

        add(header, addButton, userGrid);
        refreshGrid();
    }

    private void configureGrid() {
        userGrid.addColumn(AppUser::getId).setHeader("ID").setWidth("60px").setFlexGrow(0);
        userGrid.addColumn(AppUser::getUsername).setHeader("Username");
        userGrid.addColumn(user -> user.getRole().name()).setHeader("Role");

        UserDetails authenticatedUser = securityService.getAuthenticatedUser();
        userGrid.addComponentColumn(user -> {
            Button edit = new Button("Edytuj", e -> openUserDialog(user, true));
            Button delete = new Button("Usuń", e -> {
                userRepository.delete(user);
                refreshGrid();
            });
            if (authenticatedUser.getUsername().equals(user.getUsername())) {
                delete.setEnabled(false);
            }
            HorizontalLayout actions = new HorizontalLayout(edit, delete);
            return actions;
        }).setHeader("Actions");
    }

    private void configureDialog() {
        userDialog.setHeaderTitle("User Details");

        VerticalLayout form = new VerticalLayout(usernameField, passwordField, roleField);
        roleField.setItems("STANDARD", "TICKET_MANAGER", "ADMIN");

        closeButton.addClickListener(e -> {
            userDialog.close();
        });

        saveButton.addClickListener(e -> {
            AppUser appUser = userRepository.findByUsername(usernameField.getValue());
            if (appUser != null) {
                if (!passwordField.isEmpty()) {
                    appUser.setPassword("{noop}" + passwordField.getValue());
                }
                appUser.setRole(UserRole.valueOf(roleField.getValue()));
                userRepository.save(appUser);
            } else {
                AppUser newUser = new AppUser();
                newUser.setUsername(usernameField.getValue());
                if (!passwordField.isEmpty()) {
                    newUser.setPassword("{noop}" + passwordField.getValue());
                }
                newUser.setRole(UserRole.valueOf(roleField.getValue()));
                userRepository.save(newUser);
            }
            userDialog.close();
            refreshGrid();
        });

        userDialog.add(form, saveButton, closeButton);
    }

    private void openUserDialog(AppUser user, boolean isEdit) {
        if (isEdit) {
            usernameField.setValue(user.getUsername());
            usernameField.setReadOnly(true);
            passwordField.clear();
            roleField.setValue(user.getRole().name());
        } else {
            usernameField.clear();
            usernameField.setReadOnly(false);
            passwordField.clear();
            roleField.clear();
        }
        userDialog.open();
    }

    private void refreshGrid() {
        userGrid.setItems(userRepository.findAll());
    }
}