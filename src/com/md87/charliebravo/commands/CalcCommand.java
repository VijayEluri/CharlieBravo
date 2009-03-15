/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
