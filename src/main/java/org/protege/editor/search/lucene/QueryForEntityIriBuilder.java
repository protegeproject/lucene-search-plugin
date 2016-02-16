package org.protege.editor.search.lucene;

import org.protege.editor.owl.model.search.SearchCategory;
import org.protege.editor.owl.model.search.SearchKeyword;

import org.apache.lucene.search.BooleanQuery;

/**
 * Author: Josef Hardi <josef.hardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 10/11/2015
 */
public class QueryForEntityIriBuilder extends SearchQueryBuilder {

    private BooleanQuery.Builder builder = new BooleanQuery.Builder();

    private LuceneSearcher searcher;

    public QueryForEntityIriBuilder(LuceneSearcher searcher) {
        this.searcher = searcher;
    }

    @Override
    public void add(SearchKeyword keyword) {
        if (keyword.isBlank()) return;
        if (keyword.searchWholeWords()) {
            builder.add(LuceneUtils.createPhraseQuery(
                    IndexField.ENTITY_IRI,
                    keyword.getString()), LuceneUtils.toOccur(keyword.occurance()));
        } else {
            builder.add(LuceneUtils.createTermQuery(
                    IndexField.ENTITY_IRI,
                    keyword.getString()), LuceneUtils.toOccur(keyword.occurance()));
        }
    }

    @Override
    public SearchQuery build() {
        return new SearchQuery(builder.build(), SearchCategory.IRI, searcher);
    }

    @Override
    public boolean isBuilderFor(SearchKeyword keyword) {
        return (keyword.hasField()) ? false : true;
    }
}
