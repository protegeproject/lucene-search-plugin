package org.protege.editor.search.nci;

import org.protege.editor.search.lucene.QueryEvaluationException;
import org.protege.editor.search.lucene.SearchContext;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Josef Hardi <johardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 27/06/2016
 */
public class NegatedQuery extends ComplexQuery {

    private final List<SearchTabQuery> filters;
    private final Set<OWLEntity> resultSpace;
    private final boolean isMatchAll;

    // Not allowing external instantiation
    private NegatedQuery(List<SearchTabQuery> filters, Set<OWLEntity> resultSpace, boolean isMatchAll) {
        this.filters = filters;
        this.resultSpace = resultSpace;
        this.isMatchAll = isMatchAll;
    }

    @Override
    public boolean isMatchAll() {
        return isMatchAll;
    }

    public List<SearchTabQuery> getFilters() {
        return Collections.unmodifiableList(filters);
    }

    @Override
    public String getAlgebraString() {
        String booleanOperator = isMatchAll ? "AND" : "OR";
        StringBuilder sb = new StringBuilder();
        sb.append("NOT(");
        boolean needOperator = false;
        for (SearchTabQuery filter : filters) {
            if (needOperator) {
                sb.append("\n   ");
                sb.append(booleanOperator).append(" ");
            }
            sb.append(filter.getAlgebraString());
            needOperator = true;
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public Set<OWLEntity> evaluate(SearchProgressListener listener) throws QueryEvaluationException {
        Set<OWLEntity> toReturn = new HashSet<>();
        for (SearchTabQuery filter : filters) {
            Set<OWLEntity> evalResult = filter.evaluate(listener);
            if (isMatchAll) {
                ResultSetUtils.intersect(toReturn, evalResult);
            }
            else { // match any
                ResultSetUtils.union(toReturn, evalResult);
            }
        }
        ResultSetUtils.complement(toReturn, resultSpace);
        return toReturn;
    }

    public static class Builder {

        private List<SearchTabQuery> filters = new ArrayList<>();

        private final Set<OWLEntity> allEntities = new HashSet<>();

        public Builder(SearchContext searchContext) {
            for (OWLOntology ontology : searchContext.getOntologies()) {
                allEntities.addAll(ontology.getSignature());
            }
        }

        public Builder add(SearchTabQuery filter) {
            filters.add(filter);
            return this;
        }

        public NegatedQuery build(boolean isMatchAll) {
            return new NegatedQuery(filters, allEntities, isMatchAll);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + NegatedQuery.class.getSimpleName().hashCode();
        result = prime * result + filters.hashCode();
        result = prime * result + resultSpace.hashCode();
        result = prime * result + (isMatchAll ? 1 : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof NegatedQuery)) {
            return false;
        }
        NegatedQuery other = (NegatedQuery) obj;
        return this.filters.equals(other.filters) && this.resultSpace.equals(other.resultSpace) && this.isMatchAll == other.isMatchAll;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("NOT(");
        sb.append("Filters:").append(filters);
        sb.append(")");
        return sb.toString();
    }
}
