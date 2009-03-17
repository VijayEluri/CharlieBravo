/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.md87.charliebravo;

import com.dmdirc.parser.irc.ChannelClientInfo;
import com.dmdirc.parser.irc.ChannelInfo;
import com.dmdirc.parser.irc.ClientInfo;
import com.dmdirc.parser.irc.IRCParser;
import com.dmdirc.parser.irc.callbacks.interfaces.IChannelMessage;
import com.dmdirc.parser.irc.callbacks.interfaces.IPrivateMessage;
import com.md87.charliebravo.commands.AuthenticateCommand;
import com.md87.charliebravo.commands.CalcCommand;
import com.md87.charliebravo.commands.FollowupsCommand;
import com.md87.charliebravo.commands.GitCommand;
import com.md87.charliebravo.commands.GoogleCommand;
import com.md87.charliebravo.commands.HelpCommand;
import com.md87.charliebravo.commands.IssueCommand;
import com.md87.charliebravo.commands.QuitCommand;
import com.md87.charliebravo.commands.SetCommand;
import com.md87.charliebravo.commands.TranslateCommand;
import com.md87.charliebravo.commands.WhoisCommand;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author chris
 */
public class InputHandler implements IChannelMessage, IPrivateMessage {

    protected IRCParser parser;

    protected final Config config;

    protected final List<Command> commands = new ArrayList<Command>();
    protected final Map<String, Response> responses = new HashMap<String, Response>();

    public InputHandler(final Config config) {
        this.config = config;
        
        commands.add(new GoogleCommand());
        commands.add(new QuitCommand());
        commands.add(new HelpCommand());
        commands.add(new FollowupsCommand());
        commands.add(new AuthenticateCommand());
        commands.add(new CalcCommand());
        commands.add(new WhoisCommand());
        commands.add(new TranslateCommand());
        commands.add(new IssueCommand());
        commands.add(new GitCommand());
        commands.add(new SetCommand());
    }

    public Config getConfig() {
        return config;
    }

    public List<Command> getCommands() {
        return new ArrayList<Command>(commands);
    }

    public Response getLastResponse(final String target) {
        return responses.get(target);
    }

    public IRCParser getParser() {
        return parser;
    }

    public void setParser(final IRCParser parser) {
        this.parser = parser;
        
        parser.getCallbackManager().addCallback("OnChannelMessage", this);
        parser.getCallbackManager().addCallback("OnPrivateMessage", this);
    }

    public void onChannelMessage(final IRCParser tParser, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient, final String sMessage, final String sHost) {
        if (sMessage.startsWith(tParser.getMyNickname() + ", ")) {
            handleInput(ClientInfo.parseHost(sHost), cChannel.getName(),
                    sMessage.substring((tParser.getMyNickname() + ", ").length()));
        }
    }

    public void onPrivateMessage(final IRCParser tParser, final String sMessage, final String sHost) {
        handleInput(ClientInfo.parseHost(sHost), ClientInfo.parseHost(sHost), sMessage);
    }

    protected void handleInput(final String source, final String target, final String text) {
        final Response response = new Response(parser, source, target);
        Command command = null;
        int index = 0;

        try {
            if (responses.containsKey(target)) {
                for (Followup followup : responses.get(target).getFollowups()) {
                    if (followup.matches(text)) {
                        command = followup;
                    }
                }
            }

            if (command == null) {
                for (Command pcommand : commands) {
                    if (text.toLowerCase().startsWith(pcommand.getClass()
                            .getSimpleName().replace("Command", "").toLowerCase())) {
                        command = pcommand;
                        index = pcommand.getClass().getSimpleName().length() - 6;
                    }
                }
            }

            if (command != null) {
                command.execute(this, response, text.substring(Math.min(text.length(), index)));
            }
        } catch (Throwable ex) {
            response.sendMessage("an error has occured: " + ex.getMessage());
            response.addFollowup(new StacktraceFollowup(ex));
        }

        if (command != null) {
            if (response.isInheritFollows() && responses.containsKey(target)) {
                response.addFollowups(responses.get(target).getFollowups());
            }
            responses.put(target, response);
        }
    }

    protected static class StacktraceFollowup implements Followup {

        private final Throwable ex;
        private final int index;

        public StacktraceFollowup(Throwable ex) {
            this(ex, 0);
        }

        public StacktraceFollowup(Throwable ex, int index) {
            this.ex = ex;
            this.index = index;
        }

        public boolean matches(String line) {
            return index == 0 ? line.equals("stacktrace") : line.equals("more");
        }

        public void execute(final InputHandler handler, Response response, String line) throws Exception {
            StringBuilder trace = new StringBuilder();

            int i;

            for (i = index; i < ex.getStackTrace().length; i++) {
                if (trace.length() > 0) {
                    int next = ex.getStackTrace()[i].toString().length() + 2;

                    if (trace.length() + next + 33 + (index == 0 ? 5 : 4)
                            + String.valueOf(i - index).length() > response.getMaxLength()) {
                        break;
                    }

                    trace.append("; ");
                }

                trace.append(ex.getStackTrace()[i].toString());
            }

            response.sendMessage("the " + (index == 0 ? "first" : 
                i < ex.getStackTrace().length ? "next" : "last")
                    + " " + (i - index) + " elements of the trace are: " + trace);

            if (i < ex.getStackTrace().length) {
                response.addFollowup(new StacktraceFollowup(ex, i));
            }
        }

    }

}
