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
public class LogicalAxiomQueryBuilder extends SearchQueryBuilder {

    protected static final Logger logger = LoggerFactory.getLogger(LogicalAxiomQueryBuilder.class);

    private LuceneSearcher searcher;

    public LogicalAxiomQueryBuilder(LuceneSearcher searcher) {
        this.searcher = searcher;
    }

    @Override
    public SearchQuery buildSearchQueryFor(SearchKeyword keyword) {
        final Query query;
        if (keyword.searchByRegex()) {
            query = LuceneUtils.createRegexQuery(IndexField.AXIOM_DISPLAY_NAME, keyword.getString());
        }
        else {
            String keywordString = keyword.getSyntacticString();
            query = LuceneUtils.createQuery(IndexField.AXIOM_DISPLAY_NAME, keywordString, searcher.getTextAnalyzer());
        }
        return new BasicSearchQuery(query, SearchCategory.LOGICAL_AXIOM, searcher);
    }

    @Override
    public boolean isBuilderFor(SearchKeyword keyword, Collection<SearchCategory> categories) {
        if (categories.contains(SearchCategory.LOGICAL_AXIOM)) {
            return (keyword.hasField()) ? false : true;
        }
        return false;
    }
}
