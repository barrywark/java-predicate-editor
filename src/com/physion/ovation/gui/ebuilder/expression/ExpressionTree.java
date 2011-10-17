package com.physion.ovation.gui.ebuilder.expression;


import com.physion.ovation.gui.ebuilder.datatypes.ClassDescription;
import com.physion.ovation.gui.ebuilder.datamodel.DataModel;

/**
 * This class represents an "entire" expression tree.
 * I.e. it tells you the Class Under Qualification as
 * well as having a reference to the "root" Expression.
 */
public class ExpressionTree {

    private ClassDescription classUnderQualification;
    private Expression rootExpression;


    /**
     * Not public.
     */
    ExpressionTree(ClassDescription classUnderQualification,
                   Expression rootExpression) {
        this.classUnderQualification = classUnderQualification;
        this.rootExpression = rootExpression;
    }


    /**
     * Create a default ExpressionTree.
     * TODO:  Are the below values what we want as a default?
     */
    public ExpressionTree() {
        classUnderQualification = DataModel.getClassDescription("Epoch");
        rootExpression = new OperatorExpression(ExpressionTranslator.OE_OR);
    }


    public ClassDescription getClassUnderQualification() {
        return(classUnderQualification);
    }


    public IExpression getRootExpression() {
        return(rootExpression);
    }


    /**
     * Get a string version of this class that can be used
     * for debugging purposes.
     */
    public String toString() {

        String string = "CUQ: "+classUnderQualification;
        string += "\nrootExpression:\n"+rootExpression.toString();
        return(string);
    }
}

