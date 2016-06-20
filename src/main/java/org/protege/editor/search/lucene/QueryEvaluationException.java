package org.protege.editor.search.lucene;

public class QueryEvaluationException extends Exception {

    private static final long serialVersionUID = 9123317920839652102L;

    public QueryEvaluationException() {
    }

    public QueryEvaluationException(String message) {
        super(message);
    }

    public QueryEvaluationException(Throwable t) {
        super(t);
    }

    public QueryEvaluationException(String message, Throwable t) {
        super(message, t);
    }
}
