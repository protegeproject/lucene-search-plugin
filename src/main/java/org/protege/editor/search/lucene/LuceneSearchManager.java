package org.protege.editor.search.lucene;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.search.SearchCategory;
import org.protege.editor.owl.model.search.SearchInterruptionException;
import org.protege.editor.owl.model.search.SearchResult;
import org.protege.editor.owl.model.search.SearchResultHandler;
import org.protege.editor.owl.model.search.SearchStringParser;
import org.protege.editor.owl.model.search.impl.SearchMetadataImportContext;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.util.ProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.SwingUtilities;

import com.google.common.base.Stopwatch;

/**
 * Author: Josef Hardi <josef.hardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 04/11/2015
 */
public class LuceneSearchManager extends LuceneSearcher {

    private static final Logger logger = LoggerFactory.getLogger(LuceneSearchManager.class);

    private OWLEditorKit editorKit;

    private Set<SearchCategory> categories = new HashSet<>();

    private ExecutorService service = Executors.newSingleThreadExecutor();

    private AtomicLong lastSearchId = new AtomicLong(0);

    private SearchStringParser searchStringParser = new LuceneStringParser();

    private LuceneIndexer indexer;

    private IndexDelegator indexDelegator;

    private Directory indexDirectory;

    private OWLOntologyChangeListener ontologyChangeListener;

    private OWLModelManagerListener modelManagerListener;

    private final List<ProgressMonitor> progressMonitors = new ArrayList<>();

    public LuceneSearchManager() {
        // NO-OP
    }

    @Override
    public void initialise() {
        this.editorKit = getEditorKit();
        this.indexer = new LuceneIndexer(editorKit);
        categories.add(SearchCategory.DISPLAY_NAME);
        categories.add(SearchCategory.IRI);
        categories.add(SearchCategory.ANNOTATION_VALUE);
        ontologyChangeListener = new OWLOntologyChangeListener() {
            public void ontologiesChanged(List<? extends OWLOntologyChange> changes) {
                updateIndex(changes);
            }
        };
        modelManagerListener = new OWLModelManagerListener() {
            public void handleChange(OWLModelManagerChangeEvent event) {
                if (isCacheChangingEvent(event)) {
                    markIndexAsStale(false);
                }
                else if (isCacheMutatingEvent(event)) {
                    markIndexAsStale(true);
                }
                else if (isCacheSavingEvent(event)) {
                    saveIndex();
                }
            }
        };
        editorKit.getOWLModelManager().addOntologyChangeListener(ontologyChangeListener);
        editorKit.getModelManager().addListener(modelManagerListener);
    }

    private boolean isCacheChangingEvent(OWLModelManagerChangeEvent event) {
        return event.isType(EventType.ACTIVE_ONTOLOGY_CHANGED);
    }

    private boolean isCacheMutatingEvent(OWLModelManagerChangeEvent event) {
        return event.isType(EventType.ENTITY_RENDERER_CHANGED)
                || event.isType(EventType.ENTITY_RENDERING_CHANGED);
    }

    private boolean isCacheSavingEvent(OWLModelManagerChangeEvent event) {
        return event.isType(EventType.ONTOLOGY_SAVED);
    }

    private void updateIndex(List<? extends OWLOntologyChange> changes) {
        if (indexDelegator != null) {
            service.submit(() -> updatingIndex(changes));
        }
    }

    private void updatingIndex(List<? extends OWLOntologyChange> changes) {
        try {
            RemoveChangeSet removeChangeSet = RemoveChangeSet.create(changes, new RemoveChangeSetHandler(editorKit));
            indexer.doRemove(indexDelegator, removeChangeSet);
            AddChangeSet addChangeSet = AddChangeSet.create(changes, new AddChangeSetHandler(editorKit));
            indexer.doAppend(indexDelegator, addChangeSet);
        }
        catch (IOException e) {
            logger.error("... update index failed");
            e.printStackTrace();
        }
    }

    private void markIndexAsStale(boolean forceDelete) {
        if (forceDelete) {
            if (indexDirectory != null) { // remove the index
                logger.info("Rebuilding index");
                OWLOntology activeOntology = editorKit.getOWLModelManager().getActiveOntology();
                LuceneSearchPreferences.removeIndexLocation(activeOntology);
                indexDelegator = null;
            }
        }
        lastSearchId.set(0);
    }

    private void saveIndex() {
        OWLOntology activeOntology = editorKit.getOWLModelManager().getActiveOntology();
        LuceneSearchPreferences.setIndexSnapshot(activeOntology);
    }

    @Override
    public void addProgressMonitor(ProgressMonitor pm) {
        progressMonitors.add(pm);
    }

    @Override
    public void dispose() {
        if (editorKit == null) {
            return;
        }
        editorKit.getOWLModelManager().removeOntologyChangeListener(ontologyChangeListener);
        editorKit.getOWLModelManager().removeListener(modelManagerListener);
    }

    @Override
    public IndexSearcher getIndexSearcher() throws IOException {
        if (indexDelegator == null) {
            throw new RuntimeException("No index was loaded");
        }
        return indexDelegator.getSearcher();
    }

    @Override
    public boolean isSearchType(SearchCategory category) {
        return categories.contains(category);
    }

    @Override
    public void setCategories(Collection<SearchCategory> categories) {
        this.categories.clear();
        this.categories.addAll(categories);
        markIndexAsStale(false);
    }

    @Override
    public void performSearch(String searchString, SearchResultHandler searchResultHandler) {
        try {
            if (lastSearchId.getAndIncrement() == 0) {
                Directory directory = loadOrCreateIndexDirectory();
                if (!DirectoryReader.indexExists(directory)) {
                    service.submit(this::buildingIndex);
                }
            }
            SearchQueries searchQueries = prepareQuery(searchString);
            service.submit(new SearchCallable(lastSearchId.incrementAndGet(), searchQueries, searchResultHandler));
        }
        catch (IOException e) {
            logger.error("Search failed to perform", e);
        }
    }

    private Directory loadOrCreateIndexDirectory() throws IOException {
        OWLOntology activeOntology = editorKit.getOWLModelManager().getActiveOntology();
        String indexLocation = LuceneSearchPreferences.getIndexLocation(activeOntology);
        Directory directory = FSDirectory.open(Paths.get(indexLocation));
        setIndexDirectory(directory);
        logger.info("Using index located at {}", indexLocation);
        return directory;
    }

    private void setIndexDirectory(Directory indexDirectory) throws IOException {
        this.indexDirectory = indexDirectory;
        fireIndexDirectoryChange();
    }

    private void fireIndexDirectoryChange() throws IOException {
        if (indexDelegator != null) {
            indexDelegator.dispose();
        }
        indexDelegator = IndexDelegator.create(indexDirectory, indexer.getIndexWriterConfig());
    }

    private SearchQueries prepareQuery(String searchString) {
        QueryBasedInputHandler handler = new QueryBasedInputHandler(this);
        searchStringParser.parse(searchString, handler);
        return handler.getQueryObject();
    }

    private void buildingIndex() {
        fireIndexingStarted();
        try {
            indexer.doIndex(indexDelegator,
                    new SearchMetadataImportContext(editorKit),
                    progress -> fireIndexingProgressed(progress));
            saveIndex();
        }
        catch (IOException e) {
            logger.error("... build index failed", e);
        }
        finally {
            fireIndexingFinished();
        }
    }

    private class SearchCallable implements Runnable {
        private long searchId;
        private SearchQueries searchQueries;
        private SearchResultHandler searchResultHandler;
        private QueryRunner queryRunner = new QueryRunner(lastSearchId);

        private SearchCallable(long searchId, SearchQueries searchQueries, SearchResultHandler searchResultHandler) {
            this.searchId = searchId;
            this.searchQueries = searchQueries;
            this.searchResultHandler = searchResultHandler;
        }

        @Override
        public void run() {
            logger.debug("Starting search {} (pattern: {})", searchId, searchQueries);
            Stopwatch stopwatch = Stopwatch.createStarted();
            fireSearchStarted();
            ResultDocumentHandler documentHandler = new ResultDocumentHandler(editorKit);
            try {
                queryRunner.execute(searchId, searchQueries, documentHandler, progress -> fireSearchProgressed(progress));
            }
            catch (SearchInterruptionException e) {
                return; // search terminated prematurely
            }
            fireSearchFinished();
            Set<SearchResult> results = documentHandler.getSearchResults();
            stopwatch.stop();
            logger.debug("... finished search {} in {} ms ({} results)", searchId, stopwatch.elapsed(TimeUnit.MILLISECONDS), results.size());
            showResults(results, searchResultHandler);
        }

        private void showResults(final Set<SearchResult> results, final SearchResultHandler searchResultHandler) {
            if (SwingUtilities.isEventDispatchThread()) {
                searchResultHandler.searchFinished(results);
            }
            else {
                SwingUtilities.invokeLater(() -> searchResultHandler.searchFinished(results));
            }
        }
    }

    /*
     * Private methods to handle progress monitor
     */

    private void fireIndexingStarted() {
        SwingUtilities.invokeLater(() -> {
            for (ProgressMonitor pm : progressMonitors) {
                pm.setSize(100);
                pm.setMessage("Initializing index");
                pm.setStarted();
            }
        });
    }

    private void fireIndexingProgressed(final long progress) {
        SwingUtilities.invokeLater(() -> {
            for (ProgressMonitor pm : progressMonitors) {
                pm.setProgress(progress);
                switch ((int)progress % 4) {
                    case 0: pm.setMessage("indexing"); break;
                    case 1: pm.setMessage("indexing."); break;
                    case 2: pm.setMessage("indexing.."); break;
                    case 3: pm.setMessage("indexing..."); break;
                }
            }
        });
    }

    private void fireIndexingFinished() {
        SwingUtilities.invokeLater(() -> {
            for (ProgressMonitor pm : progressMonitors) {
                pm.setFinished();
            }
        });
    }

    private void fireSearchStarted() {
        SwingUtilities.invokeLater(() -> {
            for (ProgressMonitor pm : progressMonitors) {
                pm.setSize(100);
                pm.setStarted();
            }
        });
    }

    private void fireSearchProgressed(final long progress) {
        SwingUtilities.invokeLater(() -> {
            for (ProgressMonitor pm : progressMonitors) {
                pm.setProgress(progress);
                switch ((int)progress % 4) {
                    case 0: pm.setMessage("searching"); break;
                    case 1: pm.setMessage("searching."); break;
                    case 2: pm.setMessage("searching.."); break;
                    case 3: pm.setMessage("searching..."); break;
                }
            }
        });
    }

    private void fireSearchFinished() {
        SwingUtilities.invokeLater(() -> {
            for (ProgressMonitor pm : progressMonitors) {
                pm.setFinished();
            }
        });
    }
}
