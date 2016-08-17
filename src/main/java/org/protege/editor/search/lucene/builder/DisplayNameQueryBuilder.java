package org.protege.editor.search.lucene.builder;

import org.protege.editor.owl.model.search.SearchCategory;
import org.protege.editor.owl.model.search.SearchKeyword;
import org.protege.editor.search.lucene.BasicSearchQuery;
import org.protege.editor.search.lucene.IndexField;
import org.protege.editor.search.lucene.LuceneSearcher;
import org.protege.editor.search.lucene.LuceneUtils;
import org.protege.editor.search.lucene.SearchQuery;
import org.protege.editor.search.lucene.SearchQueryBuilder;

import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * @author Josef Hardi <johardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 23/06/2016
 */
public class DisplayNameQueryBuilder extends SearchQueryBuilder {

    protected static final Logger logger = LoggerFactory.getLogger(DisplayNameQueryBuilder.class);

    private LuceneSearcher searcher;

    public DisplayNameQueryBuilder(LuceneSearcher searcher) {
        this.searcher = searcher;
    }

    @Override
    public SearchQuery buildSearchQueryFor(SearchKeyword keyword) {
        final Query query;
        if (keyword.searchByRegex()) {
            query = LuceneUtils.createRegexQuery(IndexField.DISPLAY_NAME, keyword.getString());
        }
        else {
            String keywordString = keyword.getSyntacticString();
            query = LuceneUtils.createQuery(IndexField.DISPLAY_NAME, keywordString, searcher.getTextAnalyzer());
        }
        return new BasicSearchQuery(query, SearchCategory.DISPLAY_NAME, searcher);
    }

    @Override
    public boolean isBuilderFor(SearchKeyword keyword, Collection<SearchCategory> categories) {
        if (categories.contains(SearchCategory.DISPLAY_NAME)) {
            return (keyword.hasField()) ? false : true;
        }
        return false;
    }
}
