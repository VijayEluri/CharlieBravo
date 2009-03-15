/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.dmdirc.addons.calc;

/**
 *
 * @author chris
 */
public class Token {

    private final TokenType type;
    private String content;

    public Token(TokenType type, String content) {
        this.type = type;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public TokenType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "[type: " + type + "; content: " + content + "]";
    }

}
