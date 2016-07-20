package org.protege.editor.search.nci;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.search.lucene.IndexField;
import org.protege.editor.search.lucene.RemoveChangeSetHandler;

import org.apache.lucene.index.Term;
import org.semanticweb.owlapi.model.HasFiller;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLBooleanClassExpression;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
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
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLRestriction;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.util.AxiomSubjectProvider;
import org.semanticweb.owlapi.vocab.XSDVocabulary;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SearchTabRemoveChangeSetHandler extends RemoveChangeSetHandler implements OWLAxiomVisitor {

    public SearchTabRemoveChangeSetHandler(OWLEditorKit editorKit) {
        super(editorKit);
    }

    @Override
    public void visit(RemoveAxiom change) {
        OWLAxiom changeAxiom = change.getAxiom();
        changeAxiom.accept(this);
    }

    @Override
    public void visit(OWLDeclarationAxiom axiom) {
        OWLEntity entity = axiom.getEntity();
        List<Term> terms = new ArrayList<>();
        terms.add(new Term(IndexField.ENTITY_IRI, getIri(entity)));
        terms.add(new Term(IndexField.ENTITY_TYPE, getType(entity)));
        removeFilters.add(terms);
    }

    @Override
    public void visit(OWLAnnotationAssertionAxiom axiom) {
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

    @Override
    public void visit(OWLSubClassOfAxiom axiom) {
        visitLogicalAxiom(axiom);
        if (!(axiom.getSubClass() instanceof OWLClass)) {
            return;
        }
        OWLClass cls = axiom.getSubClass().asOWLClass();
        if (axiom.getSuperClass() instanceof OWLRestriction) {
            OWLRestriction restriction = (OWLRestriction) axiom.getSuperClass();
            visitObjectRestriction(cls, restriction);
        }
        else if (axiom.getSuperClass() instanceof OWLBooleanClassExpression) {
            OWLBooleanClassExpression expr = (OWLBooleanClassExpression) axiom.getSuperClass();
            if (expr instanceof OWLObjectIntersectionOf) {
                for (OWLClassExpression ce : expr.asConjunctSet()) {
                    if (ce instanceof OWLRestriction) {
                        visitObjectRestriction(cls, (OWLRestriction) ce);
                    }
                }
            }
            else if (expr instanceof OWLObjectUnionOf) {
                for (OWLClassExpression ce : expr.asDisjunctSet()) {
                    if (ce instanceof OWLRestriction) {
                        visitObjectRestriction(cls, (OWLRestriction) ce);
                    }
                }
            }
            else if (expr instanceof OWLObjectComplementOf) {
                OWLClassExpression ce = ((OWLObjectComplementOf) expr).getObjectComplementOf();
                if (ce instanceof OWLRestriction) {
                    visitObjectRestriction(cls, (OWLRestriction) ce);
                }
            }
        }
    }

    private void visitObjectRestriction(OWLClass subclass, OWLRestriction restriction) {
        if (restriction.getProperty() instanceof OWLProperty) {
            OWLProperty property = (OWLProperty) restriction.getProperty();
            if (restriction instanceof HasFiller<?>) {
                List<Term> terms = new ArrayList<>();
                HasFiller<?> restrictionWithFiller = (HasFiller<?>) restriction;
                if (restrictionWithFiller.getFiller() instanceof OWLClass) {
                    OWLClass filler = (OWLClass) restrictionWithFiller.getFiller();
                    terms.add(new Term(IndexField.ENTITY_IRI, getIri(subclass)));
                    terms.add(new Term(IndexField.OBJECT_PROPERTY_IRI, getIri(property)));
                    terms.add(new Term(IndexField.FILLER_IRI, getIri(filler)));
                }
                else {
                    terms.add(new Term(IndexField.ENTITY_IRI, getIri(subclass)));
                    terms.add(new Term(IndexField.OBJECT_PROPERTY_IRI, getIri(property)));
                    terms.add(new Term(IndexField.FILLER_IRI, ""));
                }
                removeFilters.add(terms);
            }
        }
    }

    @Override
    public void visit(OWLEquivalentClassesAxiom axiom) {
        visitLogicalAxiom(axiom);
        Set<OWLSubClassOfAxiom> subClassAxioms = axiom.asOWLSubClassOfAxioms();
        for (OWLSubClassOfAxiom sc : subClassAxioms) {
            sc.accept(this);
        }
    }

    //@formatter:off
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
        List<Term> terms = new ArrayList<>();
        OWLObject subject = new AxiomSubjectProvider().getSubject(axiom);
        if (subject instanceof OWLEntity) {
            OWLEntity entity = (OWLEntity) subject;
            terms.add(new Term(IndexField.ENTITY_IRI, getIri(entity)));
            terms.add(new Term(IndexField.AXIOM_DISPLAY_NAME, getDisplayName(axiom)));
            terms.add(new Term(IndexField.AXIOM_TYPE, getType(axiom)));
            removeFilters.add(terms);
        }
    }

    //@formatter:off
    @Override public void visit(OWLSubAnnotationPropertyOfAxiom axiom) { doesNothing(); }
    @Override public void visit(OWLAnnotationPropertyDomainAxiom axiom) { doesNothing(); }
    @Override public void visit(OWLAnnotationPropertyRangeAxiom axiom) { doesNothing(); }
    @Override public void visit(SWRLRule rule) { doesNothing(); }
    @Override public void visit(OWLDatatypeDefinitionAxiom axiom) { doesNothing(); }

    //@formatter:on
    private void doesNothing() {
        // NO-OP
    }

    private String strip(String s) {
        return s.replaceAll("\\^\\^.*$", "") // remove datatype ending
                .replaceAll("^\"|\"$", "") // remove enclosed quotes
                .replaceAll("<[^>]+>", " ") // trim XML tags
                .replaceAll("\\s+", " ") // trim excessive white spaces
                .trim();
    }
}
