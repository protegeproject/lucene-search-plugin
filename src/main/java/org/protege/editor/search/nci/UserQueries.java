package org.protege.editor.search.nci;

import org.protege.editor.search.lucene.SearchQueries;
import org.protege.editor.search.lucene.SearchQuery;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class UserQueries implements Iterable<Entry<SearchQueries, Boolean>> {

    private Map<SearchQueries, Boolean> userQuerySettings = new HashMap<>();

    public void add(SearchQuery searchQuery, boolean isLinked) {
        SearchQueries queries = new SearchQueries();
        queries.add(searchQuery);
        add(queries, isLinked);
    }

    public void add(SearchQueries searchQueries, boolean isLinked) {
        userQuerySettings.put(searchQueries, isLinked);
    }

    @Override
    public Iterator<Entry<SearchQueries, Boolean>> iterator() {
        return userQuerySettings.entrySet().iterator();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (SearchQueries searchQuery : userQuerySettings.keySet()) {
            sb.append(searchQuery.toString());
            if (userQuerySettings.get(searchQuery).equals(Boolean.TRUE)) {
                sb.append(" (*)");
            } else {
                sb.append(" ( )");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
