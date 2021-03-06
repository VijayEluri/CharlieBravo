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

import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.util.DateUtils;
import com.md87.charliebravo.Command;
import com.md87.charliebravo.InputHandler;
import com.md87.charliebravo.Response;

/**
 *
 * @author chris
 */
public class WhoisCommand implements Command {

    @SuppressWarnings("unchecked")
    public void execute(InputHandler handler, Response response, String line) throws Exception {
        if (line.isEmpty()) {
            response.sendRawMessage("Who would you like to whois, " + response.getSource() + "?");
        } else {
            final ClientInfo ci = handler.getParser().getClient(line);

            if (ci == null) {
                if (handler.getConfig().hasOption(line, "internal.lastseen")) {
                    final StringBuilder extra = new StringBuilder();

                    if (handler.getConfig().hasOption(line, "admin.level")) {
                        extra.append(", and has access level ");
                        extra.append(handler.getConfig().getOption(line, "admin.level"));
                    }

                    response.sendMessage(line + " last authenticated with me "
                            + DateUtils.formatDuration((int)
                            (System.currentTimeMillis() -
                            Long.valueOf(handler.getConfig().getOption(line, "internal.lastseen")))
                            / 1000)
                            + " ago" + extra);
                } else {
                    response.sendMessage("I am not aware of anyone by that name", true);
                }
            } else {
                final String openid = (String) ci.getMap().get("OpenID");
                final boolean you = ci.getNickname().equals(response.getSource());

                if (openid == null) {
                    response.sendMessage((you ? "You have" : ci.getNickname() + " has")
                            + " not authenticated with me", true);
                } else {
                    final StringBuilder extra = new StringBuilder();

                    if (handler.getConfig().hasOption(openid, "admin.level")) {
                        extra.append(", and ").append(you ? "have" : "has").append(" access level ");
                        extra.append(handler.getConfig().getOption(openid, "admin.level"));
                    }

                    response.sendMessage((you ? "you are" : ci.getNickname() + " is")
                            + " authenticated as " + openid + extra);
                }
            }
        }

    }

}
