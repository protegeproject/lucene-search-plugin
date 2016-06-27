package org.protege.editor.search.nci;

import org.protege.editor.search.lucene.SearchQuery;

import org.semanticweb.owlapi.model.OWLEntity;

import java.util.Set;

/**
 * @author Josef Hardi <johardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 27/06/2016
 */
/*default*/ abstract class RequiresPostProcessing implements SearchQuery {

    /*default*/ abstract Set<OWLEntity> performPostProcessing(Set<OWLEntity> producedResults);
}
