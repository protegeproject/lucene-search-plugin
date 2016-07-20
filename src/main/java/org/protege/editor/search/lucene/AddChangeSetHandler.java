package org.protege.editor.search.lucene;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLEditorKitOntologyShortFormProvider;
import org.protege.editor.owl.model.OWLEditorKitShortFormProvider;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.protege.editor.owl.ui.renderer.context.OWLObjectRenderingContext;
import org.protege.editor.owl.ui.renderer.styledstring.OWLObjectStyledStringRenderer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.util.OWLOntologyChangeVisitorAdapter;

import java.util.HashSet;
import java.util.Set;

public class AddChangeSetHandler extends OWLOntologyChangeVisitorAdapter {

    private final OWLEditorKit editorKit;

    private final OWLEntityFinder entityFinder;
    private final OWLModelManager objectRenderer;
    private final OWLObjectStyledStringRenderer styledStringRenderer;

    protected Set<Document> documents = new HashSet<>();

    public AddChangeSetHandler(OWLEditorKit editorKit) {
        this.editorKit = editorKit;
        entityFinder = editorKit.getOWLModelManager().getOWLEntityFinder();
        objectRenderer = editorKit.getOWLModelManager();
        styledStringRenderer = new OWLObjectStyledStringRenderer(
                new OWLObjectRenderingContext(
                    new OWLEditorKitShortFormProvider(editorKit),
                    new OWLEditorKitOntologyShortFormProvider(editorKit)));
    }

    public Set<Document> getDocuments() {
        return documents;
    }

    @Override
    public void visit(AddAxiom change) {
        OWLAxiom changeAxiom = change.getAxiom();
        if (changeAxiom instanceof OWLDeclarationAxiom) {
            OWLEntity entity = ((OWLDeclarationAxiom) changeAxiom).getEntity();
            Document doc = new Document();
            doc.add(new TextField(IndexField.ENTITY_IRI, getIri(entity), Store.YES));
            doc.add(new TextField(IndexField.DISPLAY_NAME, getDisplayName(entity), Store.YES));
            doc.add(new StringField(IndexField.ENTITY_TYPE, getType(entity), Store.YES));
            documents.add(doc);
        }
        else if (changeAxiom instanceof OWLAnnotationAssertionAxiom) {
            OWLAnnotationAssertionAxiom axiom = (OWLAnnotationAssertionAxiom) changeAxiom;
            if (axiom.getSubject() instanceof IRI) {
                Document doc = new Document();
                OWLEntity entity = getOWLEntity((IRI) axiom.getSubject());
                doc.add(new TextField(IndexField.ENTITY_IRI, getIri(entity), Store.YES));
                doc.add(new TextField(IndexField.DISPLAY_NAME, getDisplayName(entity), Store.YES));
                doc.add(new TextField(IndexField.ANNOTATION_IRI, getIri(axiom.getProperty()), Store.YES));
                doc.add(new TextField(IndexField.ANNOTATION_DISPLAY_NAME, getDisplayName(axiom.getProperty()), Store.YES));
                doc.add(new TextField(IndexField.ANNOTATION_TEXT, getAnnotationText(axiom.getAnnotation()), Store.YES));
                documents.add(doc);
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
