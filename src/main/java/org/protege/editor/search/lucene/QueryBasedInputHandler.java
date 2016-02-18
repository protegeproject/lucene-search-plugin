package org.protege.editor.search.lucene;

import org.protege.editor.owl.model.search.CompoundKeyword;
import org.protege.editor.owl.model.search.SearchInputHandlerBase;
import org.protege.editor.owl.model.search.SearchKeyword;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Josef Hardi <josef.hardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 04/11/2015
 */
public class QueryBasedInputHandler extends SearchInputHandlerBase<SearchQueries> {

    private SearchQueries searchQueries = new SearchQueries();

    private LuceneSearcher searcher;

    public QueryBasedInputHandler(LuceneSearcher searcher) {
        this.searcher = searcher;
    }

    private List<SearchQueryBuilder> getBuilders() {
        List<SearchQueryBuilder> builders = new ArrayList<>();
        builders.add(new QueryForEntityIriBuilder(searcher));
        builders.add(new QueryForDisplayNameBuilder(searcher));
        builders.add(new QueryForAnnotationValueBuilder(searcher));
        builders.add(new QueryForFilteredAnnotationBuilder(searcher));
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
