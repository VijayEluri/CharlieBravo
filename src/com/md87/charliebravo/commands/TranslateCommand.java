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
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author chris
 */
public class TranslateCommand implements Command {

    protected static final String[][] LANGUAGES = {
        {"AFRIKAANS", "af"},
        {"ALBANIAN", "sq"},
        {"AMHARIC", "am"},
        {"ARABIC", "ar"},
        {"ARMENIAN", "hy"},
        {"AZERBAIJANI", "az"},
        {"BASQUE", "eu"},
        {"BELARUSIAN", "be"},
        {"BENGALI", "bn"},
        {"BIHARI", "bh"},
        {"BULGARIAN", "bg"},
        {"BURMESE", "my"},
        {"CATALAN", "ca"},
        {"CHEROKEE", "chr"},
        {"CHINESE", "zh"},
        {"CHINESE (SIMPLIFIED)", "zh-CN"},
        {"CHINESE (TRADITIONAL)", "zh-TW"},
        {"SIMPLIFIED CHINESE", "zh-CN"},
        {"TRADITIONAL CHINESE", "zh-TW"},
        {"CROATIAN", "hr"},
        {"CZECH", "cs"},
        {"DANISH", "da"},
        {"DHIVEHI", "dv"},
        {"DUTCH", "nl"},
        {"ENGLISH", "en"},
        {"ESPERANTO", "eo"},
        {"ESTONIAN", "et"},
        {"FILIPINO", "tl"},
        {"FINNISH", "fi"},
        {"FRENCH", "fr"},
        {"GALICIAN", "gl"},
        {"GEORGIAN", "ka"},
        {"GERMAN", "de"},
        {"GREEK", "el"},
        {"GUARANI", "gn"},
        {"GUJARATI", "gu"},
        {"HEBREW", "iw"},
        {"HINDI", "hi"},
        {"HUNGARIAN", "hu"},
        {"ICELANDIC", "is"},
        {"INDONESIAN", "id"},
        {"INUKTITUT", "iu"},
        {"ITALIAN", "it"},
        {"JAPANESE", "ja"},
        {"KANNADA", "kn"},
        {"KAZAKH", "kk"},
        {"KHMER", "km"},
        {"KOREAN", "ko"},
        {"KURDISH", "ku"},
        {"KYRGYZ", "ky"},
        {"LAOTHIAN", "lo"},
        {"LATVIAN", "lv"},
        {"LITHUANIAN", "lt"},
        {"MACEDONIAN", "mk"},
        {"MALAY", "ms"},
        {"MALAYALAM", "ml"},
        {"MALTESE", "mt"},
        {"MARATHI", "mr"},
        {"MONGOLIAN", "mn"},
        {"NEPALI", "ne"},
        {"NORWEGIAN", "no"},
        {"ORIYA", "or"},
        {"PASHTO", "ps"},
        {"PERSIAN", "fa"},
        {"POLISH", "pl"},
        {"PORTUGUESE", "pt-PT"},
        {"PUNJABI", "pa"},
        {"ROMANIAN", "ro"},
        {"RUSSIAN", "ru"},
        {"SANSKRIT", "sa"},
        {"SERBIAN", "sr"},
        {"SINDHI", "sd"},
        {"SINHALESE", "si"},
        {"SLOVAK", "sk"},
        {"SLOVENIAN", "sl"},
        {"SPANISH", "es"},
        {"SWAHILI", "sw"},
        {"SWEDISH", "sv"},
        {"TAJIK", "tg"},
        {"TAMIL", "ta"},
        {"TAGALOG", "tl"},
        {"TELUGU", "te"},
        {"THAI", "th"},
        {"TIBETAN", "bo"},
        {"TURKISH", "tr"},
        {"UKRAINIAN", "uk"},
        {"URDU", "ur"},
        {"UZBEK", "uz"},
        {"UIGHUR", "ug"},
        {"VIETNAMESE", "vi"},
    };

    public void execute(final InputHandler handler, Response response, String line) throws MalformedURLException, IOException, JSONException {
        String target = "en";
        String text = line;
        int offset;

        if ((offset = text.lastIndexOf(" into ")) > -1) {
            final String lang = text.substring(offset + 6);

            for (String[] pair : LANGUAGES) {
                if (pair[0].equalsIgnoreCase(lang)) {
                    target = pair[1];
                    text = text.substring(0, offset);
                }
            }
        }

        URL url = new URL("http://ajax.googleapis.com/ajax/services/language/translate?v=1.0&langpair=%7C" + target + "&q=" +
                URLEncoder.encode(text, Charset.defaultCharset().name()));
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

        response.sendMessage("that translates to \""
                + json.getJSONObject("responseData").getString("translatedText") + "\"");
        response.addFollowup(new LanguageFollowup(json.getJSONObject("responseData")));
    }

    protected static class LanguageFollowup implements Followup {

        private final JSONObject result;

        public LanguageFollowup(JSONObject result) {
            this.result = result;
        }

        public boolean matches(String line) {
            return line.equals("language");
        }

        public void execute(final InputHandler handler, Response response, String line) throws Exception {
            final String target = result.getString("detectedSourceLanguage");
            for (String[] pair : LANGUAGES) {
                if (pair[1].equalsIgnoreCase(target)) {
                    response.sendMessage("the language was detected as "
                            + pair[0].charAt(0) + pair[0].substring(1).toLowerCase());
                    return;
                }
            }
            
            response.sendMessage("the language was detected as '"
                    + target + "', but I don't know what that is");
        }

    }

}
