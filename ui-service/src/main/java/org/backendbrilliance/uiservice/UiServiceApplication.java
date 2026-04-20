package org.backendbrilliance.uiservice;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@StyleSheet(Lumo.UTILITY_STYLESHEET)
@Push
public class UiServiceApplication implements AppShellConfigurator {

    @Override
    public void configurePage(AppShellSettings settings) {
        // Explicitly set light mode — override any OS dark mode preference
        settings.addInlineWithContents(
                "document.documentElement.setAttribute('theme', 'light');",
                com.vaadin.flow.component.page.Inline.Wrapping.JAVASCRIPT
        );
        settings.setPageTitle("HookSpy");
    }

    public static void main(String[] args) {
        SpringApplication.run(UiServiceApplication.class, args);
    }

}
