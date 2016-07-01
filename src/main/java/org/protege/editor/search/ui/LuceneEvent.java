package org.protege.editor.search.ui;

import org.protege.editor.search.nci.FilteredQuery;
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
    private Object source;
    private FilteredQuery query;
    private Collection<OWLEntity> results;

    /**
     * Constructor for a Lucene event
     *
     * @param source    Source object that triggered this event
     */
    public LuceneEvent(Object source) {
        this.source = checkNotNull(source);
    }

    /**
     * Constructor for a Lucene search event
     *
     * @param source    Source object that triggered this event
     * @param query Lucene query
     */
    private LuceneEvent(Object source, FilteredQuery query) {
        this.source = checkNotNull(source);
        this.query = checkNotNull(query);
    }

    /**
     * Constructor for a Lucene search event that carries search results
     *
     * @param source    Source object that triggered this event
     * @param query Lucene query
     * @param results   Collection of entities in the search results
     */
    private LuceneEvent(Object source, FilteredQuery query, Collection<OWLEntity> results) {
        this.source = checkNotNull(source);
        this.query = checkNotNull(query);
        this.results = checkNotNull(results);
    }

    /**
     * Get the Lucene event source object
     *
     * @return Event source object
     */
    public Object getSource() {
        return source;
    }

    /**
     * Get the Lucene query performed
     *
     * @return Optional Lucene query
     */
    public Optional<FilteredQuery> getQuery() {
        return Optional.ofNullable(query);
    }

    /**
     * Get the collection entities in the search results
     *
     * @return Optional collection of entities
     */
    public Optional<Collection<OWLEntity>> getResults() {
        return Optional.ofNullable(results);
    }
}