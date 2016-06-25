package org.protege.editor.search.nci;

import org.semanticweb.owlapi.model.OWLEntity;

import java.util.Collection;

public interface NciSearchResultHandler {

    void searchFinished(Collection<OWLEntity> searchResults);
}
