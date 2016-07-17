package org.protege.editor.search.nci;

import org.protege.editor.search.lucene.IndexField;
import org.protege.editor.search.lucene.LuceneSearcher;
import org.protege.editor.search.lucene.LuceneUtils;
import org.protege.editor.search.lucene.QueryEvaluationException;

import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Josef Hardi <johardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 27/06/2016
 */
public class NestedQuery extends ComplexQuery {

    private final List<SearchTabQuery> fillerFilters;
    private final OWLProperty property;
    private final boolean isMatchAll;

    private final LuceneSearcher searcher;

    // Not allowing external instantiation
    private NestedQuery(List<SearchTabQuery> fillerFilters, OWLProperty property, boolean isMatchAll, LuceneSearcher searcher) {
        this.fillerFilters = fillerFilters;
        this.property = property;
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
        sb.append("(").append(localName(property.getIRI()));
        sb.append("\n");
        boolean needOperator = false;
        for (SearchTabQuery filter : fillerFilters) {
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

    private static String localName(IRI propertyIri) {
        String s = propertyIri.toString();
        return (s.lastIndexOf('#') != 0)
                ? s.substring(s.lastIndexOf('#'))
                : s.substring(s.lastIndexOf('/'));
    }

    @Override
    public Set<OWLEntity> evaluate(SearchProgressListener listener, AtomicBoolean stopSearch) throws QueryEvaluationException {
        Set<OWLEntity> toReturn = new HashSet<>();
        Set<OWLEntity> fillers = evaluateFillerQuery(listener, stopSearch);
        for (OWLEntity filler : fillers) {
            if (filler instanceof OWLClass) {
                if (stopSearch.get()) { // if should stop
                    return toReturn;
                }
                if (property instanceof OWLObjectProperty) {
                    Query luceneQuery = createObjectRestrictionQuery(property.getIRI().toString(), filler.getIRI().toString());
                    KeywordQuery query = new KeywordQuery(luceneQuery, searcher);
                    toReturn.addAll(query.evaluate(listener, stopSearch));
                }
                else if (property instanceof OWLAnnotationProperty) {
                    Query luceneQuery = createAnnotationRestrictionQuery(property.getIRI().toString(), filler.getIRI().toString());
                    KeywordQuery query = new KeywordQuery(luceneQuery, searcher);
                    toReturn.addAll(query.evaluate(listener, stopSearch));
                }
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

        public NestedQuery build(OWLProperty property, boolean isMatchAll) {
            return new NestedQuery(fillerFilters, property, isMatchAll, searcher);
        }
    }

    private Set<OWLEntity> evaluateFillerQuery(SearchProgressListener listener, AtomicBoolean stopSearch) throws QueryEvaluationException {
        Set<OWLEntity> toReturn = new HashSet<>();
        for (SearchTabQuery filter : fillerFilters) {
            if (stopSearch.get()) { // if should stop
                return toReturn;
            }
            Set<OWLEntity> evalResult = filter.evaluate(listener, stopSearch);
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

    private BooleanQuery createAnnotationRestrictionQuery(String propertyIri, String annotationValueIri) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        builder.add(LuceneUtils.createTermQuery(IndexField.ANNOTATION_IRI, propertyIri), Occur.MUST);
        builder.add(LuceneUtils.createTermQuery(IndexField.ANNOTATION_VALUE_IRI, annotationValueIri), Occur.MUST);
        return builder.build();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + NestedQuery.class.getSimpleName().hashCode();
        result = prime * result + fillerFilters.hashCode();
        result = prime * result + property.hashCode();
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
        return this.property.equals(other.property) && this.fillerFilters.equals(other.fillerFilters) && this.isMatchAll == other.isMatchAll;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("propertyIri: ").append(property.getIRI()).append(",\n");
        sb.append("fillerFilters: ").append(fillerFilters);
        return sb.toString();
    }
}
