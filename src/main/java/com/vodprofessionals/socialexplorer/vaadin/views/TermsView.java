package com.vodprofessionals.socialexplorer.vaadin.views;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import com.vodprofessionals.socialexplorer.vaadin.container.ServiceContainer;
import org.vaadin.tokenfield.TokenField;

import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class TermsView extends VerticalLayout implements View {

    public TermsView() {
        UI.getCurrent().getPage().setTitle("Social Explorer Terms Management");

        setSizeFull();
        addStyleName("dashboard-view");

        VerticalLayout page = new VerticalLayout();

        // Header piece
        {
            HorizontalLayout viewHeader = new HorizontalLayout();
            viewHeader.setWidth("100%");
            viewHeader.setSpacing(true);
            viewHeader.addStyleName("toolbar");
            page.addComponent(viewHeader);

            Label title = new Label("Manage Services and Terms");
            title.setSizeUndefined();
            title.addStyleName("h1");
            viewHeader.addComponent(title);
            setComponentAlignment(title, Alignment.MIDDLE_LEFT);
            setExpandRatio(title, 1);
        }

        {
            VerticalLayout servicesLayout = new VerticalLayout();

            Object editedItemId;
            ServiceContainer serviceContainer = new ServiceContainer();
            List servicesTerms = new LinkedList<String>();
            TextField serviceName = new TextField("Service name");
            TokenField serviceTerms = new TokenField("Service terms");
            Table servicesTable = new Table();

            {
                HorizontalLayout viewHeader = new HorizontalLayout();
                viewHeader.setWidth("100%");
                viewHeader.setSpacing(true);
                viewHeader.addStyleName("toolbar");

                {
                    Label title = new Label("Services");
                    title.setSizeUndefined();
                    title.addStyleName("h2");
                    viewHeader.addComponent(title);
                    setComponentAlignment(title, Alignment.MIDDLE_LEFT);
                    setExpandRatio(title, 1);

                    Button newButton = new Button("Add New Service");
                    newButton.addStyleName("small");
                    newButton.addStyleName("default");
                    newButton.addClickListener((Button.ClickEvent event) -> {
                        // TODO Implement!
                    });
                    viewHeader.addComponent(newButton);
                }
                servicesLayout.addComponent(viewHeader);
            }

            {
                HorizontalLayout l = new HorizontalLayout();
                l.setWidth("100%");
                l.setSpacing(true);

                {
                    servicesTable.setSizeFull();
                    servicesTable.addStyleName("borderless");
                    servicesTable.setWidth("400px");
                    servicesTable.setHeight("100%");
                    servicesTable.setSelectable(true);
                    servicesTable.setMultiSelect(false);
                    servicesTable.setImmediate(true);
                    servicesTable.setContainerDataSource(serviceContainer);
                    servicesTable.addItemClickListener((ItemClickEvent event) -> {
                        // TODO Implement
                    });
                    l.addComponent(servicesTable);
                }

                {
                    l.addComponent(serviceName);
                    l.addComponent(serviceTerms);

                    Button save = new Button("Save");
                    save.addClickListener((Button.ClickEvent event) -> {
                        // TODO Implement Save
                    });
                    l.addComponent(save);
                }

                servicesLayout.addComponent(l);
            }

            page.addComponent(servicesLayout);
        }


        page.setSizeFull();
        page.setWidth("100%");
        addComponent(page);

    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {

    }
}