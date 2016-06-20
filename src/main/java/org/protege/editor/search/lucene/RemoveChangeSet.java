package org.protege.editor.search.lucene;

import org.apache.lucene.index.Term;
import org.semanticweb.owlapi.model.OWLOntologyChange;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class RemoveChangeSet implements Iterable<List<Term>> {

    private Set<List<Term>> removeFilters;

    private RemoveChangeSet(Set<List<Term>> removeFilters) {
        this.removeFilters = removeFilters;
    }

    public static RemoveChangeSet create(List<? extends OWLOntologyChange> changes, RemoveChangeSetHandler handler) {
        for (OWLOntologyChange change : changes) {
            change.accept(handler);
        }
        return new RemoveChangeSet(handler.getRemoveFilters());
    }

    public Set<List<Term>> getRemoveFilters() {
        return removeFilters;
    }

    public int size() {
        return removeFilters.size();
    }

    @Override
    public Iterator<List<Term>> iterator() {
        return removeFilters.iterator();
    }
}
