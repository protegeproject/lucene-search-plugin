package org.protege.editor.search.nci;

import org.protege.editor.owl.model.search.CompoundKeyword;
import org.protege.editor.owl.model.search.SearchInput;
import org.protege.editor.owl.model.search.SearchInputHandlerBase;
import org.protege.editor.owl.model.search.SearchKeyword;
import org.protege.editor.search.lucene.LuceneSearcher;
import org.protege.editor.search.lucene.SearchQueryBuilder;
import org.protege.editor.search.lucene.builder.AnnotationValueQueryBuilder;
import org.protege.editor.search.lucene.builder.DisplayNameQueryBuilder;
import org.protege.editor.search.lucene.builder.EntityIriQueryBuilder;
import org.protege.editor.search.lucene.builder.FilteredAnnotationQueryBuilder;

import java.util.ArrayList;
import java.util.List;

public class NciQueryBasedInputHandler extends SearchInputHandlerBase<UserQuery> {

    private UserQuery userQuery = new UserQuery();

    private LuceneSearcher searcher;

    public NciQueryBasedInputHandler(LuceneSearcher searcher) {
        this.searcher = searcher;
    }

    private List<SearchQueryBuilder> getBuilders() {
        List<SearchQueryBuilder> builders = new ArrayList<>();
        builders.add(new EntityIriQueryBuilder(searcher));
        builders.add(new DisplayNameQueryBuilder(searcher));
        builders.add(new AnnotationValueQueryBuilder(searcher));
        builders.add(new FilteredAnnotationQueryBuilder(searcher));
        return builders;
    }

    @Override
    public UserQuery getQueryObject() {
        return userQuery;
    }

    private void handle(SearchInput searchInput) {
        if (searchInput instanceof SearchKeyword) {
            handle((SearchKeyword) searchInput);
        } else if (searchInput instanceof CompoundKeyword) {
            handle((CompoundKeyword) searchInput);
        }
    }

    @Override
    public void handle(SearchKeyword searchKeyword) {
        for (SearchQueryBuilder builder : getBuilders()) {
            if (builder.isBuilderFor(searchKeyword)) {
                builder.add(searchKeyword);
                userQuery.add(builder.build());
            }
        }
    }

    @Override
    public void handle(CompoundKeyword compoundKeyword) {
        if (compoundKeyword instanceof OrSearch) {
            handle((OrSearch) compoundKeyword);
        } else if (compoundKeyword instanceof AndSearch) {
            handle((AndSearch) compoundKeyword);
        }
    }

    public void handle(OrSearch orSearch) {
        for (SearchInput searchGroup : orSearch.getSearchGroup()) {
            handle(searchGroup);
        }
    }

    public void handle(AndSearch andSearch) {
        for (SearchKeyword keyword : andSearch) {
            handle(keyword);
        }
    }
}
