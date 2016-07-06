package org.protege.editor.search.nci;

import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.AnnotationAssertion;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.AnnotationProperty;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.Class;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.DataProperty;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.Datatype;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.IRI;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.NamedIndividual;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.ObjectProperty;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.PlainLiteral;

import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.Namespaces;

public final class KoalaOntology {

    public static final String ONTOLOGY_ID = "http://protege.stanford.edu/plugins/owl/owl-library/koala.owl#";

    public static final String KOALA = "<?xml version=\"1.0\"?>\n"
        + "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns=\"http://protege.stanford.edu/plugins/owl/owl-library/koala.owl#\" xml:base=\"http://protege.stanford.edu/plugins/owl/owl-library/koala.owl\">\n"
        + "  <owl:Ontology rdf:about=\"\"/>\n"
        + "  <owl:Class rdf:ID=\"Female\" rdfs:label=\"Female\"><owl:equivalentClass><owl:Restriction><owl:onProperty><owl:FunctionalProperty rdf:about=\"#hasGender\"/></owl:onProperty><owl:hasValue><Gender rdf:ID=\"female\"/></owl:hasValue></owl:Restriction></owl:equivalentClass></owl:Class>\n"
        + "  <owl:Class rdf:ID=\"Marsupials\"><owl:disjointWith><owl:Class rdf:about=\"#Person\"/></owl:disjointWith><rdfs:subClassOf><owl:Class rdf:about=\"#Animal\"/></rdfs:subClassOf></owl:Class>\n"
        + "  <owl:Class rdf:ID=\"Student\"><owl:equivalentClass><owl:Class><owl:intersectionOf rdf:parseType=\"Collection\"><owl:Class rdf:about=\"#Person\"/><owl:Restriction><owl:onProperty><owl:FunctionalProperty rdf:about=\"#isHardWorking\"/></owl:onProperty><owl:hasValue rdf:datatype=\"http://www.w3.org/2001/XMLSchema#boolean\">true</owl:hasValue></owl:Restriction><owl:Restriction><owl:someValuesFrom><owl:Class rdf:about=\"#University\"/></owl:someValuesFrom><owl:onProperty><owl:ObjectProperty rdf:about=\"#hasHabitat\"/></owl:onProperty></owl:Restriction></owl:intersectionOf></owl:Class></owl:equivalentClass></owl:Class>\n"
        + "  <owl:Class rdf:ID=\"KoalaWithPhD\"><owl:versionInfo>1.2</owl:versionInfo><owl:equivalentClass><owl:Class><owl:intersectionOf rdf:parseType=\"Collection\"><owl:Restriction><owl:hasValue><Degree rdf:ID=\"PhD\"/></owl:hasValue><owl:onProperty><owl:ObjectProperty rdf:about=\"#hasDegree\"/></owl:onProperty></owl:Restriction><owl:Class rdf:about=\"#Koala\"/></owl:intersectionOf></owl:Class></owl:equivalentClass></owl:Class>\n"
        + "  <owl:Class rdf:ID=\"University\" rdfs:label=\"University\"><rdfs:subClassOf><owl:Class rdf:ID=\"Habitat\"/></rdfs:subClassOf></owl:Class>\n"
        + "  <owl:Class rdf:ID=\"Koala\" rdfs:label=\"Koala\"><rdfs:subClassOf><owl:Restriction><owl:hasValue rdf:datatype=\"http://www.w3.org/2001/XMLSchema#boolean\">false</owl:hasValue><owl:onProperty><owl:FunctionalProperty rdf:about=\"#isHardWorking\"/></owl:onProperty></owl:Restriction></rdfs:subClassOf><rdfs:subClassOf><owl:Restriction><owl:someValuesFrom><owl:Class rdf:about=\"#DryEucalyptForest\"/></owl:someValuesFrom><owl:onProperty><owl:ObjectProperty rdf:about=\"#hasHabitat\"/></owl:onProperty></owl:Restriction></rdfs:subClassOf><rdfs:subClassOf rdf:resource=\"#Marsupials\"/></owl:Class>\n"
        + "  <owl:Class rdf:ID=\"Animal\"><rdfs:seeAlso>Male</rdfs:seeAlso><rdfs:subClassOf><owl:Restriction><owl:onProperty><owl:ObjectProperty rdf:about=\"#hasHabitat\"/></owl:onProperty><owl:minCardinality rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\">1</owl:minCardinality></owl:Restriction></rdfs:subClassOf><rdfs:subClassOf><owl:Restriction><owl:cardinality rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\">1</owl:cardinality><owl:onProperty><owl:FunctionalProperty rdf:about=\"#hasGender\"/></owl:onProperty></owl:Restriction></rdfs:subClassOf><owl:versionInfo>1.1</owl:versionInfo></owl:Class>\n"
        + "  <owl:Class rdf:ID=\"Forest\"><rdfs:subClassOf rdf:resource=\"#Habitat\"/></owl:Class>\n"
        + "  <owl:Class rdf:ID=\"Rainforest\" rdfs:label=\"Rain Forest\"><rdfs:subClassOf rdf:resource=\"#Forest\"/></owl:Class>\n"
        + "  <owl:Class rdf:ID=\"GraduateStudent\"><rdfs:subClassOf><owl:Restriction><owl:onProperty><owl:ObjectProperty rdf:about=\"#hasDegree\"/></owl:onProperty><owl:someValuesFrom><owl:Class><owl:oneOf rdf:parseType=\"Collection\"><Degree rdf:ID=\"BA\"/><Degree rdf:ID=\"BS\"/></owl:oneOf></owl:Class></owl:someValuesFrom></owl:Restriction></rdfs:subClassOf><rdfs:subClassOf rdf:resource=\"#Student\"/></owl:Class>\n"
        + "  <owl:Class rdf:ID=\"Parent\"><owl:equivalentClass><owl:Class><owl:intersectionOf rdf:parseType=\"Collection\"><owl:Class rdf:about=\"#Animal\"/><owl:Restriction><owl:onProperty><owl:ObjectProperty rdf:about=\"#hasChildren\"/></owl:onProperty><owl:minCardinality rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\">1</owl:minCardinality></owl:Restriction></owl:intersectionOf></owl:Class></owl:equivalentClass><rdfs:subClassOf rdf:resource=\"#Animal\"/></owl:Class>\n"
        + "  <owl:Class rdf:ID=\"DryEucalyptForest\" rdfs:label=\"Dry Eucalypt Forest\"><rdfs:subClassOf rdf:resource=\"#Forest\"/></owl:Class>\n"
        + "  <owl:Class rdf:ID=\"Quokka\" rdfs:label=\"Quokka\"><rdfs:subClassOf><owl:Restriction><owl:hasValue rdf:datatype=\"http://www.w3.org/2001/XMLSchema#boolean\">true</owl:hasValue><owl:onProperty><owl:FunctionalProperty rdf:about=\"#isHardWorking\"/></owl:onProperty></owl:Restriction></rdfs:subClassOf><rdfs:subClassOf rdf:resource=\"#Marsupials\"/></owl:Class>\n"
        + "  <owl:Class rdf:ID=\"TasmanianDevil\" rdfs:label=\"Tasmanian Devil\"><rdfs:subClassOf rdf:resource=\"#Marsupials\"/></owl:Class>\n"
        + "  <owl:Class rdf:ID=\"MaleStudentWith3Daughters\"><owl:equivalentClass><owl:Class><owl:intersectionOf rdf:parseType=\"Collection\"><owl:Class rdf:about=\"#Student\"/><owl:Restriction><owl:onProperty><owl:FunctionalProperty rdf:about=\"#hasGender\"/></owl:onProperty><owl:hasValue><Gender rdf:ID=\"male\"/></owl:hasValue></owl:Restriction><owl:Restriction><owl:onProperty><owl:ObjectProperty rdf:about=\"#hasChildren\"/></owl:onProperty><owl:cardinality rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\">3</owl:cardinality></owl:Restriction><owl:Restriction><owl:allValuesFrom rdf:resource=\"#Female\"/><owl:onProperty><owl:ObjectProperty rdf:about=\"#hasChildren\"/></owl:onProperty></owl:Restriction></owl:intersectionOf></owl:Class></owl:equivalentClass></owl:Class>\n"
        + "  <owl:Class rdf:ID=\"Degree\"/>\n  <owl:Class rdf:ID=\"Gender\"/>\n"
        + "  <owl:Class rdf:ID=\"Male\" rdfs:label=\"Male\"><owl:equivalentClass><owl:Restriction><owl:hasValue rdf:resource=\"#male\"/><owl:onProperty><owl:FunctionalProperty rdf:about=\"#hasGender\"/></owl:onProperty></owl:Restriction></owl:equivalentClass></owl:Class>\n"
        + "  <owl:Class rdf:ID=\"Person\"><rdfs:subClassOf rdf:resource=\"#Animal\"/><owl:disjointWith rdf:resource=\"#Marsupials\"/></owl:Class>\n"
        + "  <owl:ObjectProperty rdf:ID=\"hasHabitat\"><rdfs:range rdf:resource=\"#Habitat\"/><rdfs:domain rdf:resource=\"#Animal\"/></owl:ObjectProperty>\n"
        + "  <owl:ObjectProperty rdf:ID=\"hasDegree\"><rdfs:domain rdf:resource=\"#Person\"/><rdfs:range rdf:resource=\"#Degree\"/></owl:ObjectProperty>\n"
        + "  <owl:ObjectProperty rdf:ID=\"hasChildren\"><rdfs:range rdf:resource=\"#Animal\"/><rdfs:domain rdf:resource=\"#Animal\"/></owl:ObjectProperty>\n"
        + "  <owl:FunctionalProperty rdf:ID=\"hasGender\"><rdfs:range rdf:resource=\"#Gender\"/><rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#ObjectProperty\"/><rdfs:domain rdf:resource=\"#Animal\"/></owl:FunctionalProperty>\n"
        + "  <owl:FunctionalProperty rdf:ID=\"isHardWorking\"><rdfs:range rdf:resource=\"http://www.w3.org/2001/XMLSchema#boolean\"/><rdfs:domain rdf:resource=\"#Person\"/><rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#DatatypeProperty\"/></owl:FunctionalProperty>\n"
        + "  <Degree rdf:ID=\"MA\"/>\n"
        + "</rdf:RDF>";

    public static final IRI genderIri = IRI(ONTOLOGY_ID, "Gender");
    public static final IRI femaleIri = IRI(ONTOLOGY_ID, "Female");
    public static final IRI marsupialsIri = IRI(ONTOLOGY_ID, "Marsupials");
    public static final IRI studentIri = IRI(ONTOLOGY_ID, "Student");
    public static final IRI koalaWithPhdIri = IRI(ONTOLOGY_ID, "KoalaWithPhD");
    public static final IRI universityIri = IRI(ONTOLOGY_ID, "University");
    public static final IRI koalaIri = IRI(ONTOLOGY_ID, "Koala");
    public static final IRI animalIri = IRI(ONTOLOGY_ID, "Animal");
    public static final IRI habitatIri = IRI(ONTOLOGY_ID, "Habitat");
    public static final IRI forestIri = IRI(ONTOLOGY_ID, "Forest");
    public static final IRI rainForestIri = IRI(ONTOLOGY_ID, "Rainforest");
    public static final IRI graduateStudentIri = IRI(ONTOLOGY_ID, "GraduateStudent");
    public static final IRI parentIri = IRI(ONTOLOGY_ID, "Parent");
    public static final IRI dryEucalyptForestIri = IRI(ONTOLOGY_ID, "DryEucalyptForest");
    public static final IRI quokkaIri = IRI(ONTOLOGY_ID, "Quokka");
    public static final IRI tasmanianDevilIri = IRI(ONTOLOGY_ID, "TasmanianDevil");
    public static final IRI maleStudentWith3DaughtersIri = IRI(ONTOLOGY_ID, "MaleStudentWith3Daughters");
    public static final IRI degreeIri = IRI(ONTOLOGY_ID, "Degree");
    public static final IRI maleIri = IRI(ONTOLOGY_ID, "Male");
    public static final IRI personIri = IRI(ONTOLOGY_ID, "Person");
    public static final IRI hasHabitatIri = IRI(ONTOLOGY_ID, "hasHabitat");
    public static final IRI hasDegreeIri = IRI(ONTOLOGY_ID, "hasDegree");
    public static final IRI hasChildrenIri = IRI(ONTOLOGY_ID, "hasChildren");
    public static final IRI hasGenderIri = IRI(ONTOLOGY_ID, "hasGender");
    public static final IRI isHardWorkingIri = IRI(ONTOLOGY_ID, "isHardWorking");

    // Individual IRI
    public static final IRI _maleIri = IRI(ONTOLOGY_ID, "male");
    public static final IRI _femaleIri = IRI(ONTOLOGY_ID, "female");
    public static final IRI _baIri = IRI(ONTOLOGY_ID, "BA");
    public static final IRI _bsIri = IRI(ONTOLOGY_ID, "BS");
    public static final IRI _maIri = IRI(ONTOLOGY_ID, "MA");
    public static final IRI _phdIri = IRI(ONTOLOGY_ID, "PhD");

    public static final OWLClass gender = Class(genderIri);
    public static final OWLClass female = Class(femaleIri);
    public static final OWLClass marsupials = Class(marsupialsIri);
    public static final OWLClass student = Class(studentIri);
    public static final OWLClass koalaWithPhd = Class(koalaWithPhdIri);
    public static final OWLClass university = Class(universityIri);
    public static final OWLClass koala = Class(koalaIri);
    public static final OWLClass animal = Class(animalIri);
    public static final OWLClass habitat = Class(habitatIri);
    public static final OWLClass forest = Class(forestIri);
    public static final OWLClass rainForest = Class(rainForestIri);
    public static final OWLClass graduateStudent = Class(graduateStudentIri);
    public static final OWLClass parent = Class(parentIri);
    public static final OWLClass dryEucalyptForest = Class(dryEucalyptForestIri);
    public static final OWLClass quokka = Class(quokkaIri);
    public static final OWLClass tasmanianDevil = Class(tasmanianDevilIri);
    public static final OWLClass maleStudentWith3Daughters = Class(maleStudentWith3DaughtersIri);
    public static final OWLClass degree = Class(degreeIri);
    public static final OWLClass male = Class(maleIri);
    public static final OWLClass person = Class(personIri);
    public static final OWLObjectProperty hasHabitat = ObjectProperty(hasHabitatIri);
    public static final OWLObjectProperty hasDegree = ObjectProperty(hasDegreeIri);
    public static final OWLObjectProperty hasChildren = ObjectProperty(hasChildrenIri);
    public static final OWLObjectProperty hasGender = ObjectProperty(hasGenderIri);
    public static final OWLDataProperty isHardWorking = DataProperty(isHardWorkingIri);

    // Individuals
    public static final OWLIndividual _male = NamedIndividual(_maleIri);
    public static final OWLIndividual _female = NamedIndividual(_femaleIri);
    public static final OWLIndividual _ba = NamedIndividual(_baIri);
    public static final OWLIndividual _bs = NamedIndividual(_bsIri);
    public static final OWLIndividual _ma = NamedIndividual(_maIri);
    public static final OWLIndividual _phd = NamedIndividual(_phdIri);

    // Default classes / properties / datatypes
    public static final OWLClass owlThing = Class(IRI(Namespaces.OWL + "Thing"));
    public static final OWLAnnotationProperty owlVersionInfo = AnnotationProperty(IRI(Namespaces.OWL + "versionInfo"));
    public static final OWLAnnotationProperty rdfsSeeAlso = AnnotationProperty(IRI(Namespaces.RDFS + "seeAlso"));
    public static final OWLAnnotationProperty rdfsLabel = AnnotationProperty(IRI(Namespaces.RDFS + "label"));
    public static final OWLDatatype rdfPlainLiteral = Datatype(IRI(Namespaces.RDF + "PlainLiteral"));
    public static final OWLDatatype xsdBoolean = Datatype(IRI(Namespaces.XSD + "boolean"));

    public static final OWLAnnotationAssertionAxiom femaleLabel = AnnotationAssertion(rdfsLabel, femaleIri, PlainLiteral("Female"));
    public static final OWLAnnotationAssertionAxiom universityLabel = AnnotationAssertion(rdfsLabel, universityIri, PlainLiteral("University"));
    public static final OWLAnnotationAssertionAxiom koalaLabel = AnnotationAssertion(rdfsLabel, koalaIri, PlainLiteral("Koala"));
    public static final OWLAnnotationAssertionAxiom rainForesetLabel = AnnotationAssertion(rdfsLabel, rainForestIri, PlainLiteral("Rain Forest"));
    public static final OWLAnnotationAssertionAxiom dryEucalyptForestLabel = AnnotationAssertion(rdfsLabel, dryEucalyptForestIri, PlainLiteral("Dry Eucalypt Forest"));
    public static final OWLAnnotationAssertionAxiom quokkaLabel = AnnotationAssertion(rdfsLabel, quokkaIri, PlainLiteral("Quokka"));
    public static final OWLAnnotationAssertionAxiom tasmanianDevilLabel = AnnotationAssertion(rdfsLabel, tasmanianDevilIri, PlainLiteral("Tasmanian Devil"));
    public static final OWLAnnotationAssertionAxiom maleLabel = AnnotationAssertion(rdfsLabel, maleIri, PlainLiteral("Male"));

    /**
     * Produce the OWL ontology given the input {@code OWLOntologyManager}.
     */
    public static OWLOntology load(OWLOntologyManager mngr) throws OWLOntologyCreationException {
        return mngr.loadOntologyFromOntologyDocument(new StringDocumentSource(KOALA));
    }
}
