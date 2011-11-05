/**
 * Copyright (c) 2011. Physion Consulting LLC
 * All rights reserved.
 */
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


/**
 * This class represents an "entire" expression tree.
 * I.e. it tells you the Class Under Qualification as
 * well as having a reference to the "root" Expression.
 *
 * An ExpressionTree object contains a rootExpression
 * member data that is a reference to a class that
 * implements the IExpression interface.
 * (As of October 2011, the only class that does that is
 * the Expression class.)
 *
 * An IExpression, and all of the interfaces that extend
 * it, are used to represent a tree that can be easily
 * parsed and turned into a PQL expression.
 *
 * The GUI translates an ExpressionTree into a RowData
 * structure.  The RowData structure "looks" like what
 * the user sees in the GUI.
 *
 * @see ExpressionTreeToRowData
 * @see RowDataToExpressionTree
 */
public class ExpressionTree
    implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Just for testing.
     */
    public static final String SAVE_FILE_NAME = "testSaved.ExpTree";

    /**
     * This is a the name of the Class Under Qualification for the
     * root of the tree.  This is a String, not a ClassDescription
     * object.  E.g. "Epoch", "EpochGroup", "Source".
     */
    private String classUnderQualification;

    /**
     * This is the root of the Expression tree.  It will be
     * a collection operator such as: OperatorExpression(or),
     * OperatorExpression(and), or OperatorExpression(not).
     */
    private IOperatorExpression rootExpression;


    /**
     * Create an ExpressionTree with the passed in values.
     *
     * @param classUnderQualification The name of the class that
     * is the "root" class of the tree.
     *
     * @param rootExpression This is the root of the Expression tree.
     * It will be a collection operator such as: IOperatorExpression(or),
     * IOperatorExpression(and), or IOperatorExpression(not).
     */
    public ExpressionTree(String classUnderQualification,
                          IOperatorExpression rootExpression) {

        this.classUnderQualification = classUnderQualification;
        this.rootExpression = rootExpression;
    }


    /**
     * Get the name of the Class Under Qualification for the
     * root of the tree.
     *
     * Please note, if you want to turn the returned String
     * into a ClassDescription object, you will need to use
     * the DataModel class and do something like this:
     *
     *      String className = someExpressionTree.getClassUnderQualification();
     *      ClassDescription cd;
     *      cd = DataModel.getClassDescription(className);
     * 
     */
    public String getClassUnderQualification() {
        return(classUnderQualification);
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
            System.err.println("\nInput File: \""+fileName+"\"\n");
            e.printStackTrace();
        }

        return(null);
    }


    /**
     * Test the serialization of this object by writing it out
     * and reading it in and seeing if the read in value is the
     * same as the written out value.
     *
     * @return Returns true if the value that we read in
     * matched the original value that we wrote out.
     */
    public boolean testSerialization() {

        writeExpressionTree(SAVE_FILE_NAME);
        ExpressionTree expressionTree = readExpressionTree(SAVE_FILE_NAME);
        boolean same = expressionTree.toString().equals(this.toString());

        if (same)
            System.out.println("ExpressionTree written and read versions are "+
                               "the same.");
        else
            System.out.println("ERROR:  ExpressionTree written and read "+
                               "versions are different.");

        return(same);
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

