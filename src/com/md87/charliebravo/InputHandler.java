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


import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.ChannelKickListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelMessageListener;
import com.dmdirc.parser.interfaces.callbacks.PrivateCtcpListener;
import com.dmdirc.parser.interfaces.callbacks.PrivateMessageListener;
import com.dmdirc.parser.irc.IRCParser;

import com.dmdirc.util.RollingList;
import com.md87.charliebravo.commands.AuthenticateCommand;
import com.md87.charliebravo.commands.CalcCommand;
import com.md87.charliebravo.commands.DefineCommand;
import com.md87.charliebravo.commands.FollowupsCommand;
import com.md87.charliebravo.commands.GitCommand;
import com.md87.charliebravo.commands.GoogleCommand;
import com.md87.charliebravo.commands.HelpCommand;
import com.md87.charliebravo.commands.IssueCommand;
import com.md87.charliebravo.commands.NewzbinCommand;
import com.md87.charliebravo.commands.QuitCommand;
import com.md87.charliebravo.commands.ReloadCommand;
import com.md87.charliebravo.commands.SetCommand;
import com.md87.charliebravo.commands.SkillCommand;
import com.md87.charliebravo.commands.SnippetsCommand;
import com.md87.charliebravo.commands.LawCommand;
import com.md87.charliebravo.commands.QuoteCommand;
import com.md87.charliebravo.commands.TranslateCommand;
import com.md87.charliebravo.commands.WhoisCommand;
import com.md87.util.crypto.ArcFourEncrypter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import net.miginfocom.Base64;

/**
 *
 * @author chris
 */
public class InputHandler implements ChannelMessageListener, PrivateMessageListener, PrivateCtcpListener, ChannelKickListener {

    protected IRCParser parser;

    protected final Config config;

    protected final List<Command> commands = new ArrayList<Command>();
    protected final Map<String, Response> responses = new HashMap<String, Response>();
    protected final Map<String, RollingList<String>> snippets = new HashMap<String, RollingList<String>>();

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
        commands.add(new SnippetsCommand());
        commands.add(new NewzbinCommand());
        commands.add(new ReloadCommand());
        commands.add(new DefineCommand());
        commands.add(new LawCommand());
        commands.add(new QuoteCommand());
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

    public RollingList<String> getSnippets(final String channel) {
        return snippets.get(channel);
    }

    public IRCParser getParser() {
        return parser;
    }

    public void setParser(final IRCParser parser) {
        this.parser = parser;
        
        parser.getCallbackManager().addCallback(ChannelMessageListener.class, this);
        parser.getCallbackManager().addCallback(PrivateMessageListener.class, this);
        parser.getCallbackManager().addCallback(PrivateCtcpListener.class, this);
        parser.getCallbackManager().addCallback(ChannelKickListener.class, this);
    }

    @Override
    public void onChannelMessage(final Parser tParser, final Date date, final ChannelInfo cChannel,
            final ChannelClientInfo cChannelClient, final String sMessage, final String sHost) {
        for (String nick : getNicknames()) {
            if (sMessage.matches("^(?i)" + Matcher.quoteReplacement(nick) + "[,:!] .*")) {
                handleInput(tParser.parseHostmask(sHost)[0], cChannel.getName(),
                        sMessage.substring(nick.length() + 2));
                break;
            } else if (sMessage.matches("^(?i)" + Matcher.quoteReplacement(nick) + "\\?")
                    && snippets.containsKey(cChannel.getName())) {
                final RollingList<String> snips = snippets.get(cChannel.getName());
                final String snippet = snips.get(snips.getList().size() - 1);
                handleInput(tParser.parseHostmask(sHost)[0], cChannel.getName(), snippet);
                snips.remove(snippet);
                break;
            }
        }

        for (Map.Entry<String, String> snippet : config.getConfigfile()
                .getKeyDomain("snippets").entrySet()) {
            if (sMessage.matches(snippet.getKey())) {
                if (!snippets.containsKey(cChannel.getName())) {
                    snippets.put(cChannel.getName(), new RollingList<String>(10));
                }

                snippets.get(cChannel.getName()).add(sMessage.replaceFirst(snippet.getKey(),
                        snippet.getValue()));
                System.out.println("Snippet: " + sMessage.replaceFirst(snippet.getKey(),
                        snippet.getValue()));
            }
        }
    }

    protected String[] getNicknames() {
        return new String[] {
            parser.getMyNickname(),
            parser.getMyNickname().replaceAll("[a-z]", ""),
            parser.getMyNickname().replaceAll("[^a-zA-Z]", ""),
            parser.getMyNickname().replaceAll("[^A-Z]", ""),
        };
    }

    @Override
    public void onPrivateMessage(final Parser tParser, final Date date, final String sMessage, final String sHost) {
        handleInput(tParser.parseHostmask(sHost)[0], tParser.parseHostmask(sHost)[0], sMessage);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onPrivateCTCP(Parser tParser, Date date, String sType, String sMessage, String sHost) {
        final ClientInfo client = tParser.getClient(sHost);
        if ("COOKIE".equals(sType)) {
            final String status = (String) client.getMap().get("Cookie");
            final String key1 = (String) client.getMap().get("Key1");
            final String key2 = (String) client.getMap().get("Key2");
            final String idp = (String) client.getMap().get("OpenID-p");
            
            if ("GETKEY".equals(sMessage) && "SET".equals(status)) {
                parser.sendCTCP(tParser.parseHostmask(sHost)[0], "COOKIE", "SETKEY " + key1);
                client.getMap().put("Cookie", "GETKEY");
            } else if (sMessage.startsWith("OFFER")) {
                final String what = sMessage.substring(6);

                if (config.getConfigfile().getKeyDomain("cookies").containsKey(what)) {
                    client.getMap().put("Key1", config.getConfigfile()
                            .getKeyDomain("cookies").get(what));
                    client.getMap().put("OpenID-p", config.getConfigfile()
                            .getKeyDomain("cookie-ids").get(what));
                }

                final byte[] bytes = new byte[50];
                new Random().nextBytes(bytes);
                final String newkey2 = Base64.encodeToString(bytes, false);

                client.getMap().put("Cookie", "OFFER");
                client.getMap().put("Key2", newkey2);
                parser.sendCTCP(tParser.parseHostmask(sHost)[0], "COOKIE", "GET " + newkey2);
            } else if (sMessage.startsWith("SHOW") && "OFFER".equals(status)) {
                final String payload = sMessage.substring(5);
                final String info = new String(new ArcFourEncrypter(key1 + key2)
                        .encrypt(Base64.decode(payload)));

                if (idp.equals(info)) {
                    client.getMap().put("OpenID", idp);
                    parser.sendNotice(client.getNickname(), "You are now authenticated as " + idp);
                }
            }
        }
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
                    if (text.equalsIgnoreCase(pcommand.getClass()
                            .getSimpleName().replace("Command", "")) ||
                            text.toLowerCase().startsWith(pcommand.getClass()
                            .getSimpleName().replace("Command", "").toLowerCase() + " ")) {
                        command = pcommand;
                        index = pcommand.getClass().getSimpleName().length() - 6;
                        break;
                    }
                }
            }

            if (command != null) {
                boolean cont = true;

                if (command.getClass().isAnnotationPresent(CommandOptions.class)) {
                    final CommandOptions opts = command.getClass()
                            .getAnnotation(CommandOptions.class);

                    final String id = (String) parser.getClient(source)
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
            ex.printStackTrace();
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

    @Override
    public void onChannelKick(Parser tParser, Date date, ChannelInfo cChannel,
            ChannelClientInfo cKickedClient, ChannelClientInfo cKickedByClient,
            String sReason, String sKickedByHost) {
        if (cKickedClient.getClient().equals(parser.getLocalClient())) {
            tParser.joinChannel(cChannel.getName());
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
