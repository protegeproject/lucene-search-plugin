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
        KeywordQuery containsQuery = getQueryFactory().createContainsFilter(KoalaOntology.rdfsLabel, "male");
        Set<OWLEntity> results = containsQuery.evaluate(null);
        assertThat(results, hasSize(2));
        assertThat(results, containsInAnyOrder(
                KoalaOntology.female,
                KoalaOntology.male));
    }

    @Test
    public void testExactMatchQuery() throws IOException, QueryEvaluationException {
        KeywordQuery containsQuery = getQueryFactory().createExactMatchFilter(KoalaOntology.rdfsLabel, "male");
        Set<OWLEntity> results = containsQuery.evaluate(null);
        assertThat(results, hasSize(1));
        assertThat(results, containsInAnyOrder(KoalaOntology.male));
    }

    @Test
    public void testStartsWithQuery() throws IOException, QueryEvaluationException {
        KeywordQuery containsQuery = getQueryFactory().createStartsWithFilter(KoalaOntology.rdfsLabel, "fem");
        Set<OWLEntity> results = containsQuery.evaluate(null);
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
        PropertyValuePresent containsQuery = getQueryFactory().createProperyValuePresentFilter(KoalaOntology.rdfsLabel);
        Set<OWLEntity> results = containsQuery.evaluate(null);
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
    }

    @Test
    public void testPropertyValueAbsentQuery() throws IOException, QueryEvaluationException {
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
                KoalaOntology.person,
                KoalaOntology.hasHabitat,
                KoalaOntology.hasDegree,
                KoalaOntology.hasChildren,
                KoalaOntology.hasGender,
                KoalaOntology.isHardWorking,
                KoalaOntology._male,
                KoalaOntology._female,
                KoalaOntology._ba,
                KoalaOntology._bs,
                KoalaOntology._ma,
                KoalaOntology._phd,
                KoalaOntology.owlThing,
                KoalaOntology.owlVersionInfo,
                KoalaOntology.rdfsSeeAlso,
                KoalaOntology.rdfsLabel,
                KoalaOntology.rdfPlainLiteral,
                KoalaOntology.xsdBoolean));
    }

    @Test
    public void testPropertyRestrictionPresentQuery() throws IOException, QueryEvaluationException {
        PropertyRestrictionPresent containsQuery = getQueryFactory().createPropertyRestrictionPresentFilter(KoalaOntology.hasHabitat);
        Set<OWLEntity> results = containsQuery.evaluate(null);
        assertThat(results, hasSize(3));
        assertThat(results, containsInAnyOrder(
                KoalaOntology.animal,
                KoalaOntology.koala,
                KoalaOntology.student));
    }

    @Test
    public void testPropertyRestrictionAbsentQuery() throws IOException, QueryEvaluationException {
        PropertyRestrictionAbsent containsQuery = getQueryFactory().createPropertyRestrictionAbsentFilter(KoalaOntology.hasHabitat);
        Set<OWLEntity> results = containsQuery.evaluate(null);
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
    }

    private BasicQuery.Factory getQueryFactory() throws IOException {
        LuceneSearcher searcher = new ThinLuceneSearcher(delegator.getSearcher(), editorKit);
        return new BasicQuery.Factory(searchContext, searcher);
    }
}
