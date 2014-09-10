package com.vodprofessionals.socialexplorer.vaadin.views;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vodprofessionals.socialexplorer.vaadin.components.GoogleCharts;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
        center.addComponent(buildChart());
        editors.addComponent(center);

        return editors;
    }

    private Component buildChart() {
        VerticalLayout content = new VerticalLayout();

        List<List<Object>> dataTable = new LinkedList<List<Object>>();
        List<Object> a = new LinkedList<Object>();

        a.add("ColA");
        a.add("ColB");
        dataTable.add(a);

        a = new LinkedList<Object>();
        a.add("Item1");
        a.add(13);
        dataTable.add(a);

        a = new LinkedList<Object>();
        a.add("Item2");
        a.add(8);
        dataTable.add(a);

        Map<GoogleCharts.Option, String> options = new HashMap<GoogleCharts.Option, String>();
        options.put(GoogleCharts.Option.TITLE, "The title of the chart");

        content.addComponent(new GoogleCharts(
                GoogleCharts.Type.BARCHART,
                dataTable,
                options
        ));

        return content;
    }
}
