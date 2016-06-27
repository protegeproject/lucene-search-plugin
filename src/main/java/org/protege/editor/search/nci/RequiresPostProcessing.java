package org.protege.editor.search.nci;

import org.protege.editor.search.lucene.SearchQuery;

import org.semanticweb.owlapi.model.OWLEntity;

import java.util.Set;

/*default*/ abstract class RequiresPostProcessing implements SearchQuery {

    /*default*/ abstract Set<OWLEntity> performPostProcessing(Set<OWLEntity> producedResults);
}
