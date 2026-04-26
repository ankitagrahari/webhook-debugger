package org.backendbrilliance.uiservice.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.backendbrilliance.common.enums.Tier;
import org.backendbrilliance.uiservice.exception.EmailAlreadyExistsException;
import org.backendbrilliance.uiservice.service.UserService;

import java.util.LinkedHashMap;
import java.util.Map;

@Route("register")
@PageTitle("Create account — HookSpy")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    private final UserService userService;
    private Tier selectedTier = Tier.FREE;

    private final Map<Tier, Div> tierRows = new LinkedHashMap<>();
    private final Map<Tier, Div> radioOuters = new LinkedHashMap<>();
    private final VerticalLayout featureList = new VerticalLayout();

    private static final Map<Tier, String[]> FEATURES = new LinkedHashMap<>() {{
        put(Tier.FREE, new String[]{
                "1 endpoint", "100 requests/day", "1 day history", "cURL export"
        });
        put(Tier.PRO, new String[]{
                "10 endpoints", "Unlimited requests", "30 day history",
                "Replay any request", "cURL export", "Slack & email alerts"
        });
        put(Tier.TEAM, new String[]{
                "50 endpoints", "Unlimited requests", "90 day history",
                "Shared workspace", "Replay", "cURL export", "Alerts"
        });
    }};

    public RegisterView(UserService userService) {
        this.userService = userService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle().set("background", "#f8f9fa").set("padding", "24px 0");

        // Logo
        Div rule = new Div();
        rule.getStyle().set("width", "28px").set("height", "3px")
                .set("background", "#3b4bdb").set("border-radius", "2px")
                .set("margin", "0 auto 14px");

        Span logo = new Span("HookSpy");
        logo.getStyle().set("font-size", "22px").set("font-weight", "700")
                .set("color", "#111827").set("letter-spacing", "-0.4px");

        Span tagline = new Span("Start debugging webhooks");
        tagline.getStyle().set("font-size", "13px").set("color", "#9ca3af");

        VerticalLayout logoArea = new VerticalLayout(rule, logo, tagline);
        logoArea.setAlignItems(Alignment.CENTER);
        logoArea.setSpacing(false);
        logoArea.setPadding(false);
        logoArea.getStyle().set("gap", "4px").set("margin-bottom", "16px");

        Span planLabel = new Span("Pick a plan");
        planLabel.getStyle().set("font-size", "13px").set("font-weight", "600")
                .set("color", "#111827");

        // ── Plans in one horizontal row ──────────────────────────
        HorizontalLayout tierRow = new HorizontalLayout();
        tierRow.setWidthFull();
        tierRow.setSpacing(false);
        tierRow.getStyle().set("gap", "8px");

        addTierCard(tierRow, Tier.FREE,  "Free",  "₹0/mo",   true);
        addTierCard(tierRow, Tier.PRO,   "Pro",   "₹299/mo", false);
        addTierCard(tierRow, Tier.TEAM,  "Team",  "₹799/mo", false);

        // ── Features spanning full width below ───────────────────
        Span featuresLabel = new Span("Features include:");
        featuresLabel.getStyle().set("font-size", "12px").set("font-weight", "600")
                .set("color", "#374151").set("text-transform", "uppercase")
                .set("letter-spacing", "0.5px");

        featureList.setPadding(false);
        featureList.setSpacing(false);
        featureList.setWidthFull();
        featureList.getStyle().set("gap", "5px");
        renderFeatures(Tier.FREE);

        Div featuresBox = new Div();
        featuresBox.setWidthFull();
        featuresBox.getStyle()
                .set("background", "#f9fafb")
                .set("border", "1px solid #e5e7eb")
                .set("border-radius", "8px")
                .set("padding", "12px 14px")
                .set("display", "flex").set("flex-direction", "column").set("gap", "8px");
        featuresBox.add(featuresLabel, featureList);

        Hr divider = new Hr();
        divider.getStyle().set("margin", "2px 0").set("border", "none")
                .set("border-top", "1px solid #e5e7eb");

        // Fields
        EmailField emailField = new EmailField("Email");
        emailField.setPlaceholder("you@example.com");
        emailField.setWidthFull();

        PasswordField passwordField = new PasswordField("Password");
        passwordField.setPlaceholder("Min. 8 characters");
        passwordField.setWidthFull();

        PasswordField confirmField = new PasswordField("Confirm password");
        confirmField.setPlaceholder("Repeat your password");
        confirmField.setWidthFull();

        Button registerBtn = new Button("Create account");
        registerBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerBtn.setWidthFull();
        registerBtn.addClickListener(e ->
                handleRegister(emailField, passwordField, confirmField));

        Div loginLinkDiv = new Div();
        loginLinkDiv.getStyle().set("font-size", "13px").set("color", "#6b7280")
                .set("text-align", "center");
        Anchor login = new Anchor("/login", "Sign in");
        login.getStyle().set("color", "#3b4bdb").set("font-weight", "500");
        loginLinkDiv.add(new Paragraph("Already have an account? "), login);

        VerticalLayout form = new VerticalLayout(
                planLabel, tierRow, featuresBox,
                divider,
                emailField, passwordField, confirmField,
                registerBtn, loginLinkDiv);
        form.setPadding(false);
        form.getStyle().set("gap", "10px");

        VerticalLayout card = new VerticalLayout(logoArea, form);
        card.setAlignItems(Alignment.STRETCH);
        card.setWidth("440px");
        card.getStyle()
                .set("background", "#ffffff").set("border", "1px solid #e5e7eb")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 6px rgba(0,0,0,0.05)")
                .set("padding", "28px 24px");
        card.setPadding(false);
        card.setSpacing(false);

        Span footer = new Span("backendbrilliance.dev");
        footer.getStyle().set("font-size", "12px").set("color", "#9ca3af")
                .set("margin-top", "20px");

        add(card, footer);
    }

    private void addTierCard(HorizontalLayout parent, Tier tier,
                             String name, String price, boolean selected) {
        Div card = new Div();
        applyCardStyle(card, selected);
        card.getStyle().set("flex", "1");

        Span nameSpan = new Span(name);
        nameSpan.getStyle().set("font-size", "13px").set("font-weight", "700")
                .set("color", "#111827").set("display", "block");

        Span priceSpan = new Span(price);
        priceSpan.getStyle().set("font-size", "12px").set("color", "#6b7280")
                .set("display", "block").set("margin-top", "2px");

        // Radio circle
        Div radio = new Div();
        applyRadioStyle(radio, selected);
        radioOuters.put(tier, radio);
        if (selected) radio.add(buildRadioDot());

        // Top row: name left, radio right
        Div topRow = new Div();
        topRow.getStyle().set("display", "flex").set("align-items", "flex-start")
                .set("justify-content", "space-between");
        topRow.add(nameSpan, radio);

        card.add(topRow, priceSpan);
        tierRows.put(tier, card);

        card.addClickListener(e -> selectTier(tier));
        parent.add(card);
    }

    private void selectTier(Tier tier) {
        selectedTier = tier;
        tierRows.forEach((t, row) -> applyCardStyle(row, t == tier));
        radioOuters.forEach((t, radio) -> {
            applyRadioStyle(radio, t == tier);
            radio.removeAll();
            if (t == tier) radio.add(buildRadioDot());
        });
        renderFeatures(tier);
    }

    private void applyCardStyle(Div card, boolean selected) {
        card.getStyle()
                .set("padding", "10px 12px")
                .set("border", "1.5px solid " + (selected ? "#3b4bdb" : "#e5e7eb"))
                .set("border-radius", "8px")
                .set("cursor", "pointer")
                .set("background", selected ? "rgba(59,75,219,0.04)" : "#ffffff")
                .set("transition", "all 0.15s ease");
    }

    private void applyRadioStyle(Div radio, boolean selected) {
        radio.getStyle()
                .set("width", "16px").set("height", "16px")
                .set("border-radius", "50%")
                .set("border", "2px solid " + (selected ? "#3b4bdb" : "#d1d5db"))
                .set("display", "flex").set("align-items", "center")
                .set("justify-content", "center").set("flex-shrink", "0")
                .set("margin-top", "2px");
    }

    private Div buildRadioDot() {
        Div dot = new Div();
        dot.getStyle().set("width", "7px").set("height", "7px")
                .set("border-radius", "50%").set("background", "#3b4bdb");
        return dot;
    }

    private void renderFeatures(Tier tier) {
        featureList.removeAll();
        // Two-column layout for features
        HorizontalLayout cols = new HorizontalLayout();
        cols.setWidthFull();
        cols.setSpacing(false);
        cols.getStyle().set("gap", "8px").set("flex-wrap", "wrap");

        String[] features = FEATURES.getOrDefault(tier, new String[]{});
        for (String feature : features) {
            Div item = new Div();
            item.getStyle().set("display", "flex").set("align-items", "center")
                    .set("gap", "6px").set("width", "calc(50% - 4px)");

            Span check = new Span("✓");
            check.getStyle().set("color", "#16a34a").set("font-weight", "700")
                    .set("font-size", "12px").set("flex-shrink", "0");

            Span text = new Span(feature);
            text.getStyle().set("font-size", "12px").set("color", "#4b5563");

            item.add(check, text);
            cols.add(item);
        }
        featureList.add(cols);
    }

    private void handleRegister(EmailField emailField,
                                PasswordField passwordField,
                                PasswordField confirmField) {
        String email = emailField.getValue().trim();
        String password = passwordField.getValue();
        String confirm = confirmField.getValue();

        if (email.isBlank()) { showError("Email is required."); return; }
        if (password.length() < 8) { showError("Password must be at least 8 characters."); return; }
        if (!password.equals(confirm)) { showError("Passwords do not match."); return; }

        try {
            userService.register(email, password, selectedTier);
            getUI().ifPresent(ui -> ui.navigate("login?registered=true"));
        } catch (EmailAlreadyExistsException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Something went wrong. Please try again.");
        }
    }

    private void showError(String message) {
        Notification n = Notification.show(message, 4000, Notification.Position.MIDDLE);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}