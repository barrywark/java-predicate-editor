//package com.physion.ovation.gui.ebuilder.datamodel;

import java.util.ArrayList;

import com.physion.ovation.gui.ebuilder.datatypes.ClassDescription;
import com.physion.ovation.gui.ebuilder.datatypes.Attribute;
import com.physion.ovation.gui.ebuilder.datatypes.Type;
import com.physion.ovation.gui.ebuilder.datatypes.Cardinality;


/**
 * 
 */
class RowData {

    /**
     * This is the "topmost", or "root" class that is the ancestor
     * of all other rows.
     */
    private static ClassDescription classUnderQualification;

    /**
     * This is the class from whose attributes the user
     * will select to create a "path" to this row's childmost attribute.
     * You might also think of this as the "Class Under Qualification"
     * as far as this row is concerned.
     *
     * For example, this might be Epoch or User or Source.
     *
     * TODO: Perhaps come up with a better name?  cuq, entity?
     */
    //private ClassDescription parentClass;

    /**
     * This is the class from whose attributes the user
     * will select to create a "path" to this row's childmost attribute.
     * You might also think of this as the "Class Under Qualification"
     * as far as this row is concerned.
     *
     * For example, this might be Epoch or User or Source.
     *
     * If our parentRow member data is null, then this RowData instance
     * is the "root" row for the whole tree.  I.e. it is the
     * "Class Under Qualification".
     *
     * All rows except the root row have a non-null parentRow.
     * The class of the root row is stored in our static member
     * data classUnderQualification.
     */
    private RowData parentRow;

    /**
     * This is the path to the childmost attribute that
     * this row is specifying.
     *
     * For example, parentClass might be Epoch, and then
     * attributePath could be a list containing the Attributes:
     *
     *      epochGroup  (is of type EpochGroup)
     *      source      (is of type Source)
     *      label       (is of type string)
     *
     * So the above would be specifying the label of the source of
     * the epochGroup of the parentClass.
     *
     * TODO:  Do we want a list of Attribute objects or just Strings.
     */
    private ArrayList<Attribute> attributePath = new ArrayList<Attribute>();
    //private ArrayList<String> attributePath = new ArrayList<String>();

    /**
     * The operator the user selected for this attribute.
     * For example, ==, !=, >=, <=, <, >.
     * Note that "is null" and "is not null" are also considered operators.
     *
     * Please note, this might be null if this row is a "compound" row
     * that ends with Any, All, or None.
     *
     * We could create enum types to hold these operators, but I think that
     * is a very heavy solution for not much gain.  The user won't be
     * able to enter the values because GUI widgets will be used to
     * select the values, so there isn't any user risk of invalid values
     * being used.  But, there is programmer risk of that.
     * We might change this.
     */
    private String attributeOperator;

    /**
     * If the attributeOperator is set to something AND it is not
     * set to "is null" or "is not null", then this value is the
     * value that the user entered as the desired value for the
     * attribute.
     *
     * For example, if the attributePath is:  epochGroup.source.label
     * then attributeValue might be something like "Test 27".
     */
    private Object attributeValue;

    /**
     * If this row is a "compound" row, this will be set to
     * Any, All, or None.
     *
     * If this is set to Count, then the
     * attributeOperator member data will be set to some sort of
     * operator such as ==, >, or "is null".
     *
     * If this is null, then this row is an "attribute" row as
     * opposed to a "compound" row.
     */
    private CollectionOperator collectionOperator;

    private ArrayList<RowData> childRows = new ArrayList<RowData>();


    /**
     * Returns true if this row ends with the Any, All, or None
     * "collection" operator.
     */
    public boolean isCompoundRow() {

        if (childRows.isEmpty())
            System.err.println(
                "WARNING: Compound row with no child rows defined.");

        if (collectionOperator == null)
            return(false);

        return(collectionOperator.isCompoundOperator());
    }


    public /*static*/ void setClassUnderQualification(
        ClassDescription classUnderQualification) {

        RowData.classUnderQualification = classUnderQualification;

        if (parentRow != null) {
            System.out.println(
                "WARNING:  parentRow != null.  Are you confused?");
            parentRow = null;
        }

        if (!childRows.isEmpty()) {
            System.out.println("INFO:  Clearing all childRows.");
            childRows.clear();
        }
    }


    /*
    public void setParentClass(ClassDescription parentClass) {
        this.parentClass = parentClass;
    }
    */
    public void setParentRow(RowData parentRow) {
        this.parentRow = parentRow;
    }

    public void setCollectionOperator(CollectionOperator collectionOperator) {
        this.collectionOperator = collectionOperator;
    }


    public void setAttributeOperator(String attributeOperator) {
        this.attributeOperator = attributeOperator;
    }


    /*
    public void setAttributePath(ArrayList<String> attributePath) {
        this.attributePath = attributePath;
    }
    */
    public void setAttributePath(ArrayList<Attribute> attributePath) {
        this.attributePath = attributePath;
    }


    public void setAttributeValue(Object attributeValue) {
        this.attributeValue = attributeValue;
    }


    /**
     * TODO: Do we want to set the parentRow of every child to
     * be this RowData instance?  If so, then we probably want
     * to have an addChildRow() method.
     */
    public void setChildRows(ArrayList<RowData> childRows) {

        this.childRows = childRows;
        for (RowData childRow : childRows)
            childRow.setParentRow(this);
    }


    public String toString() {
        return(toString(""));
    }


    /**
     * Get the "childmost" or "leaf" Attribute that is specified
     * by this row.
     */
    public Attribute getChildmostAttribute() {

        if (attributePath.isEmpty()) {
            //System.out.println("attributePath.isEmpty()");
            return(null);
        }
        else
            return(attributePath.get(attributePath.size()-1));
    }


    public ClassDescription getParentClass() {

        if (parentRow == null) {
            //System.out.println("parentRow == null");
            return(classUnderQualification);
        }
        else {
            if (parentRow.getChildmostAttribute() == null) {
                //System.out.println("parentRow.getChildmostAttribute == null");
                return(classUnderQualification);
            }
            else {
                //System.out.println("parentRow.getChildmostAttribute != null");
                //System.out.println("parentRow.getChildmostAttribute().getClassDescription() = "+parentRow.getChildmostAttribute().getClassDescription());
                //System.out.println("parentRow.getChildmostAttribute() = "+parentRow.getChildmostAttribute());
                return(parentRow.getChildmostAttribute().getClassDescription());
            }
        }
    }


    public String toString(String indent) {

        //String string = indent+parentClass.getName()+" |";

        //ClassDescription parentClass = getParentClass();
        //String string = indent+getParentClass().getName()+" |";
        String string;
        if (getParentClass() != null) 
            string = indent+getParentClass().getName()+" |";
        else 
            string = indent+"ERROR: No Parent Class"+" |";


        /**
         * Do a quick sanity check.
         */
        if (collectionOperator != null &&
            collectionOperator.isCompoundOperator() &&
            attributeOperator != null) {
            string += "ERROR: RowData is in an inconsistent state.";
            string += "\ncollectionOperator = "+collectionOperator;
            string += "\nattributeOperator = "+attributeOperator;
            return(string);
        }

        //if (!attributePath.isEmpty()) {
        //}
        boolean first = true;
        //for (String attributeName : attributePath) {
        for (Attribute attribute : attributePath) {

            if (first)
                string += " ";
            else
                string += ".";

            //string += attributeName;
            string += attribute.getName();

            first = false;
        }

        if (collectionOperator != null)
            string += " "+collectionOperator;

        if (attributeOperator != null)
            string += " "+attributeOperator;

        /**
         * Another quick sanity check.
         * If the user specified an attributeOperator other than
         * the "is null" and "is not null" values, then s/he also
         * must have specified an attributeValue.
         */
        if ((attributeOperator != null) &&
            ((attributeOperator != "is null") &&
             (attributeOperator != "is not null")) &&
            (attributeValue == null)) {
            string += "ERROR: RowData is in an inconsistent state.";
            string += "\nattributeOperator = "+attributeOperator;
            string += "\nattributeValue = "+attributeValue;
            return(string);
        }

        if (attributeValue != null) {
            string += " \""+attributeValue+"\"";
        }

        for (RowData childRow : childRows)
            string += "\n"+childRow.toString(indent+"  ");

        return(string);
    }

    
    /**
     * This is a simple test program for this class.
     */
    public static void main(String[] args) {

        System.out.println("RowData test is starting...");

        ClassDescription entityBaseCD =
            new ClassDescription("EntityBase", null);
        ClassDescription taggableEntityBaseCD =
            new ClassDescription("TaggableEntityBase", entityBaseCD);
        ClassDescription userCD =
            new ClassDescription("User", taggableEntityBaseCD);
        ClassDescription keywordTagCD =
            new ClassDescription("KeywordTag", entityBaseCD);
        ClassDescription timelineElementCD =
            new ClassDescription("TimelineElement", taggableEntityBaseCD);
        ClassDescription epochCD =
            new ClassDescription("Epoch", timelineElementCD);
        ClassDescription epochGroupCD =
            new ClassDescription("EpochGroup", timelineElementCD);
        ClassDescription sourceCD =
            new ClassDescription("Source", taggableEntityBaseCD);

        /**
         * Initialize values of the EntityBase class.
         */
        Attribute attribute = new Attribute("owner", Type.REFERENCE,
                                            userCD, Cardinality.TO_ONE);
        entityBaseCD.addAttribute(attribute);

        attribute = new Attribute("uuid", Type.UTF_8_STRING);
        entityBaseCD.addAttribute(attribute);

        attribute = new Attribute("incomplete", Type.BOOLEAN);
        entityBaseCD.addAttribute(attribute);

        /**
         * Initialize values of the Epoch class.
         */
        attribute = new Attribute("protocolID", Type.UTF_8_STRING);
        epochCD.addAttribute(attribute);

        attribute = new Attribute("excludeFromAnalysis", Type.BOOLEAN);
        epochCD.addAttribute(attribute);

        attribute = new Attribute("nextEpoch", Type.REFERENCE,
                                  epochCD, Cardinality.TO_ONE);
        epochCD.addAttribute(attribute);

        /**
         * Initialize values of the Source class.
         */
        attribute = new Attribute("label", Type.UTF_8_STRING);
        sourceCD.addAttribute(attribute);

        attribute = new Attribute("parent", Type.REFERENCE,
                                  sourceCD, Cardinality.TO_ONE);
        sourceCD.addAttribute(attribute);

        /**
         * Initialize values of the EpochGroup class.
         */
        attribute = new Attribute("label", Type.UTF_8_STRING);
        epochGroupCD.addAttribute(attribute);

        attribute = new Attribute("source", Type.REFERENCE,
                                  sourceCD, Cardinality.TO_ONE);
        epochGroupCD.addAttribute(attribute);

        /**
         * Now create some RowData values.
         */
        RowData rootRow = new RowData();
        //rootRow.setParentClass(epochCD);
        rootRow.setClassUnderQualification(epochCD);
        //RowData.setClassUnderQualification(epochCD);

        rootRow.setCollectionOperator(CollectionOperator.ANY);

        RowData rowData = new RowData();
        //rowData.setParentClass(epochCD);
        //rowData.setParentRow(rootRow);

        ArrayList<Attribute> attributePath = new ArrayList<Attribute>();
        //ArrayList<String> attributePath = new ArrayList<String>();
        attribute = new Attribute("epochGroup", Type.REFERENCE,
                                  epochGroupCD, Cardinality.TO_ONE);
        attributePath.add(attribute);
        attribute = new Attribute("source", Type.REFERENCE,
                                  sourceCD, Cardinality.TO_ONE);
        attributePath.add(attribute);
        attribute = new Attribute("label", Type.UTF_8_STRING);
        attributePath.add(attribute);
        rowData.setAttributePath(attributePath);

        rowData.setAttributeOperator("==");
        rowData.setAttributeValue("Test 27");

        ArrayList<RowData> childRows = new ArrayList<RowData>();

        childRows.add(rowData);

        /**
         * Create another child row.
         */

        rowData = new RowData();
        //rowData.setParentClass(epochCD);
        rowData.setCollectionOperator(CollectionOperator.ALL);

        attributePath = new ArrayList<Attribute>();
        attribute = new Attribute("epochGroup", Type.REFERENCE,
                                  epochGroupCD, Cardinality.TO_ONE);
        attributePath.add(attribute);
        attribute = new Attribute("epochs", Type.REFERENCE,
                                  epochCD, Cardinality.TO_MANY);
        attributePath.add(attribute);

        rowData.setAttributePath(attributePath);

        childRows.add(rowData);

        RowData rowData2 = new RowData();
        //rowData2.setParentClass(epochCD);
        attributePath = new ArrayList<Attribute>();
        attribute = new Attribute("startTime", Type.DATE_TIME);
        attributePath.add(attribute);

        rowData2.setAttributeOperator(">=");
        rowData2.setAttributeValue("07/23/2011");

        rowData2.setAttributePath(attributePath);
        ArrayList<RowData> childRows2 = new ArrayList<RowData>();
        childRows2.add(rowData2);
        rowData.setChildRows(childRows2);

        /**
         * Create another child row.
         */
        attributePath = new ArrayList<Attribute>();
        attribute = new Attribute("epochGroup", Type.REFERENCE,
                                  epochGroupCD, Cardinality.TO_ONE);
        attributePath.add(attribute);
        attribute = new Attribute("source", Type.REFERENCE,
                                  sourceCD, Cardinality.TO_ONE);
        attributePath.add(attribute);

        RowData rowData3 = new RowData();
        rowData3.setAttributePath(attributePath);
        rowData3.setCollectionOperator(CollectionOperator.NONE);

        RowData rowData4 = new RowData();
        attribute = new Attribute("label", Type.UTF_8_STRING);
        attributePath = new ArrayList<Attribute>();
        attributePath.add(attribute);
        rowData4.setAttributePath(attributePath);
        rowData4.setAttributeOperator("==");
        rowData4.setAttributeValue("Test 50");

        ArrayList<RowData> childRows3 = new ArrayList<RowData>();
        childRows3.add(rowData4);
        rowData3.setChildRows(childRows3);


        childRows.add(rowData3);

        rootRow.setChildRows(childRows);
        System.out.println(rootRow);

        System.out.println("RowData test is ending.");
    }
}
