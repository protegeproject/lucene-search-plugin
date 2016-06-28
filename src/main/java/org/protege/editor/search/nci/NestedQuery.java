package org.protege.editor.search.nci;

import org.protege.editor.search.lucene.IndexField;
import org.protege.editor.search.lucene.LuceneSearcher;
import org.protege.editor.search.lucene.LuceneUtils;
import org.protege.editor.search.lucene.QueryEvaluationException;

import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;

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
public class NestedQuery extends ComplexQuery {

    private final List<SearchTabQuery> fillerFilters;
    private final String propertyIri;
    private final boolean isMatchAll;

    private final LuceneSearcher searcher;

    // Not allowing external instantiation
    private NestedQuery(List<SearchTabQuery> fillerFilters, IRI propertyIri, boolean isMatchAll, LuceneSearcher searcher) {
        this.fillerFilters = fillerFilters;
        this.propertyIri = propertyIri.toString();
        this.isMatchAll = isMatchAll;
        this.searcher = searcher;
    }

    @Override
    public boolean isMatchAll() {
        return isMatchAll;
    }

    public List<SearchTabQuery> getFillerFilters() {
        return Collections.unmodifiableList(fillerFilters);
    }

    @Override
    public String getAlgebraString() {
        String booleanOperator = isMatchAll ? "AND" : "OR";
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(localName(propertyIri));
        sb.append("\n");
        boolean needOperator = false;
        for (SearchTabQuery filter : fillerFilters) {
            if (needOperator) {
                sb.append("   ");
                sb.append(booleanOperator).append(" ");
            }
            sb.append(filter.getAlgebraString());
            sb.append("\n");
            needOperator = true;
        }
        sb.append(")");
        return sb.toString();
    }

    private static String localName(String propertyIri) {
        return (propertyIri.lastIndexOf('#') != 0)
                ? propertyIri.substring(propertyIri.lastIndexOf('#'))
                : propertyIri.substring(propertyIri.lastIndexOf('/'));
    }

    @Override
    public Set<OWLEntity> evaluate(SearchProgressListener listener) throws QueryEvaluationException {
        Set<OWLEntity> toReturn = new HashSet<>();
        Set<OWLEntity> fillers = evaluateFillerQuery(listener);
        for (OWLEntity filler : fillers) {
            if (filler instanceof OWLClass) {
                Query luceneQuery = createObjectRestrictionQuery(propertyIri, filler.getIRI().toString());
                KeywordQuery query = new KeywordQuery(luceneQuery, searcher);
                toReturn.addAll(query.evaluate(listener));
            }
        }
        return toReturn;
    }

    public static class Builder {

        private LuceneSearcher searcher;

        private List<SearchTabQuery> fillerFilters = new ArrayList<>();

        public Builder(LuceneSearcher searcher) {
            this.searcher = searcher;
        }

        public Builder add(SearchTabQuery fillerFilter) {
            fillerFilters.add(fillerFilter);
            return this;
        }

        public NestedQuery build(IRI propertyIri, boolean isMatchAll) {
            return new NestedQuery(fillerFilters, propertyIri, isMatchAll, searcher);
        }
    }

    private Set<OWLEntity> evaluateFillerQuery(SearchProgressListener listener) throws QueryEvaluationException {
        Set<OWLEntity> toReturn = new HashSet<>();
        for (SearchTabQuery filter : fillerFilters) {
            Set<OWLEntity> evalResult = filter.evaluate(listener);
            if (isMatchAll) {
                ResultSetUtils.intersect(toReturn, evalResult);
            }
            else { // match any
                ResultSetUtils.union(toReturn, evalResult);
            }
        }
        return toReturn;
    }

    private BooleanQuery createObjectRestrictionQuery(String propertyIri, String fillerIri) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        builder.add(LuceneUtils.createTermQuery(IndexField.OBJECT_PROPERTY_IRI, propertyIri), Occur.MUST);
        builder.add(LuceneUtils.createTermQuery(IndexField.FILLER_IRI, fillerIri), Occur.MUST);
        return builder.build();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + NestedQuery.class.getSimpleName().hashCode();
        result = prime * result + fillerFilters.hashCode();
        result = prime * result + propertyIri.hashCode();
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
        if (!(obj instanceof NestedQuery)) {
            return false;
        }
        NestedQuery other = (NestedQuery) obj;
        return this.propertyIri.equals(other.propertyIri) && this.fillerFilters.equals(other.fillerFilters) && this.isMatchAll == other.isMatchAll;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("propertyIri: ").append(propertyIri).append(",\n");
        sb.append("fillerFilters: ").append(fillerFilters);
        return sb.toString();
    }
}
