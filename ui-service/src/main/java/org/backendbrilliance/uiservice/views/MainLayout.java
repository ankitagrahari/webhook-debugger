package org.backendbrilliance.uiservice.views;

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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.RouteParameters;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.backendbrilliance.uiservice.entity.Endpoint;
import org.backendbrilliance.uiservice.exception.TierLimitException;
import org.backendbrilliance.uiservice.service.EndpointService;
import org.backendbrilliance.uiservice.service.WebhookRequestService;

import java.util.List;

@Slf4j
@PermitAll
public class MainLayout extends AppLayout {

    private final EndpointService endpointService;
    private final WebhookRequestService requestService;
    private VerticalLayout endpointListContainer;

    public MainLayout(EndpointService endpointService,
                      WebhookRequestService requestService) {
        this.endpointService = endpointService;
        this.requestService = requestService;
        setPrimarySection(Section.DRAWER);
        addToNavbar(buildNavbar());
        addToDrawer(buildDrawer());
    }

    private HorizontalLayout buildNavbar() {
        DrawerToggle toggle = new DrawerToggle();

        // Brand
        Span appName = new Span("HookSpy");
        appName.getStyle()
                .set("font-weight", "700").set("font-size", "17px")
                .set("color", "#111827").set("letter-spacing", "-0.3px");

        Span badge = new Span("BETA");
        badge.getStyle()
                .set("font-size", "9px").set("font-weight", "700")
                .set("background", "rgba(59,75,219,0.08)").set("color", "#3b4bdb")
                .set("padding", "2px 7px").set("border-radius", "4px")
                .set("letter-spacing", "0.6px").set("border", "1px solid rgba(59,75,219,0.2)");

        HorizontalLayout brand = new HorizontalLayout(toggle, appName, badge);
        brand.setAlignItems(FlexComponent.Alignment.CENTER);
        brand.setSpacing(false);
        brand.getStyle().set("gap", "8px");

        // Right
        Anchor docs = new Anchor("#", "Docs");
        docs.getStyle().set("font-size", "13px").set("color", "#6b7280")
                .set("text-decoration", "none").set("font-weight", "500");

        Div avatar = new Div();
        avatar.getStyle()
                .set("width", "30px").set("height", "30px").set("border-radius", "50%")
                .set("background", "#3b4bdb").set("color", "#fff")
                .set("font-size", "12px").set("font-weight", "700")
                .set("display", "flex").set("align-items", "center")
                .set("justify-content", "center").set("cursor", "pointer")
                .set("user-select", "none");
        avatar.setText("D");

        HorizontalLayout right = new HorizontalLayout(docs, avatar);
        right.setAlignItems(FlexComponent.Alignment.CENTER);
        right.getStyle().set("gap", "16px").set("margin-right", "4px");

        HorizontalLayout navbar = new HorizontalLayout(brand, right);
        navbar.setWidthFull();
        navbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        navbar.setAlignItems(FlexComponent.Alignment.CENTER);
        navbar.getStyle().set("padding", "0 16px");
        return navbar;
    }

    private VerticalLayout buildDrawer() {
        // Header
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.getStyle().set("padding", "14px 16px 6px");

        Span title = new Span("Endpoints");
        title.getStyle()
                .set("font-size", "10px").set("font-weight", "700")
                .set("letter-spacing", "1px").set("text-transform", "uppercase")
                .set("color", "#9ca3af");

        Button newBtn = new Button(new Icon(VaadinIcon.PLUS));
        newBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        newBtn.getStyle().set("border", "none").set("color", "#9ca3af").set("padding", "2px");
        newBtn.getElement().setAttribute("title", "New endpoint");
        newBtn.addClickListener(e -> openCreateEndpointDialog());
        header.add(title, newBtn);

        // List
        endpointListContainer = new VerticalLayout();
        endpointListContainer.setPadding(false);
        endpointListContainer.setSpacing(false);
        endpointListContainer.getStyle().set("gap", "1px").set("padding", "4px 0");
        refreshEndpointList();

        Scroller scroller = new Scroller(endpointListContainer);
        scroller.setWidthFull();

        // Footer
        Hr sep = new Hr();
        sep.getStyle().set("margin", "0").set("border", "none")
                .set("border-top", "1px solid #f3f4f6");

        Span footer = new Span("backendbrilliance.dev");
        footer.getStyle()
                .set("font-size", "11px").set("color", "#9ca3af")
                .set("padding", "10px 16px").set("display", "block");

        VerticalLayout drawer = new VerticalLayout(header, scroller, sep, footer);
        drawer.setSizeFull();
        drawer.setPadding(false);
        drawer.setSpacing(false);
        return drawer;
    }

    public void refreshEndpointList() {
        endpointListContainer.removeAll();
        List<Endpoint> endpoints = endpointService.getAllEndpoints();

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
        // Green dot
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
                .set("text-overflow", "ellipsis").set("color", "#374151");

        long count = requestService.countRequests(endpoint.getId());
        Span countBadge = new Span(String.valueOf(count));
        countBadge.getStyle()
                .set("font-size", "11px").set("background", "#f3f4f6")
                .set("color", "#6b7280").set("padding", "1px 7px")
                .set("border-radius", "10px").set("flex-shrink", "0")
                .set("font-weight", "500");

        HorizontalLayout item = new HorizontalLayout(dot, name, countBadge);
        item.setWidthFull();
        item.setAlignItems(FlexComponent.Alignment.CENTER);
        item.getStyle()
                .set("padding", "7px 12px").set("margin", "0 6px")
                .set("width", "calc(100% - 12px)").set("border-radius", "6px")
                .set("cursor", "pointer").set("border-left", "2px solid transparent")
                .set("transition", "all 0.12s ease").set("gap", "8px");
        item.setFlexGrow(1, name);

        item.getElement().addEventListener("mouseover", e ->
                item.getStyle().set("background", "#f9fafb").set("border-left-color", "#3b4bdb"));
        item.getElement().addEventListener("mouseout", e ->
                item.getStyle().remove("background").set("border-left-color", "transparent"));

        item.addClickListener(e ->
                item.getUI().ifPresent(ui ->
                        ui.navigate(DashboardView.class,
                                new RouteParameters("slug", endpoint.getSlug()))));
        return item;
    }

    private void openCreateEndpointDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("New Endpoint");
        dialog.setWidth("420px");

        TextField labelField = new TextField("Label (optional)");
        labelField.setPlaceholder("e.g. Stripe Payments");
        labelField.setWidthFull();

        Paragraph hint = new Paragraph("A unique capture URL will be generated automatically.");
        hint.getStyle().set("font-size", "13px").set("color", "#6b7280").set("margin", "0");

        VerticalLayout content = new VerticalLayout(labelField, hint);
        content.setPadding(false);
        dialog.add(content);

        Button create = new Button("Create endpoint", e -> {
            Endpoint created;
            try {
                created = endpointService.createEndpoint(labelField.getValue());
            } catch (TierLimitException ex) {
                Notification n2 = Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE);
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