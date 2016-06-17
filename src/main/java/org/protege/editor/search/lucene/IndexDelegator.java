package org.protege.editor.search.lucene;

import org.protege.editor.core.Disposable;
import org.protege.editor.search.lucene.AbstractLuceneIndexer.IndexProgressListener;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

/**
 * Author: Josef Hardi <josef.hardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 29/01/2016
 */
public class IndexDelegator implements Disposable {

    private static final Logger logger = LoggerFactory.getLogger(IndexDelegator.class);

    private final Directory directory;

    private final IndexWriter indexWriter;

    private IndexSearcher indexSearcher;

    // Prevent external instantiation
    private IndexDelegator(IndexWriter writer, Directory directory) {
        this.indexWriter = writer;
        this.directory = directory;
    }

    public static IndexDelegator create(Directory directory, IndexWriterConfig writerConfig) throws IOException {
        IndexWriter writer = new IndexWriter(directory, writerConfig);
        return new IndexDelegator(writer, directory);
    }

    public IndexWriter getWriter() {
        return indexWriter;
    }

    public IndexSearcher getSearcher() throws IOException {
        if (indexSearcher == null) {
            indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
        }
        return indexSearcher;
    }

    public void buildIndex(Set<Document> documents, IndexProgressListener listener) throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        logger.info("... start writing index");
        int progress = 1;
        for (Document doc : documents) {
            indexWriter.addDocument(doc);
            if (listener != null) {
                listener.fireIndexingProgressed(percentage(progress++, documents.size()));
            }
        }
        commitIndex();
        stopwatch.stop();
        logger.info("... built index in {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    public void appendIndex(AddChangeSet changeSet) throws IOException {
        for (Document doc : changeSet) {
            indexWriter.addDocument(doc);
        }
        commitIndex();
    }

    public void removeIndex(RemoveChangeSet changeSet) throws IOException {
        for (List<Term> terms : changeSet) {
            indexWriter.deleteDocuments(terms.toArray(new Term[terms.size()]));
        }
        commitIndex();
    }

    @Override
    public void dispose() throws IOException {
        if (isOpen(indexWriter)) {
            indexWriter.close();
            logger.info("Closed search index");
        }
    }

    /*
     * Private utility methods
     */

    private boolean isOpen(final IndexWriter indexWriter) {
        if (indexWriter == null) return false;
        return indexWriter.isOpen();
    }

    private void commitIndex() throws IOException {
        if (isOpen(indexWriter)) {
            indexWriter.commit();
        }
    }

    private static int percentage(int progress, int total) {
        return (progress * 100) / total;
    }
}
