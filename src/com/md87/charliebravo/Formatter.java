/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.md87.charliebravo;

/**
 *
 * @author chris
 */
public class Formatter {

    /**
     * Tests for and adds one component of the duration format.
     *
     * @param builder The string builder to append text to
     * @param current The number of seconds in the duration
     * @param duration The number of seconds in this component
     * @param name The name of this component
     * @return The number of seconds used by this component
     */
    private static int doDuration(final StringBuilder builder, final int current,
            final int duration, final String name) {
        int res = 0;

        if (current >= duration) {
            final int units = current / duration;
            res = units * duration;

            if (builder.length() > 0) {
                builder.append(", ");
            }

            builder.append(units);
            builder.append(' ');
            builder.append(name + (units != 1 ? 's' : ""));
        }

        return res;
    }

    /**
     * Formats the specified number of seconds as a string containing the
     * number of days, hours, minutes and seconds.
     *
     * @param duration The duration in seconds to be formatted
     * @return A textual version of the duration
     */
    public static String formatDuration(final int duration) {
        final StringBuilder buff = new StringBuilder();

        int seconds = duration;

        seconds -= doDuration(buff, seconds, 60*60*24, "day");
        seconds -= doDuration(buff, seconds, 60*60, "hour");
        seconds -= doDuration(buff, seconds, 60, "minute");
        seconds -= doDuration(buff, seconds, 1, "second");

        return buff.toString();
    }

}
