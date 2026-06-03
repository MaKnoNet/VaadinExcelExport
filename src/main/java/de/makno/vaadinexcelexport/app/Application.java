package de.makno.vaadinexcelexport.app;

import com.vaadin.flow.component.page.AppShellConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Spring-Boot-Einstiegspunkt der Vaadin-Demo-Anwendung. */
@SpringBootApplication
public class Application implements AppShellConfigurator {

    private static final long serialVersionUID = 1L;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
