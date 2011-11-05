/**
 * Copyright (c) 2011. Physion Consulting LLC
 * All rights reserved.
 */
package com.physion.ovation.gui.ebuilder.expression;


import java.io.Serializable;

/**
 */
public class StringLiteralValueExpression
    extends Expression
    implements IStringLiteralValueExpression, Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String value;


    @Override
    public Object getValue() {
        return(value);
    }


    public StringLiteralValueExpression(String value) {
        this.value = value;
    }


    public String toString(String indent) {
        return(indent+"StringLiteralValueExpression("+value+")");
    }
}
