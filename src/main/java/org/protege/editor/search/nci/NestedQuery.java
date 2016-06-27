package org.protege.editor.search.nci;

import org.protege.editor.owl.model.search.SearchCategory;
import org.protege.editor.owl.model.search.SearchResult;
import org.protege.editor.search.lucene.AbstractDocumentHandler;
import org.protege.editor.search.lucene.IndexField;
import org.protege.editor.search.lucene.LuceneSearcher;
import org.protege.editor.search.lucene.LuceneUtils;
import org.protege.editor.search.lucene.QueryEvaluationException;
import org.protege.editor.search.lucene.ResultDocumentHandler;
import org.protege.editor.search.lucene.SearchQuery;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Josef Hardi <johardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 27/06/2016
 */
public class NestedQuery implements SearchQuery {

    private UserQuery fillerQuery;
    private String propertyName;
    private SearchCategory category;

    private LuceneSearcher searcher;

    public NestedQuery(UserQuery fillerQuery, String propertyName, LuceneSearcher searcher) {
        this.fillerQuery = fillerQuery;
        this.propertyName = propertyName;
        this.category = SearchCategory.OTHER;
        this.searcher = searcher;
    }

    @Override
    public void evaluate(AbstractDocumentHandler handler) throws QueryEvaluationException {
        evaluate(handler, null);
    }

    @Override
    public void evaluate(AbstractDocumentHandler handler, SearchProgressListener listener) throws QueryEvaluationException {
        Set<String> fillers = evaluateFillerQuery();
        Set<Document> docs = new HashSet<>();
        for (String fillerName : fillers) {
            BooleanQuery query = createObjectRestrictionQuery(propertyName, fillerName);
            docs.addAll(evaluateQuery(query));
        }
        int counter = 0;
        for (Document doc : docs) {
            handler.handle(category, doc);
            if (listener != null) {
                listener.fireSearchingProgressed((counter++*100)/docs.size());
            }
        }
    }

    private Set<String> evaluateFillerQuery() throws QueryEvaluationException {
        Set<String> fillers = new HashSet<>();
        ResultDocumentHandler handler = new ResultDocumentHandler(searcher.getEditorKit());
        for (SearchQuery query : fillerQuery) {
            query.evaluate(handler);
        }
        Set<SearchResult> results = handler.getSearchResults();
        results.stream().forEach(result -> fillers.add(result.getSubjectRendering()));
        return fillers;
    }

    private static BooleanQuery createObjectRestrictionQuery(String propertyName, String filler) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        builder.add(LuceneUtils.createTermQuery(IndexField.OBJECT_PROPERTY_DISPLAY_NAME, propertyName), Occur.MUST);
        builder.add(LuceneUtils.createTermQuery(IndexField.FILLER_DISPLAY_NAME, filler), Occur.MUST);
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
        return NestedQuery.class.getSimpleName().hashCode() + propertyName.hashCode() + fillerQuery.hashCode()
                + category.hashCode();
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
                && this.category.equals(other.category);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("propertyName").append(": ").append(propertyName).append(",\n");
        sb.append("fillerQuery").append(": ").append(fillerQuery);
        return sb.toString();
    }
}
