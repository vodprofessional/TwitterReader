package com.vodprofessionals.socialexplorer.vaadin.views;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.connection.SimpleJDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.query.TableQuery;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import com.vodprofessionals.socialexplorer.vaadin.components.SQLTokenField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.tokenfield.TokenField;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 *
 */
public class TermsView extends VerticalLayout implements View {
    Logger logger = LoggerFactory.getLogger(TermsView.class);
    SQLContainer servicesContainer;
    SQLContainer dynamicContainer;
    Table servicesTable = new Table();
    Table dynamicTable = new Table();

    public TermsView() {
        UI.getCurrent().getPage().setTitle("Twitter Collector Terms Management");

        setSizeFull();
        addStyleName("transactions");


        // SERVICES HEADER
        HorizontalLayout servicesHeader = new HorizontalLayout();
        servicesHeader.setWidth("100%");
        servicesHeader.setSpacing(true);
        servicesHeader.addStyleName("toolbar");
        addComponent(servicesHeader);

        {
            final Label title = new Label("Manage Services");
            title.setSizeUndefined();
            title.addStyleName("h1");
            servicesHeader.addComponent(title);
            servicesHeader.setComponentAlignment(title, Alignment.MIDDLE_LEFT);
            servicesHeader.setExpandRatio(title, 1);

            Button edit = new Button();
            edit.addStyleName("icon-edit");
            edit.addStyleName("icon-only");
            servicesHeader.addComponent(edit);
            edit.setDescription("Add new Service");
            edit.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent clickEvent) {
                    // TODO Implement
                }
            });
            servicesHeader.setComponentAlignment(edit, Alignment.MIDDLE_LEFT);
        }

        // CONTAINER SERVICES
        HorizontalLayout servicesPanel = new HorizontalLayout();
        servicesPanel.setWidth("100%");
        servicesPanel.setSpacing(false);
        addComponent(servicesPanel);
        setExpandRatio(servicesPanel, 1);

        {
            servicesTable = new Table();
            servicesTable.setSizeFull();
            servicesTable.addStyleName("borderless");
            servicesTable.setSelectable(true);

            servicesPanel.addComponent(servicesTable);
        }

        // DYNAMIC HEADER
        HorizontalLayout dynamicHeader = new HorizontalLayout();
        dynamicHeader.setWidth("100%");
        dynamicHeader.setSpacing(true);
        dynamicHeader.addStyleName("toolbar");
        addComponent(dynamicHeader);

        {
            final Label title = new Label("Manage Dynamic Terms");
            title.setSizeUndefined();
            title.addStyleName("h1");
            dynamicHeader.addComponent(title);
            dynamicHeader.setComponentAlignment(title, Alignment.MIDDLE_LEFT);
            dynamicHeader.setExpandRatio(title, 1);

            Button editTerms = new Button();
            editTerms.addStyleName("icon-edit");
            editTerms.addStyleName("icon-only");
            dynamicHeader.addComponent(editTerms);
            editTerms.setDescription("Add new dynamic term");
            editTerms.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent clickEvent) {
                    // TODO Implement
                }
            });
            dynamicHeader.setComponentAlignment(editTerms, Alignment.MIDDLE_LEFT);
        }

        // CONTAINER DYNAMIC
        HorizontalLayout dynamicPanel = new HorizontalLayout();
        dynamicPanel.setWidth("100%");
        dynamicPanel.setSpacing(false);
        addComponent(dynamicPanel);
        setExpandRatio(dynamicPanel, 1);

        {
            dynamicTable = new Table();
            dynamicTable.setSizeFull();
            dynamicTable.addStyleName("borderless");
            dynamicTable.setSelectable(true);
            dynamicPanel.addComponent(dynamicTable);
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
        try {
            Config c = ConfigFactory.load();
            SimpleJDBCConnectionPool connPool = new SimpleJDBCConnectionPool(
                    c.getString("database.driver"),
                    c.getString("database.url"),
                    c.getString("database.username"),
                    c.getString("database.password"));

            final SQLContainer termsContainer = new SQLContainer(new TableQuery("search_terms", connPool));
            final SQLContainer dynamicServicesContainer = new SQLContainer(new TableQuery("services", connPool));


            servicesContainer = new SQLContainer(new TableQuery("services", connPool));
            servicesTable.setContainerDataSource(servicesContainer);
            servicesTable.addGeneratedColumn("Terms", new Table.ColumnGenerator() {
                @Override
                public Object generateCell(Table source, Object itemId, Object columnId) {
                    final Object dynaId = source.getItem(itemId).getItemProperty("id").getValue();
                    termsContainer.removeAllContainerFilters();
                    termsContainer.addContainerFilter(new And(new Compare.Equal("container_id", dynaId), new Compare.Equal("term_type", "static"), new Compare.Equal("status", "active")));

                    Collection<?> terms = termsContainer.getItemIds();
                    Set<Object> t = new HashSet<>();
                    for (Iterator<?> it = terms.iterator(); it.hasNext();) {
                        t.add(termsContainer.getItem(it.next()).getItemProperty("term").getValue());
                    }

                    SQLTokenField tokenField = new SQLTokenField(termsContainer, "static", dynaId);
                    tokenField.setRememberNewTokens(true);
                    tokenField.setValue(t);

                    return tokenField;
                }
            });
            servicesTable.setVisibleColumns(new Object[]{ "name", "Terms" });


            dynamicContainer = new SQLContainer(new TableQuery("service_extras", connPool));
            dynamicTable.setContainerDataSource(dynamicContainer);
            dynamicTable.addGeneratedColumn("Service", new Table.ColumnGenerator() {
                @Override
                public Object generateCell(Table source, Object itemId, Object columnId) {
                    Object serviceId = source.getItem(itemId).getItemProperty("service_id").getValue();
                    dynamicServicesContainer.removeAllContainerFilters();
                    dynamicServicesContainer.addContainerFilter(new Compare.Equal("id", serviceId));
                    Item i = dynamicServicesContainer.getItem(dynamicServicesContainer.firstItemId());

                    return i.getItemProperty("name").getValue();
                }
            });
            final SQLContainer dynamicTermsContainer = new SQLContainer(new TableQuery("search_terms", connPool));
            dynamicTable.addGeneratedColumn("Terms", new Table.ColumnGenerator() {
                @Override
                public Object generateCell(Table source, Object itemId, Object columnId) {
                    Object dynaId = source.getItem(itemId).getItemProperty("id").getValue();
                    dynamicTermsContainer.removeAllContainerFilters();
                    dynamicTermsContainer.addContainerFilter(new And(new Compare.Equal("container_id", dynaId), new Compare.Equal("term_type", "dynamic"), new Compare.Equal("status", "active")));

                    Collection<?> terms = dynamicTermsContainer.getItemIds();
                    Set<Object> t = new HashSet<>();
                    for (Iterator<?> it = terms.iterator(); it.hasNext();) {
                        t.add(dynamicTermsContainer.getItem(it.next()).getItemProperty("term").getValue());
                    }

                    SQLTokenField tokenField = new SQLTokenField(termsContainer, "dynamic", dynaId);
                    tokenField.setRememberNewTokens(true);
                    tokenField.setValue(t);

                    return tokenField;
                }
            });
            dynamicTable.setVisibleColumns(new Object[] { "name", "tx_datetime", "channel", "Service", "Terms" });
        } catch (SQLException e) {
            logger.error("Error trying to connect to database from web interface", e);
            servicesTable.setContainerDataSource(new IndexedContainer());
            dynamicTable.setContainerDataSource(new IndexedContainer());
        }
    }
}