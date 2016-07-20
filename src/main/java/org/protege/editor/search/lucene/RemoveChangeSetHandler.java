package org.protege.editor.search.lucene;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLEditorKitOntologyShortFormProvider;
import org.protege.editor.owl.model.OWLEditorKitShortFormProvider;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.protege.editor.owl.ui.renderer.context.OWLObjectRenderingContext;
import org.protege.editor.owl.ui.renderer.styledstring.OWLObjectStyledStringRenderer;

import org.apache.lucene.index.Term;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.util.OWLOntologyChangeVisitorAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RemoveChangeSetHandler extends OWLOntologyChangeVisitorAdapter {

    private final OWLEditorKit editorKit;

    private final OWLEntityFinder entityFinder;
    private final OWLModelManager objectRenderer;
    private final OWLObjectStyledStringRenderer styledStringRenderer;

    protected Set<List<Term>> removeFilters = new HashSet<>();

    public RemoveChangeSetHandler(OWLEditorKit editorKit) {
        this.editorKit = editorKit;
        entityFinder = editorKit.getOWLModelManager().getOWLEntityFinder();
        objectRenderer = editorKit.getOWLModelManager();
        styledStringRenderer = new OWLObjectStyledStringRenderer(
                new OWLObjectRenderingContext(
                    new OWLEditorKitShortFormProvider(editorKit),
                    new OWLEditorKitOntologyShortFormProvider(editorKit)));
    }

    public Set<List<Term>> getRemoveFilters() {
        return removeFilters;
    }

    @Override
    public void visit(RemoveAxiom change) {
        OWLAxiom changeAxiom = change.getAxiom();
        if (changeAxiom instanceof OWLDeclarationAxiom) {
            OWLEntity entity = ((OWLDeclarationAxiom) changeAxiom).getEntity();
            List<Term> terms = new ArrayList<>();
            terms.add(new Term(IndexField.ENTITY_IRI, getIri(entity)));
            terms.add(new Term(IndexField.ENTITY_TYPE, getType(entity)));
            removeFilters.add(terms);
        }
        else if (changeAxiom instanceof OWLAnnotationAssertionAxiom) {
            OWLAnnotationAssertionAxiom axiom = (OWLAnnotationAssertionAxiom) changeAxiom;
            if (axiom.getSubject() instanceof IRI) {
                List<Term> terms = new ArrayList<>();
                OWLEntity entity = getOWLEntity((IRI) axiom.getSubject());
                terms.add(new Term(IndexField.ENTITY_IRI, getIri(entity)));
                terms.add(new Term(IndexField.ANNOTATION_IRI, getIri(axiom.getProperty())));
                terms.add(new Term(IndexField.ANNOTATION_TEXT, getAnnotationText(axiom.getAnnotation())));
                removeFilters.add(terms);
            }
        }
    }

    protected OWLEditorKit getEditorKit() {
        return editorKit;
    }

    protected OWLEntity getOWLEntity(IRI identifier) {
        return entityFinder.getEntities(identifier).stream().findFirst().get();
    }

    protected String getIri(OWLEntity entity) {
        return entity.getIRI().toString();
    }

    protected String getType(OWLObject object) {
        if (object instanceof OWLEntity) {
            return ((OWLEntity) object).getEntityType().getName();
        }
        else if (object instanceof OWLAxiom) {
            return ((OWLAxiom) object).getAxiomType().getName();
        }
        return "(Unknown type)";
    }

    protected String getDisplayName(OWLObject object) {
        return objectRenderer.getRendering(object);
    }

    protected String getAnnotationText(OWLAnnotation annotation) {
        return styledStringRenderer.getRendering(annotation).getString();
    }
}
