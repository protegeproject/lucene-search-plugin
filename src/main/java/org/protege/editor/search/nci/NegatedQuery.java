package org.protege.editor.search.nci;

import org.protege.editor.owl.model.search.SearchCategory;
import org.protege.editor.search.lucene.AbstractDocumentHandler;
import org.protege.editor.search.lucene.QueryEvaluationException;
import org.protege.editor.search.lucene.SearchQuery;

public class NegatedQuery implements SearchQuery {

    private UserQuery filterQuery;
    private SearchCategory category;

    public NegatedQuery(UserQuery filterQuery) {
        this.filterQuery = filterQuery;
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
