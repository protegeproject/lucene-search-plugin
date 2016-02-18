package org.protege.editor.search.lucene;

import org.protege.editor.owl.model.search.SearchCategory;
import org.protege.editor.owl.model.search.SearchKeyword;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Josef Hardi <josef.hardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 10/11/2015
 */
public class QueryForAnnotationValueBuilder extends SearchQueryBuilder {

    protected static final Logger logger = LoggerFactory.getLogger(QueryForAnnotationValueBuilder.class);

    private LuceneSearcher searcher;

    private BooleanQuery query;

    public QueryForAnnotationValueBuilder(LuceneSearcher searcher) {
        this.searcher = searcher;
    }

    @Override
    public void add(SearchKeyword keyword) {
        if (keyword.isBlank()) return;
        try {
            query = LuceneUtils.createQuery(IndexField.ANNOTATION_TEXT, keyword.getString(), new StandardAnalyzer());
        }
        catch (ParseException e) {
            // Silently show is as debug message
            logger.debug(e.getMessage());
        }
    }

    @Override
    public SearchQuery build() {
        return new SearchQuery(query, SearchCategory.ANNOTATION_VALUE, searcher);
    }

    @Override
    public boolean isBuilderFor(SearchKeyword keyword) {
        return (keyword.hasField()) ? false : true;
    }
}
