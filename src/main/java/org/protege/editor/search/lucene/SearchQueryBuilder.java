package org.protege.editor.search.lucene;

import org.protege.editor.owl.model.search.SearchCategory;
import org.protege.editor.owl.model.search.SearchTerm;

import java.util.Collection;

/**
 * Author: Josef Hardi <josef.hardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 10/11/2015
 */
public abstract class SearchQueryBuilder {

    public abstract boolean isBuilderFor(SearchTerm term, Collection<SearchCategory> categories);

    public abstract SearchQuery buildSearchQueryFor(SearchTerm term);
}
