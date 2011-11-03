package com.physion.ovation.gui.ebuilder.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import com.physion.ovation.gui.ebuilder.ExpressionBuilder;
import com.physion.ovation.gui.ebuilder.datatypes.CollectionOperator;
import com.physion.ovation.gui.ebuilder.datatypes.ClassDescription;
import com.physion.ovation.gui.ebuilder.datatypes.Attribute;
import com.physion.ovation.gui.ebuilder.datatypes.Cardinality;
import com.physion.ovation.gui.ebuilder.datatypes.Type;
import com.physion.ovation.gui.ebuilder.datatypes.Operator;
import com.physion.ovation.gui.ebuilder.datamodel.RowData;
import com.physion.ovation.gui.ebuilder.datamodel.DataModel;


/**
 * Base class for the ExpressionTreeToRowData and RowDataToExpressionTree
 * translators.
 */
public class Translator {

    public static final String CLVE_BOOLEAN = "ovation.BooleanValue";
    public static final String CLVE_STRING = "ovation.StringValue";
    public static final String CLVE_INTEGER = "ovation.IntegerValue";
    public static final String CLVE_FLOAT = "ovation.FloatingPointValue";
    public static final String CLVE_DATE = "ovation.DateValue";

    public static final String AE_VALUE = "value";
    public static final String AE_THIS = "this";

    public static final String OE_NOT = "not";
    public static final String OE_OR = "or";
    public static final String OE_AND = "and";
    public static final String OE_ALL = "all";
    public static final String OE_COUNT = "count";
    public static final String OE_AS = "as";
    public static final String OE_PARAMETER = "parameter";
    public static final String OE_ANY = "any";
    public static final String OE_ELEMENTS_OF_TYPE = "elementsOfType";

    public static final String OE_EQUALS = "==";
    public static final String OE_NOT_EQUALS = "!=";
    public static final String OE_LESS_THAN = "<";
    public static final String OE_GREATER_THAN = ">";
    public static final String OE_LESS_THAN_EQUALS = "<=";
    public static final String OE_GREATER_THAN_EQUALS = ">=";
    public static final String OE_MATCHES_CASE_SENSITIVE = "=~";
    public static final String OE_MATCHES_CASE_INSENSITIVE = "=~~";
    public static final String OE_DOES_NOT_MATCH_CASE_SENSITIVE = "!~";
    public static final String OE_DOES_NOT_MATCH_CASE_INSENSITIVE = "!~~";

    public static final String OE_IS_NULL = "isnull";
    // Note there is no OE_IS_NOT_NULL value.
    public static final String OE_DOT = ".";


    /**
     * A long list of tests.
     */
    public static void runAllTests() {

        RowData rootRow;
        RowData rowData;
        RowData rowData2;
        RowData rowData3;
        RowData rowData4;
        ExpressionTree expression;

        ClassDescription epochCD = DataModel.getClassDescription("Epoch");
        ClassDescription epochGroupCD = DataModel.getClassDescription(
            "EpochGroup");
        ClassDescription sourceCD = DataModel.getClassDescription("Source");
        ClassDescription responseCD = DataModel.getClassDescription("Response");
        ClassDescription resourceCD = DataModel.getClassDescription("Resource");
        ClassDescription derivedResponseCD =
            DataModel.getClassDescription("DerivedResponse");
        ClassDescription externalDeviceCD = 
            DataModel.getClassDescription("ExternalDevice");

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
        
        printResults("CollectionOperator.ANY With Two Operands", rootRow);

        /**
         * Test the All collection operator, (which becomes "and"),
         * and the Boolean type.
         *
         *      incomplete is true
         *
         * Note, the GUI does not accept:
         *
         *      incomplete == true
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("incomplete"));
        /**
         * This is the wrong way to handle booleans in the GUI.
         */
        //rowData.setAttributeOperator(Operator.EQUALS);
        //rowData.setAttributeValue(new Boolean(true));
        /**
         * This is the correct way.
         */
        rowData.setAttributeOperator(Operator.IS_TRUE);

        rootRow.addChildRow(rowData);

        printResults("Attribute Boolean Operator.IS_TRUE", rootRow);

        /**
         * Test an attribute path with two levels.
         * Also test using None collection operator which
         * gets turned into two operators:  "not" with
         * "or" as its only operand.
         *
         *      Epoch None
         *          epochGroup.label == "Test 27"
         *
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.NONE);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("epochGroup"));
        rowData.addAttribute(epochGroupCD.getAttribute("label"));
        rowData.setAttributeOperator(Operator.EQUALS);
        rowData.setAttributeValue("Test 27");
        rootRow.addChildRow(rowData);

        printResults("Attribute Path Nested Twice", rootRow);

        /**
         * Test an attribute path with three levels:
         *
         *      epochGroup.source.label == "Test 27"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.NONE);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("epochGroup"));
        rowData.addAttribute(epochGroupCD.getAttribute("source"));
        rowData.addAttribute(sourceCD.getAttribute("label"));
        rowData.setAttributeOperator(Operator.EQUALS);
        rowData.setAttributeValue("Test 27");
        rootRow.addChildRow(rowData);

        printResults("Attribute Path Nested Thrice", rootRow);

        /**
         * Test a reference value for null.
         *
         *      Epoch | All
         *        Epoch | owner is null
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("owner"));
        rowData.addAttribute(Attribute.IS_NULL);  // optional call
        rowData.setAttributeOperator(Operator.IS_NULL);
        rootRow.addChildRow(rowData);

        printResults("Reference Value Operator.IS_NULL", rootRow);

        /**
         * Test a reference value for not null.
         *
         *      Epoch | All
         *        Epoch | owner is not null
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("owner"));
        rowData.addAttribute(Attribute.IS_NOT_NULL);  // optional call
        rowData.setAttributeOperator(Operator.IS_NOT_NULL);
        rootRow.addChildRow(rowData);

        printResults("Reference Value Operator.IS_NOT_NULL", rootRow);

        /**
         * Test a compound row:
         *
         *      Epoch | All
         *        Epoch | responses All have Any
         *          Response | uuid == "xyz"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("responses"));
        rowData.setCollectionOperator(CollectionOperator.ALL);
        rowData.setCollectionOperator2(CollectionOperator.ANY);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(responseCD.getAttribute("uuid"));
        rowData2.setAttributeOperator(Operator.EQUALS);
        rowData2.setAttributeValue("xyz");
        rowData.addChildRow(rowData2);

        printResults("Compound Row", rootRow);

        /**
         * Test a compound row with lots of None collection operators:
         *
         *      Epoch | None
         *        Epoch | responses None have None
         *          Response | uuid == "xyz"
         *          Response | samplingRate != 1.23
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.NONE);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("responses"));
        rowData.setCollectionOperator(CollectionOperator.NONE);
        rowData.setCollectionOperator2(CollectionOperator.NONE);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(responseCD.getAttribute("uuid"));
        rowData2.setAttributeOperator(Operator.EQUALS);
        rowData2.setAttributeValue("xyz");
        rowData.addChildRow(rowData2);

        rowData2 = new RowData();
        rowData2.addAttribute(responseCD.getAttribute("samplingRate"));
        rowData2.setAttributeOperator(Operator.NOT_EQUALS);
        rowData2.setAttributeValue(new Double(1.23));
        rowData.addChildRow(rowData2);

        printResults("Compound Row With Lots Of None Collection Operators",
                     rootRow);

        /**
         * Test a compound row with lots of None collection operators:
         *
         *      Epoch | None
         *        Epoch | responses None have None
         *          Response | uuid == "xyz"
         *          Response | samplingRate != 1.23
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.NONE);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("prevEpoch"));
        rowData.addAttribute(epochCD.getAttribute("responses"));
        rowData.setCollectionOperator(CollectionOperator.NONE);
        rowData.setCollectionOperator2(CollectionOperator.NONE);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(responseCD.getAttribute("uuid"));
        rowData2.setAttributeOperator(Operator.EQUALS);
        rowData2.setAttributeValue("xyz");
        rowData.addChildRow(rowData2);

        rowData2 = new RowData();
        rowData2.addAttribute(responseCD.getAttribute("samplingRate"));
        rowData2.setAttributeOperator(Operator.NOT_EQUALS);
        rowData2.setAttributeValue(new Double(1.23));
        rowData.addChildRow(rowData2);

        printResults("Same As Above, But Nested", rootRow);

        /**
         * Test a compound row:
         *
         *      Epoch | All
         *        Epoch | responses All have Any
         *          Response | resources Any have Any
         *            Epoch | protocolID != "Test 27"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("responses"));
        rowData.setCollectionOperator(CollectionOperator.ALL);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(responseCD.getAttribute("resources"));
        rowData2.setCollectionOperator(CollectionOperator.ANY);
        rowData.addChildRow(rowData2);

        rowData3 = new RowData();
        rowData3.addAttribute(resourceCD.getAttribute("uuid"));
        rowData3.setAttributeOperator(Operator.NOT_EQUALS);
        rowData3.setAttributeValue("ID 27");
        rowData2.addChildRow(rowData3);

        printResults("Compound Row Nested Classes", rootRow);

        /**
         * Test a compound row that uses the Count collection operator:
         *
         *      Epoch | All
         *        Epoch | responses All have Any
         *          Response | resources Count <= 5
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("responses"));
        rowData.setCollectionOperator(CollectionOperator.ALL);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(responseCD.getAttribute("resources"));
        rowData2.setCollectionOperator(CollectionOperator.COUNT);
        rowData2.setAttributeOperator(Operator.LESS_THAN_EQUALS);
        rowData2.setAttributeValue(new Integer(5));
        rowData.addChildRow(rowData2);

        printResults("Compound Row ALL COUNT", rootRow);

        /**
         * Test a compound row:
         *
         *      Epoch | Any
         *        Epoch | responses Any have Any
         *          Response | uuid == "xyz"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ANY);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("responses"));
        rowData.setCollectionOperator(CollectionOperator.ANY);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(responseCD.getAttribute("uuid"));
        rowData2.setAttributeOperator(Operator.EQUALS);
        rowData2.setAttributeValue("xyz");
        rowData.addChildRow(rowData2);

        printResults("Compound Row ANY ANY", rootRow);

        /**
         * Test a compound row:
         *
         *      Epoch | None
         *        Epoch | responses None have Any
         *          Response | uuid == "xyz"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.NONE);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("responses"));
        rowData.setCollectionOperator(CollectionOperator.NONE);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(responseCD.getAttribute("uuid"));
        rowData2.setAttributeOperator(Operator.EQUALS);
        rowData2.setAttributeValue("xyz");
        rowData.addChildRow(rowData2);

        printResults("Compound Row NONE NONE", rootRow);

        /**
         * Test a PER_USER attribute type.
         *
         *      Epoch | All
         *        Epoch | My keywords None have Any
         *          KeywordTag | uuid == "xyz"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("mykeywords"));
        rowData.setCollectionOperator(CollectionOperator.NONE);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(epochCD.getAttribute("uuid"));
        rowData2.setAttributeOperator(Operator.EQUALS);
        rowData2.setAttributeValue("xyz");
        rowData.addChildRow(rowData2);

        printResults("PER_USER", rootRow);

        /**
         * Test a PER_USER attribute type with Count.
         *
         *      Epoch | All
         *        Epoch | My keywords Count == 5
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("mykeywords"));
        rowData.setCollectionOperator(CollectionOperator.COUNT);
        rowData.setAttributeOperator(Operator.EQUALS);
        rowData.setAttributeValue(new Integer(5));
        rootRow.addChildRow(rowData);

        printResults("PER_USER CollectionOperator.COUNT", rootRow);

        /**
         * Test a nested PER_USER attribute type.
         *
         *      Epoch | Any
         *        Epoch | nextEpoch.All keywords Any have Any
         *          KeywordTag | uuid == "xyz"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ANY);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("keywords"));
        rowData.setCollectionOperator(CollectionOperator.ANY);
        rowData.setCollectionOperator2(CollectionOperator.ANY);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(epochCD.getAttribute("uuid"));
        rowData2.setAttributeOperator(Operator.EQUALS);
        rowData2.setAttributeValue("xyz");
        rowData.addChildRow(rowData2);

        printResults("PER_USER Nested Once CollectionOperator.ANY", rootRow);

        /**
         * Test a nested PER_USER attribute type.
         *
         *      Epoch | None
         *        Epoch | nextEpoch.All keywords None have All
         *          KeywordTag | uuid == "xyz"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.NONE);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("keywords"));
        rowData.setCollectionOperator(CollectionOperator.NONE);
        rowData.setCollectionOperator2(CollectionOperator.ALL);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(epochCD.getAttribute("uuid"));
        rowData2.setAttributeOperator(Operator.EQUALS);
        rowData2.setAttributeValue("xyz");
        rowData.addChildRow(rowData2);

        printResults("PER_USER Nested Twice CollectionOperator.NONE", rootRow);

        /**
         * Test a nested PER_USER attribute type.
         *
         *     Epoch | All
         *       Epoch | nextEpoch.nextEpoch.prevEpoch.All keywords All have Any
         *         KeywordTag | uuid == "xyz"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("prevEpoch"));
        rowData.addAttribute(epochCD.getAttribute("keywords"));
        rowData.setCollectionOperator(CollectionOperator.ALL);
        rowData.setCollectionOperator2(CollectionOperator.ANY);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(epochCD.getAttribute("uuid"));
        rowData2.setAttributeOperator(Operator.EQUALS);
        rowData2.setAttributeValue("xyz");
        rowData.addChildRow(rowData2);

        printResults("PER_USER Nested Thrice CollectionOperator.ALL", rootRow);

        /**
         * Test a PARAMETERS_MAP row of type time.
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("protocolParameters"));
        rowData.setPropName("someTimeKey");
        rowData.setPropType(Type.DATE_TIME);
        rowData.setAttributeOperator(Operator.EQUALS);
        rowData.setAttributeValue(new Date(1262304000000L));
        rootRow.addChildRow(rowData);

        printResults("PARAMETERS_MAP Type Date", rootRow);

        /**
         * Test a PARAMETERS_MAP row of type float, that
         * has an attributePath that is more than one level deep.
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("prevEpoch"));
        rowData.addAttribute(epochCD.getAttribute("protocolParameters"));
        rowData.setPropName("someKey");
        rowData.setPropType(Type.FLOAT_64);
        rowData.setAttributeOperator(Operator.EQUALS);
        rowData.setAttributeValue(new Double(12.3));
        rootRow.addChildRow(rowData);

        printResults("PARAMETERS_MAP Nested", rootRow);

        /**
         * Test a PER_USER_PARAMETERS_MAP row.
         *
         *      Any properties.someKey(int) != "34"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("properties"));
        rowData.setPropName("someKey");
        rowData.setPropType(Type.INT_32);
        rowData.setAttributeOperator(Operator.NOT_EQUALS);
        rowData.setAttributeValue(new Integer(34));
        rootRow.addChildRow(rowData);

        printResults("PER_USER_PARAMETERS_MAP", rootRow);

        /**
         * Test a PER_USER_PARAMETERS_MAP row.
         *
         * nextEpoch.nextEpoch.prevEpoch.Any properties.someKey(int) != "34"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ANY);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("prevEpoch"));
        rowData.addAttribute(epochCD.getAttribute("properties"));
        rowData.setPropName("someKey");
        rowData.setPropType(Type.INT_32);
        rowData.setAttributeOperator(Operator.NOT_EQUALS);
        rowData.setAttributeValue(new Integer(34));
        rootRow.addChildRow(rowData);

        printResults("PER_USER_PARAMETERS_MAP Nested", rootRow);

        /**
         * Test handling a reference.
         *
         *  nextEpoch.nextEpoch is null
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.setAttributeOperator(Operator.IS_NULL);
        rootRow.addChildRow(rowData);

        printResults("Reference isnull()", rootRow);

        /**
         * Test compound row.
         *
         *      All of the following
         *        None of the following
         *          protocolID =~~ "xyz"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.setCollectionOperator(CollectionOperator.NONE);
        rootRow.addChildRow(rowData);

        rowData4 = new RowData();
        rowData4.addAttribute(epochCD.getAttribute("protocolID"));
        rowData4.setAttributeOperator(Operator.MATCHES_CASE_INSENSITIVE);
        rowData4.setAttributeValue("xyz");
        rowData.addChildRow(rowData4);

        printResults("Compound Operators Nested", rootRow);

        /**
         * Test compound row.
         *
         *      Any of the following
         *        All of the following
         *          None of the following
         *            protocolID =~~ "xyz"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.setCollectionOperator(CollectionOperator.ANY);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.setCollectionOperator(CollectionOperator.ALL);
        rowData.addChildRow(rowData2);

        rowData3 = new RowData();
        rowData3.setCollectionOperator(CollectionOperator.NONE);
        rowData2.addChildRow(rowData3);

        rowData4 = new RowData();
        rowData4.addAttribute(epochCD.getAttribute("protocolID"));
        rowData4.setAttributeOperator(Operator.MATCHES_CASE_INSENSITIVE);
        rowData4.setAttributeValue("xyz");
        rowData3.addChildRow(rowData4);

        printResults("Compound Operators In Different Positions", rootRow);

        /**
         * Test compound row.
         *
         *      All of the following
         *        All of the following
         *          Any of the following
         *            None of the following
         *              protocolID =~~ "xyz"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.setCollectionOperator(CollectionOperator.ALL);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.setCollectionOperator(CollectionOperator.ANY);
        rowData.addChildRow(rowData2);

        rowData3 = new RowData();
        rowData3.setCollectionOperator(CollectionOperator.NONE);
        rowData2.addChildRow(rowData3);

        rowData4 = new RowData();
        rowData4.addAttribute(epochCD.getAttribute("protocolID"));
        rowData4.setAttributeOperator(Operator.MATCHES_CASE_INSENSITIVE);
        rowData4.setAttributeValue("xyz");
        rowData3.addChildRow(rowData4);

        printResults("Compound Operators In Different Positions", rootRow);

        /**
         * Test compound row.
         *
         *      Any of the following
         *        Any of the following
         *          All of the following
         *            None of the following
         *              protocolID =~~ "xyz"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ANY);

        rowData = new RowData();
        rowData.setCollectionOperator(CollectionOperator.ANY);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.setCollectionOperator(CollectionOperator.ALL);
        rowData.addChildRow(rowData2);

        rowData3 = new RowData();
        rowData3.setCollectionOperator(CollectionOperator.NONE);
        rowData2.addChildRow(rowData3);

        rowData4 = new RowData();
        rowData4.addAttribute(epochCD.getAttribute("protocolID"));
        rowData4.setAttributeOperator(Operator.MATCHES_CASE_INSENSITIVE);
        rowData4.setAttributeValue("xyz");
        rowData3.addChildRow(rowData4);

        printResults("Compound Operators In Different Positions", rootRow);

        /**
         * Test compound row.
         *
         *      None of the following
         *        None of the following
         *          All of the following
         *            Any of the following
         *              protocolID =~~ "xyz"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.NONE);

        rowData = new RowData();
        rowData.setCollectionOperator(CollectionOperator.NONE);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.setCollectionOperator(CollectionOperator.ALL);
        rowData.addChildRow(rowData2);

        rowData3 = new RowData();
        rowData3.setCollectionOperator(CollectionOperator.ANY);
        rowData2.addChildRow(rowData3);

        rowData4 = new RowData();
        rowData4.addAttribute(epochCD.getAttribute("protocolID"));
        rowData4.setAttributeOperator(Operator.MATCHES_CASE_INSENSITIVE);
        rowData4.setAttributeValue("xyz");
        rowData3.addChildRow(rowData4);

        printResults("Compound Operators In Different Positions", rootRow);

        /**
         * Test compound row with PARAMETERS_MAP and PER_USER child.
         *
         *  Modified rootRow:
         *  Epoch | Any
         *    Epoch | nextEpoch.My DerivedResponses None
         *      DerivedResponse | derivationParameters.somekey(boolean) is true
         *      DerivedResponse | My Keywords Count == "5"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ANY);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("myderivedResponses"));
        rowData.setCollectionOperator(CollectionOperator.NONE);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(derivedResponseCD.getAttribute(
                              "derivationParameters"));
        rowData2.setPropName("someKey");
        rowData2.setPropType(Type.BOOLEAN);
        rowData2.setAttributeOperator(Operator.IS_TRUE);
        rowData.addChildRow(rowData2);

        rowData2 = new RowData();
        rowData2.addAttribute(derivedResponseCD.getAttribute("mykeywords"));
        rowData2.setCollectionOperator(CollectionOperator.COUNT);
        rowData2.setAttributeValue(new Integer(5));
        rowData.addChildRow(rowData2);

        /**
         * Test compound row with PARAMETERS_MAP, PER_USER children.
         *
         *  Modified rootRow:
         *  Epoch | Any
         *    Epoch | nextEpoch.My DerivedResponses None
         *      DerivedResponse | derivationParameters.somekey(boolean) is true
         *      DerivedResponse | My Keywords Count == "5"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ANY);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("myderivedResponses"));
        rowData.setCollectionOperator(CollectionOperator.NONE);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(derivedResponseCD.getAttribute(
                              "derivationParameters"));
        rowData2.setPropName("someKey");
        rowData2.setPropType(Type.BOOLEAN);
        rowData2.setAttributeOperator(Operator.IS_TRUE);
        rowData.addChildRow(rowData2);

        rowData2 = new RowData();
        rowData2.addAttribute(derivedResponseCD.getAttribute("mykeywords"));
        rowData2.setCollectionOperator(CollectionOperator.COUNT);
        rowData2.setAttributeValue(new Integer(5));
        rowData.addChildRow(rowData2);

        printResults("Nested PER_USER With PARAMETERS_MAP & PER_USER Children",
                     rootRow);

        /**
         * Test compound row with PARAMETERS_MAP, PER_USER, and
         * PER_USER_PARAMETERS_MAP child.
         *
         *  Epoch | Any
         *    Epoch | nextEpoch.My DerivedResponses None
         *      DerivedResponse | derivationParameters.someKey(boolean) is true
         *      DerivedResponse | My Keywords Count == "5"
         *      DerivedResponse | externalDevice.My Property.someKey2(float) ==
         *                                                               "34.5"
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ANY);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("myderivedResponses"));
        rowData.setCollectionOperator(CollectionOperator.NONE);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(derivedResponseCD.getAttribute(
                              "derivationParameters"));
        rowData2.setPropName("someKey");
        rowData2.setPropType(Type.BOOLEAN);
        rowData2.setAttributeOperator(Operator.IS_TRUE);
        rowData.addChildRow(rowData2);

        rowData2 = new RowData();
        rowData2.addAttribute(derivedResponseCD.getAttribute("mykeywords"));
        rowData2.setCollectionOperator(CollectionOperator.COUNT);
        rowData2.setAttributeValue(new Integer(5));
        rowData.addChildRow(rowData2);

        rowData2 = new RowData();
        rowData2.addAttribute(derivedResponseCD.getAttribute("externalDevice"));
        rowData2.addAttribute(externalDeviceCD.getAttribute("myproperties"));
        rowData2.setPropName("someKey2");
        rowData2.setPropType(Type.FLOAT_64);
        rowData2.setAttributeOperator(Operator.EQUALS);
        rowData2.setAttributeValue(new Double(34.5));
        rowData.addChildRow(rowData2);

        printResults("Nested PER_USER With PM, PU, and PUPM Children",
                     rootRow);

        /**
         * Test compound row where class changes between parent and child.
         */
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.NONE);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("responses"));
        rowData.setCollectionOperator(CollectionOperator.NONE);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.setCollectionOperator(CollectionOperator.NONE);
        rowData.addChildRow(rowData2);

        rowData3 = new RowData();
        rowData3.addAttribute(responseCD.getAttribute("samplingRate"));
        rowData3.setAttributeOperator(Operator.GREATER_THAN_EQUALS);
        rowData3.setAttributeValue(new Double(55.2));
        rowData2.addChildRow(rowData3);

        printResults("Compound Row, Class Change Between Parent And Child",
                     rootRow);
    }


    /**
     * This is just a single test of what I am working on right now.
     */
    public static void runOneTest() {

        RowData rowData;
        RowData rowData2;
        RowData rootRow;
        ExpressionTree expression;

        ClassDescription epochCD = DataModel.getClassDescription("Epoch");
        ClassDescription epochGroupCD = DataModel.getClassDescription(
            "EpochGroup");
        ClassDescription sourceCD = DataModel.getClassDescription("Source");
        ClassDescription responseCD = DataModel.getClassDescription("Response");
        ClassDescription derivedResponseCD = DataModel.getClassDescription(
            "DerivedResponse");
        ClassDescription externalDeviceCD = DataModel.getClassDescription(
            "ExternalDevice");

/*
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        //rowData.addAttribute(epochCD.getAttribute("epochGroup"));
        //rowData.addAttribute(epochGroupCD.getAttribute("source"));
        //rowData.addAttribute(epochCD.getAttribute("owner"));
        rowData.addAttribute(epochCD.getAttribute("incomplete"));
        //rowData.addAttribute(Attribute.IS_NULL);
        //rowData.setAttributeOperator(Operator.IS_NULL);
        rowData.setAttributeOperator(Operator.IS_TRUE);
        //rowData.setAttributeOperator(Operator.EQUALS);
        //rowData.setAttributeValue("xyz");
        rootRow.addChildRow(rowData);
*/
        /*
        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("protocolID"));
        rowData.setAttributeOperator(Operator.NOT_EQUALS);
        rowData.setAttributeValue("abc");
        rootRow.addChildRow(rowData);
        */

/*
        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("epochGroup"));
        rowData.addAttribute(epochGroupCD.getAttribute("source"));
        rowData.setAttributeOperator(Operator.IS_NULL);
        rootRow.addChildRow(rowData);

        testTranslation(rootRow);
*/
/*
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.NONE);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("responses"));
        rowData.setCollectionOperator(CollectionOperator.ALL);
        rowData.setCollectionOperator2(CollectionOperator.ANY);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(responseCD.getAttribute("uuid"));
        rowData2.setAttributeOperator(Operator.EQUALS);
        rowData2.setAttributeValue("xyz");
        rowData.addChildRow(rowData2);

        rowData2 = new RowData();
        rowData2.addAttribute(responseCD.getAttribute("samplingRate"));
        rowData2.setAttributeOperator(Operator.NOT_EQUALS);
        rowData2.setAttributeValue(new Double(1.23));
        rowData.addChildRow(rowData2);

        testTranslation(rootRow);
*/
/*
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ANY);

        rowData = new RowData();
        //rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("keywords"));
        rowData.setCollectionOperator(CollectionOperator.ANY);
        rowData.setCollectionOperator2(CollectionOperator.ANY);
        rootRow.addChildRow(rowData);

        rowData2 = new RowData();
        rowData2.addAttribute(epochCD.getAttribute("uuid"));
        rowData2.setAttributeOperator(Operator.EQUALS);
        rowData2.setAttributeValue("xyz");
        rowData.addChildRow(rowData2);
*/
/*
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("protocolParameters"));
        rowData.setPropName("someTimeKey");
        rowData.setPropType(Type.DATE_TIME);
        rowData.setAttributeOperator(Operator.EQUALS);
        rowData.setAttributeValue(new Date(1262304000000L));
        rootRow.addChildRow(rowData);
*/
/*
        rootRow = new RowData();
        rootRow.setClassUnderQualification(epochCD);
        rootRow.setCollectionOperator(CollectionOperator.ALL);

        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("prevEpoch"));
        rowData.addAttribute(epochCD.getAttribute("properties"));
        rowData.setPropName("someKey");
        rowData.setPropType(Type.INT_32);
        rowData.setAttributeOperator(Operator.NOT_EQUALS);
        rowData.setAttributeValue(new Integer(34));
        rootRow.addChildRow(rowData);
*/
        rootRow = new RowData();
        rootRow.setClassUnderQualification(derivedResponseCD);
        rootRow.setCollectionOperator(CollectionOperator.ANY);

/*
        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("myderivedResponses"));
        rowData.setCollectionOperator(CollectionOperator.NONE);
        rootRow.addChildRow(rowData);
*/
        rowData2 = new RowData();
        rowData2.addAttribute(derivedResponseCD.getAttribute(
                              "derivationParameters"));
        rowData2.setPropName("someKey");
        rowData2.setPropType(Type.BOOLEAN);
        rowData2.setAttributeOperator(Operator.IS_TRUE);
        rootRow.addChildRow(rowData2);
/*
        rootRow = new RowData();
        rootRow.setClassUnderQualification(derivedResponseCD);
        rootRow.setCollectionOperator(CollectionOperator.ANY);

/*
        rowData = new RowData();
        rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
        rowData.addAttribute(epochCD.getAttribute("myderivedResponses"));
        rowData.setCollectionOperator(CollectionOperator.NONE);
        rootRow.addChildRow(rowData);
        */

/*
        rowData2 = new RowData();
        rowData2.addAttribute(derivedResponseCD.getAttribute("mykeywords"));
        rowData2.setCollectionOperator2(CollectionOperator.ANY);
        rowData2.setCollectionOperator(CollectionOperator.COUNT);
        rowData2.setAttributeOperator(Operator.EQUALS);
        rowData2.setAttributeValue(new Integer(4));
        rootRow.addChildRow(rowData2);
/*
        rootRow = RowData.createTestRowData();
        System.out.println("\nRowData:\n"+rootRow);
*/
        System.out.println("\nRowData:\n"+rootRow);
        testTranslation(rootRow);
        /*
        rootRow.testSerialization();
        System.out.println("\nRowData:\n"+rootRow);
        expression = ExpressionTranslator.createExpressionTree(rootRow);
        System.out.println("\nExpression:\n"+expression);
        rootRow = ExpressionTranslator.createRowData(expression);
        System.out.println("\nExpression Translated To RowData:\n"+rootRow);
        */
    }


    /**
     * This is just a convenience method to call the other
     * testTranslation() method with the verbose flag set
     * to true and the exitOnFirstError flag set to true.
     */
    private static boolean testTranslation(Object someKindOfTree) {
        return(testTranslation(someKindOfTree, true, true));
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
     * message is printed.
     *
     * @param someKindOfTree Pass either a RowData root row object
     * or pass an ExpressionTree object.
     *
     * @param verbose Pass true if you want the trees to be printed.
     * Pass false if all you want to see is the success or fail message.
     *
     * @param exitOnFirstError Pass true if you want this method to
     * call System.exit(1) if it will return false.
     *
     * @return Returns true if the translation worked.  False otherwise.
     */
    private static boolean testTranslation(Object someKindOfTree,
                                           boolean verbose,
                                           boolean exitOnFirstError) {

        RowData rootRow = null;
        ExpressionTree expressionTree = null;

        if (someKindOfTree instanceof RowData)
            rootRow = (RowData)someKindOfTree;
        else if (someKindOfTree instanceof ExpressionTree)
            expressionTree = (ExpressionTree)someKindOfTree;
        else {
            String s = "You must pass an Object of type RowData or "+
                "ExpressionTree.  You passed: "+someKindOfTree;
            throw(new IllegalArgumentException(s));
        }

        boolean same;
        if (rootRow != null) {
            System.out.println("\nStarting With RowData:\n"+rootRow);
            expressionTree = RowDataToExpressionTree.translate(rootRow);
            System.out.println("\nRowData Translated To Expression:\n"+
                expressionTree);
            RowData newRowData = ExpressionTreeToRowData.translate(
                expressionTree);
            System.out.println("\nExpressionTree Translated Back To RowData:\n"+
                newRowData);

            same = rootRow.toString(true, "").equals(
                newRowData.toString(true, ""));
        }
        else {
            System.out.println("\nStarting With ExpressionTree:\n"+
                expressionTree);
            rootRow = ExpressionTreeToRowData.translate(expressionTree);
            System.out.println("\nExpressionTree Translated To RowData:\n"+
                rootRow);
            ExpressionTree newExpressionTree = RowDataToExpressionTree.
                translate(rootRow);
            System.out.println("\nRowData Translated Back To Expression:\n"+
                newExpressionTree);

            same = expressionTree.toString().equals(
                newExpressionTree.toString());
        }

        if (same)
            System.out.println("\nOriginal and translated versions are "+
                               "the same.");
        else
            System.out.println("\nERROR:  Original and translated versions "+
                               "are different.");

        if (exitOnFirstError && !same)
            System.exit(1);

        return(same);
    }


    private static void printResults(String label, RowData rootRow) {

        System.out.println("\n===== "+label+" =====");

        System.out.println("\nOriginal RowData:\n"+rootRow);

        ExpressionTree expression = RowDataToExpressionTree.translate(
            rootRow);
        System.out.println("\nTranslated To Expression:\n"+expression);

        System.out.print("\nTest RowData Serialization: ");
        rootRow.testSerialization();

        System.out.print("\nTest ExpressionTree Serialization: ");
        expression.testSerialization();

        System.out.print("\nTest Translation: ");
        testTranslation(rootRow, true, true);
    }


    /**
     * This is a simple test program for this class.
     */
    public static void main(String[] args) {

        System.out.println("ExpressionTranslator test is starting...");

        runAllTests();
        //runOneTest();

        System.out.println("\nExpressionTranslator test is ending.");
    }
}
