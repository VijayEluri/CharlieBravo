/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.md87.charliebravo;

import com.dmdirc.parser.irc.IRCParser;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author chris
 */
public class Response {

    private final IRCParser parser;
    private final String source, target;
    private final List<Followup> followups = new ArrayList<Followup>();
    private boolean inheritFollows = false;

    public Response(IRCParser parser, String source, String target) {
        this.parser = parser;
        this.source = source;
        this.target = target;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public void sendMessage(final String message) {
        sendMessage(message, false);
    }

    public void sendMessage(final String message, final boolean suffix) {
        final String line = (suffix ? "" : source + ", ")
                + message + (suffix ? ", " + source : "") + ".";

        sendRawMessage(line);
    }

    public void sendRawMessage(final String line) {
        parser.sendMessage(target, line);

        for (Followup followup : new ArrayList<Followup>(followups)) {
            if (followup instanceof RepeatFollowup) {
                followups.remove(followup);
            }
        }

        followups.add(new RepeatFollowup(line));
    }

    public void addFollowup(final Followup followup) {
        followups.add(followup);
    }

    public List<Followup> getFollowups() {
        return followups;
    }

    public int getMaxLength() {
        return parser.getMaxLength("PRIVMSG", target) - source.length() - 3;
    }

    public boolean isInheritFollows() {
        return inheritFollows;
    }

    public void setInheritFollows(boolean inheritFollows) {
        this.inheritFollows = inheritFollows;
    }

    public void addFollowups(List<Followup> followups) {
        this.followups.addAll(followups);
    }

    protected static class RepeatFollowup implements Followup {

        private final String myLine;

        public RepeatFollowup(String myLine) {
            this.myLine = myLine;
        }

        public boolean matches(String line) {
            return line.startsWith("repeat") || line.equals("say again");
        }

        public void execute(InputHandler handler, Response response, String line) throws Exception {
            response.setInheritFollows(true);
            response.sendRawMessage(myLine);
        }

    }

}
