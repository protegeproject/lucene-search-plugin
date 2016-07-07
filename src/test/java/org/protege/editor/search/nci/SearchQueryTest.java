package org.protege.editor.search.nci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManagerImpl;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.protege.editor.search.lucene.IndexDelegator;
import org.protege.editor.search.lucene.LuceneSearcher;
import org.protege.editor.search.lucene.QueryEvaluationException;
import org.protege.editor.search.lucene.SearchContext;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class SearchQueryTest {

    private SearchTabIndexer indexer;

    private IndexDelegator delegator;

    @Mock
    private OWLModelManagerImpl mngr;

    @Mock
    private OWLEntityFinder entityFinder;

    @Mock
    private OWLEditorKit editorKit;

    @Mock
    private SearchContext searchContext;

    @Mock
    private LuceneSearcher searcher;

    @Before
    public void setUp() throws Exception {
        OWLOntologyManager ontologyMngr = OWLManager.createOWLOntologyManager();
        when(mngr.getOWLOntologyManager()).thenReturn(ontologyMngr);
        when(mngr.getRendering(any(OWLObject.class))).thenAnswer(
                new Answer<String>() {
                @Override
                public String answer(InvocationOnMock invocation){
                    OWLObject arg = (OWLObject) invocation.getArguments()[0];
                    if (arg instanceof OWLEntity) {
                        String iriString = ((OWLEntity) arg).getIRI().toString();
                        return iriString.substring(iriString.lastIndexOf('#'));
                    }
                    else {
                        return arg.toString();
                    }
                }});

        OWLOntology koalaOntology = KoalaOntology.load(ontologyMngr);
        when(entityFinder.getEntities(any(IRI.class))).thenAnswer(
                new Answer<Set<OWLEntity>>() {
                    @Override
                    public Set<OWLEntity> answer(InvocationOnMock invocation){
                        IRI arg = (IRI) invocation.getArguments()[0];
                        return koalaOntology.getEntitiesInSignature(arg);
                    }});

        when(editorKit.getOWLModelManager()).thenReturn(mngr);
        when(editorKit.getOWLModelManager().getOWLEntityFinder()).thenReturn(entityFinder);
        indexer = new SearchTabIndexer(editorKit);
        
        Directory directory = new RAMDirectory();
        Set<OWLOntology> activeOntologies = new HashSet<>();
        activeOntologies.add(koalaOntology);
        when(searchContext.getOntologies()).thenReturn(activeOntologies);
        
        delegator = IndexDelegator.getInstance(directory, indexer.getIndexWriterConfig());
        indexer.doIndex(delegator, searchContext, null);
    }

    @Test
    public void testContainsQuery() throws IOException, QueryEvaluationException {
        KeywordQuery query = getQueryFactory().createContainsFilter(KoalaOntology.rdfsLabel, "male");
        Set<OWLEntity> results = query.evaluate(null);
        assertThat(results, hasSize(2));
        assertThat(results, containsInAnyOrder(
                KoalaOntology.female,
                KoalaOntology.male));
    }

    @Test
    public void testExactMatchQuery() throws IOException, QueryEvaluationException {
        KeywordQuery query = getQueryFactory().createExactMatchFilter(KoalaOntology.rdfsLabel, "male");
        Set<OWLEntity> results = query.evaluate(null);
        assertThat(results, hasSize(1));
        assertThat(results, containsInAnyOrder(KoalaOntology.male));
    }

    @Test
    public void testStartsWithQuery() throws IOException, QueryEvaluationException {
        KeywordQuery query = getQueryFactory().createStartsWithFilter(KoalaOntology.rdfsLabel, "fem");
        Set<OWLEntity> results = query.evaluate(null);
        assertThat(results, hasSize(1));
        assertThat(results, containsInAnyOrder(KoalaOntology.female));
    }

    @Test
    public void testEndsWithQuery() throws IOException, QueryEvaluationException {
        KeywordQuery query = getQueryFactory().createEndsWithFilter(KoalaOntology.rdfsLabel, "male");
        Set<OWLEntity> results = query.evaluate(null);
        assertThat(results, hasSize(2));
        assertThat(results, containsInAnyOrder(
                KoalaOntology.female,
                KoalaOntology.male));
    }

    @Test
    public void testPropertyValuePresentQuery() throws IOException, QueryEvaluationException {
        /*
         * AP: rdfs:label
         */
        PropertyValuePresent query = getQueryFactory().createProperyValuePresentFilter(KoalaOntology.rdfsLabel);
        Set<OWLEntity> results = query.evaluate(null);
        assertThat(results, hasSize(8));
        assertThat(results, containsInAnyOrder(
                KoalaOntology.female,
                KoalaOntology.university,
                KoalaOntology.koala,
                KoalaOntology.rainForest,
                KoalaOntology.dryEucalyptForest,
                KoalaOntology.quokka,
                KoalaOntology.tasmanianDevil,
                KoalaOntology.male));
        
        /*
         * OP: hasHabitat
         */
        query = getQueryFactory().createProperyValuePresentFilter(KoalaOntology.hasHabitat);
        results = query.evaluate(null);
        assertThat(results, hasSize(0));
        
        /*
         * OP: hasChildren
         */
        query = getQueryFactory().createProperyValuePresentFilter(KoalaOntology.hasChildren);
        results = query.evaluate(null);
        assertThat(results, hasSize(0));
        
        /*
         * OP: hasDegree
         */
        query = getQueryFactory().createProperyValuePresentFilter(KoalaOntology.hasDegree);
        results = query.evaluate(null);
        assertThat(results, hasSize(0));
        
        /*
         * OP: hasGender
         */
        query = getQueryFactory().createProperyValuePresentFilter(KoalaOntology.hasGender);
        results = query.evaluate(null);
        assertThat(results, hasSize(0));
        
        /*
         * DP: isHardWorking
         */
        query = getQueryFactory().createProperyValuePresentFilter(KoalaOntology.isHardWorking);
        results = query.evaluate(null);
        assertThat(results, hasSize(0));
    }

    @Test
    public void testPropertyValueAbsentQuery() throws IOException, QueryEvaluationException {
        /*
         * AP: rdfs:label
         */
        PropertyValueAbsent query = getQueryFactory().createPropertyValueAbsentFilter(KoalaOntology.rdfsLabel);
        Set<OWLEntity> results = query.evaluate(null);
        assertThat(results, hasSize(29));
        assertThat(results, containsInAnyOrder(
                KoalaOntology.gender,
                KoalaOntology.marsupials,
                KoalaOntology.student,
                KoalaOntology.koalaWithPhd,
                KoalaOntology.animal,
                KoalaOntology.habitat,
                KoalaOntology.forest,
                KoalaOntology.graduateStudent,
                KoalaOntology.parent,
                KoalaOntology.maleStudentWith3Daughters,
                KoalaOntology.degree,
                KoalaOntology.person, // 12 classes
                KoalaOntology.hasHabitat,
                KoalaOntology.hasDegree,
                KoalaOntology.hasChildren,
                KoalaOntology.hasGender,
                KoalaOntology.isHardWorking, // 5 properties
                KoalaOntology._male,
                KoalaOntology._female,
                KoalaOntology._ba,
                KoalaOntology._bs,
                KoalaOntology._ma,
                KoalaOntology._phd, // 6 individuals
                KoalaOntology.owlThing,
                KoalaOntology.owlVersionInfo,
                KoalaOntology.rdfsSeeAlso,
                KoalaOntology.rdfsLabel,
                KoalaOntology.rdfPlainLiteral,
                KoalaOntology.xsdBoolean));
        
        /*
         * OP: hasHabitat
         */
        query = getQueryFactory().createPropertyValueAbsentFilter(KoalaOntology.hasHabitat);
        results = query.evaluate(null);
        assertThat(results, hasSize(37));
        assertThat(results, containsInAnyOrder(KoalaOntology.allEntities.toArray()));
        
        /*
         * OP: hasChildren
         */
        query = getQueryFactory().createPropertyValueAbsentFilter(KoalaOntology.hasChildren);
        results = query.evaluate(null);
        assertThat(results, hasSize(37));
        assertThat(results, containsInAnyOrder(KoalaOntology.allEntities.toArray()));
        
        /*
         * OP: hasDegree
         */
        query = getQueryFactory().createPropertyValueAbsentFilter(KoalaOntology.hasDegree);
        results = query.evaluate(null);
        assertThat(results, hasSize(37));
        assertThat(results, containsInAnyOrder(KoalaOntology.allEntities.toArray()));
        
        /*
         * OP: hasGender
         */
        query = getQueryFactory().createPropertyValueAbsentFilter(KoalaOntology.hasGender);
        results = query.evaluate(null);
        assertThat(results, hasSize(37));
        assertThat(results, containsInAnyOrder(KoalaOntology.allEntities.toArray()));
        
        /*
         * DP: isHardWorking
         */
        query = getQueryFactory().createPropertyValueAbsentFilter(KoalaOntology.isHardWorking);
        results = query.evaluate(null);
        assertThat(results, hasSize(37));
        assertThat(results, containsInAnyOrder(KoalaOntology.allEntities.toArray()));
    }

    @Test
    public void testPropertyRestrictionPresentQuery() throws IOException, QueryEvaluationException {
        /*
         * OP: hasHabitat
         */
        PropertyRestrictionPresent query = getQueryFactory().createPropertyRestrictionPresentFilter(KoalaOntology.hasHabitat);
        Set<OWLEntity> results = query.evaluate(null);
        assertThat(results, hasSize(3));
        assertThat(results, containsInAnyOrder(
                KoalaOntology.animal,
                KoalaOntology.koala,
                KoalaOntology.student));
        
        /*
         * OP: hasChildren
         */
        query = getQueryFactory().createPropertyRestrictionPresentFilter(KoalaOntology.hasChildren);
        results = query.evaluate(null);
        assertThat(results, hasSize(2));
        assertThat(results, containsInAnyOrder(
                KoalaOntology.maleStudentWith3Daughters,
                KoalaOntology.parent));
        
        /*
         * OP: hasDegree
         */
        query = getQueryFactory().createPropertyRestrictionPresentFilter(KoalaOntology.hasDegree);
        results = query.evaluate(null);
        assertThat(results, hasSize(2));
        assertThat(results, containsInAnyOrder(
                KoalaOntology.graduateStudent,
                KoalaOntology.koalaWithPhd));
        
        /*
         * OP: hasGender
         */
        query = getQueryFactory().createPropertyRestrictionPresentFilter(KoalaOntology.hasGender);
        results = query.evaluate(null);
        assertThat(results, hasSize(4));
        assertThat(results, containsInAnyOrder(
                KoalaOntology.animal,
                KoalaOntology.female,
                KoalaOntology.male,
                KoalaOntology.maleStudentWith3Daughters));
        
        /*
         * DP: isHardWorking
         */
        query = getQueryFactory().createPropertyRestrictionPresentFilter(KoalaOntology.isHardWorking);
        results = query.evaluate(null);
        assertThat(results, hasSize(3));
        assertThat(results, containsInAnyOrder(
                KoalaOntology.koala,
                KoalaOntology.quokka,
                KoalaOntology.student));
    }

    @Test
    public void testPropertyRestrictionAbsentQuery() throws IOException, QueryEvaluationException {
        /*
         * OP: hasHabitat
         */
        PropertyRestrictionAbsent query = getQueryFactory().createPropertyRestrictionAbsentFilter(KoalaOntology.hasHabitat);
        Set<OWLEntity> results = query.evaluate(null);
        assertThat(results, hasSize(18));
        assertThat(results, containsInAnyOrder(
                KoalaOntology.gender,
                KoalaOntology.female,
                KoalaOntology.marsupials,
                KoalaOntology.koalaWithPhd,
                KoalaOntology.university,
                KoalaOntology.habitat,
                KoalaOntology.forest,
                KoalaOntology.rainForest,
                KoalaOntology.graduateStudent,
                KoalaOntology.parent,
                KoalaOntology.dryEucalyptForest,
                KoalaOntology.quokka,
                KoalaOntology.tasmanianDevil,
                KoalaOntology.maleStudentWith3Daughters,
                KoalaOntology.degree,
                KoalaOntology.male,
                KoalaOntology.person,
                KoalaOntology.owlThing));
        
        /*
         * OP: hasChildren
         */
        query = getQueryFactory().createPropertyRestrictionAbsentFilter(KoalaOntology.hasChildren);
        results = query.evaluate(null);
        assertThat(results, hasSize(19));
        assertThat(results, containsInAnyOrder(
                KoalaOntology.animal,
                KoalaOntology.gender,
                KoalaOntology.female,
                KoalaOntology.marsupials,
                KoalaOntology.koala,
                KoalaOntology.koalaWithPhd,
                KoalaOntology.university,
                KoalaOntology.habitat,
                KoalaOntology.forest,
                KoalaOntology.rainForest,
                KoalaOntology.graduateStudent,
                KoalaOntology.dryEucalyptForest,
                KoalaOntology.quokka,
                KoalaOntology.student,
                KoalaOntology.tasmanianDevil,
                KoalaOntology.degree,
                KoalaOntology.male,
                KoalaOntology.person,
                KoalaOntology.owlThing));
        
        /*
         * OP: hasDegree
         */
        query = getQueryFactory().createPropertyRestrictionAbsentFilter(KoalaOntology.hasDegree);
        results = query.evaluate(null);
        assertThat(results, hasSize(19));
        assertThat(results, containsInAnyOrder(
                KoalaOntology.animal,
                KoalaOntology.degree,
                KoalaOntology.dryEucalyptForest,
                KoalaOntology.female,
                KoalaOntology.forest,
                KoalaOntology.gender,
                KoalaOntology.habitat,
                KoalaOntology.koala,
                KoalaOntology.male,
                KoalaOntology.maleStudentWith3Daughters,
                KoalaOntology.marsupials,
                KoalaOntology.parent,
                KoalaOntology.person,
                KoalaOntology.quokka,
                KoalaOntology.rainForest,
                KoalaOntology.student,
                KoalaOntology.tasmanianDevil,
                KoalaOntology.university,
                KoalaOntology.owlThing));
        
        /*
         * OP: hasGender
         */
        query = getQueryFactory().createPropertyRestrictionAbsentFilter(KoalaOntology.hasGender);
        results = query.evaluate(null);
        assertThat(results, hasSize(17));
        assertThat(results, containsInAnyOrder(
                KoalaOntology.degree,
                KoalaOntology.dryEucalyptForest,
                KoalaOntology.forest,
                KoalaOntology.gender,
                KoalaOntology.graduateStudent,
                KoalaOntology.habitat,
                KoalaOntology.koala,
                KoalaOntology.koalaWithPhd,
                KoalaOntology.marsupials,
                KoalaOntology.parent,
                KoalaOntology.person,
                KoalaOntology.quokka,
                KoalaOntology.rainForest,
                KoalaOntology.student,
                KoalaOntology.tasmanianDevil,
                KoalaOntology.university,
                KoalaOntology.owlThing));
        
        /*
         * DP: isHardWorking
         */
        query = getQueryFactory().createPropertyRestrictionAbsentFilter(KoalaOntology.isHardWorking);
        results = query.evaluate(null);
        assertThat(results, hasSize(18));
        assertThat(results, containsInAnyOrder(
                KoalaOntology.animal,
                KoalaOntology.degree,
                KoalaOntology.dryEucalyptForest,
                KoalaOntology.female,
                KoalaOntology.forest,
                KoalaOntology.gender,
                KoalaOntology.graduateStudent,
                KoalaOntology.habitat,
                KoalaOntology.koalaWithPhd,
                KoalaOntology.male,
                KoalaOntology.maleStudentWith3Daughters,
                KoalaOntology.marsupials,
                KoalaOntology.parent,
                KoalaOntology.person,
                KoalaOntology.rainForest,
                KoalaOntology.tasmanianDevil,
                KoalaOntology.university,
                KoalaOntology.owlThing));
    }

    private BasicQuery.Factory getQueryFactory() throws IOException {
        LuceneSearcher searcher = new ThinLuceneSearcher(delegator.getSearcher(), editorKit);
        return new BasicQuery.Factory(searchContext, searcher);
    }
}
