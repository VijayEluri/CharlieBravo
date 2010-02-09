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
public class GoogleCommand implements Command {

    public void execute(final InputHandler handler, Response response, String line) throws MalformedURLException, IOException, JSONException {
        URL url = new URL("http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=" + 
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

        if (json.getJSONObject("responseData").getJSONArray("results").length() == 0) {
            response.sendMessage("There were no results for '" + line + "'", true);
        } else {
            final JSONObject obj = json.getJSONObject("responseData")
                    .getJSONArray("results").getJSONObject(0);
            response.sendMessage("the first result for '" + line + "' is "
                    + obj.getString("unescapedUrl") + ", titled '"
                    + obj.getString("titleNoFormatting") + "'");
            response.addFollowup(new DescriptionFollowup(obj));
            response.addFollowup(new CacheFollowup(obj));
            response.addFollowup(new NextFollowup(line,
                    json.getJSONObject("responseData").getJSONArray("results"), 1));
        }
    }

    protected static class DescriptionFollowup implements Followup {

        private final JSONObject result;

        public DescriptionFollowup(JSONObject result) {
            this.result = result;
        }

        public boolean matches(String line) {
            return line.equals("description");
        }

        public void execute(final InputHandler handler, Response response, String line) throws Exception {
            response.setInheritFollows(true);
            response.sendMessage("the description of '" + result.getString("unescapedUrl")
                    + "' is: " + result.getString("content"));
        }

    }

    protected static class CacheFollowup implements Followup {

        private final JSONObject result;

        public CacheFollowup(JSONObject result) {
            this.result = result;
        }

        public boolean matches(String line) {
            return line.equals("cache");
        }

        public void execute(final InputHandler handler, Response response, String line) throws Exception {
            response.setInheritFollows(true);
            if (result.has("cacheUrl")) {
                response.sendMessage("the cached version of '" + result.getString("url")
                        + "' is: " + result.getString("cacheUrl"));
            } else {
                response.sendMessage("There doesn't seem to be a cached version of '"
                        + result.getString("unescapedUrl") + "'", true);
            }
        }

    }

    protected static class NextFollowup implements Followup {

        private final String query;
        private final JSONArray result;
        private final int offset;

        public NextFollowup(String query, JSONArray results, final int offset) {
            this.query = query;
            this.result = results;
            this.offset = offset;
        }

        public boolean matches(String line) {
            return line.equals("next") || line.equals("more");
        }

        public void execute(final InputHandler handler, Response response, String line) throws Exception {
            if (result.length() <= offset) {
                response.setInheritFollows(true);
                response.sendMessage("There are no more results", true);
            } else {
                final JSONObject obj = result.getJSONObject(offset);
                response.sendMessage("the next result for '" + query + "' is "
                        + obj.getString("unescapedUrl") + ", titled '"
                        + obj.getString("titleNoFormatting") + "'");
                response.addFollowup(new DescriptionFollowup(obj));
                response.addFollowup(new CacheFollowup(obj));
                response.addFollowup(new NextFollowup(query, result, offset + 1));
            }
        }

    }

}
