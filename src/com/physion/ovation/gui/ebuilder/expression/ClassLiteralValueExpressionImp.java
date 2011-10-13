package com.physion.ovation.gui.ebuilder.expression;


/**
 * TODO: Decide whether the "value" is a String or a Class object.
 * Decide whether the value will be gotten using getValue() or
 * if we will have a special method.
 */
public class ClassLiteralValueExpressionImp
    extends ExpressionImp
    implements ClassLiteralValueExpression {

    private String value;


    @Override
    public Object getValue() {
        return(value);
    }


    ClassLiteralValueExpressionImp(String value) {
        this.value = value;
    }


    public String toString(String indent) {
        return(indent+"ClassLiteralValueExpression("+value+")");
    }
}
