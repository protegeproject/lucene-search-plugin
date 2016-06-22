package org.protege.editor.search.nci;

import org.protege.editor.owl.model.search.SearchCategory;
import org.protege.editor.search.lucene.BasicSearchQuery;
import org.protege.editor.search.lucene.IndexField;
import org.protege.editor.search.lucene.LuceneSearcher;
import org.protege.editor.search.lucene.SearchQueries;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLProperty;

/**
 * @author Josef Hardi <johardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 22/06/2016
 */
public class UserQuery extends SearchQueries {

    private boolean isMatchAll = true;

    public boolean isMatchAll() {
        return isMatchAll;
    }

    public void setMatchAll(boolean isMatchAll) {
        this.isMatchAll = isMatchAll;
    }

    public static class Builder {

        private UserQuery userQuery = new UserQuery();

        private final LuceneSearcher searcher;

        public Builder(LuceneSearcher searcher) {
            this.searcher = searcher;
        }

        public Builder addBasicQuery(OWLProperty property, String searchString, QueryType type) {
            userQuery.add(new BasicSearchQuery(
                    createFilterQuery(property, searchString, type),
                    getSearchCategory(property), searcher));
            return this;
        }

        public Builder addNestedQuery(UserQuery fillerFilters, String propertyName) {
            userQuery.add(new NestedQuery(fillerFilters, propertyName, searcher));
            return this;
        }

        public UserQuery build(boolean isMatchAll) {
            userQuery.setMatchAll(isMatchAll);
            return userQuery;
        }

        /*
         * Private builder methods
         */

        private static Query createFilterQuery(OWLProperty property, String searchString, QueryType type) {
            if (type.equals(QueryType.CONTAINS)) {
                return createContainsQuery(property, searchString);
            }
            else if (type.equals(QueryType.STARTS_WITH)) {
                return createStartsWithQuery(property, searchString);
            }
            else if (type.equals(QueryType.ENDS_WITH)) {
                return createEndsWithMatchQuery(property, searchString);
            }
            else { // QueryType.EXACT_MATCH
                return createExactMatchQuery(property, searchString);
            }
        }

        private static BooleanQuery createContainsQuery(OWLProperty property, String searchString) {
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            builder.add(new TermQuery(new Term(getPropertyIriIndexField(property), property.getIRI().toString())), Occur.MUST);
            builder.add(new TermQuery(new Term(getPropertyValueIndexField(property), searchString)), Occur.MUST);
            return builder.build();
        }

        private static BooleanQuery createStartsWithQuery(OWLProperty property, String searchString) {
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            builder.add(new TermQuery(new Term(getPropertyIriIndexField(property), property.getIRI().toString())), Occur.MUST);
            builder.add(new PrefixQuery(new Term(getPropertyValueIndexField(property), searchString + "*")), Occur.MUST);
            return builder.build();
        }

        private static BooleanQuery createEndsWithMatchQuery(OWLProperty property, String searchString) {
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            builder.add(new TermQuery(new Term(getPropertyIriIndexField(property), property.getIRI().toString())), Occur.MUST);
            builder.add(new WildcardQuery(new Term(getPropertyValueIndexField(property), "*" + searchString)), Occur.MUST);
            return builder.build();
        }

        private static BooleanQuery createExactMatchQuery(OWLProperty property, String searchString) {
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            builder.add(new TermQuery(new Term(getPropertyIriIndexField(property), property.getIRI().toString())), Occur.MUST);
            builder.add(new PhraseQuery(getPropertyValueIndexField(property), searchString.split("\\s+")), Occur.MUST);
            return builder.build();
        }

        private static String getPropertyIriIndexField(OWLProperty property) {
            if (property instanceof OWLDataProperty) {
                return IndexField.DATA_PROPERTY_IRI;
            }
            else if (property instanceof OWLObjectProperty) {
                return IndexField.OBJECT_PROPERTY_IRI;
            }
            else { // OWLAnnotationProperty
                return IndexField.ANNOTATION_IRI;
            }
        }

        private static String getPropertyValueIndexField(OWLProperty property) {
            if (property instanceof OWLDataProperty || property instanceof OWLObjectProperty) {
                return IndexField.FILLER_DISPLAY_NAME;
            }
            else { // OWLAnnotationProperty
                return IndexField.ANNOTATION_TEXT;
            }
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
