package org.protege.editor.search.nci;

import org.protege.editor.owl.model.search.SearchCategory;
import org.protege.editor.search.lucene.AbstractDocumentHandler;
import org.protege.editor.search.lucene.QueryEvaluationException;
import org.protege.editor.search.lucene.SearchQuery;

import org.semanticweb.owlapi.model.OWLEntity;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Josef Hardi <johardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 27/06/2016
 */
public class NegatedQuery extends RequiresPostProcessing {

    private final UserQuery filterQuery;
    private final Set<OWLEntity> resultSpace;
    private final SearchCategory category;

    public NegatedQuery(UserQuery filterQuery, Set<OWLEntity> resultSpace) {
        this.filterQuery = filterQuery;
        this.resultSpace = resultSpace;
        this.category = SearchCategory.OTHER;
    }

    @Override
    public void evaluate(AbstractDocumentHandler handler) throws QueryEvaluationException {
        for (SearchQuery query : filterQuery) {
            query.evaluate(handler);
        }
    }

    @Override
    public void evaluate(AbstractDocumentHandler handler, SearchProgressListener listener) throws QueryEvaluationException {
        for (SearchQuery query : filterQuery) {
            query.evaluate(handler, listener);
        }
    }

    @Override
    /*default*/ Set<OWLEntity> performPostProcessing(Set<OWLEntity> producedResults) {
        Set<OWLEntity> finalResults = new HashSet<>(resultSpace);
        NciSearchUtils.difference(finalResults, producedResults);
        return finalResults;
    }

    @Override
    public int hashCode() {
        return NegatedQuery.class.getSimpleName().hashCode() + filterQuery.hashCode() + category.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof NegatedQuery)) {
            return false;
        }
        NegatedQuery other = (NegatedQuery) obj;
        return this.filterQuery.equals(filterQuery) && this.category.equals(other.category);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("filterQuery").append(": ").append(filterQuery);
        return sb.toString();
    }
}
