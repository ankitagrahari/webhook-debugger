package org.backendbrilliance.uiservice.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.shared.communication.PushMode;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.backendbrilliance.common.enums.Tier;
import org.backendbrilliance.uiservice.entity.Endpoint;
import org.backendbrilliance.uiservice.entity.WebhookRequest;
import org.backendbrilliance.uiservice.service.EndpointService;
import org.backendbrilliance.uiservice.service.ReplayService;
import org.backendbrilliance.uiservice.service.WebhookRequestService;
import org.backendbrilliance.uiservice.service.security.AuthenticatedUser;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Route(value = "dashboard/:slug", layout = MainLayout.class)
@PermitAll
@PageTitle("Dashboard — HookSpy")
public class DashboardView extends VerticalLayout implements BeforeEnterObserver {

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("MMM dd, HH:mm:ss");

    private final EndpointService endpointService;
    private final WebhookRequestService requestService;
    private final ReplayService replayService;
    private final AuthenticatedUser authenticatedUser;

    private Endpoint currentEndpoint;
    private Grid<WebhookRequest> requestGrid;
    private VerticalLayout detailPanel;
    private Span liveIndicator;
    private Tier currentTier = Tier.FREE;
    private Span requestCountBadge;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> pollingTask;
    private UI ui;

    public DashboardView(EndpointService endpointService,
                         WebhookRequestService requestService,
                         ReplayService replayService, AuthenticatedUser authenticatedUser) {
        this.endpointService = endpointService;
        this.requestService = requestService;
        this.replayService = replayService;
        this.authenticatedUser = authenticatedUser;
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background", "#f8f9fa");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String slug = event.getRouteParameters().get("slug").orElse("");
        currentEndpoint = endpointService.findBySlug(slug).orElse(null);
        removeAll();
        if (currentEndpoint == null) {
            add(buildEmptyState("Endpoint not found",
                    "The endpoint \"" + slug + "\" doesn't exist.", VaadinIcon.WARNING));
            return;
        }

        currentTier = authenticatedUser.get()
                    .map(user -> endpointService.getTierForUser(user.getId()))
                    .orElse(Tier.FREE);

        add(buildUrlBar());
        addAndExpand(buildMainContent());
    }

    private HorizontalLayout buildUrlBar() {
        String label = currentEndpoint.getLabel() != null
                ? currentEndpoint.getLabel()
                : currentEndpoint.getSlug().substring(0, 8) + "…";

        Span nameSpan = new Span(label);
        nameSpan.getStyle().set("font-weight", "600").set("font-size", "14px")
                .set("color", "#111827").set("white-space", "nowrap");

        String endpointUrl = "http://localhost:8080/h/" + currentEndpoint.getSlug();

        Span urlDisplay = new Span(endpointUrl);
        urlDisplay.getStyle()
                .set("font-family", "var(--hs-monospace, monospace)")
                .set("font-size", "12px").set("color", "#4b5563")
                .set("background", "#f3f4f6").set("border", "1px solid #e5e7eb")
                .set("border-radius", "6px").set("padding", "6px 12px")
                .set("white-space", "nowrap").set("overflow", "hidden")
                .set("text-overflow", "ellipsis").set("flex", "1");

        Button copyBtn = new Button(new Icon(VaadinIcon.COPY_O));
        copyBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        copyBtn.getElement().setAttribute("title", "Copy URL");
        copyBtn.addClickListener(e -> {
            copyBtn.getUI().ifPresent(u ->
                    u.getPage().executeJs("navigator.clipboard.writeText($0)", endpointUrl));
            Notification n = Notification.show("URL copied!", 2000, Notification.Position.BOTTOM_END);
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        liveIndicator = new Span("● LIVE");
        liveIndicator.addClassName("live-indicator");
        liveIndicator.getStyle().set("font-size", "10px").set("font-weight", "700")
                .set("letter-spacing", "0.8px").set("color", "#16a34a").set("white-space", "nowrap");

        // Tier badge
        Span tierBadge = new Span(currentTier.name());
        tierBadge.getStyle()
                .set("font-size", "10px").set("font-weight", "700")
                .set("padding", "2px 7px").set("border-radius", "4px")
                .set("letter-spacing", "0.5px")
                .set("background", currentTier == Tier.FREE ? "#f3f4f6" : "#ede9fe")
                .set("color", currentTier == Tier.FREE ? "#6b7280" : "#7c3aed")
                .set("border", "1px solid " + (currentTier == Tier.FREE ? "#e5e7eb" : "#ddd6fe"));

        HorizontalLayout bar = new HorizontalLayout(nameSpan, urlDisplay, copyBtn, tierBadge, liveIndicator);
        bar.setWidthFull();
        bar.setAlignItems(FlexComponent.Alignment.CENTER);
        bar.getStyle().set("padding", "10px 20px").set("background", "#ffffff")
                .set("border-bottom", "1px solid #e5e7eb").set("flex-shrink", "0").set("gap", "10px");
        bar.setFlexGrow(1, urlDisplay);
        return bar;
    }

    private SplitLayout buildMainContent() {
        SplitLayout split = new SplitLayout(buildRequestList(), buildDetailPanel());
        split.setOrientation(SplitLayout.Orientation.VERTICAL);
        split.setSplitterPosition(48);
        split.setSizeFull();
        return split;
    }

    private VerticalLayout buildRequestList() {
        Span title = new Span("Requests");
        title.getStyle().set("font-size", "11px").set("font-weight", "700")
                .set("text-transform", "uppercase").set("letter-spacing", "0.7px")
                .set("color", "#6b7280");

        long count = requestService.countRequests(currentEndpoint.getId());

        requestCountBadge = new Span(count + " captured");

        requestCountBadge.getStyle().set("font-size", "12px").set("color", "#9ca3af")
                .set("background", "#f3f4f6").set("padding", "2px 8px")
                .set("border-radius", "10px").set("font-weight", "500");

        HorizontalLayout toolbar = new HorizontalLayout(title, requestCountBadge);
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.getStyle().set("padding", "9px 16px").set("background", "#ffffff")
                .set("border-bottom", "1px solid #e5e7eb").set("flex-shrink", "0");
        toolbar.setFlexGrow(1, requestCountBadge);

        requestGrid = new Grid<>(WebhookRequest.class, false);
        requestGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
        requestGrid.setSizeFull();
        requestGrid.setSelectionMode(Grid.SelectionMode.SINGLE);

        requestGrid.addComponentColumn(req -> {
            Div dot = new Div();
            dot.getStyle()
                    .set("width", "8px").set("height", "8px").set("border-radius", "50%")
                    .set("background", "#16a34a")  // green = captured OK
                    .set("margin", "auto");
            return dot;
        }).setWidth("40px").setFlexGrow(0).setHeader("");

        requestGrid.addComponentColumn(req -> buildMethodBadge(req.getMethod()))
                .setHeader("Method").setWidth("100px").setFlexGrow(0);
        requestGrid.addColumn(req -> "/h/" + currentEndpoint.getSlug())
                .setHeader("Path").setFlexGrow(1);
        requestGrid.addColumn(WebhookRequest::getSourceIp)
                .setHeader("Source").setWidth("135px").setFlexGrow(0);
        requestGrid.addColumn(req -> req.getBodySize() != null ? formatBytes(req.getBodySize()) : "—")
                .setHeader("Size").setWidth("75px").setFlexGrow(0);
        requestGrid.addColumn(req -> req.getReceivedAt().format(TIME_FMT))
                .setHeader("Received").setWidth("165px").setFlexGrow(0);
        requestGrid.addSelectionListener(e ->
                e.getFirstSelectedItem().ifPresent(this::showDetail));
        refreshGrid();

        VerticalLayout container = new VerticalLayout(toolbar, requestGrid);
        container.setSizeFull();
        container.setPadding(false);
        container.setSpacing(false);
        container.getStyle().set("background", "#ffffff");
        return container;
    }

    private VerticalLayout buildDetailPanel() {
        detailPanel = new VerticalLayout();
        detailPanel.setSizeFull();
        detailPanel.setPadding(false);
        detailPanel.setSpacing(false);
        detailPanel.getStyle().set("background", "#ffffff")
                .set("border-top", "2px solid #e5e7eb");
        showEmptyDetail();
        return detailPanel;
    }

    private void showEmptyDetail() {
        detailPanel.removeAll();
        detailPanel.add(buildEmptyState("Select a request",
                "Click any row above to inspect headers, body, and query parameters.",
                VaadinIcon.HOURGLASS));
    }

    private void showDetail(WebhookRequest req) {
        detailPanel.removeAll();

        Span path = new Span("/h/" + currentEndpoint.getSlug());
        path.getStyle().set("font-family", "var(--hs-monospace, monospace)")
                .set("font-size", "12px").set("color", "#374151").set("font-weight", "500");

        Span meta = new Span(req.getReceivedAt().format(TIME_FMT) + "  ·  " + req.getSourceIp());
        meta.getStyle().set("font-size", "12px").set("color", "#9ca3af");

        Button replayBtn = new Button("Replay", new Icon(VaadinIcon.REFRESH));
        replayBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        replayBtn.addClickListener(e -> openReplayDialog(req));

        boolean canReplay = currentTier != Tier.FREE;
        replayBtn.setEnabled(canReplay);
        if (!canReplay) replayBtn.getElement().setAttribute("title",
                "Upgrade to PRO to replay requests");

        Button curlBtn = new Button("cURL", new Icon(VaadinIcon.CODE));
        curlBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        curlBtn.addClickListener(e -> {
            curlBtn.getUI().ifPresent(u ->
                    u.getPage().executeJs("navigator.clipboard.writeText($0)", buildCurlCommand(req)));
            Notification n = Notification.show("cURL copied!", 2000, Notification.Position.BOTTOM_END);
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        HorizontalLayout actions = new HorizontalLayout(replayBtn, curlBtn);
        actions.setSpacing(false);
        actions.getStyle().set("gap", "6px").set("flex-shrink", "0");

        HorizontalLayout detailHeader = new HorizontalLayout(
                buildMethodBadge(req.getMethod()), path, meta, actions);
        detailHeader.setWidthFull();
        detailHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        detailHeader.getStyle().set("padding", "10px 16px")
                .set("border-bottom", "1px solid #e5e7eb")
                .set("flex-shrink", "0").set("background", "#f9fafb").set("gap", "10px");
        detailHeader.setFlexGrow(1, meta);

        TabSheet tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.add("Body", buildBodyTab(req));
        tabs.add("Headers (" + (req.getHeaders() != null ? req.getHeaders().size() : 0) + ")",
                buildMapTab(req.getHeaders(), "No headers"));
        tabs.add("Query (" + (req.getQueryParams() != null ? req.getQueryParams().size() : 0) + ")",
                buildMapTab(req.getQueryParams(), "No query parameters"));

        detailPanel.add(detailHeader, tabs);
        detailPanel.setFlexGrow(1, tabs);
    }

    private void openReplayDialog(WebhookRequest req) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Replay Request");
        dialog.setWidth("520px");

        HorizontalLayout summary = new HorizontalLayout(
                buildMethodBadge(req.getMethod()), new Span("/h/" + currentEndpoint.getSlug()));
        summary.setAlignItems(FlexComponent.Alignment.CENTER);
        summary.getStyle().set("gap", "8px");

        TextField targetUrl = new TextField("Target URL");
        targetUrl.setPlaceholder("https://your-server.com/webhook");
        targetUrl.setWidthFull();
        targetUrl.setHelperText("Replays with the original method, headers, and body.");
        targetUrl.setValue("http://localhost:8080/h/" + currentEndpoint.getSlug());

        ProgressBar spinner = new ProgressBar();
        spinner.setIndeterminate(true);
        spinner.setVisible(false);

        VerticalLayout resultArea = new VerticalLayout();
        resultArea.setPadding(false);
        resultArea.setVisible(false);

        VerticalLayout content = new VerticalLayout(summary, targetUrl, spinner, resultArea);
        content.setPadding(false);
        dialog.add(content);

        Button sendBtn = new Button("Send", new Icon(VaadinIcon.PAPERPLANE));
        sendBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        sendBtn.addClickListener(e -> {
            String target = targetUrl.getValue().trim();
            if (target.isBlank()) {
                Notification.show("Enter a target URL", 2000, Notification.Position.MIDDLE);
                return;
            }
            sendBtn.setEnabled(false);
            spinner.setVisible(true);
            resultArea.setVisible(false);
            new Thread(() -> {
                ReplayService.ReplayResult result = replayService.replay(req, target);
                dialog.getUI().ifPresent(u -> u.access(() -> {
                    spinner.setVisible(false);
                    sendBtn.setEnabled(true);
                    resultArea.removeAll();
                    resultArea.add(buildReplayResultWidget(result));
                    resultArea.setVisible(true);
                }));
            }).start();
        });

        Button closeBtn = new Button("Close", e -> dialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dialog.getFooter().add(closeBtn, sendBtn);
        dialog.open();
    }

    private VerticalLayout buildReplayResultWidget(ReplayService.ReplayResult result) {
        VerticalLayout widget = new VerticalLayout();
        widget.setPadding(false);
        widget.setSpacing(false);
        widget.getStyle().set("border", "1px solid #e5e7eb").set("border-radius", "6px")
                .set("overflow", "hidden").set("margin-top", "4px");

        boolean ok = result.success() && result.statusCode() < 400;
        Span statusBar = new Span(result.statusLabel());
        statusBar.getStyle().set("display", "block").set("padding", "6px 12px")
                .set("font-size", "12px").set("font-weight", "600")
                .set("background", ok ? "#f0fdf4" : "#fef2f2")
                .set("color", ok ? "#16a34a" : "#dc2626")
                .set("border-bottom", "1px solid " + (ok ? "#dcfce7" : "#fee2e2"));

        String bodyText = result.success()
                ? (result.responseBody() != null && !result.responseBody().isBlank()
                ? result.responseBody() : "(empty response)")
                : result.errorMessage();

        Pre responseBody = new Pre(bodyText);
        responseBody.getStyle().set("margin", "0").set("padding", "10px 12px")
                .set("font-size", "12px").set("font-family", "var(--hs-monospace, monospace)")
                .set("max-height", "200px").set("overflow", "auto")
                .set("white-space", "pre-wrap").set("word-break", "break-all")
                .set("color", "#374151").set("background", "#f9fafb");

        widget.add(statusBar, responseBody);
        return widget;
    }

    private VerticalLayout buildBodyTab(WebhookRequest req) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);
        if (req.getBody() == null || req.getBody().isBlank()) {
            layout.add(buildEmptyState("Empty body", "This request had no body.", VaadinIcon.FILE_O));
            return layout;
        }
        String display = req.getBody();
        try {
            if (req.getContentType() != null && req.getContentType().contains("json")) {
                com.fasterxml.jackson.databind.ObjectMapper om =
                        new com.fasterxml.jackson.databind.ObjectMapper();
                display = om.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(om.readValue(display, Object.class));
            }
        } catch (Exception ignored) {}

        Pre pre = new Pre(display);
        pre.getStyle().set("background", "#f6f8fa").set("color", "#24292f")
                .set("font-family", "var(--hs-monospace, monospace)").set("font-size", "12.5px")
                .set("line-height", "1.65").set("padding", "16px").set("margin", "0")
                .set("overflow", "auto").set("white-space", "pre-wrap")
                .set("word-break", "break-all").set("height", "100%")
                .set("border-top", "none");

        layout.add(pre);
        layout.setFlexGrow(1, pre);
        return layout;
    }

    private VerticalLayout buildMapTab(Map<String, String> map, String emptyMsg) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSizeFull();
        if (map == null || map.isEmpty()) {
            layout.add(buildEmptyState(emptyMsg, "", VaadinIcon.INFO_CIRCLE_O));
            return layout;
        }
        Grid<Map.Entry<String, String>> grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_COMPACT,
                GridVariant.LUMO_NO_ROW_BORDERS);
        grid.setSizeFull();
        grid.addColumn(Map.Entry::getKey).setHeader("Name").setWidth("220px").setFlexGrow(0);
        grid.addColumn(Map.Entry::getValue).setHeader("Value").setFlexGrow(1);
        grid.setItems(map.entrySet());
        layout.add(grid);
        layout.setFlexGrow(1, grid);
        return layout;
    }

    private String buildCurlCommand(WebhookRequest req) {
        StringBuilder sb = new StringBuilder("curl -X ").append(req.getMethod());
        sb.append(" \\\n  'http://localhost:8080/h/").append(currentEndpoint.getSlug()).append("'");
        if (req.getHeaders() != null) {
            req.getHeaders().forEach((k, v) -> {
                String lower = k.toLowerCase();
                if (!lower.equals("host") && !lower.equals("content-length"))
                    sb.append(" \\\n  -H '").append(k).append(": ").append(v).append("'");
            });
        }
        if (req.getBody() != null && !req.getBody().isBlank())
            sb.append(" \\\n  -d '").append(req.getBody().replace("'", "'\"'\"'")).append("'");
        return sb.toString();
    }

    private Span buildMethodBadge(String method) {
        String[] colors = switch (method.toUpperCase()) {
            case "GET"    -> new String[]{"#dcfce7", "#16a34a", "#bbf7d0"};
            case "POST"   -> new String[]{"#dbeafe", "#2563eb", "#bfdbfe"};
            case "PUT"    -> new String[]{"#fef3c7", "#d97706", "#fde68a"};
            case "PATCH"  -> new String[]{"#ede9fe", "#7c3aed", "#ddd6fe"};
            case "DELETE" -> new String[]{"#fee2e2", "#dc2626", "#fecaca"};
            default       -> new String[]{"#f3f4f6", "#6b7280", "#e5e7eb"};
        };
        Span badge = new Span(method);
        badge.getStyle()
                .set("font-size", "10px").set("font-weight", "700")
                .set("padding", "2px 8px").set("border-radius", "4px")
                .set("letter-spacing", "0.4px").set("flex-shrink", "0")
                .set("font-family", "var(--hs-monospace, monospace)")
                .set("background", colors[0]).set("color", colors[1])
                .set("border", "1px solid " + colors[2]);
        return badge;
    }

    private VerticalLayout buildEmptyState(String title, String sub, VaadinIcon icon) {
        Icon ico = icon.create();
        ico.setSize("32px");
        ico.getStyle().set("color", "#d1d5db");
        Span t = new Span(title);
        t.getStyle().set("font-weight", "600").set("font-size", "14px").set("color", "#374151");
        Span s = new Span(sub);
        s.getStyle().set("font-size", "13px").set("color", "#9ca3af")
                .set("text-align", "center").set("max-width", "280px").set("line-height", "1.5");
        VerticalLayout empty = new VerticalLayout(ico, t, s);
        empty.setSizeFull();
        empty.setAlignItems(FlexComponent.Alignment.CENTER);
        empty.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        empty.getStyle().set("gap", "8px");

        // Add if we're on the dashboard and have an endpoint
        if (currentEndpoint != null) {
            Div curlBox = new Div();
            curlBox.getStyle()
                    .set("background", "var(--hs-code-bg, #f6f8fa)")
                    .set("border", "1px solid var(--hs-border, #e5e7eb)")
                    .set("border-radius", "6px").set("padding", "10px 14px")
                    .set("font-family", "var(--hs-monospace, monospace)")
                    .set("font-size", "12px").set("color", "var(--hs-code-text, #24292f)")
                    .set("margin-top", "12px").set("max-width", "420px")
                    .set("cursor", "pointer");

            String cmd = "curl -X POST http://localhost:8080/h/" +
                    currentEndpoint.getSlug() +
                    " -H \"Content-Type: application/json\" -d '{\"test\":true}'";
            curlBox.setText(cmd);
            curlBox.setTitle("Click to copy");
            curlBox.addClickListener(e ->
                    curlBox.getUI().ifPresent(ui ->
                            ui.getPage().executeJs(
                                    "navigator.clipboard.writeText($0)", cmd)));

            Span hint = new Span("Click the command above to copy and send your first request");
            hint.getStyle().set("font-size", "12px")
                    .set("color", "var(--lumo-tertiary-text-color, #9ca3af)")
                    .set("margin-top", "6px");

            empty.add(curlBox, hint);
        }

        return empty;
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1048576) return (bytes / 1024) + " KB";
        return (bytes / 1048576) + " MB";
    }

    private void refreshGrid() {
        if (currentEndpoint == null || requestGrid == null) return;
        requestGrid.setItems(requestService.getLatestRequests(currentEndpoint.getId(), currentTier));
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        this.ui = attachEvent.getUI();
        ui.getPushConfiguration().setPushMode(PushMode.AUTOMATIC);
        pollingTask = scheduler.scheduleAtFixedRate(() -> {
            if (currentEndpoint == null || requestGrid == null) return;
            try {
                List<WebhookRequest> fresh = requestService.getLatestRequests(currentEndpoint.getId(), currentTier);
                long totalCount = requestService.countRequests(currentEndpoint.getId());
                ui.access(() -> {
                    requestGrid.setItems(fresh);
                    if (requestCountBadge != null) {
                        requestCountBadge.setText(totalCount + " captured");
                    }
                });
            } catch (Exception e) {
                log.warn("Polling error: {}", e.getMessage());
            }
        }, 3, 3, TimeUnit.SECONDS);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (pollingTask != null) pollingTask.cancel(true);
    }
}