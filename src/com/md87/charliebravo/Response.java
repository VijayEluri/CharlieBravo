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
