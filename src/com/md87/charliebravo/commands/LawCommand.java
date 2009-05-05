/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.md87.charliebravo.commands;

import com.dmdirc.util.Downloader;
import com.md87.charliebravo.Command;
import com.md87.charliebravo.InputHandler;
import com.md87.charliebravo.Response;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author chris
 */
public class LawCommand implements Command {

    protected static final String URL = "http://www.statutelaw.gov.uk/SearchResults.aspx?TYPE=QS&Title=%s&Year=%s&Number=%s&LegType=All+Legislation";

    protected static final Pattern YEAR_EXTRACTOR = Pattern.compile("((1[2-9]|20)[0-9]{2})");
    protected static final Pattern NUMBER_EXTRACTOR = Pattern.compile("c\\.?\\s*([0-9]+)");

    protected static final Pattern TITLE_MATCHER = Pattern.compile(".*?<a href=\"legResults.*?activeTextDocId=([0-9]+).*?\">(.*?)</a>.*?");
    protected static final Pattern YEAR_MATCHER = Pattern.compile(".*?<td class=\"year\">(.*?)</td>.*?");
    protected static final Pattern TYPE_MATCHER = Pattern.compile(".*?<td class=\"type\">(.*?)</td>.*?");

    public void execute(InputHandler handler, Response response, String line) throws Exception {
        if (line.isEmpty()) {
            response.sendMessage("You need to specify some search terms", true);
        } else {
            String title = line, year = "", number = "";
            Matcher matcher;

            if ((matcher = YEAR_EXTRACTOR.matcher(title)).find()) {
                year = matcher.group(1);
                title = title.replace(matcher.group(), "");
            }

            if ((matcher = NUMBER_EXTRACTOR.matcher(title)).find()) {
                number = matcher.group(1);
                title = title.replace(matcher.group(), "");
            }

            title = URLEncoder.encode(title.replaceAll("\\s+", " ").trim(), Charset.defaultCharset().name());
            
            final List<String> result = Downloader.getPage(String.format(URL, title, year, number));
            final List<Result> results = new ArrayList<Result>();

            String resDocId = null, resTitle = null, resYear = null, resType = null;
            for (String resline : result) {
                if ((matcher = TITLE_MATCHER.matcher(resline)).matches()) {
                    resDocId = matcher.group(1);
                    resTitle = matcher.group(2);
                } else if ((matcher = YEAR_MATCHER.matcher(resline)).matches()) {
                    resYear = matcher.group(1);
                } else if ((matcher = TYPE_MATCHER.matcher(resline)).matches()) {
                    resType = matcher.group(1);
                    results.add(new Result(resDocId, resTitle, resYear, resType));
                }
            }

            if (results.isEmpty()) {
                response.sendMessage("There were no results for that query", true);
            } else {
                response.sendMessage("the first result is: " + results.get(0));
            }
        }

    }

    protected static class Result {

        protected static final String LINK = "http://www.statutelaw.gov.uk/legResults.aspx?activeTextDocId=%s&PageNumber=1";

        private final String docId, title, year, type;

        public Result(String docId, String title, String year, String type) {
            this.docId = docId;
            this.title = title;
            this.year = year;
            this.type = type;
        }

        @Override
        public String toString() {
            return title + " (" + type + "; " + year + "). See "
                    + String.format(LINK, docId);
        }

    }

}
