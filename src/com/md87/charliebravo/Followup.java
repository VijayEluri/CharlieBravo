/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.md87.charliebravo;

/**
 *
 * @author chris
 */
public interface Followup extends Command {

    boolean matches(final String line);

}
