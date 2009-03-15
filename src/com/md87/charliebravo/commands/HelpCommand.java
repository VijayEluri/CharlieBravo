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
public class HelpCommand implements Command {

    public void execute(InputHandler handler, Response response, String line) throws Exception {
        final StringBuilder builder = new StringBuilder();

        for (Command comm : handler.getCommands()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }

            builder.append(comm.getClass().getSimpleName().replace("Command", "").toLowerCase());
        }

        response.sendMessage("I know the following commands: " + builder.toString());
    }

}
