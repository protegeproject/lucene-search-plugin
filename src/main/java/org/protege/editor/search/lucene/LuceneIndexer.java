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
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.util.AxiomSubjectProvider;

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
                    for (OWLAnnotationAssertionAxiom axiom : ontology.getAnnotationAssertionAxioms(entity.getIRI())) {
                        axiom.accept(this);
                    }
                }
                for (OWLAxiom axiom : ontology.getLogicalAxioms()) {
                    axiom.accept(this);
                }
            }

            /*
             * Index optimization note: Use TextField when the text is used heavily in the search
             * (including for searching its sub-text / sub-string) and StringField when it is
             * rarely used or the searching requires its full-text or full-string.
             */

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

            //@formatter:off
            @Override public void visit(OWLSubClassOfAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLAsymmetricObjectPropertyAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLReflexiveObjectPropertyAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLDisjointClassesAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLDataPropertyDomainAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLObjectPropertyDomainAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLEquivalentObjectPropertiesAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLDifferentIndividualsAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLDisjointDataPropertiesAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLDisjointObjectPropertiesAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLObjectPropertyRangeAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLObjectPropertyAssertionAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLFunctionalObjectPropertyAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLSubObjectPropertyOfAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLDisjointUnionAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLSymmetricObjectPropertyAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLDataPropertyRangeAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLFunctionalDataPropertyAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLEquivalentDataPropertiesAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLClassAssertionAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLEquivalentClassesAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLDataPropertyAssertionAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLTransitiveObjectPropertyAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLSubDataPropertyOfAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLSameIndividualAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLSubPropertyChainOfAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLInverseObjectPropertiesAxiom axiom) { visitLogicalAxiom(axiom); }
            @Override public void visit(OWLHasKeyAxiom axiom) { visitLogicalAxiom(axiom); }

            //@formatter:on
            private void visitLogicalAxiom(OWLAxiom axiom) {
                Document doc = new Document();
                OWLObject subject = new AxiomSubjectProvider().getSubject(axiom);
                if (subject instanceof OWLEntity) {
                    OWLEntity entity = (OWLEntity) subject;
                    doc.add(new TextField(IndexField.ENTITY_IRI, getEntityId(entity), Store.YES));
                    doc.add(new TextField(IndexField.DISPLAY_NAME, getDisplayName(entity), Store.YES));
                    doc.add(new TextField(IndexField.AXIOM_DISPLAY_NAME, getDisplayName(axiom), Store.YES));
                    doc.add(new StringField(IndexField.AXIOM_TYPE, getType(axiom), Store.YES));
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

            private String getType(OWLObject object) {
                if (object instanceof OWLEntity) {
                    return ((OWLEntity) object).getEntityType().getName();
                }
                else if (object instanceof OWLAxiom) {
                    return ((OWLAxiom) object).getAxiomType().getName();
                }
                return "(Unknown type)";
            }

            private String getDisplayName(OWLObject object) {
                return objectRenderer.getRendering(object);
            }

            private String getAnnotationText(OWLAnnotation annotation) {
                return styledStringRenderer.getRendering(annotation).getString();
            }
        };
    }
}
