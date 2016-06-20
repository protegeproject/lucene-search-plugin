package org.protege.editor.search.lucene;

import org.protege.editor.owl.model.search.SearchInterruptionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Author: Josef Hardi <josef.hardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 20/11/2015
 */
public class QueryRunner {

    private static final Logger logger = LoggerFactory.getLogger(QueryRunner.class);

    private AtomicLong mutexId;

    public QueryRunner(AtomicLong mutexId) {
        this.mutexId = mutexId;
    }

    public void execute(Long searchId, SearchQueries searchQueries, AbstractDocumentHandler handler, SearchProgressListener listener)
            throws SearchInterruptionException {
        int counter = 1;
        for (SearchQuery searchQuery : searchQueries) {
            pollingSearchStatus(searchId);
            try {
                execute(searchQuery, handler);
            }
            catch (QueryEvaluationException e) {
                logger.error("Error while executing the search query: {}", e.getMessage());
            }
            listener.fireSearchingProgressed((counter++*100)/searchQueries.size());
        }
    }

    public void execute(SearchQuery searchQuery, AbstractDocumentHandler handler) throws QueryEvaluationException {
        logger.debug("... executing query " + searchQuery);
        searchQuery.evaluate(handler);
    }

    private void pollingSearchStatus(Long searchId) throws SearchInterruptionException {
        if (!isLatestSearch(searchId)) {
            throw new SearchInterruptionException();
        }
    }

    private boolean isLatestSearch(long searchId) {
        return searchId == mutexId.get();
    }

    public interface SearchProgressListener {
        
        void fireSearchingProgressed(int progress);
    }
}
