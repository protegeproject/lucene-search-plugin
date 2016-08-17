package org.protege.editor.search.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RegexpQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;

/**
 * @author Josef Hardi <johardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 23/06/2016
 */
public class LuceneUtils {

    public static Query createQuery(String searchField, String searchString) {
        try {
            QueryParser parser = new QueryParser(searchField, new StandardAnalyzer());
            return parser.parse(searchString);
        }
        catch (ParseException e) {
            return createTermQuery(searchField, ""); // return an empty term query
        }
    }

    public static Query createQuery(String searchField, String searchString, Analyzer textAnalyzer) {
        try {
            QueryParser parser = new QueryParser(searchField, textAnalyzer);
            return parser.parse(searchString);
        }
        catch (ParseException e) {
            return createTermQuery(searchField, ""); // return an empty term query
        }
    }

    public static PhraseQuery createPhraseQuery(String searchField, String keyword) {
        return new PhraseQuery(searchField, keyword.split("\\s+"));
    }

    public static TermQuery createTermQuery(String searchField, String keyword) {
        return new TermQuery(new Term(searchField, keyword));
    }

    public static PrefixQuery createPrefixQuery(String searchField, String keyword) {
        return new PrefixQuery(new Term(searchField, keyword));
    }

    public static WildcardQuery createSuffixQuery(String searchField, String keyword) {
        return new WildcardQuery(new Term(searchField, "*" + keyword));
    }

    public static WildcardQuery createLikeQuery(String searchField, String keyword) {
        return new WildcardQuery(new Term(searchField, "*" + keyword + "*"));
    }

    public static RegexpQuery createRegexQuery(String searchField, String regexPattern) {
        return new RegexpQuery(new Term(searchField, regexPattern));
    }
}
