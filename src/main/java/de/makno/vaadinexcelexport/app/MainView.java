package de.makno.vaadinexcelexport.app;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Einstiegspunkt der Anwendung: zeigt beide Export-Varianten in einem {@link TabSheet} –
 * xlsbuilder (Tab 1) und Flowingcode Grid Exporter (Tab 2) – mit denselben Beispieldaten,
 * damit die Ergebnisse direkt verglichen werden können.
 *
 * <p><b>Thread-Sicherheit:</b> Klasse nicht thread-safe; eine Instanz pro UI-Request.
 */
@Route("")
@PageTitle("Excel-Export Demo")
public class MainView extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    private static final String TAB_XLSBUILDER = "xlsbuilder-Export";
    private static final String TAB_FLOWINGCODE = "Vaadin Grid Exporter";

    public MainView() {
        TabSheet tabs = new TabSheet();
        tabs.add(TAB_XLSBUILDER, new SampleDataView());
        tabs.add(TAB_FLOWINGCODE, new FlowingcodeExportView());
        tabs.setSizeFull();

        add(tabs);
        setSizeFull();
        setPadding(false);
    }
}
