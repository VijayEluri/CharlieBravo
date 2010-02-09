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

import com.md87.charliebravo.Command;
import com.md87.charliebravo.Followup;
import com.md87.charliebravo.InputHandler;
import com.md87.charliebravo.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author chris
 */
public class DefineCommand implements Command {

    public void execute(final InputHandler handler, Response response, String line) throws MalformedURLException, IOException, JSONException {
        URL url = new URL("http://apps.md87.co.uk/services/wiktionary/?query=" +
                URLEncoder.encode(line, Charset.defaultCharset().name()));
        URLConnection connection = url.openConnection();
        connection.addRequestProperty("Referer", "http://chris.smith.name/");

        String input;
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        while((input = reader.readLine()) != null) {
            builder.append(input);
        }

        JSONObject json = new JSONObject(builder.toString());
        if (json.getInt("responseStatus") != 200) {
            throw new IOException(json.getString("responseDetails"));
        }

        if (json.getJSONArray("responseData").length() == 0) {
            response.sendMessage("There were no results for '" + line + "'", true);
        } else {
            final StringBuilder res = new StringBuilder();
            res.append("there ");

            if (json.getJSONArray("responseData").length() == 1) {
                res.append("was 1 match");
            } else {
                res.append("were ");
                res.append(json.getJSONArray("responseData").length());
                res.append(" matches");
            }

            res.append(" for '");
            res.append(line);
            res.append("'");

            if (json.getJSONArray("responseData").length() == 1) {
                res.append(". It is ");
            } else {
                res.append(". Result 1 is ");
            }

            final String name = json.getJSONArray("responseData").getJSONObject(0).getString("title");

            res.append('\'');
            res.append(name);
            res.append("', which has ");

            final int defs = json.getJSONArray("responseData").getJSONObject(0)
                    .getJSONArray("definitions").length();
            res.append(defs);

            res.append(" definition");
            
            if (defs != 1) {
                res.append("s, the first of which is");
            }
            
            res.append(": ");

            res.append(json.getJSONArray("responseData").getJSONObject(0)
                    .getJSONArray("definitions").get(0));

            response.sendMessage(res.toString());
            response.addFollowup(new NextWordFollowup(json.getJSONArray("responseData"), 1));
            response.addFollowup(new NextDefinitionFollowup(json.getJSONArray("responseData")
                    .getJSONObject(0).getJSONArray("definitions"), 1,
                    new NextWordFollowup(json.getJSONArray("responseData"), 1)));
        }
    }

    protected static class NextWordFollowup implements Followup {

        private final JSONArray words;
        private final int next;

        public NextWordFollowup(JSONArray words, int next) {
            this.words = words;
            this.next = next;
        }

        public boolean matches(String line) {
            return next < words.length() && line.startsWith("next word");
        }

        public void execute(InputHandler handler, Response response, String line) throws Exception {
            final StringBuilder res = new StringBuilder();

            res.append("result " + (next + 1) + " is ");

            final String name = words.getJSONObject(next).getString("title");

            res.append('\'');
            res.append(name);
            res.append("', which has ");

            final int defs = words.getJSONObject(next)
                    .getJSONArray("definitions").length();
            res.append(defs);

            res.append(" definition");

            if (defs != 1) {
                res.append("s, the first of which is");
            }

            res.append(": ");

            res.append(words.getJSONObject(next)
                    .getJSONArray("definitions").get(0));

            response.sendMessage(res.toString());
            response.addFollowup(new NextWordFollowup(words, next + 1));
            response.addFollowup(new NextDefinitionFollowup(words.getJSONObject(next)
                    .getJSONArray("definitions"), 1, new NextWordFollowup(words, next + 1)));
        }

    }

    protected static class NextDefinitionFollowup implements Followup {

        private final JSONArray defs;
        private final int next;
        private final NextWordFollowup nextword;

        public NextDefinitionFollowup(JSONArray defs, int next, NextWordFollowup nextword) {
            this.defs = defs;
            this.next = next;
            this.nextword = nextword;
        }

        public boolean matches(String line) {
            return next < defs.length() && line.startsWith("next definition");
        }

        public void execute(InputHandler handler, Response response, String line) throws Exception {
            final StringBuilder res = new StringBuilder();

            res.append("definition " + (next + 1) + " is: ");
            res.append(defs.get(next));

            response.sendMessage(res.toString());
            response.addFollowup(nextword);
            response.addFollowup(new NextDefinitionFollowup(defs, next + 1, nextword));
        }

    }

}
