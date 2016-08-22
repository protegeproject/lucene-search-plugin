package org.protege.editor.search.lucene.builder;

import org.protege.editor.owl.model.search.SearchCategory;
import org.protege.editor.owl.model.search.SearchTerm;
import org.protege.editor.search.lucene.BasicSearchQuery;
import org.protege.editor.search.lucene.IndexField;
import org.protege.editor.search.lucene.LuceneSearcher;
import org.protege.editor.search.lucene.LuceneUtils;
import org.protege.editor.search.lucene.SearchQuery;
import org.protege.editor.search.lucene.SearchQueryBuilder;

import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * @author Josef Hardi <johardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 23/06/2016
 */
public class FilteredAnnotationQueryBuilder extends SearchQueryBuilder {

    protected static final Logger logger = LoggerFactory.getLogger(FilteredAnnotationQueryBuilder.class);

    private LuceneSearcher searcher;

    public FilteredAnnotationQueryBuilder(LuceneSearcher searcher) {
        this.searcher = searcher;
    }

    @Override
    public SearchQuery buildSearchQueryFor(SearchTerm term) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        builder.add(LuceneUtils.createTermQuery(IndexField.ANNOTATION_DISPLAY_NAME, term.getField()), Occur.MUST);
        builder.add(LuceneUtils.createQuery(IndexField.ANNOTATION_TEXT, term.getString(), searcher.getTextAnalyzer()), Occur.MUST);
        return new BasicSearchQuery(builder.build(), SearchCategory.ANNOTATION_VALUE, searcher);
    }

    @Override
    public boolean isBuilderFor(SearchTerm term, Collection<SearchCategory> categories) {
        if (categories.contains(SearchCategory.ANNOTATION_VALUE)) {
            return (term.hasField()) ? true : false;
        }
        return false;
    }
}
