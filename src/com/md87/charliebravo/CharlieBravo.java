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

}
