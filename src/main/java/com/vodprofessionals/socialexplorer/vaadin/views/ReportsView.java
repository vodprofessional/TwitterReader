package com.vodprofessionals.socialexplorer.vaadin.views;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

/**
 *
 */
public class ReportsView extends VerticalLayout implements View {
    private TabSheet editors;

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        setSizeFull();
        addStyleName("reports");

        addComponent(buildDraftsView());
    }

    private Component buildDraftsView() {
        editors = new TabSheet();
        editors.setSizeFull();
        editors.addStyleName("borderless");
        editors.addStyleName("editors");

        final VerticalLayout center = new VerticalLayout();
        center.setSizeFull();
        center.setCaption("Tweets in past 6 days");
        editors.addComponent(center);

        return editors;
    }
}
