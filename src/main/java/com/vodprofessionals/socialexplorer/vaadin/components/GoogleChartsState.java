package com.vodprofessionals.socialexplorer.vaadin.components;

import com.vaadin.shared.ui.JavaScriptComponentState;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class GoogleChartsState extends JavaScriptComponentState {
    public String jsChartClass;
    public List<List<Object>> dataTable;
    public Map<String, String> options;
}
