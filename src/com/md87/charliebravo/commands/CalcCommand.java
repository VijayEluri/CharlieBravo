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

package com.md87.charliebravo.commands;

import com.dmdirc.addons.calc.Evaluator;
import com.dmdirc.addons.calc.Lexer;
import com.dmdirc.addons.calc.Parser;
import com.md87.charliebravo.Command;
import com.md87.charliebravo.Followup;
import com.md87.charliebravo.InputHandler;
import com.md87.charliebravo.Response;

/**
 *
 * @author chris
 */
public class CalcCommand implements Command {

    public void execute(InputHandler handler, Response response, String line) throws Exception {
        final Lexer lexer = new Lexer(line);
        final Parser parser = new Parser(lexer);
        final Evaluator evaluator = new Evaluator(parser.parse());
        final Number result = evaluator.evaluate();

        response.sendMessage(line + " = " + result);
        response.addFollowup(new AddFollowup(result.toString()));
        response.addFollowup(new SubtractFollowup(result.toString()));
        response.addFollowup(new MultiplyByFollowup(result.toString()));
        response.addFollowup(new DivideByFollowup(result.toString()));
    }

    protected static class AddFollowup implements Followup {

        private final String result;

        public AddFollowup(String result) {
            this.result = result;
        }

        public boolean matches(String line) {
            return line.startsWith("add ");
        }

        public void execute(InputHandler handler, Response response, String line) throws Exception {
            new CalcCommand().execute(handler, response, result + " + " + line.substring(4));
        }
    }

    protected static class SubtractFollowup implements Followup {

        private final String result;

        public SubtractFollowup(String result) {
            this.result = result;
        }

        public boolean matches(String line) {
            return line.startsWith("subtract ");
        }

        public void execute(InputHandler handler, Response response, String line) throws Exception {
            new CalcCommand().execute(handler, response, result + " - " + line.substring(9));
        }
    }

    protected static class MultiplyByFollowup implements Followup {

        private final String result;

        public MultiplyByFollowup(String result) {
            this.result = result;
        }

        public boolean matches(String line) {
            return line.startsWith("multiply by ");
        }

        public void execute(InputHandler handler, Response response, String line) throws Exception {
            new CalcCommand().execute(handler, response, result + " * " + line.substring(13));
        }
    }

    protected static class DivideByFollowup implements Followup {

        private final String result;

        public DivideByFollowup(String result) {
            this.result = result;
        }

        public boolean matches(String line) {
            return line.startsWith("divide by ");
        }

        public void execute(InputHandler handler, Response response, String line) throws Exception {
            new CalcCommand().execute(handler, response, result + " / " + line.substring(10));
        }
    }

}
