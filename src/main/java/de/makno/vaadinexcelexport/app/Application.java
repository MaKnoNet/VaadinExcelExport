package de.makno.vaadinexcelexport.app;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring-Boot-Einstiegspunkt der Vaadin-Demo-Anwendung.
 *
 * <p>{@link Push} aktiviert Server-Push (WebSocket): nötig, damit der Test im Hintergrund-Thread
 * Fortschritt und Ergebnisse an den Browser zurückspielen kann (siehe {@link MainView}).
 */
@Push
@SpringBootApplication
public class Application implements AppShellConfigurator {

    private static final long serialVersionUID = 1L;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
