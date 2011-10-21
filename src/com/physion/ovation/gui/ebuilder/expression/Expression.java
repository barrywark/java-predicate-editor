package com.physion.ovation.gui.ebuilder.expression;

import java.io.Serializable;


/**
 */
public class Expression
    implements IExpression, Serializable {

    /**
     * Required by Expression interface, but not needed for
     * current work.
     */
    @Override
    public void accept(IExpressionVisitor expressionVisitor) {
    }

    /**
     * Get a string version of this class that can be used
     * for debugging purposes.
     *
     * @param indent The lines of the returned string will
     * all be indented by (at least) this amount.
     */
    public String toString(String indent) {
        return("ERROR: You need to override this.");
    }
}

