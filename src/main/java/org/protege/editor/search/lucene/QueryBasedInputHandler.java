package org.protege.editor.search.lucene;

import org.protege.editor.owl.model.search.CompoundKeyword;
import org.protege.editor.owl.model.search.SearchCategory;
import org.protege.editor.owl.model.search.SearchInputHandlerBase;
import org.protege.editor.owl.model.search.SearchKeyword;
import org.protege.editor.search.lucene.builder.AnnotationValueQueryBuilder;
import org.protege.editor.search.lucene.builder.DisplayNameQueryBuilder;
import org.protege.editor.search.lucene.builder.EntityIriQueryBuilder;
import org.protege.editor.search.lucene.builder.FilteredAnnotationQueryBuilder;
import org.protege.editor.search.lucene.builder.LogicalAxiomQueryBuilder;

import org.semanticweb.owlapi.util.CollectionFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Author: Josef Hardi <josef.hardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 04/11/2015
 */
public class QueryBasedInputHandler extends SearchInputHandlerBase<SearchQueries> {

    private SearchQueries searchQueries = new SearchQueries();

    private Collection<SearchCategory> categories;
    private LuceneSearcher searcher;

    public QueryBasedInputHandler(LuceneSearcher searcher) {
        this.searcher = searcher;
        categories = CollectionFactory.createSet(
                SearchCategory.DISPLAY_NAME,
                SearchCategory.IRI,
                SearchCategory.ANNOTATION_VALUE,
                SearchCategory.LOGICAL_AXIOM,
                SearchCategory.OTHER);
    }

    public void setCategories(Collection<SearchCategory> categories) {
        this.categories = categories;
    }

    private List<SearchQueryBuilder> getBuilders() {
        List<SearchQueryBuilder> builders = new ArrayList<>();
        if (categories.contains(SearchCategory.IRI)) {
            builders.add(new EntityIriQueryBuilder(searcher));
        }
        if (categories.contains(SearchCategory.DISPLAY_NAME)) {
            builders.add(new DisplayNameQueryBuilder(searcher));
        }
        if (categories.contains(SearchCategory.ANNOTATION_VALUE)) {
            builders.add(new AnnotationValueQueryBuilder(searcher));
            builders.add(new FilteredAnnotationQueryBuilder(searcher));
        }
        if (categories.contains(SearchCategory.LOGICAL_AXIOM)) {
            builders.add(new LogicalAxiomQueryBuilder(searcher));
        }
        return builders;
    }

    @Override
    public SearchQueries getQueryObject() {
        return searchQueries;
    }

    @Override
    public void handle(SearchKeyword searchKeyword) {
        for (SearchQueryBuilder builder : getBuilders()) {
            if (builder.isBuilderFor(searchKeyword)) {
                builder.add(searchKeyword);
                searchQueries.add(builder.build());
            }
        }
    }

    @Override
    public void handle(CompoundKeyword compoundKeyword) {
        for (SearchKeyword keyword : compoundKeyword) {
            handle(keyword);
        }
    }
}
