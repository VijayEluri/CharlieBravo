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
import com.md87.charliebravo.InputHandler;
import com.md87.charliebravo.Response;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author chris
 */
public class QuoteCommand implements Command {

    protected static final String URL = "http://apps.md87.co.uk/quotes/rss?id=%s";

    protected static final Pattern QUOTE_MATCHER = Pattern.compile(".*<description><![CDATA[(.*?)]]></description>.*");

    public void execute(InputHandler handler, Response response, String line) throws Exception {
        if (line.isEmpty()) {
            response.sendMessage("You need to specify a quote number", true);
        } else if (!line.matches("^[0-9]+$")) {
            response.sendMessage("You need to specify a valid quote number", true);
        } else {
            final List<String> result = Downloader.getPage(String.format(URL, line));
            final StringBuilder builder = new StringBuilder();

            for (String rline : result) {
                builder.append(rline);
            }

            final Matcher matcher = QUOTE_MATCHER.matcher(builder);

            if (matcher.matches()) {
                final String quote = matcher.group(1);
                response.sendMessage("quote " + line + " is: " + quote);
            } else {
                response.sendMessage("There were no results for that quote", true);
            }
        }

    }

}
