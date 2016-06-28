package org.protege.editor.search.nci;

import org.protege.editor.search.lucene.LuceneSearcher;
import org.protege.editor.search.lucene.QueryEvaluationException;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Josef Hardi <johardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 27/06/2016
 */
public class PropertyValueAbsent extends BasicQuery {

    private final Query luceneQuery;
    private final Set<OWLEntity> resultSpace;

    private final LuceneSearcher searcher;

    public PropertyValueAbsent(Query luceneQuery, Set<OWLEntity> resultSpace, LuceneSearcher searcher) {
        this.luceneQuery = luceneQuery;
        this.resultSpace = resultSpace;
        this.searcher = searcher;
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
    public Set<OWLEntity> evaluate(SearchProgressListener listener) throws QueryEvaluationException {
        NciResultDocumentHandler handler = new NciResultDocumentHandler(searcher.getEditorKit());
        Set<Document> docs = evaluate();
        int counter = 0;
        for (Document doc : docs) {
            handler.handle(doc);
            if (listener != null) {
                listener.fireSearchingProgressed((counter++*100)/docs.size());
            }
        }
        /*
         * Compute the final results using difference operation A - B
         */
        Set<OWLEntity> producedResults = handler.getSearchResults();
        Set<OWLEntity> finalResults = new HashSet<>(resultSpace);
        ResultSetUtils.difference(finalResults, producedResults);
        return finalResults;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + PropertyValueAbsent.class.getSimpleName().hashCode();
        result = prime * result + luceneQuery.hashCode();
        result = prime * result + resultSpace.hashCode();
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
        if (!(obj instanceof PropertyValueAbsent)) {
            return false;
        }
        PropertyValueAbsent other = (PropertyValueAbsent) obj;
        return this.luceneQuery.equals(other.luceneQuery) && this.resultSpace.equals(other.resultSpace);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Query: ").append(luceneQuery);
        return sb.toString();
    }
}
