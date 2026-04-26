package org.backendbrilliance.uiservice.views;

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Sign in — HookSpy")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm();

    public LoginView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle().set("background", "#f8f9fa");

        // Logo
        Div accentBar = new Div();
        accentBar.getStyle().set("width", "28px").set("height", "3px")
                .set("background", "#3b4bdb").set("border-radius", "2px")
                .set("margin", "0 auto 14px");

        Span logo = new Span("HookSpy");
        logo.getStyle().set("font-size", "22px").set("font-weight", "700")
                .set("color", "#111827").set("letter-spacing", "-0.4px");

        Span tagline = new Span("Webhook debugger for developers");
        tagline.getStyle().set("font-size", "13px").set("color", "#9ca3af");

        VerticalLayout logoArea = new VerticalLayout(accentBar, logo, tagline);
        logoArea.setAlignItems(Alignment.CENTER);
        logoArea.setSpacing(false);
        logoArea.setPadding(false);
        logoArea.getStyle().set("gap", "4px").set("margin-bottom", "16px");

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.getForm().setTitle("");
        i18n.getForm().setUsername("Email");
        i18n.getForm().setPassword("Password");
        i18n.getForm().setSubmit("Sign in");
        i18n.getErrorMessage().setTitle("Incorrect credentials");
        i18n.getErrorMessage().setMessage("Check your email and password.");
        loginForm.setI18n(i18n);
        loginForm.setAction("login");
        loginForm.setForgotPasswordButtonVisible(false);

        Span registerLink = new Span();
        Anchor register = new Anchor("/register", "Create a free account");
        register.getStyle().set("color", "#3b4bdb").set("font-weight", "500");
        registerLink.getStyle().set("font-size", "13px").set("color", "#6b7280")
                .set("text-align", "center");
        registerLink.add(new Paragraph("No account? "), register);

        VerticalLayout card = new VerticalLayout(logoArea, loginForm, registerLink);
        card.setAlignItems(Alignment.CENTER);
        card.setWidth("360px");
        card.getStyle()
                .set("background", "#ffffff").set("border", "1px solid #e5e7eb")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 6px rgba(0,0,0,0.05), 0 1px 3px rgba(0,0,0,0.08)")
                .set("padding", "28px 24px");
        card.setPadding(false);
        card.setSpacing(false);

        Span footer = new Span("backendbrilliance.dev");
        footer.getStyle().set("font-size", "12px").set("color", "#9ca3af").set("margin-top", "20px");

        add(card, footer);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var params = event.getLocation().getQueryParameters().getParameters();

        if (params.containsKey("error")) {
            loginForm.setError(true);
        }

        if (params.containsKey("registered")) {
            Notification n = Notification.show(
                    "Account created! Sign in to get started.",
                    4000, Notification.Position.TOP_CENTER);
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
    }
}