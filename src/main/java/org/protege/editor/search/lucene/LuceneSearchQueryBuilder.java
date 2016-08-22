package org.protege.editor.search.lucene;

import org.protege.editor.owl.model.search.SearchCategory;
import org.protege.editor.owl.model.search.SearchInput;
import org.protege.editor.owl.model.search.SearchInputVisitor;
import org.protege.editor.owl.model.search.SearchTerm;
import org.protege.editor.search.lucene.builder.AnnotationValueQueryBuilder;
import org.protege.editor.search.lucene.builder.DisplayNameQueryBuilder;
import org.protege.editor.search.lucene.builder.EntityIriQueryBuilder;
import org.protege.editor.search.lucene.builder.FilteredAnnotationQueryBuilder;
import org.protege.editor.search.lucene.builder.LogicalAxiomQueryBuilder;

import org.semanticweb.owlapi.util.CollectionFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LuceneSearchQueryBuilder implements SearchInputVisitor {

    private Collection<SearchCategory> categories;
    private LuceneSearcher searcher;

    private List<SearchQuery> searchQueries = new ArrayList<>();

    public LuceneSearchQueryBuilder(LuceneSearcher searcher) {
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

    @Override
    public void visit(SearchInput searchInput) {
        for (SearchTerm term : searchInput) {
            UnionQuery.Builder unionQueryBuilder = new UnionQuery.Builder();
            for (SearchQueryBuilder queryBuilder : getBuilders()) {
                if (queryBuilder.isBuilderFor(term, categories)) {
                    unionQueryBuilder.add(queryBuilder.buildSearchQueryFor(term));
                }
            }
            searchQueries.add(unionQueryBuilder.build());
        }
    }

    public List<SearchQuery> build() {
        return searchQueries;
    }

    private List<SearchQueryBuilder> getBuilders() {
        List<SearchQueryBuilder> builders = new ArrayList<>();
        builders.add(new EntityIriQueryBuilder(searcher));
        builders.add(new DisplayNameQueryBuilder(searcher));
        builders.add(new AnnotationValueQueryBuilder(searcher));
        builders.add(new FilteredAnnotationQueryBuilder(searcher));
        builders.add(new LogicalAxiomQueryBuilder(searcher));
        return builders;
    }
}
