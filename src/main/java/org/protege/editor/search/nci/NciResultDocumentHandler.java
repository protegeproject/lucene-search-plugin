package org.protege.editor.search.nci;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.protege.editor.owl.model.search.SearchCategory;
import org.protege.editor.search.lucene.AbstractDocumentHandler;
import org.protege.editor.search.lucene.IndexField;
import org.protege.editor.search.lucene.SearchQuery;

import org.apache.lucene.document.Document;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class NciResultDocumentHandler extends AbstractDocumentHandler {

    private OWLEntityFinder entityFinder;
    private Set<OWLEntity> results = new HashSet<>();

    public NciResultDocumentHandler(OWLEditorKit editorKit) {
        entityFinder = editorKit.getOWLModelManager().getOWLEntityFinder();
    }

    @Override
    public void handle(SearchCategory category, Document doc) {
        String subjectIri = doc.get(IndexField.ENTITY_IRI);
        Optional<OWLEntity> entity = entityFinder.getEntities(IRI.create(subjectIri)).stream().findFirst();
        if (entity.isPresent()) {
            results.add(entity.get());
        }
    }

    public Set<OWLEntity> getSearchResults(SearchQuery query) {
        if (query instanceof RequiresPostProcessing) {
            RequiresPostProcessing postProcessingQuery = ((RequiresPostProcessing) query);
            return postProcessingQuery.performPostProcessing(results);
        }
        else {
            return results;
        }
    }
}
