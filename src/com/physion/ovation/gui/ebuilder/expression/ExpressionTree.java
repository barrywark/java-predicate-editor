package com.physion.ovation.gui.ebuilder.expression;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;

import com.physion.ovation.gui.ebuilder.datatypes.ClassDescription;
import com.physion.ovation.gui.ebuilder.datamodel.DataModel;


/**
 * This class represents an "entire" expression tree.
 * I.e. it tells you the Class Under Qualification as
 * well as having a reference to the "root" Expression.
 */
public class ExpressionTree
    implements Serializable {

    //private ClassDescription classUnderQualification;
    private String classUnderQualification;
    private Expression rootExpression;


    /**
     * Not public because no one will interface with
     * the ExpressionTree class like this.
     */
    ExpressionTree(String classUnderQualification,
                   Expression rootExpression) {
        this.classUnderQualification = classUnderQualification;
        this.rootExpression = rootExpression;
    }


    /**
     * Create a default ExpressionTree.
     * TODO:  Are the below values what we want as a default?
     */
    public ExpressionTree() {
        //classUnderQualification = DataModel.getClassDescription("Epoch");
        classUnderQualification = DataModel.getPossibleCUQs().get(0).getName();
        rootExpression = new OperatorExpression(ExpressionTranslator.OE_OR);
    }


    public ClassDescription getClassUnderQualification() {
        return(DataModel.getClassDescription(classUnderQualification));
    }


    public IExpression getRootExpression() {
        return(rootExpression);
    }


    /**
     * Write out this ExpressionTree to the passed in file.
     *
     * TODO:  Would you rather hand me an OutputStream?
     * How should errors be handled?
     * Currently I am hiding them while I do development,
     * but a final version should probably throw the
     * Exception up to the caller.
     *
     * @param fileName The name of the file into which this
     * ExpressionTree will be written.
     */
    public void writeExpressionTree(String fileName) {

        try {
            FileOutputStream outputFile = new FileOutputStream(fileName);
            ObjectOutputStream outputStream = new ObjectOutputStream(
                outputFile);
            outputStream.writeObject(this);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Read an ExpressionTree object from the passed in file.
     *
     * TODO:  Would you rather hand me an OutputStream?
     * How should errors be handled?
     * Currently I am hiding them while I do development,
     * but a final version should probably throw the
     * Exception up to the caller.
     *
     * @param fileName The name of the file from which an
     * ExpressionTree will be read.  Returns null if there
     * was an error.
     */
    public static ExpressionTree readExpressionTree(String fileName) {

        Object obj = null;

        try {
            FileInputStream inputFile = new FileInputStream(fileName);
            ObjectInputStream inputStream = new ObjectInputStream(inputFile);
            obj = inputStream.readObject();

            if (obj instanceof ExpressionTree) {
                return((ExpressionTree)obj);
            }
        }
        catch (FileNotFoundException e) {
            /**
             * Ignore it.  The first time we are run, no output
             * file will have been written.
             */
            System.err.println("File not found: "+fileName);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return(null);
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

