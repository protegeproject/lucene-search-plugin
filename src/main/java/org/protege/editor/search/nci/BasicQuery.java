package org.protege.editor.search.nci;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.protege.editor.search.lucene.IndexField;
import org.protege.editor.search.lucene.LuceneSearcher;
import org.protege.editor.search.lucene.LuceneUtils;
import org.protege.editor.search.lucene.QueryEvaluationException;
import org.protege.editor.search.lucene.SearchContext;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLProperty;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author Josef Hardi <johardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 28/06/2016
 */
public abstract class BasicQuery implements SearchTabQuery {

    public abstract Query getLuceneQuery();

    public abstract LuceneSearcher getSearcher();

    protected Set<Document> evaluate() throws QueryEvaluationException {
        try {
            Set<Document> docs = new HashSet<>();
            TopDocs hits = getSearcher().search(getLuceneQuery());
            int hitNumber = hits.scoreDocs.length;
            for (int i = 1; i <= hitNumber; i++) {
                Document doc = getSearcher().find(hits.scoreDocs[i-1].doc);
                docs.add(doc);
            }
            return docs;
        }
        catch (IOException e) {
            throw new QueryEvaluationException(e);
        }
    }

    public static class Factory {

        private final SearchContext searchContext;
        private final LuceneSearcher searcher;

        private final Set<OWLEntity> allEntities = new HashSet<>();
        private final Set<OWLEntity> allClasses = new HashSet<>();

        public Factory(SearchContext searchContext, LuceneSearcher searcher) {
            this.searchContext = searchContext;
            this.searcher = searcher;
        }

        public BasicQuery createQuery(OWLProperty property, QueryType type, String searchString) {
            if (QueryType.ValueQueryTypes.contains(type)) {
                if (type.equals(QueryType.CONTAINS)) {
                    return createContainsFilter(property, searchString);
                }
                else if (type.equals(QueryType.STARTS_WITH)) {
                    return createStartsWithFilter(property, searchString);
                }
                else if (type.equals(QueryType.ENDS_WITH)) {
                    return createEndsWithFilter(property, searchString);
                }
                else if (type.equals(QueryType.EXACT_MATCH)) {
                    return createExactMatchFilter(property, searchString);
                }
            }
            else if (QueryType.NonValueQueryTypes.contains(type)) {
                if (type.equals(QueryType.PROPERTY_VALUE_PRESENT)) {
                    return createProperyValuePresentFilter(property);
                }
                else if (type.equals(QueryType.PROPERTY_VALUE_ABSENT)) {
                    return createPropertyValueAbsentFilter(property);
                }
                else if (type.equals(QueryType.PROPERTY_RESTRICTION_PRESENT)) {
                    return createPropertyRestrictionPresentFilter(property);
                }
                else if (type.equals(QueryType.PROPERTY_RESTRICTION_ABSENT)) {
                    return createPropertyRestrictionAbsentFilter(property);
                }
            }
            throw new IllegalArgumentException("Unsupported query type: " + type);
        }

        public KeywordQuery createContainsFilter(OWLProperty property, String searchString) {
            return new KeywordQuery(createContainsQuery(property, searchString), searcher,
                    String.format("%s contains %s", getDisplayName(property), searchString));
        }

        public KeywordQuery createStartsWithFilter(OWLProperty property, String searchString) {
            return new KeywordQuery(createStartsWithQuery(property, searchString), searcher,
                    String.format("%s starts with %s", getDisplayName(property), searchString));
        }

        public KeywordQuery createEndsWithFilter(OWLProperty property, String searchString) {
            return new KeywordQuery(createEndsWithQuery(property, searchString), searcher,
                    String.format("%s ends with %s", getDisplayName(property), searchString));
        }

        public KeywordQuery createExactMatchFilter(OWLProperty property, String searchString) {
            return new KeywordQuery(createExactMatchQuery(property, searchString), searcher,
                    String.format("%s exact match %s", getDisplayName(property), searchString));
        }

        public PropertyValuePresent createProperyValuePresentFilter(OWLProperty property) {
            return new PropertyValuePresent(createPropertyValueQuery(property), searcher,
                    String.format("PropertyPresent(%s)", getDisplayName(property)));
        }

        public PropertyValueAbsent createPropertyValueAbsentFilter(OWLProperty property) {
            populateAllEntities();
            return new PropertyValueAbsent(createPropertyValueQuery(property), allEntities, searcher,
                    String.format("PropertyAbsent(%s)", getDisplayName(property)));
        }

        public PropertyRestrictionPresent createPropertyRestrictionPresentFilter(OWLProperty property) {
            return new PropertyRestrictionPresent(createPropertyRestrictionQuery(property), searcher,
                    String.format("PropertyRestrictionPresent(%s)", getDisplayName(property)));
        }

        public PropertyRestrictionAbsent createPropertyRestrictionAbsentFilter(OWLProperty property) {
            populateAllClasses();
            return new PropertyRestrictionAbsent(createPropertyRestrictionQuery(property), allClasses, searcher,
                    String.format("PropertyRestrictionAbsent(%s)", getDisplayName(property)));
        }

        /*
         * Private builder methods
         */

        private String getDisplayName(OWLEntity entity) {
            return searcher.getEditorKit().getOWLModelManager().getRendering(entity);
        }

        private static BooleanQuery createContainsQuery(OWLProperty property, String searchString) {
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            builder.add(LuceneUtils.createTermQuery(IndexField.ANNOTATION_IRI, property.getIRI().toString()), Occur.MUST);
            builder.add(LuceneUtils.createLikeQuery(IndexField.ANNOTATION_TEXT, searchString), Occur.MUST);
            return builder.build();
        }

        private static BooleanQuery createStartsWithQuery(OWLProperty property, String searchString) {
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            builder.add(LuceneUtils.createTermQuery(IndexField.ANNOTATION_IRI, property.getIRI().toString()), Occur.MUST);
            builder.add(LuceneUtils.createPrefixQuery(IndexField.ANNOTATION_TEXT, searchString), Occur.MUST);
            return builder.build();
        }

        private static BooleanQuery createEndsWithQuery(OWLProperty property, String searchString) {
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            builder.add(LuceneUtils.createTermQuery(IndexField.ANNOTATION_IRI, property.getIRI().toString()), Occur.MUST);
            builder.add(LuceneUtils.createSuffixQuery(IndexField.ANNOTATION_TEXT, searchString), Occur.MUST);
            return builder.build();
        }

        private static BooleanQuery createExactMatchQuery(OWLProperty property, String searchString) {
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            builder.add(LuceneUtils.createTermQuery(IndexField.ANNOTATION_IRI, property.getIRI().toString()), Occur.MUST);
            builder.add(LuceneUtils.createPhraseQuery(IndexField.ANNOTATION_TEXT, searchString), Occur.MUST);
            return builder.build();
        }

        private static Query createPropertyValueQuery(OWLProperty property) {
            return LuceneUtils.createTermQuery(IndexField.ANNOTATION_IRI, property.getIRI().toString());
        }

        private static Query createPropertyRestrictionQuery(OWLProperty property) {
            return LuceneUtils.createTermQuery(IndexField.OBJECT_PROPERTY_IRI, property.getIRI().toString());
        }

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
    }

    protected class SearchDocumentHandler {

        private OWLEntityFinder entityFinder;
        private Set<OWLEntity> results = new HashSet<>();

        public SearchDocumentHandler(OWLEditorKit editorKit) {
            entityFinder = editorKit.getOWLModelManager().getOWLEntityFinder();
        }

        public void handle(Document doc) {
            String subjectIri = doc.get(IndexField.ENTITY_IRI);
            Optional<OWLEntity> entity = entityFinder.getEntities(IRI.create(subjectIri)).stream().findFirst();
            if (entity.isPresent()) {
                results.add(entity.get());
            }
        }

        public Set<OWLEntity> getSearchResults() {
            return results;
        }
    }
}
