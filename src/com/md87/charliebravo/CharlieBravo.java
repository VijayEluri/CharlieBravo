/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
    
    public void run() {
        while (true) {
            final MyInfo myinfo = new MyInfo();
            myinfo.setNickname("CharlieBravo");
            myinfo.setRealname("Charlie Bravo");
            myinfo.setUsername("charliebravo");

            final IRCParser parser = new IRCParser(myinfo, new ServerInfo("irc.quakenet.org", 6667, ""));
            handler.setParser(parser);

            parser.getCallbackManager().addCallback("OnPost005", this);
            parser.getCallbackManager().addCallback("OnDebugInfo", this);
            parser.getCallbackManager().addCallback("OnDataIn", this);
            parser.getCallbackManager().addCallback("OnDataOut", this);
            parser.run();
        }
    }

    public void onPost005(final IRCParser tParser) {
        tParser.joinChannel("#MD87");
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
