package com.vodprofessionals.socialexplorer.vaadin.views;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vodprofessionals.socialexplorer.model.Reports;
import com.vodprofessionals.socialexplorer.persistence.ContextAwareRDBMSDriver;
import com.vodprofessionals.socialexplorer.vaadin.components.GoogleCharts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class ReportsView extends VerticalLayout implements View {
    Logger logger = LoggerFactory.getLogger(TermsView.class);
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
        List<List<Object>> data = (new Reports(ContextAwareRDBMSDriver.driver())).numTweets();
        center.addComponent(buildChart(data));
        editors.addComponent(center);

        return editors;
    }

    private Component buildChart(List<List<Object>> dataTable) {
        VerticalLayout content = new VerticalLayout();

        Map<GoogleCharts.Option, String> options = new HashMap<GoogleCharts.Option, String>();
        options.put(GoogleCharts.Option.TITLE, "The title of the chart");

        content.addComponent(new GoogleCharts(
                GoogleCharts.Type.LINECHART,
                dataTable,
                options
        ));

        return content;
    }

    private List<List<Object>> getNumTweetsReportData() {
        List<List<Object>> dataTable = new LinkedList<List<Object>>();


        return dataTable;
    }
}
