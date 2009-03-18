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
import com.md87.charliebravo.commands.SkillCommand;
import com.md87.charliebravo.commands.TranslateCommand;
import com.md87.charliebravo.commands.WhoisCommand;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

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
        commands.add(new SkillCommand());
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
        if (sMessage.matches("^(?i)" + Matcher.quoteReplacement(tParser.getMyNickname()) + "[,:!] .*")) {
            handleInput(ClientInfo.parseHost(sHost), cChannel.getName(),
                    sMessage.substring(tParser.getMyNickname().length() + 2));
        }
    }

    public void onPrivateMessage(final IRCParser tParser, final String sMessage, final String sHost) {
        handleInput(ClientInfo.parseHost(sHost), ClientInfo.parseHost(sHost), sMessage);
    }

    protected void handleInput(final String source, final String target, final String text) {
        new Thread(new Runnable() {
            public void run() {
                handleInputImpl(source, target, text);
            }
        }).start();
    }

    protected void handleInputImpl(final String source, final String target, final String text) {
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
                boolean cont = true;

                if (command.getClass().isAnnotationPresent(CommandOptions.class)) {
                    final CommandOptions opts = command.getClass()
                            .getAnnotation(CommandOptions.class);

                    final String id = (String) parser.getClientInfoOrFake(source)
                            .getMap().get("OpenID");

                    if (opts.requireAuthorisation() && id == null) {
                        response.sendMessage("You must be authorised to use that command", true);
                        cont = false;
                    } else if (opts.requireLevel() > -1 &&
                            (!config.hasOption(id, "admin.level")
                            || (Integer.valueOf(config.getOption(id, "admin.level"))
                            < opts.requireLevel()))) {
                        response.sendMessage("You have insufficient access to " +
                                "use that command", true);
                        response.addFollowup(new LevelErrorFollowup(response.getSource(),
                                opts.requireLevel(),
                                config.hasOption(id, "admin.level")
                                ? Integer.valueOf(config.getOption(id, "admin.level")) : -1));
                        cont = false;
                    } else {
                        int count = 0;
                        final StringBuilder missing = new StringBuilder();
                        
                        for (String setting : opts.requiredSettings()) {

                            if (!config.hasOption(id, setting)) {
                                if (missing.length() > 0) {
                                    missing.append(", ");
                                }

                                count++;
                                missing.append(setting);
                            }
                        }

                        if (count > 0) {
                            cont = false;
                            response.sendRawMessage("You must have the following setting"
                                    + (count == 1 ? "" : "s")
                                    + " in order to use that command, " + response.getSource()
                                    + ": " + missing.toString()
                                    .replaceAll("^(.*), (.*?)$", "$1 and $2") + ".");
                        }
                    }
                }

                if (cont) {
                    command.execute(this, response, text.substring(Math.min(text.length(), index)));
                }
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

    protected static class LevelErrorFollowup implements Followup {

        private final String source;
        private final int required, desired;

        public LevelErrorFollowup(String source, int required, int desired) {
            this.source = source;
            this.required = required;
            this.desired = desired;
        }

        public boolean matches(String line) {
            return line.equalsIgnoreCase("details");
        }

        public void execute(InputHandler handler, Response response, String line) throws Exception {
            final boolean you = response.getSource().equals(source);
            response.sendMessage("that command requires level " + required
                    + " access, but " + (you ? "you" : source) + " "
                    + (desired == -1 ? "do" + (you ? "" : "es") + " not have any assigned level"
                    : "only ha" + (you ? "ve" : "s") + " level " + desired));
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
