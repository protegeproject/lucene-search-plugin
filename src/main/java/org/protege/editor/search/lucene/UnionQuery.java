package org.protege.editor.search.lucene;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Josef Hardi <johardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 22/06/2016
 */
public class UnionQuery implements SearchQuery {

    private Set<SearchQuery> queries;

    private UnionQuery(Set<SearchQuery> queries) {
        this.queries = queries;
    }

    public boolean isEmpty() {
        return queries.isEmpty();
    }

    public int size() {
        return queries.size();
    }

    @Override
    public void evaluate(AbstractDocumentHandler handler) throws QueryEvaluationException {
        for (SearchQuery query : queries) {
            query.evaluate(handler);
        }
    }

    @Override
    public void evaluate(AbstractDocumentHandler handler, SearchProgressListener listener) throws QueryEvaluationException {
        for (SearchQuery query : queries) {
            query.evaluate(handler, listener);
        }
    }

    public static class Builder {

        private final Set<SearchQuery> queries = new HashSet<>();

        public Builder() {
            // NO-OP
        }

        public Builder add(SearchQuery query) {
            queries.add(query);
            return this;
        }

        public UnionQuery build() {
            return new UnionQuery(queries);
        }
    }
}
