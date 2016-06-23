package org.protege.editor.search.lucene;

import org.protege.editor.owl.model.find.OWLEntityFinderPreferences;
import org.protege.editor.owl.model.search.SearchInput;
import org.protege.editor.owl.model.search.SearchKeyword;
import org.protege.editor.owl.model.search.SearchKeyword.Occurance;
import org.protege.editor.owl.model.search.SearchStringParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jgoodies.common.base.Strings;

/**
 * @author Josef Hardi <johardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 22/06/2016
 */
public class LuceneStringParser implements SearchStringParser {

    private static final Pattern filteredSearchStringPattern = Pattern.compile("([^:,]*):(\"[^\"]*\"|[^,\"]*)");

    @Override
    public SearchInput parse(String searchString) {
        SearchInput.Builder builder = new SearchInput.Builder();
        searchString = searchString.trim();
        for (String searchGroup : searchString.split("&")) {
            if (!Strings.isBlank(searchGroup)) {
                String searchField = "";
                Matcher m = filteredSearchStringPattern.matcher(searchGroup);
                if (m.find()) {
                    searchField = m.group(1).trim();
                    searchString = m.group(2).trim();
                }
                SearchKeyword keyword = createSearchKeyword(searchField, searchGroup);
                builder.add(keyword);
            }
        }
        return builder.build();
    }

    private static SearchKeyword createSearchKeyword(String searchField, String searchString) {
        OWLEntityFinderPreferences prefs = OWLEntityFinderPreferences.getInstance();
        return new SearchKeyword(searchField,
                appendSearchOperator(searchString),
                Occurance.INCLUDE,
                prefs.isCaseSensitive(), // is case-sensitive
                prefs.isIgnoreWhiteSpace(), // ignore whitespace
                prefs.isWholeWords(), // search whole words
                prefs.isUseRegularExpressions(), // search by regex
                false); // search by phonetic
    }

    private static String appendSearchOperator(String searchString) {
        StringBuffer sb = new StringBuffer();
        for (String s : searchString.split(" ")) {
            if (s.isEmpty()) continue;
            char prefix = s.charAt(0);
            switch (prefix) {
                case '+': // include sign
                case '-': sb.append(s); break; // exclude sign
                default: sb.append("+" + s);
            }
            sb.append(" ");
        }
        return sb.toString();
    }
}
