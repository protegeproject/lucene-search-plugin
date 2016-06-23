package org.protege.editor.search.lucene;

import org.protege.editor.owl.model.search.SearchCategory;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class BasicSearchQuery implements SearchQuery {

    private Query query;
    private SearchCategory category;

    private LuceneSearcher searcher;

    public BasicSearchQuery(Query query, SearchCategory category, LuceneSearcher searcher) {
        this.query = query;
        this.category = category;
        this.searcher = searcher;
    }

    public Query getQuery() {
        return query;
    }

    public SearchCategory getCategory() {
        return category;
    }

    public Set<Document> evaluate() throws QueryEvaluationException {
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
    public void evaluate(AbstractDocumentHandler handler) throws QueryEvaluationException {
        Set<Document> docs = evaluate();
        docs.stream().forEach((doc) -> handler.handle(category, doc));
    }

    @Override
    public void evaluate(AbstractDocumentHandler handler, SearchProgressListener listener) throws QueryEvaluationException {
        Set<Document> docs = evaluate();
        int counter = 0;
        for (Document doc : docs) {
            handler.handle(category, doc);
            listener.fireSearchingProgressed((counter++*100)/docs.size());
        }
    }

    @Override
    public int hashCode() {
        return SearchQuery.class.getSimpleName().hashCode() + query.hashCode() + category.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BasicSearchQuery)) {
            return false;
        }
        BasicSearchQuery other = (BasicSearchQuery) obj;
        return this.query.equals(other.query) && this.category.equals(other.category);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(category.name()).append(": ").append(query);
        return sb.toString();
    }
}
