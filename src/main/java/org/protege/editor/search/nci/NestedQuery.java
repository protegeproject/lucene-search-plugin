package org.protege.editor.search.nci;

import org.protege.editor.owl.model.search.SearchCategory;
import org.protege.editor.search.lucene.AbstractDocumentHandler;
import org.protege.editor.search.lucene.IndexField;
import org.protege.editor.search.lucene.LuceneSearcher;
import org.protege.editor.search.lucene.LuceneUtils;
import org.protege.editor.search.lucene.QueryEvaluationException;
import org.protege.editor.search.lucene.SearchQuery;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class NestedQuery implements SearchQuery {

    private BooleanQuery fillerQuery;
    private String propertyName;
    private SearchCategory category;

    private LuceneSearcher searcher;

    public NestedQuery(BooleanQuery fillerQuery, String propertyName, LuceneSearcher searcher) {
        this.fillerQuery = fillerQuery;
        this.propertyName = propertyName;
        this.category = SearchCategory.OTHER;
        this.searcher = searcher;
    }

    @Override
    public void evaluate(AbstractDocumentHandler handler) throws QueryEvaluationException {
        Set<String> fillers = evaluateFillerQuery();
        Set<Document> docs = new HashSet<>();
        for (String filler : fillers) {
            BooleanQuery query = createObjectRestrictionQuery(propertyName, filler);
            docs.addAll(evaluateQuery(query));
        }
        docs.stream().forEach((doc) -> handler.handle(category, doc));
    }

    private Set<String> evaluateFillerQuery() throws QueryEvaluationException {
        try {
            Set<String> fillers = new HashSet<>();
            TopDocs hits = searcher.search(fillerQuery);
            int hitNumber = hits.scoreDocs.length;
            for (int i = 1; i <= hitNumber; i++) {
                Document doc = searcher.find(hits.scoreDocs[i-1].doc);
                fillers.add(doc.get(IndexField.ENTITY_IRI));
            }
            return fillers;
        }
        catch (IOException e) {
            throw new QueryEvaluationException(e);
        }
    }

    private BooleanQuery createObjectRestrictionQuery(String propertyName, String filler) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        builder.add(LuceneUtils.createTermQuery(IndexField.OBJECT_PROPERTY_DISPLAY_NAME, propertyName), Occur.MUST);
        builder.add(LuceneUtils.createTermQuery(IndexField.FILLER_IRI, filler), Occur.MUST);
        return builder.build();
    }

    private Set<Document> evaluateQuery(BooleanQuery query) throws QueryEvaluationException {
        try {
            Set<Document> docs = new HashSet<>();
            TopDocs hits = searcher.search(query);
            int hitNumber = hits.scoreDocs.length;
            for (int i = 1; i <= hitNumber; i++) {
                Document doc = searcher.find(hits.scoreDocs[i-1].doc);
                docs.add(doc);
            }
            return docs;
        }
        catch (IOException e) {
            throw new QueryEvaluationException(e);
        }
    }

    @Override
    public int hashCode() {
        return SearchQuery.class.getSimpleName().hashCode() + propertyName.hashCode()
        + fillerQuery.hashCode() + category.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof NestedQuery)) {
            return false;
        }
        NestedQuery other = (NestedQuery) obj;
        return this.propertyName.equals(propertyName)
                && this.fillerQuery.equals(other.fillerQuery)
                && this.category.equals(other.fillerQuery);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(category.name()).append(": ");
        sb.append(propertyName).append(": ").append(fillerQuery);
        return sb.toString();
    }
}
