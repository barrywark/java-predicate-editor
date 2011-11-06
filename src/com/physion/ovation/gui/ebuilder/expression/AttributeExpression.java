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
 */
public class AttributeExpression
    extends Expression
    implements IAttributeExpression, Serializable {

	private static final long serialVersionUID = 1L;

	private String attributeName;


    /**
     * Get the name of this attribute.
     */
    @Override
    public String getAttributeName() {
        return(attributeName);
    }


    /**
     * Create an AttributeExpression with the passed in attributeName.
     */
    public AttributeExpression(String attributeName) {
        this.attributeName = attributeName;
    }


    /**
     * Convert this object to a string for testing/debugging purposes.
     */
    public String toString(String indent) {
        return(indent+"AttributeExpression("+attributeName+")");
    }
}
