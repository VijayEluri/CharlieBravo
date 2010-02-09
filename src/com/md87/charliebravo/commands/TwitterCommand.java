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
import com.md87.charliebravo.CommandOptions;
import com.md87.charliebravo.InputHandler;
import com.md87.charliebravo.Response;
import twitter4j.Twitter;

/**
 *
 * @author chris
 */
@CommandOptions(requireAuthorisation=true, requiredSettings={"twitter.username","twitter.password"})
public class TwitterCommand implements Command {

    public void execute(InputHandler handler, Response response, String line) throws Exception {
        final String openID = (String) handler.getParser().getClientInfoOrFake(response.getSource())
                .getMap().get("OpenID");

        String user = handler.getConfig().getOption(openID, "twitter.username");
        String pass = handler.getConfig().getOption(openID, "twitter.password");

        Twitter twitter = new Twitter(user, pass);
        twitter.updateStatus(line);
        response.sendMessage("Done", true);
    }

}
