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
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.util.HashSet;
import java.util.Set;

/**
 * Author: Josef Hardi <josef.hardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 10/11/2015
 */
public class LuceneIndexer extends AbstractLuceneIndexer {

    private final OWLEntityFinder entityFinder;
    private final OWLModelManager objectRenderer;
    private final OWLObjectStyledStringRenderer styledStringRenderer;

    public LuceneIndexer(OWLEditorKit editorKit) {
        super();
        entityFinder = editorKit.getOWLModelManager().getOWLEntityFinder();
        objectRenderer = editorKit.getOWLModelManager();
        styledStringRenderer = new OWLObjectStyledStringRenderer(
                new OWLObjectRenderingContext(
                    new OWLEditorKitShortFormProvider(editorKit),
                    new OWLEditorKitOntologyShortFormProvider(editorKit)));
    }

    @Override
    public IndexItemsCollector getIndexItemsCollector() {

        return new IndexItemsCollector() {

            private Set<Document> documents = new HashSet<>();

            @Override
            public Set<Document> getIndexDocuments() {
                return documents;
            }

            @Override
            public void visit(OWLOntology ontology) {
                for (OWLEntity entity : ontology.getSignature()) {
                    entity.accept(this);
                }
                for (OWLAxiom axiom : ontology.getTBoxAxioms(Imports.EXCLUDED)) {
                    axiom.accept(this);
                }
            }

            @Override
            public void visit(OWLClass cls) {
                Document doc = new Document();
                doc.add(new TextField(IndexField.ENTITY_IRI, getEntityId(cls), Store.YES));
                doc.add(new TextField(IndexField.DISPLAY_NAME, getDisplayName(cls), Store.YES));
                doc.add(new StringField(IndexField.ENTITY_TYPE, getType(cls), Store.YES));
                documents.add(doc);
            }

            @Override
            public void visit(OWLObjectProperty property) {
                Document doc = new Document();
                doc.add(new TextField(IndexField.ENTITY_IRI, getEntityId(property), Store.YES));
                doc.add(new TextField(IndexField.DISPLAY_NAME, getDisplayName(property), Store.YES));
                doc.add(new StringField(IndexField.ENTITY_TYPE, getType(property), Store.YES));
                documents.add(doc);
            }

            public void visit(OWLDataProperty property) {
                Document doc = new Document();
                doc.add(new TextField(IndexField.ENTITY_IRI, getEntityId(property), Store.YES));
                doc.add(new TextField(IndexField.DISPLAY_NAME, getDisplayName(property), Store.YES));
                doc.add(new StringField(IndexField.ENTITY_TYPE, getType(property), Store.YES));
                documents.add(doc);
            }

            public void visit(OWLNamedIndividual individual) {
                Document doc = new Document();
                doc.add(new TextField(IndexField.ENTITY_IRI, getEntityId(individual), Store.YES));
                doc.add(new TextField(IndexField.DISPLAY_NAME, getDisplayName(individual), Store.YES));
                doc.add(new StringField(IndexField.ENTITY_TYPE, getType(individual), Store.YES));
                documents.add(doc);
            }

            public void visit(OWLAnnotationProperty property) {
                Document doc = new Document();
                doc.add(new TextField(IndexField.ENTITY_IRI, getEntityId(property), Store.YES));
                doc.add(new TextField(IndexField.DISPLAY_NAME, getDisplayName(property), Store.YES));
                doc.add(new StringField(IndexField.ENTITY_TYPE, getType(property), Store.YES));
                documents.add(doc);
            }

            @Override
            public void visit(OWLAnnotationAssertionAxiom axiom) {
                if (axiom.getSubject() instanceof IRI) {
                    Document doc = new Document();
                    OWLEntity entity = getOWLEntity((IRI) axiom.getSubject());
                    doc.add(new TextField(IndexField.ENTITY_IRI, getEntityId(entity), Store.YES));
                    doc.add(new TextField(IndexField.DISPLAY_NAME, getDisplayName(entity), Store.YES));
                    doc.add(new TextField(IndexField.ANNOTATION_IRI, getEntityId(axiom.getProperty()), Store.YES));
                    doc.add(new TextField(IndexField.ANNOTATION_DISPLAY_NAME, getDisplayName(axiom.getProperty()), Store.YES));
                    doc.add(new TextField(IndexField.ANNOTATION_TEXT, getAnnotationText(axiom.getAnnotation()), Store.YES));
                    documents.add(doc);
                }
            }

            /*
             * Utility methods
             */

            private OWLEntity getOWLEntity(IRI identifier) {
                return entityFinder.getEntities(identifier).stream().findFirst().get();
            }

            private String getEntityId(OWLEntity entity) {
                return entity.getIRI().toString();
            }

            private String getType(OWLEntity entity) {
                return entity.getEntityType().getName();
            }

            private String getDisplayName(OWLEntity entity) {
                return objectRenderer.getRendering(entity);
            }

            private String getAnnotationText(OWLAnnotation annotation) {
                return styledStringRenderer.getRendering(annotation).getString();
            }
        };
    }
}
