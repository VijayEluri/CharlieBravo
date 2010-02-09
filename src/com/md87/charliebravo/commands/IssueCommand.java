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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author chris
 */
public class IssueCommand implements Command {

    public void execute(InputHandler handler, Response response, String line) throws Exception {
        if (line.isEmpty() || !line.matches("^[0-9]+$")) {
            response.sendMessage("You need to specify an issue number", true);
        } else {
            final List<String> result = Downloader.getPage("http://bugs.dmdirc.com/view.php?id="
                    + line);
            final StringBuilder builder = new StringBuilder();

            for (String resline : result) {
                builder.append(resline);
            }

            if (builder.indexOf("APPLICATION ERROR #1100") > -1) {
                response.sendMessage("That issue was not found", true);
            } else if (builder.indexOf("<p>Access Denied.</p>") > -1) {
                response.sendMessage("that issue is private. Please see "
                        + "http://bugs.dmdirc.com/view/" + line);
            } else {
                final Map<String, String> data = new HashMap<String, String>();

                final Pattern pattern = Pattern.compile(
                        "<td class=\"category\".*?>\\s*(.*?)\\s*"
                        + "</td>\\s*(?:<!--.*?-->\\s*)?<td.*?>\\s*(.*?)\\s*</td>",
                        Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
                final Matcher matcher = pattern.matcher(builder);

                while (matcher.find()) {
                    data.put(matcher.group(1).toLowerCase(), matcher.group(2));
                }

                response.sendMessage("issue " + data.get("id") + " is \""
                        + data.get("summary").substring(9) + "\". Current "
                        + "status is " + data.get("status") + " ("
                        + data.get("resolution") + "). See http://bugs.dmdirc.com/view/"
                        + data.get("id"));
                response.addFollowup(new IssueFollowup(data));
            }
            
        }

    }

    protected static class IssueFollowup implements Followup {

        private final Map<String, String> data;

        public IssueFollowup(Map<String, String> data) {
            this.data = data;
        }

        public boolean matches(String line) {
            return data.containsKey(line.toLowerCase());
        }

        public void execute(InputHandler handler, Response response, String line) throws Exception {
            response.setInheritFollows(true);
            response.sendMessage("the " + line.toLowerCase() + " of issue "
                    + data.get("id") + " is: " + data.get(line.toLowerCase()));
        }

    }

}
