package org.protege.editor.search.nci;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.search.SearchCategory;
import org.protege.editor.owl.model.search.SearchResultHandler;
import org.protege.editor.search.lucene.AbstractLuceneIndexer;
import org.protege.editor.search.lucene.LuceneSearcher;

import org.apache.lucene.search.IndexSearcher;
import org.semanticweb.owlapi.util.ProgressMonitor;

import java.io.IOException;
import java.util.Collection;

public class ThinLuceneSearcher extends LuceneSearcher {

    private IndexSearcher searcher;
    private OWLEditorKit editorKit;

    public ThinLuceneSearcher(IndexSearcher searcher, OWLEditorKit editorKit) {
        this.searcher = searcher;
        this.editorKit = editorKit;
    }

    @Override
    public OWLEditorKit getEditorKit() {
        return editorKit;
    }

    @Override
    public void initialise() throws Exception {
        // NO-OP
    }

    @Override
    protected AbstractLuceneIndexer getIndexer() {
        return null;
    }

    @Override
    protected IndexSearcher getIndexSearcher() throws IOException {
        return searcher;
    }

    @Override
    public void dispose() {
        // NO-op
    }

    @Override
    public void addProgressMonitor(ProgressMonitor pm) {
        // NO-OP
    }

    @Override
    public boolean isSearchType(SearchCategory category) {
        return false;
    }

    @Override
    public void setCategories(Collection<SearchCategory> categories) {
        // NO-OP
    }

    @Override
    public void performSearch(String searchString, SearchResultHandler searchResultHandler) {
        // NO-OP
    }
}
