package org.protege.editor.search.nci;

import org.protege.editor.search.lucene.QueryEvaluationException;

import org.semanticweb.owlapi.model.OWLEntity;

import java.util.Set;

/**
 * @author Josef Hardi <johardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 28/06/2016
 */
public interface SearchPluginQuery {

    Set<OWLEntity> evaluate(SearchProgressListener listener) throws QueryEvaluationException;

    public interface SearchProgressListener {
        
        void fireSearchingProgressed(long progress);
    }
}
