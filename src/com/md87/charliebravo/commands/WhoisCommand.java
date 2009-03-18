/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.md87.charliebravo.commands;

import com.dmdirc.parser.irc.ClientInfo;
import com.dmdirc.ui.messages.Formatter;
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
            final ClientInfo ci = handler.getParser().getClientInfo(line);

            if (ci == null) {
                if (handler.getConfig().hasOption(line, "internal.lastseen")) {
                    final StringBuilder extra = new StringBuilder();

                    if (handler.getConfig().hasOption(line, "admin.level")) {
                        extra.append(", and has access level ");
                        extra.append(handler.getConfig().getOption(line, "admin.level"));
                    }

                    response.sendMessage(line + " last authenticated with me "
                            + Formatter.formatDuration((int)
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
                        extra.append(", and " + (you ? "have" : "has") + " access level ");
                        extra.append(handler.getConfig().getOption(openid, "admin.level"));
                    }

                    response.sendMessage((you ? "you are" : ci.getNickname() + " is")
                            + " authenticated as " + openid + extra);
                }
            }
        }

    }

}
