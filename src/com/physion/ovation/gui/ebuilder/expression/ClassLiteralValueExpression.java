package com.physion.ovation.gui.ebuilder.expression;


import java.io.Serializable;


/**
 * TODO: Decide whether the "value" is a String or a Class object.
 * Decide whether the value will be gotten using getValue() or
 * if we will have a special method.
 */
public class ClassLiteralValueExpression
    extends Expression
    implements IClassLiteralValueExpression, Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String value;


    @Override
    public Object getValue() {
        return(value);
    }


    public ClassLiteralValueExpression(String value) {
        this.value = value;
    }


    public String toString(String indent) {
        return(indent+"ClassLiteralValueExpression("+value+")");
    }
}
