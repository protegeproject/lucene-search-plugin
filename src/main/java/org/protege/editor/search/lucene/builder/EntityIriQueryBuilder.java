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
public class EntityIriQueryBuilder extends SearchQueryBuilder {

    protected static final Logger logger = LoggerFactory.getLogger(EntityIriQueryBuilder.class);

    private LuceneSearcher searcher;

    public EntityIriQueryBuilder(LuceneSearcher searcher) {
        this.searcher = searcher;
    }

    @Override
    public SearchQuery buildSearchQueryFor(SearchKeyword keyword) {
        Query query = LuceneUtils.createQuery(IndexField.ENTITY_IRI, keyword.getString());
        return new BasicSearchQuery(query, SearchCategory.IRI, searcher);
    }

    @Override
    public boolean isBuilderFor(SearchKeyword keyword, Collection<SearchCategory> categories) {
        if (categories.contains(SearchCategory.IRI)) {
            return (keyword.hasField()) ? false : true;
        }
        return false;
    }
}
