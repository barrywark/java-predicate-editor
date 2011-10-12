package com.physion.ovation.gui.ebuilder.expression;


/**
 */
public class BooleanLiteralValueExpressionImp
    extends ExpressionImp
    implements BooleanLiteralValueExpression {

    private boolean value;


    @Override
    public Object getValue() {
        return(new Boolean(value));
    }


    BooleanLiteralValueExpressionImp(Boolean value) {
        this.value = value;
    }


    public String toString(String indent) {
        return(indent+"BooleanLiteralValueExpression("+value+")");
    }
}
