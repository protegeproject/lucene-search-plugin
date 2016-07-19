package org.protege.editor.search.nci;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.search.lucene.IndexField;
import org.protege.editor.search.lucene.RemoveChangeSetHandler;

import org.apache.lucene.index.Term;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.vocab.XSDVocabulary;

import java.util.ArrayList;
import java.util.List;

public class SearchTabRemoveChangeSetHandler extends RemoveChangeSetHandler {

    public SearchTabRemoveChangeSetHandler(OWLEditorKit editorKit) {
        super(editorKit);
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
                OWLAnnotationValue value = axiom.getAnnotation().getValue();
                if (value instanceof OWLLiteral) {
                    OWLLiteral literal = (OWLLiteral) value;
                    if (literal.getDatatype().getIRI().equals(XSDVocabulary.ANY_URI.getIRI())) {
                        terms.add(new Term(IndexField.ANNOTATION_VALUE_IRI, literal.getLiteral()));
                    }
                    else {
                        terms.add(new Term(IndexField.ANNOTATION_TEXT, strip(literal.getLiteral())));
                    }
                }
                else if (value instanceof IRI) {
                    IRI iri = (IRI) value;
                    terms.add(new Term(IndexField.ANNOTATION_VALUE_IRI, iri.toString()));
                }
                removeFilters.add(terms);
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
