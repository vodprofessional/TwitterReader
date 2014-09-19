package com.vodprofessionals.socialexplorer.vaadin.views;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.converter.StringToDateConverter;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.connection.SimpleJDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.query.TableQuery;
import com.vaadin.event.ShortcutAction;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import com.vodprofessionals.socialexplorer.Application;
import com.vodprofessionals.socialexplorer.akka.SearchTermsActor;
import com.vodprofessionals.socialexplorer.vaadin.DashboardUI;
import com.vodprofessionals.socialexplorer.vaadin.components.SQLTokenField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.tokenfield.TokenField;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;


/**
 *
 */
public class TermsView extends VerticalLayout implements View {
    Logger logger = LoggerFactory.getLogger(TermsView.class);
    SQLContainer servicesContainer;
    SQLContainer dynamicContainer;
    SQLContainer dynamicServicesContainer;
    SQLContainer termsContainer;
    Table servicesTable = new Table();
    Table dynamicTable = new Table();
    Window addServiceWindow;
    Window addServiceExtraWindow;
    final ActorRef searchTermsActor = Application.actorSystem().actorOf(Props.create(SearchTermsActor.class));


    public TermsView() {
        UI.getCurrent().getPage().setTitle("Twitter Collector Terms Management");

        setSizeFull();
        addStyleName("transactions");


        // SERVICES HEADER
        HorizontalLayout servicesHeader = new HorizontalLayout();
        servicesHeader.setWidth("99%");
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
                @SuppressWarnings("unchecked")
                public void buttonClick(Button.ClickEvent clickEvent) {
                    buildNewServiceWindow();
                    getUI().addWindow(addServiceWindow);
                    addServiceWindow.focus();
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
        dynamicHeader.setWidth("99%");
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
                    buildNewServiceExtrasWindow();
                    getUI().addWindow(addServiceExtraWindow);
                    addServiceExtraWindow.focus();
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

            termsContainer = new SQLContainer(new TableQuery("search_terms", connPool));
            dynamicServicesContainer = new SQLContainer(new TableQuery("services", connPool));

            servicesContainer = new SQLContainer(new TableQuery("services", connPool));
            servicesTable.setContainerDataSource(servicesContainer);
            servicesTable.addGeneratedColumn("Terms", new Table.ColumnGenerator() {
                @Override
                @SuppressWarnings("unchecked")
                public Object generateCell(Table source, Object itemId, Object columnId) {
                    Object dynaId = source.getItem(itemId).getItemProperty("id").getValue();

                    Collection<?> terms;
                    if (null == dynaId) {
                        terms = Collections.emptySet();
                    } else {
                        termsContainer.removeAllContainerFilters();
                        termsContainer.addContainerFilter(new And(new Compare.Equal("container_id", dynaId), new Compare.Equal("term_type", "static"), new Compare.Equal("status", "active")));

                        terms = termsContainer.getItemIds();
                    }

                    Set<Object> t = new HashSet<>();
                    for (Iterator<?> it = terms.iterator(); it.hasNext();) {
                        t.add(termsContainer.getItem(it.next()).getItemProperty("term").getValue());
                    }

                    SQLTokenField tokenField = new SQLTokenField(termsContainer, "static", dynaId, searchTermsActor);
                    tokenField.setRememberNewTokens(true);
                    tokenField.setValue(t);
                    tokenField.addTokenChangedEventHandler(new SQLTokenField.TokenChangedEvent() {
                        @Override
                        public void tokenAdded(String token) {
                            ((DashboardUI) getUI()).updateTermsBadgeCount();
                        }

                        @Override
                        public void tokenRemoved(String token) {
                            ((DashboardUI) getUI()).updateTermsBadgeCount();
                        }
                    });

                    return tokenField;
                }
            });
            servicesTable.setVisibleColumns(new Object[]{ "name", "Terms" });


            dynamicContainer = new SQLContainer(new TableQuery("service_extras", connPool));
            dynamicTable.setContainerDataSource(dynamicContainer);
            dynamicTable.addGeneratedColumn("Service", new Table.ColumnGenerator() {
                @Override
                @SuppressWarnings("unchecked")
                public Object generateCell(Table source, Object itemId, Object columnId) {
                    Object serviceId = source.getItem(itemId).getItemProperty("service_id").getValue();
                    if (null != serviceId) {
                        dynamicServicesContainer.removeAllContainerFilters();
                        dynamicServicesContainer.addContainerFilter(new Compare.Equal("id", serviceId));
                        Item i = dynamicServicesContainer.getItem(dynamicServicesContainer.firstItemId());

                        return i.getItemProperty("name").getValue();
                    }
                    else {
                        return "";
                    }
                }
            });
            dynamicTable.setConverter("tx_datetime", new StringToDateConverter() {
                protected DateFormat getFormat(Locale locale) {
                    return new SimpleDateFormat("EEE, d MMMMM yyyy HH:mm:ss", locale);
                }
            });
            final SQLContainer dynamicTermsContainer = new SQLContainer(new TableQuery("search_terms", connPool));
            dynamicTable.addGeneratedColumn("Terms", new Table.ColumnGenerator() {
                @Override
                @SuppressWarnings("unchecked")
                public Object generateCell(Table source, Object itemId, Object columnId) {
                    Object dynaId = source.getItem(itemId).getItemProperty("id").getValue();

                    Collection<?> terms;
                    if (null == dynaId) {
                        terms = Collections.emptySet();
                    } else {

                        dynamicTermsContainer.removeAllContainerFilters();
                        dynamicTermsContainer.addContainerFilter(new And(new Compare.Equal("container_id", dynaId), new Compare.Equal("term_type", "dynamic"), new Compare.Equal("status", "active")));

                        terms = dynamicTermsContainer.getItemIds();
                    }

                    Set<Object> t = new HashSet<>();
                    for (Iterator<?> it = terms.iterator(); it.hasNext();) {
                        t.add(dynamicTermsContainer.getItem(it.next()).getItemProperty("term").getValue());
                    }

                    SQLTokenField tokenField = new SQLTokenField(termsContainer, "dynamic", dynaId, searchTermsActor);
                    tokenField.setRememberNewTokens(true);
                    tokenField.setValue(t);
                    tokenField.addTokenChangedEventHandler(new SQLTokenField.TokenChangedEvent() {
                        @Override
                        public void tokenAdded(String token) {
                            ((DashboardUI) getUI()).updateTermsBadgeCount();
                        }

                        @Override
                        public void tokenRemoved(String token) {
                            ((DashboardUI) getUI()).updateTermsBadgeCount();
                        }
                    });

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

    public void buildNewServiceWindow() {
        addServiceWindow = new Window("Add New Service");
        addServiceWindow.setModal(true);
        addServiceWindow.setClosable(false);
        addServiceWindow.setResizable(false);
        addServiceWindow.addStyleName("edit-dashboard");

        addServiceWindow.setContent(new VerticalLayout() {
            TextField name = new TextField("Service name");
            {
                addComponent(new FormLayout() {
                    {
                        setSizeUndefined();
                        setMargin(true);
                        addComponent(name);
                        name.focus();
                        name.selectAll();
                    }
                });
                addComponent(new HorizontalLayout() {
                    {
                        setMargin(true);
                        setSpacing(true);
                        addStyleName("footer");
                        setWidth("100%");

                        Button cancel = new Button("Cancel");
                        cancel.addClickListener(new Button.ClickListener() {
                            @Override
                            public void buttonClick(Button.ClickEvent event) {
                                addServiceWindow.close();
                            }
                        });
                        cancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE, null);
                        addComponent(cancel);
                        setExpandRatio(cancel, 1);
                        setComponentAlignment(cancel,
                                Alignment.TOP_RIGHT);

                        Button ok = new Button("Save");
                        ok.addStyleName("wide");
                        ok.addStyleName("default");
                        ok.addClickListener(new Button.ClickListener() {
                            @Override
                            @SuppressWarnings("unchecked")
                            public void buttonClick(Button.ClickEvent event) {
                                try {
                                    Object id = servicesContainer.addItem();
                                    servicesContainer.getItem(id).getItemProperty("name").setValue(name.getValue());
                                    servicesContainer.commit();
                                } catch (SQLException e) {
                                    logger.error(e.getMessage(), e);
                                }
                                addServiceWindow.close();
                            }
                        });
                        ok.setClickShortcut(ShortcutAction.KeyCode.ENTER, null);
                        addComponent(ok);
                    }
                });
            }
        });
    }

    public void buildNewServiceExtrasWindow() {
        addServiceExtraWindow = new Window("Add New Dynamic Category");
        addServiceExtraWindow.setModal(true);
        addServiceExtraWindow.setClosable(false);
        addServiceExtraWindow.setResizable(false);
        addServiceExtraWindow.addStyleName("edit-dashboard");

        addServiceExtraWindow.setContent(new VerticalLayout() {
            TextField name = new TextField("Dynamic category name");
            DateField txDateTime = new DateField("TX Datetime");
            TextField channel = new TextField("Channel");
            ComboBox services = new ComboBox("Service");
            TokenField tokenField = new TokenField("Terms");
            {
                addComponent(new FormLayout() {
                    {
                        setSizeUndefined();
                        setMargin(true);
                        addComponent(name);
                        name.focus();
                        txDateTime.setResolution(DateField.RESOLUTION_SEC);
                        txDateTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
                        addComponent(txDateTime);
                        addComponent(channel);
                        services.setInvalidAllowed(false);
                        services.setNullSelectionAllowed(false);
                        servicesContainer.removeAllContainerFilters();
                        for(Object id : servicesContainer.getItemIds()) {
                            services.addItem(servicesContainer.getItem(id).getItemProperty("name").getValue().toString());
                        }
                        addComponent(services);
                        addComponent(tokenField);
                    }
                });
                addComponent(new HorizontalLayout() {
                    {
                        setMargin(true);
                        setSpacing(true);
                        addStyleName("footer");
                        setWidth("100%");

                        Button cancel = new Button("Cancel");
                        cancel.addClickListener(new Button.ClickListener() {
                            @Override
                            public void buttonClick(Button.ClickEvent event) {
                                addServiceExtraWindow.close();
                            }
                        });
                        cancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE, null);
                        addComponent(cancel);
                        setExpandRatio(cancel, 1);
                        setComponentAlignment(cancel,
                                Alignment.TOP_RIGHT);

                        Button ok = new Button("Save");
                        ok.addStyleName("wide");
                        ok.addStyleName("default");
                        ok.addClickListener(new Button.ClickListener() {
                            @Override
                            @SuppressWarnings("unchecked")
                            public void buttonClick(Button.ClickEvent event) {
                                try {
                                    Object id = dynamicContainer.addItem();
                                    dynamicContainer.getItem(id).getItemProperty("name").setValue(name.getValue());
                                    dynamicContainer.getItem(id).getItemProperty("tx_datetime").setValue(txDateTime.getValue());
                                    dynamicContainer.getItem(id).getItemProperty("channel").setValue(channel.getValue());
                                    dynamicServicesContainer.removeAllContainerFilters();
                                    Object value = services.getValue();
                                    dynamicServicesContainer.addContainerFilter(new Compare.Equal("name", value));
                                    Object serviceId = dynamicServicesContainer.firstItemId();
                                    dynamicContainer.getItem(id).getItemProperty("service_id").setValue(Long.parseLong(serviceId.toString()));
                                    dynamicContainer.commit();

                                    // Add the tokens
                                    Collection<String> tokens = (Collection<String>) tokenField.getValue();
                                    Long dcid = (Long) dynamicContainer.getItem(dynamicContainer.lastItemId()).getItemProperty("id").getValue();

                                    for(String token: tokens) {
                                        termsContainer.removeAllContainerFilters();
                                        termsContainer.addContainerFilter(new And(new Compare.Equal("container_id", id), new Compare.Equal("term_type", "dynamic"), new Compare.Equal("term", token)));
                                        Object tid = termsContainer.firstItemId();
                                        termsContainer.removeAllContainerFilters();
                                        if (null == tid) {
                                            tid = termsContainer.addItem();
                                            Item i = termsContainer.getItem(tid);
                                            i.getItemProperty("term").setValue(token);
                                            i.getItemProperty("term_type").setValue("dynamic");
                                            i.getItemProperty("container_id").setValue(dcid);
                                            i.getItemProperty("createdAt").setValue(new Date());
                                            i.getItemProperty("status").setValue("active");
                                        } else {
                                            termsContainer.getItem(tid).getItemProperty("createdAt").setValue(new Date());
                                            termsContainer.getItem(tid).getItemProperty("status").setValue("active");
                                        }
                                        try {
                                            termsContainer.commit();
                                            searchTermsActor.tell(new SearchTermsActor.AddSearchTerm(token), null);
                                        } catch (SQLException e) {
                                            logger.error("Failed to activate term '" + token + "'", e);
                                        }
                                    }
                                    dynamicTable.refreshRowCache();
                                    ((DashboardUI) getUI()).updateTermsBadgeCount();

                                } catch (SQLException e) {
                                    logger.error(e.getMessage(), e);
                                }
                                addServiceExtraWindow.close();
                            }
                        });
                        ok.setClickShortcut(ShortcutAction.KeyCode.ENTER, null);
                        addComponent(ok);
                    }
                });
            }
        });
    }
}