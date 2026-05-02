package org.backendbrilliance.uiservice.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.theme.lumo.Lumo;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.backendbrilliance.common.enums.Tier;
import org.backendbrilliance.uiservice.entity.Endpoint;
import org.backendbrilliance.uiservice.entity.User;
import org.backendbrilliance.uiservice.exception.TierLimitException;
import org.backendbrilliance.uiservice.service.EndpointService;
import org.backendbrilliance.uiservice.service.WebhookRequestService;
import org.backendbrilliance.uiservice.service.security.AuthenticatedUser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@PermitAll
public class MainLayout extends AppLayout {

    private final EndpointService endpointService;
    private final WebhookRequestService requestService;
    private VerticalLayout endpointListContainer;
    private final AuthenticatedUser authenticatedUser;

    public MainLayout(EndpointService endpointService,
                      WebhookRequestService requestService,
                      AuthenticatedUser authenticatedUser) {
        this.endpointService = endpointService;
        this.requestService = requestService;
        this.authenticatedUser = authenticatedUser;
        setPrimarySection(Section.DRAWER);
        addToNavbar(buildNavbar());
        addToDrawer(buildDrawer());
    }

    private HorizontalLayout buildNavbar() {
        DrawerToggle toggle = new DrawerToggle();

        // Brand
        Span appName = new Span("HookSpy");
        appName.getStyle()
                .set("font-weight", "800").set("font-size", "17px")
                .set("color", "#111827").set("letter-spacing", "-0.4px");

        Span badge = new Span("BETA");
        badge.getStyle()
                .set("font-size", "9px").set("font-weight", "700")
                .set("background", "rgba(59,75,219,0.08)").set("color", "#3b4bdb")
                .set("padding", "2px 7px").set("border-radius", "4px")
                .set("letter-spacing", "0.6px")
                .set("border", "1px solid rgba(59,75,219,0.2)");

        HorizontalLayout brand = new HorizontalLayout(toggle, appName, badge);
        brand.setAlignItems(FlexComponent.Alignment.CENTER);
        brand.setSpacing(false);
        brand.getStyle().set("gap", "8px");

        // Refresh button
        Button refreshBtn = new Button(new Icon(VaadinIcon.REFRESH));
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        refreshBtn.getElement().setAttribute("title", "Refresh endpoints");
        refreshBtn.getStyle().set("color", "#9ca3af");
        refreshBtn.addClickListener(e -> refreshEndpointList());

        // Docs link
        Anchor docs = new Anchor("#", "Docs");
        docs.getStyle()
                .set("font-size", "13px").set("color", "#6b7280")
                .set("text-decoration", "none").set("font-weight", "500");

        // User avatar — shows first letter of email, click to logout
        String initial = authenticatedUser.get()
                .map(u -> u.getEmail().substring(0, 1).toUpperCase())
                .orElse("?");

        Div avatar = new Div();
        avatar.setText(initial);
        avatar.getStyle()
                .set("width", "30px").set("height", "30px")
                .set("border-radius", "50%")
                .set("background", "#3b4bdb").set("color", "#ffffff")
                .set("font-size", "12px").set("font-weight", "700")
                .set("display", "flex").set("align-items", "center")
                .set("justify-content", "center").set("cursor", "pointer")
                .set("user-select", "none").set("flex-shrink", "0");
        avatar.getElement().setAttribute("title", "Logout");
        avatar.addClickListener(e -> authenticatedUser.logout());

        HorizontalLayout right = new HorizontalLayout(refreshBtn, docs, avatar);
        right.setAlignItems(FlexComponent.Alignment.CENTER);
        right.getStyle().set("gap", "8px").set("margin-right", "4px");

        HorizontalLayout navbar = new HorizontalLayout(brand, right);
        navbar.setWidthFull();
        navbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        navbar.setAlignItems(FlexComponent.Alignment.CENTER);
        navbar.getStyle().set("padding", "0 16px");
        return navbar;
    }

    private VerticalLayout buildDrawer() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.getStyle().set("padding", "14px 16px 6px");

        Span title = new Span("Endpoints");
        title.getStyle()
                .set("font-size", "10px").set("font-weight", "700")
                .set("letter-spacing", "1px").set("text-transform", "uppercase")
                .set("color", "var(--lumo-tertiary-text-color, #9ca3af)");

        Button newBtn = new Button(new Icon(VaadinIcon.PLUS));
        newBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        newBtn.getStyle().set("border", "none")
                .set("color", "var(--lumo-tertiary-text-color, #9ca3af)")
                .set("padding", "2px");
        newBtn.getElement().setAttribute("title", "New endpoint");
        newBtn.addClickListener(e -> openCreateEndpointDialog());
        header.add(title, newBtn);

        endpointListContainer = new VerticalLayout();
        endpointListContainer.setPadding(false);
        endpointListContainer.setSpacing(false);
        endpointListContainer.getStyle().set("gap", "1px").set("padding", "4px 0");
        refreshEndpointList();

        Scroller scroller = new Scroller(endpointListContainer);
        scroller.setWidthFull();

        Hr sep = new Hr();
        sep.getStyle().set("margin", "0").set("border", "none")
                .set("border-top", "1px solid var(--hs-border, #f3f4f6)");

        Span userEmail = new Span(
                authenticatedUser.get().map(u -> u.getEmail()).orElse(""));
        userEmail.getStyle().set("font-size", "11px")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("white-space", "nowrap").set("overflow", "hidden")
                .set("text-overflow", "ellipsis");

        Span tierBadge = new Span(
                authenticatedUser.get().map(u -> u.getTier().name()).orElse("FREE"));
        tierBadge.getStyle()
                .set("font-size", "9px").set("font-weight", "700")
                .set("padding", "1px 6px").set("border-radius", "4px")
                .set("flex-shrink", "0")
                .set("background", "var(--hs-accent-light)")
                .set("color", "var(--hs-accent)")
                .set("border", "1px solid var(--hs-accent-light)");

        // Upgrade link for FREE users
        Anchor upgradeLink = new Anchor("/upgrade", "Upgrade ↗");
        upgradeLink.getStyle().set("font-size", "11px").set("color", "var(--hs-accent)")
                .set("font-weight", "600").set("text-decoration", "none");
        upgradeLink.setVisible(
                authenticatedUser.get()
                        .map(u -> u.getTier() == Tier.FREE)
                        .orElse(true));

        HorizontalLayout userRow = new HorizontalLayout(userEmail, tierBadge);
        userRow.setWidthFull();
        userRow.setAlignItems(FlexComponent.Alignment.CENTER);
        userRow.getStyle().set("padding", "8px 16px 4px").set("gap", "6px");
        userRow.setFlexGrow(1, userEmail);

        VerticalLayout footerArea = new VerticalLayout(userRow, upgradeLink);
        footerArea.setPadding(false);
        footerArea.setSpacing(false);
        footerArea.getStyle().set("padding", "0 16px 10px");

        VerticalLayout drawer = new VerticalLayout(header, scroller, sep, footerArea);
        drawer.setSizeFull();
        drawer.setPadding(false);
        drawer.setSpacing(false);
        return drawer;
    }

    public void refreshEndpointList() {
        endpointListContainer.removeAll();
        List<Endpoint> endpoints = authenticatedUser.get()
                .map(u -> endpointService.getEndpointsForUser(u.getId()))
                .orElseGet(endpointService::getAllEndpoints);

        if (endpoints.isEmpty()) {
            Span empty = new Span("No endpoints yet");
            empty.getStyle().set("font-size", "12px").set("color", "#9ca3af")
                    .set("padding", "20px 16px").set("display", "block")
                    .set("text-align", "center");
            endpointListContainer.add(empty);
            return;
        }
        for (Endpoint endpoint : endpoints) {
            endpointListContainer.add(buildEndpointItem(endpoint));
        }
    }

    private HorizontalLayout buildEndpointItem(Endpoint endpoint) {
        Div dot = new Div();
        dot.getStyle()
                .set("width", "6px").set("height", "6px").set("border-radius", "50%")
                .set("background", "#16a34a").set("flex-shrink", "0").set("margin-top", "1px");

        String displayName = endpoint.getLabel() != null
                ? endpoint.getLabel()
                : endpoint.getSlug().substring(0, 8) + "…";

        Span name = new Span(displayName);
        name.getStyle()
                .set("font-size", "13px").set("font-weight", "500")
                .set("white-space", "nowrap").set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("color", "var(--lumo-secondary-text-color, #374151)");

        long count = requestService.countRequests(endpoint.getId());
        Span countBadge = new Span(String.valueOf(count));
        countBadge.getStyle()
                .set("font-size", "11px").set("background", "var(--hs-border-light, #f3f4f6)")
                .set("color", "var(--lumo-secondary-text-color, #6b7280)")
                .set("padding", "1px 7px").set("border-radius", "10px")
                .set("flex-shrink", "0").set("font-weight", "500");

        // Delete button — visible on hover
        Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY,
                ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
        deleteBtn.getStyle().set("visibility", "hidden").set("padding", "2px")
                .set("flex-shrink", "0");
        deleteBtn.getElement().setAttribute("title", "Delete endpoint");
        deleteBtn.addClickListener(e -> confirmDelete(endpoint, displayName));

        HorizontalLayout item = new HorizontalLayout(dot, name, countBadge, deleteBtn);
        item.setWidthFull();
        item.setAlignItems(FlexComponent.Alignment.CENTER);
        item.getStyle()
                .set("padding", "7px 12px").set("margin", "0 6px")
                .set("width", "calc(100% - 12px)").set("border-radius", "6px")
                .set("cursor", "pointer").set("border-left", "2px solid transparent")
                .set("transition", "all 0.12s ease").set("gap", "8px");
        item.setFlexGrow(1, name);

        item.getElement().addEventListener("mouseover", e -> {
            item.getStyle().set("background", "var(--hs-border-light, #f9fafb)")
                    .set("border-left-color", "#3b4bdb");
            name.getStyle().set("color", "var(--lumo-body-text-color, #111827)");
            deleteBtn.getStyle().set("visibility", "visible");
        });
        item.getElement().addEventListener("mouseout", e -> {
            item.getStyle().remove("background").set("border-left-color", "transparent");
            name.getStyle().set("color", "var(--lumo-secondary-text-color, #374151)");
            deleteBtn.getStyle().set("visibility", "hidden");
        });

        item.addClickListener(e ->
                item.getUI().ifPresent(ui ->
                        ui.navigate(DashboardView.class,
                                new RouteParameters("slug", endpoint.getSlug()))));
        return item;
    }

    private void confirmDelete(Endpoint endpoint, String displayName) {
        Dialog confirm = new Dialog();
        confirm.setHeaderTitle("Delete endpoint?");

        Paragraph msg = new Paragraph(
                "All captured requests for \"" + displayName +
                        "\" will be permanently deleted. This cannot be undone.");
        msg.getStyle().set("color", "var(--lumo-secondary-text-color, #6b7280)")
                .set("font-size", "13px");
        confirm.add(msg);

        Button confirmBtn = new Button("Delete", e -> {
            endpointService.deleteEndpoint(endpoint.getId());
            confirm.close();
            refreshEndpointList();
            getUI().ifPresent(ui -> ui.navigate(""));
            Notification n = Notification.show("Endpoint deleted", 2000,
                    Notification.Position.BOTTOM_END);
            n.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        });
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelBtn = new Button("Cancel", e -> confirm.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        confirm.getFooter().add(cancelBtn, confirmBtn);
        confirm.open();
    }

    private void openCreateEndpointDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("New Endpoint");
        dialog.setWidth("420px");

        TextField labelField = new TextField("Label (optional)");
        labelField.setPlaceholder("e.g. Stripe Payments");
        labelField.setWidthFull();

        Paragraph hint = new Paragraph("A unique capture URL will be generated automatically.");
        hint.getStyle().set("font-size", "13px")
                .set("color", "var(--lumo-tertiary-text-color, #6b7280)").set("margin", "0");

        VerticalLayout content = new VerticalLayout(labelField, hint);
        content.setPadding(false);
        dialog.add(content);

        Button create = new Button("Create endpoint", e -> {
            Endpoint created;
            try {
                java.util.UUID userId = authenticatedUser.get()
                        .map(User::getId)
                        .orElse(null);
                created = endpointService.createEndpoint(labelField.getValue(), userId);
            } catch (TierLimitException ex) {
                Notification n2 = Notification.show(ex.getMessage(), 4000,
                        Notification.Position.MIDDLE);
                n2.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            dialog.close();
            refreshEndpointList();
            Notification n = Notification.show(
                    "Endpoint created — " + created.getSlug(), 3000,
                    Notification.Position.BOTTOM_END);
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            dialog.getUI().ifPresent(ui ->
                    ui.navigate(DashboardView.class,
                            new RouteParameters("slug", created.getSlug())));
        });
        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancel = new Button("Cancel", e -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(cancel, create);
        dialog.open();
    }
}