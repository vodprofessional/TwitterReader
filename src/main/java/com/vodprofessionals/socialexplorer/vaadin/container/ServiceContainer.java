package com.vodprofessionals.socialexplorer.vaadin.container;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;

/**
 *
 */
public class ServiceContainer extends IndexedContainer {
    private static final long serialVersionUID = 1L;

    public ServiceContainer() {
        addContainerProperty("Name", String.class, "");

        addService("netflix");
        addService("video");
        addService("hulu");
    }

    public Object addService(String name) {
        Object id = addItem();

        return updateService(id, name);
    }

    public Object updateService(Object id, String name) {
        Item item = getItem(id);
        if (item != null) {
            item.getItemProperty("Name").setValue(name);
        }

        return id;
    }
}
