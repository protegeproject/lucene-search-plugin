package org.protege.editor.search.lucene;

import org.apache.lucene.document.Document;
import org.semanticweb.owlapi.model.OWLOntologyChange;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class AddChangeSet implements Iterable<Document> {

    private Set<Document> documents;

    private AddChangeSet(Set<Document> documents) {
        this.documents = documents;
    }

    public static AddChangeSet create(List<? extends OWLOntologyChange> changes, AddChangeSetHandler handler) {
        for (OWLOntologyChange change : changes) {
            change.accept(handler);
        }
        return new AddChangeSet(handler.getDocuments());
    }

    public Set<Document> getAddDocuments() {
        return documents;
    }

    public int size() {
        return documents.size();
    }

    @Override
    public Iterator<Document> iterator() {
        return documents.iterator();
    }
}
