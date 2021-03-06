/**
 * Copyright (c) 2011. Physion Consulting LLC
 * All rights reserved.
 */
package com.physion.ebuilder.expression;

import java.io.Serializable;


/**
 * The com.physion.ebuilder.expression package consists
 * of a set of interfaces and a set of classes that implement those
 * interfaces.  There is almost a one-to-one mapping between interface
 * files and class files.  For example, there is an interface
 * IAttributeExpression and a class that implements that interface
 * called AttributeExpression.
 *
 * The set of interfaces are based on the already existing C++
 * interface to the Objectivity library.  It is assumed that at
 * some point in the future, this ...ebuilder.expression package
 * will be replaced with a Java version of the Objectivity library.
 */
public class StringLiteralValueExpression
    extends Expression
    implements IStringLiteralValueExpression, Serializable {

	private static final long serialVersionUID = 1L;

	private String value;


    /**
     * Get the value as a String object.
     */
    @Override
    public Object getValue() {
        return(value);
    }


    public StringLiteralValueExpression(String value) {
        this.value = value;
    }


    /**
     * Convert this object to a string for testing/debugging purposes.
     */
    public String toString(String indent) {
        return(indent+"StringLiteralValueExpression("+value+")");
    }
}
