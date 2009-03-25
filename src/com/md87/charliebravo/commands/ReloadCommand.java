/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.md87.charliebravo.commands;

import com.md87.charliebravo.Command;
import com.md87.charliebravo.CommandOptions;
import com.md87.charliebravo.InputHandler;
import com.md87.charliebravo.Response;

/**
 *
 * @author chris
 */
@CommandOptions(requireAuthorisation=true,requireLevel=100)
public class ReloadCommand implements Command {

    public void execute(InputHandler handler, Response response, String line) throws Exception {
        handler.getConfig().getConfigfile().read();
        response.sendMessage("I have reloaded my config file", true);
    }

}
