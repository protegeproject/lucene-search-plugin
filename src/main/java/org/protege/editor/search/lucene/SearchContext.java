package org.protege.editor.search.lucene;

import org.protege.editor.owl.OWLEditorKit;

import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

/**
 * @author Josef Hardi <johardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 21/06/2016
 */
public class SearchContext {

    private OWLEditorKit editorKit;

    public SearchContext(OWLEditorKit editorKit) {
        this.editorKit = editorKit;
    }

    public OWLEditorKit getEditorKit() {
        return editorKit;
    }

    public OWLOntology getActiveOntology() {
        return editorKit.getOWLModelManager().getActiveOntology();
    }

    public Set<OWLOntology> getOntologies() {
        return editorKit.getOWLModelManager().getActiveOntologies();
    }

    public boolean isIndexable() {
        return !getActiveOntology().isEmpty() && !getActiveOntology().isAnonymous();
    }
}