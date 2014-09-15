package com.vodprofessionals.socialexplorer.vaadin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vodprofessionals.socialexplorer.model.SearchTerms;
import com.vodprofessionals.socialexplorer.vaadin.views.DashboardView;
import com.vodprofessionals.socialexplorer.vaadin.views.ReportsView;
import com.vodprofessionals.socialexplorer.vaadin.views.TermsView;

@Theme("dashboard")
@Title("TwitterCollector Dashboard")
public class DashboardUI extends UI {

    private static final long serialVersionUID = 1L;

    CssLayout root = new CssLayout();

    CssLayout menu = new CssLayout();
    CssLayout content = new CssLayout();


    HashMap<String, Class<? extends View>> routes = new HashMap<String, Class<? extends View>>() {
        {
            put("/dashboard", DashboardView.class);
            put("/terms", TermsView.class);
            put("/reports", ReportsView.class);
        }
    };

    HashMap<String, Button> viewNameToMenuButton = new HashMap<String, Button>();

    private Navigator nav;


    @Override
    protected void init(VaadinRequest request) {
        setLocale(Locale.US);

        setContent(root);
        root.addStyleName("root");
        root.setSizeFull();

        buildMainView();
    }

    private void buildMainView() {

        nav = new Navigator(this, content);

        for (String route : routes.keySet()) {
            nav.addView(route, routes.get(route));
        }

        root.addComponent(new HorizontalLayout() {
            {
                setSizeFull();
                addStyleName("main-view");
                addComponent(new VerticalLayout() {
                    // Sidebar
                    {
                        addStyleName("sidebar");
                        setWidth(null);
                        setHeight("100%");

                        // Branding element
                        addComponent(new CssLayout() {
                            {
                                addStyleName("branding");
                                Label logo = new Label(
                                        "<span>TwitterCollector</span> Dashboard",
                                        ContentMode.HTML);
                                logo.setSizeUndefined();
                                addComponent(logo);
                                // addComponent(new Image(null, new
                                // ThemeResource(
                                // "img/branding.png")));
                            }
                        });

                        // Main menu
                        addComponent(menu);
                        setExpandRatio(menu, 1);
                    }
                });
                // Content
                addComponent(content);
                content.setSizeFull();
                content.addStyleName("view-content");
                setExpandRatio(content, 1);
            }

        });

        menu.removeAllComponents();

        for (final String view : new String[] { "dashboard", "terms", "reports" }) {
            Button b = new NativeButton(view.substring(0, 1).toUpperCase()
                    + view.substring(1).replace('-', ' '));
            if ("terms" == view) {
                b.addStyleName("icon-retweet");
            } else {
                b.addStyleName("icon-" + view);
            }
            b.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    clearMenuSelection();
                    event.getButton().addStyleName("selected");
                    if (!nav.getState().equals("/" + view))
                        nav.navigateTo("/" + view);
                }
            });

            menu.addComponent(b);
            viewNameToMenuButton.put("/" + view, b);
        }
        menu.addStyleName("menu");
        menu.setHeight("100%");

        viewNameToMenuButton.get("/terms").setHtmlContentAllowed(true);
        updateTermsBadgeCount();

        String f = Page.getCurrent().getUriFragment();
        if (f != null && f.startsWith("!")) {
            f = f.substring(1);
        }
        if (f == null || f.equals("") || f.equals("/")) {
            nav.navigateTo("/dashboard");
            menu.getComponent(0).addStyleName("selected");
        } else {
            nav.navigateTo(f);
            viewNameToMenuButton.get(f).addStyleName("selected");
        }
    }

    public void updateTermsBadgeCount() {
        viewNameToMenuButton.get("/terms").setCaption(
                "Terms<span class=\"badge\">" + SearchTerms.getTermsCount() + "</span>");
    }

    private void clearMenuSelection() {
        for (Iterator<Component> it = menu.iterator(); it.hasNext();) {
            Component next = it.next();
            if (next instanceof NativeButton) {
                next.removeStyleName("selected");
            } else if (next instanceof DragAndDropWrapper) {
                // Wow, this is ugly (even uglier than the rest of the code)
                ((DragAndDropWrapper) next).iterator().next()
                        .removeStyleName("selected");
            }
        }
    }
}
