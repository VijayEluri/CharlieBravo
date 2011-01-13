/*
 * Copyright (c) 2009-2010 Chris Smith
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.md87.charliebravo.commands;

import com.dmdirc.util.Downloader;
import com.md87.charliebravo.Command;
import com.md87.charliebravo.Followup;
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
public class IssueSearchCommand implements Command {

    public void execute(InputHandler handler, Response response, String line) throws Exception {
        final List<String> result = Downloader.getPage("http://jira.dmdirc.com/secure/IssueNavigator.jspa?reset=true&view=rss&jqlQuery="
                + URLEncoder.encode(line, Charset.defaultCharset().name()));
        final StringBuilder builder = new StringBuilder();

        for (String resline : result) {
            builder.append(resline);
        }

        final Pattern pattern = Pattern.compile("<item>(.*?)</item>", Pattern.DOTALL);
        final Matcher matcher = pattern.matcher(builder);
        final List<String[]> matches = new ArrayList<String[]>();
        
        while (matcher.find()) {
            final String content = matcher.group(1);
        
            final Pattern titlePattern = Pattern.compile("<title>(.*?)</title>");
            final Matcher titleMatcher = titlePattern.matcher(content);
            titleMatcher.find();
            final String title = titleMatcher.group(1);
            
            final Pattern linkPattern = Pattern.compile("<link>(.*?)</link>");
            final Matcher linkMatcher = linkPattern.matcher(content);
            linkMatcher.find();
            final String link = linkMatcher.group(1);
            
            final Pattern keyPattern = Pattern.compile("<key.*?>(.*?)</key>");
            final Matcher keyMatcher = keyPattern.matcher(content);
            keyMatcher.find();
            final String key = keyMatcher.group(1);
            
            matches.add(new String[] { title, link, key });
        }
        
        response.sendMessage("there "
                + (matches.size() == 1 ? "was 1 result" : "were " + matches.size() + " results")
                + " for that query."
                + (matches.isEmpty() ? "" :
                    (matches.size() == 1 ? " It is: " : " The first result is: ")
                    + formatResult(matches.get(0))));
        
        if (!matches.isEmpty()) {
            response.addFollowup(new NextFollowup(matches, 1));
            response.addFollowup(new DetailsFollowup(matches.get(0)[2]));
        }
    }
    
    protected static String formatResult(final String[] args) {
        return args[0] + " (" + args[1] + ")";
    }

    protected static class NextFollowup implements Followup {

        private final int offset;
        private final List<String[]> data;

        public NextFollowup(List<String[]> data, int offset) {
            this.data = data;
            this.offset = offset;
        }

        public boolean matches(String line) {
            return "next".equalsIgnoreCase(line);
        }

        public void execute(InputHandler handler, Response response, String line) throws Exception {
            if (offset >= data.size()) {
                response.setInheritFollows(true);
                response.sendMessage("There are no more results", true);
            } else {
                response.sendMessage("result " + (1 + offset) + " is: " + formatResult(data.get(offset)));
                response.addFollowup(new DetailsFollowup(data.get(offset)[2]));
                response.addFollowup(new NextFollowup(data, offset + 1));
            }
        }

    }
    
    protected static class DetailsFollowup implements Followup {

        private final String key;

        public DetailsFollowup(String key) {
            this.key = key;
        }

        public boolean matches(String line) {
            return "details".equalsIgnoreCase(line);
        }

        public void execute(InputHandler handler, Response response, String line) throws Exception {
            response.setInheritFollows(true);
            new IssueCommand().execute(handler, response, key);
        }

    }

}
