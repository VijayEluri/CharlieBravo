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

package com.md87.charliebravo;

import com.dmdirc.parser.common.MyInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.DataInListener;
import com.dmdirc.parser.interfaces.callbacks.DataOutListener;
import com.dmdirc.parser.interfaces.callbacks.DebugInfoListener;
import com.dmdirc.parser.interfaces.callbacks.Post005Listener;
import com.dmdirc.parser.irc.IRCParser;
import com.dmdirc.parser.irc.ServerInfo;
import java.util.Date;

/**
 *
 * @author chris
 */
public class CharlieBravo implements Runnable, Post005Listener,
        DebugInfoListener, DataInListener, DataOutListener {

    protected final Config config = new Config();
    protected final InputHandler handler = new InputHandler(config);
    protected final String[] servers = {"irc.quakenet.org","83.140.172.211"};
    
    public void run() {
        int server = 0;

        while (true) {
            final MyInfo myinfo = new MyInfo();
            myinfo.setNickname("CharlieBravo");
            myinfo.setRealname("Charlie Bravo");
            myinfo.setUsername("charliebravo");

            final IRCParser parser = new IRCParser(myinfo, new ServerInfo(servers[server], 6667, ""));
            handler.setParser(parser);

            parser.getCallbackManager().addCallback(Post005Listener.class, this);
            parser.getCallbackManager().addCallback(DebugInfoListener.class, this);
            parser.getCallbackManager().addCallback(DataInListener.class, this);
            parser.getCallbackManager().addCallback(DataOutListener.class, this);
            parser.run();

            server = ++server % servers.length;

            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                // Don't care!
            }
        }
    }

    @Override
    public void onPost005(final Parser tParser, final Date date) {
        tParser.joinChannel("#MD87");
        tParser.joinChannel("#DMDirc");
        tParser.joinChannel("#DMDirc.dev");
        tParser.joinChannel("#MDbot");
    }

    @Override
    public void onDebugInfo(final Parser tParser, final Date date, final int nLevel, final String sData) {
        System.out.println(nLevel + ": " + sData);
    }

    public static void main(final String ... args) throws Exception {
        new Thread(new CharlieBravo()).start();
    }

    @Override
    public void onDataIn(final Parser tParser, final Date date, final String sData) {
        System.out.println("<<< " + sData);
    }

    @Override
    public void onDataOut(final Parser tParser, final Date date, final String sData, final boolean bFromParser) {
        System.out.println(">>> " + sData);
    }
    
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

        return buff.length() == 0 ? "0 seconds" : buff.toString();
    }

}
