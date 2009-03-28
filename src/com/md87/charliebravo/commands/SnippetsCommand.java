/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.md87.charliebravo.commands;

import com.md87.charliebravo.Command;
import com.md87.charliebravo.InputHandler;
import com.md87.charliebravo.Response;

/**
 *
 * @author chris
 */
public class SnippetsCommand implements Command {

    public void execute(InputHandler handler, Response response, String line) throws Exception {
        final StringBuilder builder = new StringBuilder();

        for (String snipp : handler.getSnippets(response.getTarget()).getList()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }

            builder.append(snipp);
        }
        
        response.setInheritFollows(true);

        if (builder.length() == 0) {
            response.sendMessage("There are no detected snippets", true);
        } else {
            response.sendMessage("the following snippets are currently registered: "
                    + builder.toString());
        }
    }

}
