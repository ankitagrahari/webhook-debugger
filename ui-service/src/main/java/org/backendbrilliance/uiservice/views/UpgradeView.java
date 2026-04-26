package org.backendbrilliance.uiservice.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.PermitAll;
import org.backendbrilliance.common.enums.Tier;
import org.backendbrilliance.uiservice.service.security.AuthenticatedUser;
import org.backendbrilliance.uiservice.service.RazorpayService;

@Route(value = "upgrade", layout = MainLayout.class)
@PermitAll
@PageTitle("Upgrade — HookSpy")
public class UpgradeView extends VerticalLayout {

    public UpgradeView(AuthenticatedUser authenticatedUser,
                       RazorpayService razorpayService) {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle().set("background", "#f8f9fa").set("gap", "24px");

        Span title = new Span("Upgrade your plan");
        title.getStyle().set("font-size", "22px").set("font-weight", "700")
                .set("color", "#111827").set("letter-spacing", "-0.3px");

        Span subtitle = new Span("More endpoints, longer history, replay and alerts.");
        subtitle.getStyle().set("font-size", "14px").set("color", "#6b7280");

        HorizontalLayout cards = new HorizontalLayout(
                buildPlanCard(Tier.PRO,  "Pro",  "₹299/month",
                        new String[]{"10 endpoints", "Unlimited requests",
                                "30 day history", "Replay requests",
                                "Slack & email alerts"},
                        authenticatedUser, razorpayService),
                buildPlanCard(Tier.TEAM, "Team", "₹799/month",
                        new String[]{"50 endpoints", "Unlimited requests",
                                "90 day history", "Shared workspace",
                                "Replay requests", "Alerts"},
                        authenticatedUser, razorpayService)
        );
        cards.setSpacing(false);
        cards.getStyle().set("gap", "16px").set("flex-wrap", "wrap")
                .set("justify-content", "center");

        add(title, subtitle, cards);
    }

    private VerticalLayout buildPlanCard(Tier tier, String name, String price,
                                         String[] features,
                                         AuthenticatedUser authenticatedUser,
                                         RazorpayService razorpayService) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(false);
        card.setSpacing(false);
        card.setWidth("280px");
        card.getStyle()
                .set("background", "#ffffff").set("border", "1px solid #e5e7eb")
                .set("border-radius", "12px")
                .set("box-shadow", "0 1px 3px rgba(0,0,0,0.08)")
                .set("overflow", "hidden");

        // Header
        Div header = new Div();
        header.getStyle()
                .set("padding", "20px 20px 16px")
                .set("border-bottom", "1px solid #f3f4f6");

        Span nameSpan = new Span(name);
        nameSpan.getStyle().set("font-size", "18px").set("font-weight", "700")
                .set("color", "#111827").set("display", "block");

        Span priceSpan = new Span(price);
        priceSpan.getStyle().set("font-size", "24px").set("font-weight", "800")
                .set("color", "#3b4bdb").set("display", "block").set("margin-top", "4px");

        header.add(nameSpan, priceSpan);

        // Features
        VerticalLayout featureList = new VerticalLayout();
        featureList.setPadding(false);
        featureList.setSpacing(false);
        featureList.getStyle().set("gap", "8px").set("padding", "16px 20px");

        for (String f : features) {
            Div row = new Div();
            row.getStyle().set("display", "flex").set("align-items", "center").set("gap", "8px");
            Span check = new Span("✓");
            check.getStyle().set("color", "#16a34a").set("font-weight", "700")
                    .set("font-size", "13px");
            Span text = new Span(f);
            text.getStyle().set("font-size", "13px").set("color", "#374151");
            row.add(check, text);
            featureList.add(row);
        }

        // CTA button
        Div footer = new Div();
        footer.getStyle().set("padding", "0 20px 20px");

        Button upgradeBtn = new Button("Upgrade to " + name);
        upgradeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        upgradeBtn.setWidthFull();
        upgradeBtn.addClickListener(e -> {
            authenticatedUser.get().ifPresent(user -> {
                RazorpayService.RazorpayOrderConfig config =
                        razorpayService.createOrder(tier, user);
                launchRazorpay(config, tier, user.getId().toString(), razorpayService);
            });
        });

        footer.add(upgradeBtn);
        card.add(header, featureList, footer);
        return card;
    }

    /**
     * Opens Razorpay checkout via JS.
     * On success, calls server to confirm upgrade.
     */
    private void launchRazorpay(RazorpayService.RazorpayOrderConfig config,
                                Tier tier, String userId,
                                RazorpayService razorpayService) {
        String js = """
            var options = {
                key: '%s',
                amount: %d,
                currency: '%s',
                name: 'HookSpy',
                description: '%s',
                prefill: { email: '%s' },
                theme: { color: '#3b4bdb' },
                handler: function(response) {
                    // Notify server of successful payment
                    fetch('/api/payment/confirm', {
                        method: 'POST',
                        headers: {'Content-Type': 'application/json'},
                        body: JSON.stringify({
                            paymentId: response.razorpay_payment_id,
                            tier: '%s',
                            userId: '%s'
                        })
                    }).then(() => window.location.href = '/');
                }
            };
            var rzp = new Razorpay(options);
            rzp.open();
            """.formatted(
                config.keyId(), config.amountPaise(), config.currency(),
                config.description(), config.email(),
                tier.name(), userId);

        // Load Razorpay script then open checkout
        UI.getCurrent().getPage().executeJs(
                "var s = document.createElement('script');" +
                        "s.src = 'https://checkout.razorpay.com/v1/checkout.js';" +
                        "s.onload = function() { " + js + " };" +
                        "document.head.appendChild(s);"
        );
    }
}