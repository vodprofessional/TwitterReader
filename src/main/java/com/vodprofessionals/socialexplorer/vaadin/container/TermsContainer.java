package com.vodprofessionals.socialexplorer.vaadin.container;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;

/**
 *
 */
public class TermsContainer extends IndexedContainer {
    private static final long serialVersionUID = 1L;

    public TermsContainer() {
        addContainerProperty("Term", String.class, "");
    }

    public Object addTerm(String term) {
        Object id = addItem();

        return updateTerm(id, term);
    }

    public Object updateTerm(Object id, String term) {
        Item item = getItem(id);
        if (item != null) {
            item.getItemProperty("Term").setValue(term);
        }

        return id;
    }
}
