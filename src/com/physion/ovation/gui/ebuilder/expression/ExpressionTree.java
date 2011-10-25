package com.physion.ovation.gui.ebuilder.expression;

import java.lang.ClassNotFoundException;
import java.io.InvalidClassException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.io.IOException;

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
     * This constructor is NOT public because no other classes
     * will interface with the ExpressionTree class like this.
     */
    ExpressionTree(String classUnderQualification,
                   Expression rootExpression) {
        this.classUnderQualification = classUnderQualification;
        this.rootExpression = rootExpression;
    }


    /**
     * Create a default ExpressionTree.
     * As of October 2011, by default we set the classUnderQualification
     * to the first class in the list of "possible" CUQs in the DataModel.
     * We default the root collection operator to be the "or" operator.
     * Note, in the GUI, the "or" operator is displayed as "Any".
     */
    public ExpressionTree() {

        /**
         * Default to whatever the first class is in the list of
         * possible CUQs.
         */
        classUnderQualification = DataModel.getPossibleCUQs().get(0).getName();

        /**
         * Default to the "or", (which is displayed as "Any" in the GUI),
         * operator.
         */
        rootExpression = new OperatorExpression(ExpressionTranslator.OE_OR);
    }


    /**
     * Get the Class Under Qualification for the root of the tree.
     */
    public ClassDescription getClassUnderQualification() {
        return(DataModel.getClassDescription(classUnderQualification));
    }


    /**
     * Get the root operator, (e.g. Any, All, None), at the top of the
     * ExpressionTree.  Please note, the GUI displays Any/All/None, but
     * these are actually IOperatorExpression objects with values like:
     * "or", "and", "not(or)".
     */
    public IExpression getRootExpression() {
        return(rootExpression);
    }


    /**
     * Write out this ExpressionTree to the passed in outputStream.
     *
     * @param outputStream The ObjectOutputStream into which this
     * ExpressionTree will be written.
     *
     * @throws IOException This will be whatever exception was generated
     * by the call to outputStream.writeObject().
     */
    public void writeExpressionTree(ObjectOutputStream outputStream)
        throws IOException {
        outputStream.writeObject(this);
    }


    /**
     * Write out this ExpressionTree to the passed in file.
     * This method is only being used for development/testing
     * purposes currently.  Physion developers probably want
     * to use the version of this method that takes an
     * ObjectOutputStream.
     *
     * @param fileName The name of the file into which this
     * ExpressionTree will be written.
     */
    public void writeExpressionTree(String fileName) {

        try {
            FileOutputStream outputFile = new FileOutputStream(fileName);
            ObjectOutputStream outputStream = new ObjectOutputStream(
                outputFile);
            writeExpressionTree(outputStream);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Read an ExpressionTree object from the passed in inputStream.
     *
     * TODO:  How should errors be handled?  Hide them and just
     * return null, or throw them up to the caller?
     *
     * @param inputStream The ObjectInputStream from which an
     * ExpressionTree will be read.
     *
     * @param The read in ExpressionTree.  Returns null if there
     * was an error.
     *
     * @throws IOException This will be whatever exception was generated
     * by the call to inputStream.readObject().
     *
     * @throws ClassNotFoundException This will be thrown if the
     * call to inputStream.readObject() did not know what class
     * was read in.  This should never happen.  If it does, the
     * file that was read in is probably corrupted or of an incorrect
     * version if you see an InvalidClassException.
     */
    public static ExpressionTree readExpressionTree(ObjectInputStream
        inputStream)
        throws IOException, ClassNotFoundException {

        Object obj = null;
        try {
            obj = inputStream.readObject();
        }
        catch (InvalidClassException e) {

            String s = "The object that was read in was an ExpressionTree "+
                "object, but it, or one of the classes it contains, "+
                "might be of the wrong version.  For example, you are "+
                "using an input file that was written using an old "+
                "version of one of the classes.  If so, delete the old "+
                "input file.";
            throw(new IOException(s, e));
        }

        if (obj instanceof ExpressionTree) {
            return((ExpressionTree)obj);
        }
        else {
            /**
             * The object we read in was not an ExpressionTree.
             * This should never happen.
             */
            String s = "The object that was read in was not an "+
                "ExpressionTree object but it was some sort of "+
                "object that we know about: "+obj.getClass().getName()+".  "+
                "There is some sort of bug/inconsistency in the code.";
            throw(new IOException(s));
        }
    }


    /**
     * Read an ExpressionTree object from the passed in file.
     * This method is only being used for development/testing
     * purposes currently.  Physion developers probably want
     * to use the version of this method that takes an
     * ObjectInputStream.
     *
     * @param fileName The name of the file from which an
     * ExpressionTree will be read.
     *
     * @param The read in ExpressionTree.  Returns null if there
     * was an error.
     */
    public static ExpressionTree readExpressionTree(String fileName) {

        Object obj = null;

        try {
            FileInputStream inputFile = new FileInputStream(fileName);
            ObjectInputStream inputStream = new ObjectInputStream(inputFile);
            return(readExpressionTree(inputStream));
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

