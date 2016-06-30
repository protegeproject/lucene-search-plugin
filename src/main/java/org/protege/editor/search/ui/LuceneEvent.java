package org.protege.editor.search.ui;

import org.semanticweb.owlapi.model.OWLEntity;

import java.util.Collection;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Rafael Gonçalves <br>
 * Center for Biomedical Informatics Research <br>
 * Stanford University
 */
public class LuceneEvent {
    private Collection<OWLEntity> results;

    /**
     * No-arguments constructor
     */
    private LuceneEvent() { }

    /**
     * Constructor for an event that carries search results
     *
     * @param results   Collection of entities in the search results
     */
    private LuceneEvent(Collection<OWLEntity> results) {
        this.results = checkNotNull(results);
    }

    /**
     * Get the collection entities in the search results. Optional.empty is returned if the event does not contain a collection of results
     *
     * @return Optional collection of entities
     */
    public Optional<Collection<OWLEntity>> getResults() {
        if(results != null) {
            return Optional.of(results);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Lucene event that is fired when a search is initiated
     */
    public static LuceneEvent SEARCH_STARTED = new LuceneEvent();

    /**
     * Lucene event that is fired when a search is finished, containing the collection of entities in the results
     *
     * @param results   Collection of entities in the search results
     * @return LuceneEvent
     */
    public static LuceneEvent SEARCH_FINISHED(Collection<OWLEntity> results) {
        return new LuceneEvent(results);
    }
}
