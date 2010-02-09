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

import com.dmdirc.parser.irc.IRCParser;
import com.dmdirc.parser.irc.MyInfo;
import com.dmdirc.parser.irc.ServerInfo;
import com.dmdirc.parser.irc.callbacks.interfaces.IDataIn;
import com.dmdirc.parser.irc.callbacks.interfaces.IDataOut;
import com.dmdirc.parser.irc.callbacks.interfaces.IDebugInfo;
import com.dmdirc.parser.irc.callbacks.interfaces.IPost005;

/**
 *
 * @author chris
 */
public class CharlieBravo implements Runnable, IPost005, IDebugInfo, IDataIn, IDataOut {

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

            parser.getCallbackManager().addCallback("OnPost005", this);
            parser.getCallbackManager().addCallback("OnDebugInfo", this);
            parser.getCallbackManager().addCallback("OnDataIn", this);
            parser.getCallbackManager().addCallback("OnDataOut", this);
            parser.run();

            server = ++server % servers.length;

            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                // Don't care!
            }
        }
    }

    public void onPost005(final IRCParser tParser) {
        tParser.joinChannel("#MD87");
        tParser.joinChannel("#DMDirc");
        tParser.joinChannel("#DMDirc.dev");
        tParser.joinChannel("#MDbot");
    }

    public void onDebugInfo(final IRCParser tParser, final int nLevel, final String sData) {
        System.out.println(nLevel + ": " + sData);
    }

    public static void main(final String ... args) throws Exception {
        new Thread(new CharlieBravo()).start();
    }

    public void onDataIn(final IRCParser tParser, final String sData) {
        System.out.println("<<< " + sData);
    }

    public void onDataOut(final IRCParser tParser, final String sData, final boolean bFromParser) {
        System.out.println(">>> " + sData);
    }

}
