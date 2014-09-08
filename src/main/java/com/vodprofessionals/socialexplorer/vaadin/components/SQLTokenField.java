package com.vodprofessionals.socialexplorer.vaadin.components;

import com.vaadin.data.Item;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.tokenfield.TokenField;

import java.sql.SQLException;
import java.util.Date;

/**
 *
 */
public class SQLTokenField extends TokenField {
    Logger logger = LoggerFactory.getLogger(SQLTokenField.class);
    SQLContainer termsContainer;
    String termType;
    Object containerId;


    public SQLTokenField(SQLContainer termsContainer, String termType, Object containerId) {
        this.termsContainer = termsContainer;
        this.termType = termType;
        this.containerId = containerId;
    }

    @Override
    protected void onTokenClick(Object tokenId) {
        termsContainer.removeAllContainerFilters();
        termsContainer.addContainerFilter(new And(new Compare.Equal("container_id", containerId), new Compare.Equal("term_type", termType), new Compare.Equal("term", tokenId)));
        Object id = termsContainer.firstItemId();
        termsContainer.getItem(id).getItemProperty("status").setValue("disabled");
        try {
            termsContainer.commit();
        } catch (SQLException e) {
            logger.error("Failed to deactivate term '" + tokenId + "'", e);
        }

        this.removeToken(tokenId);
    }

    @Override
    protected void onTokenInput(Object tokenId) {
        termsContainer.removeAllContainerFilters();
        termsContainer.addContainerFilter(new And(new Compare.Equal("container_id", containerId), new Compare.Equal("term_type", termType), new Compare.Equal("term", tokenId)));
        Object id = termsContainer.firstItemId();
        termsContainer.removeAllContainerFilters();
        if (null == id) {
            id = termsContainer.addItem();
            Item i = termsContainer.getItem(id);
            i.getItemProperty("term").setValue(tokenId);
            i.getItemProperty("term_type").setValue(termType);
            i.getItemProperty("container_id").setValue(containerId);
            i.getItemProperty("status").setValue("active");
        } else {
            termsContainer.getItem(id).getItemProperty("createdAt").setValue(new Date());
            termsContainer.getItem(id).getItemProperty("status").setValue("active");
        }
        try {
            termsContainer.commit();
        } catch (SQLException e) {
            logger.error("Failed to activate term '" + tokenId + "'", e);
        }

        this.addToken(tokenId);
    }

}
