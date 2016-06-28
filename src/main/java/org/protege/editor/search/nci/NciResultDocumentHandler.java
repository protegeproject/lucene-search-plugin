package org.protege.editor.search.nci;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.protege.editor.search.lucene.IndexField;

import org.apache.lucene.document.Document;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author Josef Hardi <johardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 27/06/2016
 */
public class NciResultDocumentHandler {

    private OWLEntityFinder entityFinder;
    private Set<OWLEntity> results = new HashSet<>();

    public NciResultDocumentHandler(OWLEditorKit editorKit) {
        entityFinder = editorKit.getOWLModelManager().getOWLEntityFinder();
    }

    public void handle(Document doc) {
        String subjectIri = doc.get(IndexField.ENTITY_IRI);
        Optional<OWLEntity> entity = entityFinder.getEntities(IRI.create(subjectIri)).stream().findFirst();
        if (entity.isPresent()) {
            results.add(entity.get());
        }
    }

    public Set<OWLEntity> getSearchResults() {
        return results;
    }
}
