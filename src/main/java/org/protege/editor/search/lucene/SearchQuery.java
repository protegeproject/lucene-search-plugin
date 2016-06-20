package org.protege.editor.search.lucene;

public interface SearchQuery {

    void evaluate(AbstractDocumentHandler handler) throws QueryEvaluationException;
}
