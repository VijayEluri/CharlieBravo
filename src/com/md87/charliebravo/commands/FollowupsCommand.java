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
public class FollowupsCommand implements Command {

    public void execute(InputHandler handler, Response response, String line) throws Exception {
        final StringBuilder builder = new StringBuilder();

        for (Command comm : handler.getLastResponse(response.getTarget()).getFollowups()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }

            builder.append(comm.getClass().getSimpleName());
        }
        
        response.setInheritFollows(true);

        if (builder.length() == 0) {
            response.sendMessage("There are no registered followups", true);
        } else {
            response.sendMessage("the following followups are currently registered: "
                    + builder.toString());
        }
    }

}
