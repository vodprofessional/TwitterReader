package com.vodprofessionals.socialexplorer.vaadin.components;

import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;

import java.util.*;

/**
 *
 */
@JavaScript({"https://www.google.com/jsapi", "GoogleChartsConnector.js"})
public class GoogleCharts extends AbstractJavaScriptComponent {

    /**
     *
     * @param chartType
     */
    public GoogleCharts(Type chartType,
                        List<List<Object>> dataTable,
                        Map<Option, String> chartOptions) {
        GoogleChartsState state = getState();

        state.jsChartClass = chartType.getJsChartClass();

        state.dataTable = dataTable;

        Map<String, String> jsOptions = new HashMap<String, String>();
        for (Map.Entry<Option, String> entry : chartOptions.entrySet()) {
            jsOptions.put(entry.getKey().getJsChartOption(), entry.getValue());
        }
        state.options = jsOptions;
    }

    /**
     *
     * @return
     */
    @Override
    public GoogleChartsState getState() {
        return (GoogleChartsState) super.getState();
    }


    public enum Type {
        BARCHART("google.visualization.BarChart"),
        PIECHART("google.visualization.PieChart");


        private final String jsChartClass;

        Type(String jsChartClass) {
            this.jsChartClass = jsChartClass;
        }

        /**
         * Get the Google Charts JS class for the given chart type
         *
         * @return The Google Charts JS class to use
         */
        public String getJsChartClass() {
            return this.jsChartClass;
        }
    }


    /**
     *
     */
    public enum Option {
        TITLE("title"),
        WIDTH("width"),
        HEIGHT("height");

        private final String optionName;

        Option(String optionName) {
            this.optionName = optionName;
        }

        /**
         * Get the Google Charts JS chart option
         *
         * @return the chart option string name in JS
         */
        public String getJsChartOption() {
            return this.optionName;
        }
    }
}
