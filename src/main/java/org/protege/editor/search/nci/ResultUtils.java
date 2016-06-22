package org.protege.editor.search.nci;

import org.protege.editor.owl.model.search.SearchResult;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * @author Josef Hardi <johardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 22/06/2016
 */
public class ResultUtils {

    /**
     * Performs an intersect operation given {@code set1} and {@code set2}, where the
     * result will be stored in {@code set1}
     *
     * @param set1
     *          The first set of the search results
     * @param set2
     *          The second set of the search results
     */
    public static void intersect(Set<SearchResult> set1, Set<SearchResult> set2) {
        Set<SearchResult> buffer = new HashSet<>();
        buffer.addAll(Sets.intersection(set1, set2));
        set1.clear();
        set1.addAll(buffer);
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
    public static void union(Set<SearchResult> set1, Set<SearchResult> set2) {
        set1.addAll(set2);
    }
}
