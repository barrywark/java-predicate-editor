/**
 * Copyright (c) 2011. Physion Consulting LLC
 * All rights reserved.
 */
package com.physion.ovation.gui.ebuilder.expression;


import java.io.Serializable;


/**
 */
public class BooleanLiteralValueExpression
    extends Expression
    implements IBooleanLiteralValueExpression, Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean value;


    @Override
    public Object getValue() {
        return(new Boolean(value));
    }


    public BooleanLiteralValueExpression(Boolean value) {
        this.value = value;
    }


    public String toString(String indent) {
        return(indent+"BooleanLiteralValueExpression("+value+")");
    }
}
