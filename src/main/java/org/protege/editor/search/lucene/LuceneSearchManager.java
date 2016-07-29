package org.protege.editor.search.lucene;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.search.SearchCategory;
import org.protege.editor.owl.model.search.SearchInput;
import org.protege.editor.owl.model.search.SearchResult;
import org.protege.editor.owl.model.search.SearchResultHandler;
import org.protege.editor.owl.model.search.SearchStringParser;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.util.ProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
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

import javax.annotation.Nonnull;
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

    private AbstractLuceneIndexer indexer;

    private IndexDelegator indexDelegator;

    private Directory indexDirectory;

    private OWLOntologyChangeListener ontologyChangeListener;

    private OWLModelManagerListener modelManagerListener;

    private OWLOntology currentActiveOntology;

    private final List<ProgressMonitor> progressMonitors = new ArrayList<>();

    public LuceneSearchManager() {
        // NO-OP
    }

    @Override
    public void initialise() {
        editorKit = getEditorKit();
        indexer = new LuceneIndexer(editorKit);
        categories.add(SearchCategory.DISPLAY_NAME);
        categories.add(SearchCategory.IRI);
        categories.add(SearchCategory.ANNOTATION_VALUE);
        categories.add(SearchCategory.LOGICAL_AXIOM);
        ontologyChangeListener = new OWLOntologyChangeListener() {
            public void ontologiesChanged(List<? extends OWLOntologyChange> changes) {
                updateIndex(changes);
            }
        };
        modelManagerListener = new OWLModelManagerListener() {
            public void handleChange(OWLModelManagerChangeEvent event) {
                OWLOntology activeOntology = editorKit.getOWLModelManager().getActiveOntology();
                if (isCacheChangingEvent(event)) {
                    if (currentActiveOntology != null) {
                        /*
                         * A workaround: Protege signals ACTIVE_ONTOLOGY_CHANGED twice when opening an ontology.
                         * The loadOrCreateIndexDirectory() method shouldn't be called twice if the new active
                         * ontology is the same as the current active ontology.
                         */
                        if (!currentActiveOntology.equals(activeOntology)) {
                            loadIndex(activeOntology);
                        }
                        else {
                            // ignore if equals
                        }
                    }
                    else {
                        loadIndex(activeOntology);
                    }
                }
                else if (isCacheMutatingEvent(event)) {
                    rebuildIndex(activeOntology);
                }
                else if (isCacheSavingEvent(event)) {
                    saveIndex(activeOntology);
                }
            }
        };
        editorKit.getOWLModelManager().addOntologyChangeListener(ontologyChangeListener);
        editorKit.getModelManager().addListener(modelManagerListener);
        initializeIndex();
    }

    private void initializeIndex() {
        OWLOntology activeOntology = editorKit.getOWLModelManager().getActiveOntology();
        loadIndex(activeOntology);
    }

    private boolean isCacheChangingEvent(OWLModelManagerChangeEvent event) {
        return event.isType(EventType.ACTIVE_ONTOLOGY_CHANGED);
    }

    private boolean isCacheMutatingEvent(OWLModelManagerChangeEvent event) {
        return event.isType(EventType.ENTITY_RENDERING_CHANGED);
    }

    private boolean isCacheSavingEvent(OWLModelManagerChangeEvent event) {
        return event.isType(EventType.ONTOLOGY_SAVED);
    }

    private void loadIndex(OWLOntology activeOntology) {
        if (activeOntology != null && !activeOntology.isEmpty()) {
            logger.info("Initializing index");
            currentActiveOntology = activeOntology;
            loadIndexDirectory(activeOntology, false); // false = reload index directory, if any
            markIndexAsStale();
        }
    }

    public void rebuildIndex(OWLOntology targetOntology) {
        if (targetOntology != null && !targetOntology.isEmpty()) {
            logger.info("Rebuilding index");
            loadIndexDirectory(targetOntology, true); // true = recreate the index directory
            service.submit(this::buildingIndex);
        }
    }

    private void updateIndex(List<? extends OWLOntologyChange> changes) {
        if (indexDelegator != null) {
            logger.info("Updating index from " + changes.size() + " change(s)");
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

    private void markIndexAsStale() {
        lastSearchId.set(0);
    }

    private void saveIndex(OWLOntology targetOntology) {
        LuceneSearchPreferences.setIndexSnapshot(targetOntology);
    }

    @Override
    public void addProgressMonitor(ProgressMonitor pm) {
        progressMonitors.add(pm);
    }

    @Override
    public void dispose() {
        editorKit.getOWLModelManager().removeOntologyChangeListener(ontologyChangeListener);
        editorKit.getModelManager().removeListener(modelManagerListener);
        disposeIndexDelegator();
    }

    private void disposeIndexDelegator() {
        try {
            if (indexDelegator != null && indexLocationExists()) {
                indexDelegator.dispose();
            }
        }
        catch (IOException e) {
            logger.error("Failed to dispose index delegator", e);
        }
    }

    private boolean indexLocationExists() {
        return LuceneSearchPreferences.getIndexLocation(currentActiveOntology).isPresent();
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
    }

    @Override
    public void performSearch(String searchString, SearchResultHandler searchResultHandler) {
        try {
            if (lastSearchId.getAndIncrement() == 0) {
                if (!DirectoryReader.indexExists(getIndexDirectory())) {
                    logger.info("Building index");
                    service.submit(this::buildingIndex);
                }
            }
            List<SearchQuery> searchQueries = prepareQuery(searchString);
            service.submit(new SearchCallable(lastSearchId.incrementAndGet(), searchQueries, searchResultHandler));
        }
        catch (IOException e) {
            logger.error("Failed to perform search", e);
        }
    }

    private void loadIndexDirectory(@Nonnull OWLOntology targetOntology, boolean forceReset) {
        try {
            if (forceReset) {
                removeIndexDirectory();
                LuceneSearchPreferences.removeIndexLocation(targetOntology);
            }
            if (shouldStoreInDisk(targetOntology)) {
                String indexLocation = LuceneSearchPreferences.findIndexLocation(targetOntology);
                Directory directory = FSDirectory.open(Paths.get(indexLocation));
                setIndexDirectory(directory);
            }
            else {
                logger.info("Storing index into RAM memory");
                Directory directory = new RAMDirectory();
                setIndexDirectory(directory);
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to setup index directory", e);
        }
    }

    private boolean shouldStoreInDisk(OWLOntology targetOntology) {
        if (LuceneSearchPreferences.useInMemoryIndexStoring()) {
            IRI documentIri = editorKit.getOWLModelManager().getOWLOntologyManager().getOntologyDocumentIRI(targetOntology);
            try {
                URL resourceUrl = documentIri.toURI().toURL();
                URLConnection connection = resourceUrl.openConnection();
                connection.connect();
                int resourceSize = connection.getContentLength();
                return resourceSize > LuceneSearchPreferences.getMaxSizeForInMemoryIndexStoring()*1024*1024; // in bytes
            }
            catch (IOException e) {
                logger.error("Unable to open the ontology " + documentIri);
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    private Directory getIndexDirectory() {
        return indexDirectory;
    }

    private void removeIndexDirectory() throws IOException {
        setIndexDelegator(null);
    }

    private void setIndexDirectory(Directory indexDirectory) throws IOException {
        this.indexDirectory = indexDirectory;
        fireIndexDirectoryChange();
    }

    private void fireIndexDirectoryChange() throws IOException {
        setupIndexDelegator();
    }

    private void setupIndexDelegator() throws IOException {
        IndexDelegator newDelegator = null;
        if (getIndexDirectory() != null) {
            newDelegator = IndexDelegator.getInstance(getIndexDirectory(), indexer.getIndexWriterConfig());
        }
        setIndexDelegator(newDelegator);
    }

    private void setIndexDelegator(IndexDelegator indexDelegator) throws IOException {
        if (this.indexDelegator != null) {
            this.indexDelegator.dispose();
        }
        this.indexDelegator = indexDelegator;
    }

    private List<SearchQuery> prepareQuery(String searchString) {
        SearchInput searchInput = searchStringParser.parse(searchString);
        LuceneSearchQueryBuilder builder = new LuceneSearchQueryBuilder(this);
        builder.setCategories(categories);
        searchInput.accept(builder);
        return builder.build();
    }

    private void buildingIndex() {
        fireIndexingStarted();
        try {
            indexer.doIndex(indexDelegator, new SearchContext(editorKit), progress -> fireIndexingProgressed(progress));
            saveIndex(currentActiveOntology);
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
        private List<SearchQuery> searchQueries;
        private SearchResultHandler searchResultHandler;

        private SearchCallable(long searchId, List<SearchQuery> searchQueries, SearchResultHandler searchResultHandler) {
            this.searchId = searchId;
            this.searchQueries = searchQueries;
            this.searchResultHandler = searchResultHandler;
        }

        @Override
        public void run() {
            logger.debug("Starting search {}", searchId);
            Stopwatch stopwatch = Stopwatch.createStarted();
            fireSearchStarted();
            Set<SearchResult> finalResults = new HashSet<>();
            for (SearchQuery query : searchQueries) {
                if (!isLatestSearch()) {
                    // New search started
                    logger.debug("... terminating search {} prematurely", searchId);
                    return;
                }
                try {
                    ResultDocumentHandler handler = new ResultDocumentHandler(editorKit);
                    logger.debug("... executing query " + query);
                    query.evaluate(handler, progress -> fireSearchingProgressed(progress));
                    SearchUtils.intersect(finalResults, handler.getSearchResults());
                }
                catch (QueryEvaluationException e) {
                    logger.error("Error while executing the query: {}", e);
                }
            }
            fireSearchFinished();
            stopwatch.stop();
            logger.debug("... finished search {} in {} ms ({} results)", searchId, stopwatch.elapsed(TimeUnit.MILLISECONDS), finalResults.size());
            showResults(finalResults, searchResultHandler);
        }

        private void showResults(final Set<SearchResult> results, final SearchResultHandler searchResultHandler) {
            if (SwingUtilities.isEventDispatchThread()) {
                searchResultHandler.searchFinished(results);
            }
            else {
                SwingUtilities.invokeLater(() -> searchResultHandler.searchFinished(results));
            }
        }

        private boolean isLatestSearch() {
            return searchId == lastSearchId.get();
        }
    }

    /*
     * Private methods to handle progress monitor
     */

    private void fireIndexingStarted() {
        SwingUtilities.invokeLater(() -> {
            for (ProgressMonitor pm : progressMonitors) {
                pm.setSize(100);
                pm.setIndeterminate(true);
                pm.setMessage("initializing index");
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

    private void fireSearchingProgressed(final long progress) {
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
