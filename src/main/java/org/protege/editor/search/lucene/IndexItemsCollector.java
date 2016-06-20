package org.protege.editor.search.lucene;

import org.apache.lucene.document.Document;
import org.semanticweb.owlapi.util.OWLObjectVisitorAdapter;

import java.util.Set;

public abstract class IndexItemsCollector extends OWLObjectVisitorAdapter {

    public abstract Set<Document> getIndexDocuments();
}
