package org.protege.editor.search.lucene;

import org.protege.editor.owl.model.find.OWLEntityFinderPreferences;
import org.protege.editor.owl.model.search.SearchInput;
import org.protege.editor.owl.model.search.SearchStringParser;
import org.protege.editor.owl.model.search.SearchTerm;

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

    private static final Pattern filteredSearchStringPattern = Pattern.compile("([^=,]*)=(\"[^\"]*\"|[^,\"]*)");

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
                SearchTerm term = createSearchTerm(searchField, searchString);
                builder.add(term);
            }
        }
        return builder.build();
    }

    private static SearchTerm createSearchTerm(String searchField, String searchString) {
        OWLEntityFinderPreferences prefs = OWLEntityFinderPreferences.getInstance();
        return new SearchTerm(searchField,
                searchString,
                toLuceneSyntax(searchString),
                prefs.isCaseSensitive(), // is case-sensitive
                prefs.isIgnoreWhiteSpace(), // ignore whitespace
                prefs.isWholeWords(), // search whole words
                prefs.isUseRegularExpressions(), // search by regex
                false); // search by phonetic
    }

    private static String toLuceneSyntax(String searchString) {
        OWLEntityFinderPreferences prefs = OWLEntityFinderPreferences.getInstance();
        if (prefs.isIgnoreWhiteSpace() || prefs.isWholeWords()) {
            if (prefs.isIgnoreWhiteSpace()) {
                searchString = searchString.replaceAll("\\s+", "");
            }
            if (prefs.isWholeWords()) {
                searchString = "\"" + searchString + "\"";
            }
            return searchString;
        }
        else if (prefs.isUseRegularExpressions()) {
            return searchString;
        }
        else {
            StringBuffer sb = new StringBuffer();
            for (String s : searchString.split(" ")) {
                if (s.isEmpty()) continue;
                char prefix = s.charAt(0);
                switch (prefix) {
                    case '+': // include sign
                    case '-': sb.append(s); break; // exclude sign
                    default:
                        if (s.length() < 3) {
                            sb.append("+" + s);
                        }
                        else {
                            sb.append("+" + s + "*"); // start using a wildcard when input length >= 3
                        }
                }
                sb.append(" ");
            }
            return sb.toString();
        }
    }
}
