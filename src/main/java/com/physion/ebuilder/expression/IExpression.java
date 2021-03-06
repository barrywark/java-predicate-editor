/**
 * Copyright (c) 2011. Physion Consulting LLC
 * All rights reserved.
 */
package com.physion.ebuilder.expression;


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
public interface IExpression {

    /**
     * This method is not implemented or used in the current,
     * November 2011, version of the code.  It is declared only
     * because the C++ version of the library has this method.
     * It will eventually end up getting used if Objectivity
     * does not create a Java version of the library for
     * Physion to use.
     */
    public void accept(IExpressionVisitor expressionVisitor);
}
