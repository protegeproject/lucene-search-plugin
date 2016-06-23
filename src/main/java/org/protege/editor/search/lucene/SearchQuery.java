package org.protege.editor.search.lucene;

public interface SearchQuery {

    void evaluate(AbstractDocumentHandler handler) throws QueryEvaluationException;

    void evaluate(AbstractDocumentHandler handler, SearchProgressListener listener) throws QueryEvaluationException;

    public interface SearchProgressListener {
        
        void fireSearchingProgressed(long progress);
    }
}
