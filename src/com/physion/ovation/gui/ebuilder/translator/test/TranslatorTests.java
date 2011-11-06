/**
 * Copyright (c) 2011. Physion Consulting LLC
 * All rights reserved.
 */
package com.physion.ovation.gui.ebuilder.translator.test;

import java.util.Date;

import junit.framework.TestCase;
import org.approvaltests.Approvals;
import org.approvaltests.UseReporter;
import org.approvaltests.reporters.QuietReporter;
import org.approvaltests.reporters.JunitReporter;


import com.physion.ovation.gui.ebuilder.datamodel.DataModel;
import com.physion.ovation.gui.ebuilder.datamodel.RowData;
import com.physion.ovation.gui.ebuilder.datatypes.Attribute;
import com.physion.ovation.gui.ebuilder.datatypes.ClassDescription;
import com.physion.ovation.gui.ebuilder.datatypes.CollectionOperator;
import com.physion.ovation.gui.ebuilder.datatypes.Operator;
import com.physion.ovation.gui.ebuilder.datatypes.Type;
import com.physion.ovation.gui.ebuilder.expression.ExpressionTree;
import com.physion.ovation.gui.ebuilder.translator.RowDataToExpressionTree;
import com.physion.ovation.gui.ebuilder.translator.ExpressionTreeToRowData;


/**
 * Tests for the Translator subclasses.
 */
public class TranslatorTests
    extends TestCase {

    private static ClassDescription epochCD =
        DataModel.getClassDescription("Epoch");
    private static ClassDescription epochGroupCD =
        DataModel.getClassDescription("EpochGroup");
    private static ClassDescription sourceCD =
        DataModel.getClassDescription("Source");
    private static ClassDescription responseCD =
        DataModel.getClassDescription("Response");
    private static ClassDescription resourceCD =
        DataModel.getClassDescription("Resource");
    private static ClassDescription derivedResponseCD =
        DataModel.getClassDescription("DerivedResponse");
    private static ClassDescription externalDeviceCD = 
        DataModel.getClassDescription("ExternalDevice");
    

    //@UseReporter(QuietReporter.class)
    @UseReporter(JunitReporter.class)
    public void test1()
        throws Exception {

        RowData rootRow;
        RowData rowData;

        /**
         * Test the Any collection operator, (which becomes "or"),
         * and the String type and a couple attribute operators.
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ANY);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("protocolID"));
        rowData.setAttributeOperator(Operator.EQUALS);
        rowData.setAttributeValue("abc");
        rootRow.addChildRow(rowData);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("protocolID"));
        rowData.setAttributeOperator(Operator.MATCHES_CASE_INSENSITIVE);
        rowData.setAttributeValue("xyz");
        rootRow.addChildRow(rowData);

        String s = getResultsString(
            "CollectionOperator.ANY With Two Operands", rootRow);
        Approvals.approve(s);
    }


    /**
     * Test the translation of a RowData expression tree to
     * an ExpressionTree, AND test the translation of an
     * ExpressionTree to a RowData expression tree.
     *
     * You pass either a RowData root row object or
     * pass an ExpressionTree object.  The code will translate
     * from one class to the other and then back again, checking
     * that the results match.  If they don't match, an error
     * message is returned.
     *
     * @param someKindOfTree Pass either a RowData root row object
     * or pass an ExpressionTree object.
     */
    private static String testTranslation(Object someKindOfTree) {

        String s = "";

        RowData rootRow = null;
        ExpressionTree eTree = null;

        if (someKindOfTree instanceof RowData)
            rootRow = (RowData)someKindOfTree;
        else if (someKindOfTree instanceof ExpressionTree)
            eTree = (ExpressionTree)someKindOfTree;
        else {
            String error = "You must pass an Object of type RowData or "+
                "ExpressionTree.  You passed: "+someKindOfTree;
            throw(new IllegalArgumentException(error));
        }

        boolean same;
        if (rootRow != null) {
            s += "\nStarting With RowData:\n"+rootRow;
            eTree = RowDataToExpressionTree.translate(rootRow);
            s += "\nRowData Translated To Expression:\n"+eTree;
            RowData newRowData = ExpressionTreeToRowData.translate(eTree);
            s += "\nExpressionTree Translated Back To RowData:\n"+newRowData;

            same = rootRow.toString(true, "").equals(
                newRowData.toString(true, ""));
        }
        else {
            s += "\nStarting With ExpressionTree:\n"+eTree;
            rootRow = ExpressionTreeToRowData.translate(eTree);
            s += "\nExpressionTree Translated To RowData:\n"+rootRow;
            ExpressionTree newETree = RowDataToExpressionTree.
                translate(rootRow);
            s += "\nRowData Translated Back To Expression:\n"+newETree;

            same = eTree.toString().equals(newETree.toString());
        }

        if (same)
            s += "\nOriginal and translated versions are the same.";
        else
            s += "\nERROR:  Original and translated versions are different.";

        return(s);
    }


    private static String getResultsString(String label, RowData rootRow) {

        String s = "";

        s += "===== "+label+" =====";

        s += "\nOriginal RowData:\n"+rootRow;

        s += "\nTest Translation: ";
        s += testTranslation(rootRow);

        s += "\nTest RowData Serialization: ";
        String origRowData = rootRow.toString(true, "");
        rootRow.writeRowData("temp.rowData");
        rootRow = RowData.readRowData("temp.rowData");
        boolean same = origRowData.equals(rootRow.toString(true, ""));
        if (same)
            s += "RowData de/serialization succeed.";
        else 
            s += "RowData de/serialization failed.";

        s += "\nTest ExpressionTree Serialization: ";
        ExpressionTree eTree = RowDataToExpressionTree.translate(rootRow);
        String origETree = eTree.toString();
        eTree.writeExpressionTree("temp.expTree");
        eTree = ExpressionTree.readExpressionTree("temp.expTree");
        same = origETree.equals(eTree.toString());
        if (same)
            s += "ExpressionTree de/serialization succeed.";
        else 
            s += "ExpressionTree de/serialization failed.";

        return(s);
    }


    /**
     * This starts the testing.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(TranslatorTests.class);

    }
}
