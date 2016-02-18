package org.protege.editor.search.lucene;

import org.protege.editor.owl.model.search.SearchCategory;
import org.protege.editor.owl.model.search.SearchKeyword;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Josef Hardi <josef.hardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 10/11/2015
 */
public class QueryForFilteredAnnotationBuilder extends SearchQueryBuilder {

    protected static final Logger logger = LoggerFactory.getLogger(QueryForFilteredAnnotationBuilder.class);

    private LuceneSearcher searcher;

    private BooleanQuery.Builder builder = new BooleanQuery.Builder();

    public QueryForFilteredAnnotationBuilder(LuceneSearcher searcher) {
        this.searcher = searcher;
    }

    @Override
    public void add(SearchKeyword keyword) {
        if (keyword.isBlank()) return;
        handleFilterField(keyword);
        handleSearchString(keyword);
    }

    private void handleFilterField(SearchKeyword keyword) {
        builder.add(LuceneUtils.createTermQuery(
                IndexField.ANNOTATION_DISPLAY_NAME,
                keyword.getField()), Occur.MUST);
    }
    
    private void handleSearchString(SearchKeyword keyword) {
        try {
            BooleanQuery query = LuceneUtils.createQuery(IndexField.ANNOTATION_TEXT, keyword.getString(), new StandardAnalyzer());
            for (BooleanClause clause : query.clauses()) {
                builder.add(clause);
            }
        }
        catch (ParseException e) {
            // Silently show is as debug message
            logger.debug(e.getMessage());
        }
    }

    @Override
    public SearchQuery build() {
        return new SearchQuery(builder.build(), SearchCategory.ANNOTATION_VALUE, searcher);
    }

    @Override
    public boolean isBuilderFor(SearchKeyword keyword) {
        return (!keyword.hasField()) ? false : true;
    }
}
