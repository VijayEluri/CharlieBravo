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
import com.md87.util.crypto.ArcFourEncrypter;
import java.util.List;
import java.util.Random;
import net.miginfocom.Base64;

/**
 *
 * @author chris
 */
public class AuthenticateCommand implements Command {

    @SuppressWarnings("unchecked")
    public void execute(InputHandler handler, Response response, String line) throws Exception {
        if (line.isEmpty()) {
            response.sendMessage("You can get an auth token from http://apps.md87.co.uk/ircopenid/",
                    true);
        } else {
            final List<String> result = Downloader
                    .getPage("http://apps.MD87.co.uk/ircopenid/verify.php?id=" + line);

            if (result.isEmpty() || !result.get(0).trim().equalsIgnoreCase("Success")) {
                response.sendMessage("I could not authenticate that token", true);
            } else {
                final byte[] bytes = new byte[50];
                new Random().nextBytes(bytes);
                final String key1 = Base64.encodeToString(bytes, false);
                final int id = handler.getConfig().getConfigfile().getKeyDomain("cookies").size();
                
                final String openid = result.get(1).trim();
                handler.getParser().getClientInfoOrFake(response.getSource())
                        .getMap().put("OpenID", openid);
                handler.getParser().getClientInfoOrFake(response.getSource())
                        .getMap().put("Key1", key1);
                handler.getParser().getClientInfoOrFake(response.getSource())
                        .getMap().put("Cookie", "SET");
                handler.getConfig().setOption(openid, "internal.lastseen",
                        System.currentTimeMillis());
                handler.getConfig().setOption(openid, "internal.lastuser",
                        handler.getParser().getClientInfoOrFake(response.getSource()).toString());
                response.sendMessage("You are now authenticated as " + openid, true);

                handler.getConfig().getConfigfile().getKeyDomain("cookies")
                        .put(String.valueOf(id), key1);
                handler.getConfig().getConfigfile().getKeyDomain("cookie-ids")
                        .put(String.valueOf(id), openid);

                handler.getParser().sendCTCP(response.getSource(), "COOKIE",
                        "SET " + id + " "+ Base64.encodeToString(
                        new ArcFourEncrypter(key1).encrypt(openid.getBytes()), false));
            }
        }

    }

}
