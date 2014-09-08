package com.vodprofessionals.socialexplorer.vaadin.views;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 *
 */
public class DashboardView extends VerticalLayout implements View {

    public DashboardView() {
        setSizeFull();
        addStyleName("dashboard-view");

        HorizontalLayout top = new HorizontalLayout();
        top.setWidth("100%");
        top.setSpacing(true);
        top.addStyleName("toolbar");
        addComponent(top);
        final Label title = new Label("My Dashboard");
        title.setSizeUndefined();
        title.addStyleName("h1");
        top.addComponent(title);
        top.setComponentAlignment(title, Alignment.MIDDLE_LEFT);
        top.setExpandRatio(title, 1);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

    }
}
