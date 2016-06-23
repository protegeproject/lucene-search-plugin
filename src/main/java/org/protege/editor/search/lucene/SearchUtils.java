package org.protege.editor.search.lucene;

import org.protege.editor.owl.model.search.SearchResult;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Josef Hardi <johardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 22/06/2016
 */
public class SearchUtils {

    /**
     * Performs an intersect operation given {@code set1} and {@code set2}, where the
     * result will be stored in {@code set1}
     *
     * @param set1
     *          The first set of the search results
     * @param set2
     *          The second set of the search results
     */
    public static void intersect(final Set<SearchResult> set1, final Set<SearchResult> set2) {
        if (set1.isEmpty() && !set2.isEmpty()) {
            set1.addAll(set2); // initialize
        }
        else {
            if (set1.size() <= set2.size()) {
                set1.retainAll(set2);
            }
            else {
                Set<SearchResult> buffer = new HashSet<>(set2);
                buffer.retainAll(set1);
                set1.clear();
                set1.addAll(buffer);
            }
        }
    }

    /**
     * Performs a union operation given {@code set1} and {@code set2}, where the
     * result will be stored in {@code set1}
     *
     * @param set1
     *          The first set of the search results
     * @param set2
     *          The second set of the search results
     */
    public static void union(final Set<SearchResult> set1, final Set<SearchResult> set2) {
        set1.addAll(set2);
    }
}