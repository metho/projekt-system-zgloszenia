package com.puw.systemzgloszen.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route("admin/panel")
@RolesAllowed("ADMIN")
public class AdminPanelView extends VerticalLayout {
    public AdminPanelView() {
        add(new H2("Admin Panel"));
    }
}
