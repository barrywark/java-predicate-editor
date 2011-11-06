/**
 * Copyright (c) 2011. Physion Consulting LLC
 * All rights reserved.
 */
package com.physion.ovation.gui.ebuilder.expression;


import java.io.Serializable;


/**
 * The com.physion.ovation.gui.ebuilder.expression package consists
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
 *
 * TODO: Decide whether the "value" is a String or a Class object.
 * Decide whether the value will be gotten using getValue() or
 * if we will have a special method.
 */
public class ClassLiteralValueExpression
    extends Expression
    implements IClassLiteralValueExpression, Serializable {

	private static final long serialVersionUID = 1L;

	private String value;


    /**
     * Get the name of the class as a String object.
     * Note, the returned type might change in a future version of the code.
     */
    @Override
    public Object getValue() {
        return(value);
    }


    public ClassLiteralValueExpression(String value) {
        this.value = value;
    }

    /**
     * Convert this object to a string for testing/debugging purposes.
     */
    public String toString(String indent) {
        return(indent+"ClassLiteralValueExpression("+value+")");
    }
}
