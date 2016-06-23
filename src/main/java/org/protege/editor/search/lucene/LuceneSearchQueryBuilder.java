package org.protege.editor.search.lucene;

import org.protege.editor.owl.model.search.SearchCategory;
import org.protege.editor.owl.model.search.SearchInput;
import org.protege.editor.owl.model.search.SearchInputVisitor;
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
        for (SearchKeyword keyword : searchInput) {
            UnionQuery.Builder builder = new UnionQuery.Builder();
            for (SearchQueryBuilder queryBuilder : getBuilders()) {
                if (queryBuilder.isBuilderFor(keyword)) {
                    queryBuilder.add(keyword);
                    builder.add(queryBuilder.build());
                }
            }
            searchQueries.add(builder.build());
        }
    }

    public List<SearchQuery> build() {
        return searchQueries;
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

}
