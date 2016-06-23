package org.protege.editor.search.lucene;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.protege.editor.owl.model.search.SearchCategory;
import org.protege.editor.owl.model.search.SearchMetadata;
import org.protege.editor.owl.model.search.SearchResult;
import org.protege.editor.owl.model.search.SearchResultMatch;

import org.apache.lucene.document.Document;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableList;

/**
 * Author: Josef Hardi <josef.hardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 23/06/2016
 */
public class ResultDocumentHandler extends AbstractDocumentHandler {

    private OWLEditorKit editorKit;
    private Set<SearchResult> results = new HashSet<>();

    public ResultDocumentHandler(OWLEditorKit editorKit) {
        this.editorKit = editorKit;
    }

    public Set<SearchResult> getSearchResults() {
        return results;
    }

    @Override
    public void handle(SearchCategory category, Document doc) {
        Optional<OWLEntity> subject = getOWLEntity(doc.get(IndexField.ENTITY_IRI));
        if (subject.isPresent()) {
            SearchMetadata metadata = createSearchMetadata(category, doc, subject.get());
            SearchResult searchResult = new SearchResult(metadata, createEmptySearchResultMatch());
            results.add(searchResult);
        }
    }

    private SearchMetadata createSearchMetadata(SearchCategory category, Document doc, OWLEntity subject) {
        String subjectName = editorKit.getOWLModelManager().getRendering(subject);
        switch (category) {
            case IRI: return new SearchMetadata(category, "IRI", subject, subjectName, getContent(doc, IndexField.ENTITY_IRI));
            case DISPLAY_NAME: return new SearchMetadata(category, "DISPLAY NAME", subject, subjectName, getContent(doc, IndexField.DISPLAY_NAME));
            case ANNOTATION_VALUE: return new SearchMetadata(category, getContent(doc, IndexField.ANNOTATION_DISPLAY_NAME), subject, subjectName, getContent(doc, IndexField.ANNOTATION_TEXT));
            case LOGICAL_AXIOM: return new SearchMetadata(category, getContent(doc, IndexField.AXIOM_TYPE), subject, subjectName, getContent(doc, IndexField.AXIOM_DISPLAY_NAME));
            case OTHER: return new SearchMetadata(category, "OTHER", subject, subjectName, "(Found in multiple fields)");
            default: break;
        }
        return null;
    }

    private String getContent(Document doc, String fieldName) {
        return doc.get(fieldName);
    }

    private Optional<OWLEntity> getOWLEntity(String identifier) {
        OWLEntityFinder finder = editorKit.getOWLModelManager().getOWLEntityFinder();
        return finder.getEntities(IRI.create(identifier)).stream().findFirst();
    }

    private ImmutableList<SearchResultMatch> createEmptySearchResultMatch() {
        ImmutableList.Builder<SearchResultMatch> builder = new ImmutableList.Builder<>();
        builder.add(new SearchResultMatch("", 0, 0));
        return builder.build();
    }
}
