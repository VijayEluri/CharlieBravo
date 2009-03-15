/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.dmdirc.addons.calc;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author chris
 */
public class TreeToken {

    private final List<TreeToken> children = new ArrayList<TreeToken>();

    private final Token token;

    private boolean processed = false;

    public TreeToken(Token token) {
        this.token = token;
    }

    public List<TreeToken> getChildren() {
        return children;
    }

    public Token getToken() {
        return token;
    }

    public void addChild(final TreeToken token) {
        children.add(token);
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed() {
        processed = true;
    }

    public Number evaluate() {
        return token.getType().evaluate(this);
    }

    @Override
    public String toString() {
        return "[token: " + token + "; children: " + children + "; processed: "
                + processed + "]";
    }

}
