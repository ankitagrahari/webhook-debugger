package org.backendbrilliance.uiservice.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.PermitAll;
import org.backendbrilliance.uiservice.service.EndpointService;

@PermitAll
@Route(value = "", layout = MainLayout.class)
@RouteAlias(value = "home", layout = MainLayout.class)
@PageTitle("HookSpy — Webhook Debugger")
public class HomeView extends VerticalLayout {

    public HomeView(EndpointService endpointService) {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle().set("background", "#f8f9fa").set("gap", "20px");

        // Small accent rule
        Div rule = new Div();
        rule.getStyle().set("width", "36px").set("height", "3px")
                .set("background", "#3b4bdb").set("border-radius", "2px");

        H2 headline = new H2("Debug webhooks in real time");
        headline.getStyle().set("font-size", "26px").set("font-weight", "700")
                .set("color", "#111827").set("margin", "0").set("text-align", "center")
                .set("letter-spacing", "-0.4px");

        Paragraph sub = new Paragraph(
                "Create a unique endpoint URL, point any service at it, " +
                        "and inspect every incoming request — headers, body, query params.");
        sub.getStyle().set("font-size", "14px").set("color", "#6b7280")
                .set("text-align", "center").set("max-width", "420px")
                .set("margin", "0").set("line-height", "1.65");

        Button createBtn = new Button("Create endpoint", VaadinIcon.PLUS.create());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        createBtn.getStyle().set("font-weight", "600").set("padding", "0 28px");
        createBtn.addClickListener(e -> {
            var endpoint = endpointService.createEndpoint(null);
            createBtn.getUI().ifPresent(ui ->
                    ui.navigate(DashboardView.class, new RouteParameters("slug", endpoint.getSlug())));
        });

        HorizontalLayout hints = new HorizontalLayout(
                buildHint(VaadinIcon.BOLT, "Real-time", "Requests appear in under 3 seconds"),
                buildHint(VaadinIcon.REFRESH, "Replay", "Resend any captured request"),
                buildHint(VaadinIcon.CODE, "cURL", "Copy requests as cURL commands")
        );
        hints.getStyle().set("gap", "14px").set("margin-top", "8px");
        hints.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        add(rule, headline, sub, createBtn, hints);
    }

    private VerticalLayout buildHint(VaadinIcon iconType, String title, String desc) {
        var icon = iconType.create();
        icon.setSize("18px");
        icon.getStyle().set("color", "#3b4bdb");

        Span t = new Span(title);
        t.getStyle().set("font-weight", "600").set("font-size", "13px").set("color", "#374151");

        Span d = new Span(desc);
        d.getStyle().set("font-size", "12px").set("color", "#9ca3af").set("text-align", "center");

        VerticalLayout card = new VerticalLayout(icon, t, d);
        card.setAlignItems(Alignment.CENTER);
        card.getStyle()
                .set("padding", "14px 18px").set("background", "#ffffff")
                .set("border", "1px solid #e5e7eb").set("border-radius", "8px")
                .set("width", "155px").set("gap", "4px")
                .set("box-shadow", "0 1px 2px rgba(0,0,0,0.05)");
        card.setSpacing(false);
        card.setPadding(false);
        return card;
    }
}