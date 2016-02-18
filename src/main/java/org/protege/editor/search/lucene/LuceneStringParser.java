package org.protege.editor.search.lucene;

import org.protege.editor.owl.model.search.SearchInputHandler;
import org.protege.editor.owl.model.search.SearchKeyword;
import org.protege.editor.owl.model.search.SearchKeyword.Occurance;
import org.protege.editor.owl.model.search.SearchStringParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: Josef Hardi <josef.hardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 17/02/2016
 */
public class LuceneStringParser implements SearchStringParser {

    private static final Pattern filteredSearchStringPattern = Pattern.compile("([^:,]*):(\"[^\"]*\"|[^,\"]*)");

    @Override
    public void parse(String searchString, SearchInputHandler handler) {
        SearchKeyword keyword = parseSearchString(searchString.trim());
        handler.handle(keyword);
    }

    private SearchKeyword parseSearchString(String searchString) {
        String searchField = "";
        Matcher m = filteredSearchStringPattern.matcher(searchString);
        if (m.find()) {
            searchField = m.group(1).trim();
            searchString = m.group(2).trim();
        }
        SearchKeyword keyword = createSearchKeyword(searchField, appendSearchOperator(searchString));
        return keyword;
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

    private SearchKeyword createSearchKeyword(String field, String keyword) {
        return new SearchKeyword(field,
                keyword,
                Occurance.INCLUDE,
                false, // is case-sensitive
                false, // ignore whitespace
                false, // search whole words
                false, // search by regex
                false); // search by phonetic
    }
}
