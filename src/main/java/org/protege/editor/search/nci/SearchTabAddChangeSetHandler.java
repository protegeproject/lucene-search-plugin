package org.protege.editor.search.nci;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.search.lucene.AddChangeSetHandler;
import org.protege.editor.search.lucene.IndexField;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.vocab.XSDVocabulary;

public class SearchTabAddChangeSetHandler extends AddChangeSetHandler {

    public SearchTabAddChangeSetHandler(OWLEditorKit editorKit) {
        super(editorKit);
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
                doc.add(new StringField(IndexField.ANNOTATION_IRI, getIri(axiom.getProperty()), Store.YES));
                doc.add(new TextField(IndexField.ANNOTATION_DISPLAY_NAME, getDisplayName(axiom.getProperty()), Store.YES));
                OWLAnnotationValue value = axiom.getAnnotation().getValue();
                if (value instanceof OWLLiteral) {
                    OWLLiteral literal = (OWLLiteral) value;
                    if (literal.getDatatype().getIRI().equals(XSDVocabulary.ANY_URI.getIRI())) {
                        doc.add(new StringField(IndexField.ANNOTATION_VALUE_IRI, literal.getLiteral(), Store.YES));
                    }
                    else {
                        doc.add(new TextField(IndexField.ANNOTATION_TEXT, strip(literal.getLiteral()), Store.YES));
                    }
                }
                else if (value instanceof IRI) {
                    IRI iri = (IRI) value;
                    doc.add(new StringField(IndexField.ANNOTATION_VALUE_IRI, iri.toString(), Store.YES));
                }
                documents.add(doc);
            }
        }
    }

    private String strip(String s) {
        return s.replaceAll("\\^\\^.*$", "") // remove datatype ending
                .replaceAll("^\"|\"$", "") // remove enclosed quotes
                .replaceAll("<[^>]+>", " ") // trim XML tags
                .replaceAll("\\s+", " ") // trim excessive white spaces
                .trim();
    }
}
