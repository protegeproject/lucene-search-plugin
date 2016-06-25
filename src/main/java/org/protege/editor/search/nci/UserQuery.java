package org.protege.editor.search.nci;

import org.protege.editor.owl.model.search.SearchCategory;
import org.protege.editor.search.lucene.BasicSearchQuery;
import org.protege.editor.search.lucene.IndexField;
import org.protege.editor.search.lucene.LuceneSearcher;
import org.protege.editor.search.lucene.LuceneUtils;
import org.protege.editor.search.lucene.SearchContext;
import org.protege.editor.search.lucene.SearchQuery;

import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLProperty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Josef Hardi <johardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 22/06/2016
 */
public class UserQuery implements Iterable<SearchQuery> {

    private List<SearchQuery> queries;
    private boolean isMatchAll = true;

    private UserQuery(List<SearchQuery> queries, boolean isMatchAll) {
        this.queries = queries;
        this.isMatchAll = isMatchAll;
    }

    public static UserQuery createInstance(List<SearchQuery> queries, boolean isMatchAll) {
        return new UserQuery(queries, isMatchAll);
    }

    public boolean isMatchAll() {
        return isMatchAll;
    }

    public boolean isEmpty() {
        return queries.isEmpty();
    }

    public int size() {
        return queries.size();
    }

    @Override
    public Iterator<SearchQuery> iterator() {
        return queries.iterator();
    }

    public static class Builder {

        private final List<SearchQuery> queries = new ArrayList<>();

        private final SearchContext searchContext;
        private final LuceneSearcher searcher;

        private final Set<OWLEntity> allEntities = new HashSet<>();
        private final Set<OWLClass> allClasses = new HashSet<>();

        public Builder(SearchContext searchContext, LuceneSearcher searcher) {
            this.searchContext = searchContext;
            this.searcher = searcher;
        }

        public Builder addBasicQuery(OWLProperty property, QueryType type, String searchString, boolean isNegated) {
            if (QueryType.ValueQueryTypes.contains(type)) {
                queries.add(new BasicSearchQuery(
                        createFilterQuery(property, type, searchString, isNegated),
                        getSearchCategory(property), searcher));
            }
            else if (QueryType.NonValueQueryTypes.contains(type)) {
                if (type.equals(QueryType.PROPERTY_VALUE_PRESENT)) {
                    queries.add(new PropertyValuePresent(createPropertyValueQuery(property), searcher));
                }
                else if (type.equals(QueryType.PROPERTY_VALUE_ABSENT)) {
                    populateAllEntities();
                    queries.add(new PropertyValueAbsent(createPropertyValueQuery(property), allEntities, searcher));
                }
            }
            return this;
        }

        public Builder addNegatedQuery(UserQuery filterQuery) {
            queries.add(new NegatedQuery(filterQuery));
            return this;
        }

        public Builder addNestedQuery(UserQuery fillerFilters, String propertyName) {
            queries.add(new NestedQuery(fillerFilters, propertyName, searcher));
            return this;
        }

        public Builder addPropertyValuePresentQuery(OWLProperty property, QueryType type) {
            populateAllEntities();
            return this;
        }

        public UserQuery build(boolean isMatchAll) {
            return new UserQuery(queries, isMatchAll);
        }

        /*
         * Private builder methods
         */

        private void populateAllEntities() {
            if (allEntities.isEmpty()) {
                for (OWLOntology ontology : searchContext.getOntologies()) {
                    allEntities.addAll(ontology.getSignature());
                }
            }
        }

        private void populateAllClasses() {
            if (allClasses.isEmpty()) {
                for (OWLOntology ontology : searchContext.getOntologies()) {
                    allClasses.addAll(ontology.getClassesInSignature());
                } 
            }
        }

        private static Query createFilterQuery(OWLProperty property, QueryType type, String searchString, boolean isNegated) {
            if (type.equals(QueryType.CONTAINS)) {
                return createContainsQuery(property, searchString, isNegated);
            }
            else if (type.equals(QueryType.STARTS_WITH)) {
                return createStartsWithQuery(property, searchString, isNegated);
            }
            else if (type.equals(QueryType.ENDS_WITH)) {
                return createEndsWithMatchQuery(property, searchString, isNegated);
            }
            else if (type.equals(QueryType.EXACT_MATCH)) {
                return createExactMatchQuery(property, searchString, isNegated);
            }
            throw new IllegalArgumentException("Unsupported filter query: " + type);
        }

        private static Query createPropertyValueQuery(OWLProperty property, QueryType type) {
            
            throw new IllegalArgumentException("Unsupported filter query: " + type);
        }

        private static BooleanQuery createContainsQuery(OWLProperty property, String searchString, boolean isNegated) {
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            builder.add(LuceneUtils.createTermQuery(IndexField.ANNOTATION_IRI, property.getIRI().toString()), Occur.MUST);
            if (isNegated) {
                builder.add(LuceneUtils.createTermQuery(IndexField.ANNOTATION_TEXT, searchString), Occur.MUST_NOT);
            } else {
                builder.add(LuceneUtils.createTermQuery(IndexField.ANNOTATION_TEXT, searchString), Occur.MUST);
            }
            return builder.build();
        }

        private static BooleanQuery createStartsWithQuery(OWLProperty property, String searchString, boolean isNegated) {
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            builder.add(LuceneUtils.createTermQuery(IndexField.ANNOTATION_IRI, property.getIRI().toString()), Occur.MUST);
            if (isNegated) {
                builder.add(LuceneUtils.createPrefixQuery(IndexField.ANNOTATION_TEXT, searchString), Occur.MUST_NOT);
            } else {
                builder.add(LuceneUtils.createPrefixQuery(IndexField.ANNOTATION_TEXT, searchString), Occur.MUST);
            }
            return builder.build();
        }

        private static BooleanQuery createEndsWithMatchQuery(OWLProperty property, String searchString, boolean isNegated) {
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            builder.add(LuceneUtils.createTermQuery(IndexField.ANNOTATION_IRI, property.getIRI().toString()), Occur.MUST);
            if (isNegated) {
                builder.add(LuceneUtils.createSuffixQuery(IndexField.ANNOTATION_TEXT, searchString), Occur.MUST_NOT);
            } else {
                builder.add(LuceneUtils.createSuffixQuery(IndexField.ANNOTATION_TEXT, searchString), Occur.MUST);
            }
            return builder.build();
        }

        private static BooleanQuery createExactMatchQuery(OWLProperty property, String searchString, boolean isNegated) {
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            builder.add(LuceneUtils.createTermQuery(IndexField.ANNOTATION_IRI, property.getIRI().toString()), Occur.MUST);
            if (isNegated) {
                builder.add(LuceneUtils.createPhraseQuery(IndexField.ANNOTATION_TEXT, searchString), Occur.MUST_NOT);
            } else {
                builder.add(LuceneUtils.createPhraseQuery(IndexField.ANNOTATION_TEXT, searchString), Occur.MUST);
            }
            return builder.build();
        }

        private static Query createPropertyValueQuery(OWLProperty property) {
            return LuceneUtils.createTermQuery(IndexField.ANNOTATION_IRI, property.getIRI().toString());
        }

        private static SearchCategory getSearchCategory(OWLProperty property) {
            if (property instanceof OWLDataProperty || property instanceof OWLObjectProperty) {
                return SearchCategory.LOGICAL_AXIOM;
            }
            else { // OWLAnnotationProperty
                return SearchCategory.ANNOTATION_VALUE;
            }
        }
    }
}