package com.vodprofessionals.socialexplorer.vaadin.components;

import akka.actor.ActorRef;
import com.vaadin.data.Item;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vodprofessionals.socialexplorer.akka.SearchTermsActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.tokenfield.TokenField;

import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
@SuppressWarnings("unchecked")
public class SQLTokenField extends TokenField {
    Logger logger = LoggerFactory.getLogger(SQLTokenField.class);
    SQLContainer termsContainer;
    String termType;
    Object containerId;
    ActorRef searchTermActor;
    List<TokenChangedEvent> eventHandlers = new LinkedList<TokenChangedEvent>();


    public SQLTokenField(SQLContainer termsContainer, String termType, Object containerId, ActorRef searchTermActor) {
        this.termsContainer = termsContainer;
        this.termType = termType;
        this.containerId = containerId;
        this.searchTermActor = searchTermActor;
    }

    public void addTokenChangedEventHandler(TokenChangedEvent event) {
        eventHandlers.add(event);
    }

    @Override
    protected void onTokenClick(Object tokenId) {
        termsContainer.removeAllContainerFilters();
        termsContainer.addContainerFilter(new And(new Compare.Equal("container_id", containerId), new Compare.Equal("term_type", termType), new Compare.Equal("term", tokenId)));
        Object id = termsContainer.firstItemId();
        termsContainer.getItem(id).getItemProperty("status").setValue("disabled");
        try {
            termsContainer.commit();
            searchTermActor.tell(new SearchTermsActor.RemoveSearchTerm(tokenId.toString()), null);
            this.removeToken(tokenId);

            for (TokenChangedEvent event : eventHandlers) {
                event.tokenRemoved(tokenId.toString());
            }
        } catch (SQLException e) {
            logger.error("Failed to deactivate term '" + tokenId + "'", e);
        }
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
            i.getItemProperty("createdAt").setValue(new Date());
            i.getItemProperty("status").setValue("active");
        } else {
            termsContainer.getItem(id).getItemProperty("createdAt").setValue(new Date());
            termsContainer.getItem(id).getItemProperty("status").setValue("active");
        }
        try {
            termsContainer.commit();
            searchTermActor.tell(new SearchTermsActor.AddSearchTerm(tokenId.toString()), null);
            this.addToken(tokenId);

            for (TokenChangedEvent event : eventHandlers) {
                event.tokenAdded(tokenId.toString());
            }
        } catch (SQLException e) {
            logger.error("Failed to activate term '" + tokenId + "'", e);
        }
    }


    public interface TokenChangedEvent {
        public void tokenAdded(String token);

        public void tokenRemoved(String token);
    }
}
