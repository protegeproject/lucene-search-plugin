package org.protege.editor.search.nci;

import org.protege.editor.owl.model.search.SearchInput;
import org.protege.editor.owl.model.search.SearchInputHandler;
import org.protege.editor.owl.model.search.SearchKeyword;
import org.protege.editor.owl.model.search.SearchKeyword.Occurance;
import org.protege.editor.owl.model.search.SearchStringParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jgoodies.common.base.Strings;

/**
 * Author: Josef Hardi <josef.hardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 13/11/2015
 */
public class NciSearchStringParser implements SearchStringParser {

    private static final Pattern filteredSearchStringPattern = Pattern.compile("([^:,]*):(\"[^\"]*\"|[^,\"]*)");

    @Override
    public void parse(String searchString, SearchInputHandler handler) {
        OrSearch orSearch = new OrSearch();
        for (String searchGroupString : searchString.split("\\|")) { // divide per group
            if (!Strings.isBlank(searchGroupString)) {
                SearchInput searchGroup = parseSearchGroup(searchGroupString.trim());
                orSearch.add(searchGroup);
            }
        }
        handler.handle(orSearch);
    }
    
    public SearchInput parseSearchGroup(String searchGroupString) {
        if (searchGroupString.contains("&")) {
            return parseAndSearchString(searchGroupString);
        } else {
            return parseSearchString(searchGroupString);
        }
    }
    
    private AndSearch parseAndSearchString(String andSearchString) {
        AndSearch andSearch = new AndSearch();
        for (String keywordString : andSearchString.split("&")) {
            if (!Strings.isBlank(keywordString)) {
                SearchKeyword keyword = parseSearchString(keywordString.trim());
                andSearch.add(keyword);
            }
        }
        return andSearch;
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
