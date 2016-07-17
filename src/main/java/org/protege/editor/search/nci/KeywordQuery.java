package org.protege.editor.search.nci;

import org.protege.editor.search.lucene.LuceneSearcher;
import org.protege.editor.search.lucene.QueryEvaluationException;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Josef Hardi <johardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 28/06/2016
 */
public class KeywordQuery extends BasicQuery {

    private final Query luceneQuery;
    private final LuceneSearcher searcher;

    private final String algebraString;

    public KeywordQuery(Query luceneQuery, LuceneSearcher searcher) {
        this(luceneQuery, searcher, luceneQuery.toString());
    }

    public KeywordQuery(Query luceneQuery, LuceneSearcher searcher, String algebraString) {
        this.luceneQuery = luceneQuery;
        this.searcher = searcher;
        this.algebraString = algebraString;
    }

    @Override
    public Query getLuceneQuery() {
        return luceneQuery;
    }

    @Override
    public LuceneSearcher getSearcher() {
        return searcher;
    }

    @Override
    public String getAlgebraString() {
        return algebraString;
    }

    @Override
    public Set<OWLEntity> evaluate(SearchProgressListener listener, AtomicBoolean stopSearch) throws QueryEvaluationException {
        SearchDocumentHandler handler = new SearchDocumentHandler(searcher.getEditorKit());
        Set<Document> docs = evaluate();
        int counter = 0;
        for (Document doc : docs) {
            if (stopSearch.get()) { // if should stop
                return handler.getSearchResults();
            }
            handler.handle(doc);
            if (listener != null) {
                listener.fireSearchingProgressed((counter++*100)/docs.size());
            }
        }
        return handler.getSearchResults();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + KeywordQuery.class.getSimpleName().hashCode();
        result = prime * result + luceneQuery.hashCode();
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
        if (!(obj instanceof KeywordQuery)) {
            return false;
        }
        KeywordQuery other = (KeywordQuery) obj;
        return this.luceneQuery.equals(other.luceneQuery);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Query: ").append(luceneQuery);
        return sb.toString();
    }
}
