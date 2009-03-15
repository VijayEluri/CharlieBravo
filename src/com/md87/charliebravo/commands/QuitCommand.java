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
public class QuitCommand implements Command {

    /** {@inheritDoc} */
    @Override
    public void execute(final InputHandler handler, final Response response, final String line) {
        response.sendMessage("Goodbye", true);
        System.exit(0);
    }

}
