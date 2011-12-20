/**
 * Copyright (c) 2011. Physion Consulting LLC
 * All rights reserved.
 */
package com.physion.ovation.gui.ebuilder.expression;

import java.util.List;


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
public interface IOperatorExpression
    extends IExpression {

    /**
     * Get the operatorName for this OperatorExpression.
     */
    public String getOperatorName();


    /**
     * This method returns the operand list.
     *
     * Please note, the returned list is NOT a copy
     * of this OperatorExpression's operand list, so
     * don't mess with it.
     *
     * Please note, the returned list might be empty,
     * but it is never null.
     */
    public List<IExpression> getOperandList();
}
