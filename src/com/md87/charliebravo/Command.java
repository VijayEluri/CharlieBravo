/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.md87.charliebravo;

/**
 *
 * @author chris
 */
public interface Command {

    void execute(final InputHandler handler, final Response response,
            final String line) throws Exception;

}
