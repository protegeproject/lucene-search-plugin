package org.protege.editor.search.nci;

import org.protege.editor.owl.model.search.SearchCategory;
import org.protege.editor.search.lucene.AbstractDocumentHandler;
import org.protege.editor.search.lucene.LuceneSearcher;
import org.protege.editor.search.lucene.QueryEvaluationException;
import org.protege.editor.search.lucene.SearchQuery;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
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
public class PropertyValuePresent implements SearchQuery {

    private final Query query;
    private final SearchCategory category;
    private final LuceneSearcher searcher;

    public PropertyValuePresent(Query query, LuceneSearcher searcher) {
        this.query = query;
        this.category = SearchCategory.OTHER;
        this.searcher = searcher;
    }

    @Override
    public void evaluate(AbstractDocumentHandler handler) throws QueryEvaluationException {
        evaluate(handler, null);
    }

    @Override
    public void evaluate(AbstractDocumentHandler handler, SearchProgressListener listener) throws QueryEvaluationException {
        Set<Document> docs = evaluate();
        int counter = 0;
        for (Document doc : docs) {
            handler.handle(category, doc);
            if (listener != null) {
                listener.fireSearchingProgressed((counter++*100)/docs.size());
            }
        }
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
}
