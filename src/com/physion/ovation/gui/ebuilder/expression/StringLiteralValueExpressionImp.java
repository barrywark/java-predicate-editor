package com.physion.ovation.gui.ebuilder.expression;


/**
 */
public class StringLiteralValueExpressionImp
    extends ExpressionImp
    implements StringLiteralValueExpression {

    private String value;


    @Override
    public Object getValue() {
        return(value);
    }


    StringLiteralValueExpressionImp(String value) {
        this.value = value;
    }


    public String toString(String indent) {
        return(indent+"StringLiteralValueExpression("+value+")");
    }
}
