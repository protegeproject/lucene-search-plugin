package org.protege.editor.search.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriterConfig;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.OWLObjectVisitorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

/**
 * Author: Josef Hardi <josef.hardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 04/11/2015
 */
public abstract class AbstractLuceneIndexer extends OWLObjectVisitorAdapter {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractLuceneIndexer.class);

    private final Analyzer DEFAULT_ANALYZER = new StandardAnalyzer();

    private final Analyzer textAnalyzer;

    public AbstractLuceneIndexer() {
        textAnalyzer = DEFAULT_ANALYZER;
    }

    public AbstractLuceneIndexer(Analyzer analyzer) {
        textAnalyzer = analyzer;
    }

    public IndexWriterConfig getIndexWriterConfig() {
        return new IndexWriterConfig(textAnalyzer);
    }

    public Analyzer getTextAnalyzer() {
        return textAnalyzer;
    }

    public abstract IndexItemsCollector getIndexItemsCollector();

    public void doIndex(IndexDelegator delegator, SearchContext context, IndexProgressListener listener) throws IOException {
        IndexItemsCollector collector = getIndexItemsCollector();
        for (OWLOntology ontology : context.getOntologies()) {
            logger.info("... collecting items to index from {}", ontology.getOntologyID().getDefaultDocumentIRI().get());
            ontology.accept(collector);
        }
        Set<Document> documents = collector.getIndexDocuments();
        logger.info("... found {} items to index", documents.size());
        delegator.buildIndex(documents, listener);
    }

    public void doAppend(IndexDelegator delegator, AddChangeSet changeSet) throws IOException {
        delegator.appendIndex(changeSet);
    }

    public void doRemove(IndexDelegator delegator, RemoveChangeSet changeSet) throws IOException {
        delegator.removeIndex(changeSet);
    }

    public interface IndexProgressListener {
        
        void fireIndexingProgressed(long progress);
    }
}
