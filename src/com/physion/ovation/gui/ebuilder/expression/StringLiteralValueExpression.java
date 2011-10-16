package com.physion.ovation.gui.ebuilder.expression;


/**
 */
public class StringLiteralValueExpression
    extends Expression
    implements IStringLiteralValueExpression {

    private String value;


    @Override
    public Object getValue() {
        return(value);
    }


    StringLiteralValueExpression(String value) {
        this.value = value;
    }


    public String toString(String indent) {
        return(indent+"StringLiteralValueExpression("+value+")");
    }
}
