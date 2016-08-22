package org.protege.editor.search.lucene.builder;

import org.protege.editor.owl.model.search.SearchCategory;
import org.protege.editor.owl.model.search.SearchTerm;
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
public class AnnotationValueQueryBuilder extends SearchQueryBuilder {

    protected static final Logger logger = LoggerFactory.getLogger(AnnotationValueQueryBuilder.class);

    private LuceneSearcher searcher;

    public AnnotationValueQueryBuilder(LuceneSearcher searcher) {
        this.searcher = searcher;
    }

    @Override
    public SearchQuery buildSearchQueryFor(SearchTerm term) {
        final Query query;
        if (term.searchByRegex()) {
            query = LuceneUtils.createRegexQuery(IndexField.ANNOTATION_TEXT, term.getString());
        }
        else {
            String keywordString = term.getSyntacticString();
            query = LuceneUtils.createQuery(IndexField.ANNOTATION_TEXT, keywordString, searcher.getTextAnalyzer());
        }
        return new BasicSearchQuery(query, SearchCategory.ANNOTATION_VALUE, searcher);
    }

    @Override
    public boolean isBuilderFor(SearchTerm term, Collection<SearchCategory> categories) {
        if (categories.contains(SearchCategory.ANNOTATION_VALUE)) {
            return (term.hasField()) ? false : true;
        }
        return false;
    }
}
