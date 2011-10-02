package com.physion.ovation.gui.ebuilder.datamodel;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.EventListener;
import javax.swing.event.EventListenerList;

import com.physion.ovation.gui.ebuilder.datatypes.ClassDescription;
import com.physion.ovation.gui.ebuilder.datatypes.Attribute;
import com.physion.ovation.gui.ebuilder.datatypes.Type;
import com.physion.ovation.gui.ebuilder.datatypes.Cardinality;


/**
 * A RowData object is one row in the GUI.  The very first row in the
 * GUI is the "root" row.  Each row can have 0 or more child rows.
 * A row has an "attributePath", which is how a RowData object defines
 * the attribute that it is describing.  For example:
 *
 *      epochGroups.epoch.label == "Test 21"
 *
 * is describing an attributePath to the label attribute, and says
 * that we are looking for Epochs whose label attribute is "Test 21".
 *
 * You can ask a RowData object whether it contains a legal value
 * by calling the containsLegalValue() method.  As of October 2011,
 * this method is pretty simple, and rows that don't make sense
 * are "legal".  We can make this code more clever in the future.
 * 
 * TODO: Perhaps change the structure a bit so the root row is NOT a
 * RowData object?  It really is a bit of a different thing given that
 * it is used to set the Class Under Qualification.
 *
 * TODO: Perhaps change how the "is null" and "is not null" values are
 * handled.  Right now, I set the attributeOperator to be "is null" or
 * "is not null".  But, I also have to have the last Attribute on the
 * RowData's attributePath be the Attribute.IS_NULL or IS_NOT_NULL value.
 * This seems a bit redundant.  The problem is, I need the Attribute on
 * the path in order to have the comboBox that holds "is null" etc. be created.
 * So, maybe we should not also store that information in the attributeOperator
 * member data?
 */
public class RowData
    implements RowDataListener {

    /**
     * This is the "topmost", or "root" class that is the ancestor
     * of ALL other rows.
     *
     * Please note this value only makes sense on the root RowData object
     * in the expression tree.
     */
    private ClassDescription classUnderQualification;

    /**
     * This RowData object is a child row of the parentRow.
     *
     * If our parentRow member data is null, then this RowData instance
     * is the "root" row for the whole tree.  I.e. it defines the
     * "Class Under Qualification".
     *
     * All rows except the root row have a non-null parentRow.
     * The class of the root row is stored in the root row's member
     * data "classUnderQualification".
     */
    private RowData parentRow;

    /**
     * This is the path to the childmost attribute that
     * this row is specifying.
     *
     * For example, if this row's starting class, (i.e. the rightmost
     * class of its parentRow), might be the class "Epoch", and then
     * attributePath could be a list containing the Attributes:
     *
     *      epochGroup  (is of type EpochGroup)
     *      source      (is of type Source)
     *      label       (is of type string)
     *
     * So the above would be specifying the "label" attribute of the
     * "source" attribute of the "epochGroup" of the "Epoch" class.
     */
    private ArrayList<Attribute> attributePath = new ArrayList<Attribute>();

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
     * This member data can be a:  Boolean, String, Integer, Short,
     * Double, Date, or null.
     * Note, as of September 2011, there is no Float (int32) value.
     *
     * If this is being used to hold the value of a row's Count, it
     * is of type Integer.  If the row does not have a value, this
     * should be set to null.
     *
     * For example, if the attributePath is:  epochGroup.source.label
     * then attributeValue might be the String object "Test 21".
     */
    private Object attributeValue;

    /**
     * If the user is specifying a "keyed" "My Property" or "Any Property"
     * attribute, the propName member data will be set to the "key"
     * that the user entered in the row.
     */
    private String propName;
    private Type propType;
    
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

    /**
     * This is the list of this row's direct children.
     */
    private ArrayList<RowData> childRows = new ArrayList<RowData>();

    /**
     */
    private EventListenerList rowDataListenerList = new EventListenerList();


    /**
     * Create a RowData object that has no values set.
     */
    public RowData() {
        init();
    }


    /**
     * Initialize values for this RowData object.
     * At the moment, this method is really just a placeholder
     * for when we want to do more.
     */
    private void init() {
        parentRow = null;
    }


    /**
     * Create a RowData that is a "deep" copy of another RowData.
     * By "deep" copy, I mean a copy whose attributePath contains
     * copies of the other's Attributes.
     *
     * @param other - The other RowData object of which we will be
     * a copy.  If this is null, then this method is equivalent
     * to calling the empty RowData constructor.
     */
    public RowData(RowData other) {

        if (other == null) {
            init();
            return;
        }

        this.classUnderQualification = other.classUnderQualification;
        this.parentRow = other.parentRow;
        this.attributeOperator = other.attributeOperator;
        this.attributeValue = other.attributeValue;
        this.propName = other.propName;
        this.propType = other.propType;
        this.collectionOperator = other.collectionOperator;

        for (Attribute attribute : other.getAttributePath()) {
            this.addAttribute(new Attribute(attribute));
        }

        for (RowData childRow : other.getChildRows()) {
            this.addChildRow(new RowData(childRow));
        }
    }


    /**
     * Create a default rootRow that has no children.  Return the
     * rootRow we created AND set the RowData.rootRow static member
     * data to the rootRow we just created.
     */
    public static RowData createRootRow() {

        RowData rootRow = new RowData();
        rootRow.setClassUnderQualification(
            DataModel.getClassDescription("Epoch"));
        rootRow.setCollectionOperator(CollectionOperator.ANY);
        return(rootRow);
    }


    /**
     * Get the number of descendents from this node.  I.e. this is returns
     * the count of our direct children, PLUS all their children, and all
     * our their children, and so on.
     * This is NOT the same thing as getting the number of this node's children.
     */
    public int getDescendentCount() {

        int count = childRows.size();
        for (RowData childRow : childRows) {
            count += childRow.getDescendentCount();
        }
        return(count);
    }


    /**
     * This returns the RowData object that is at the specified "index".
     * This method is intended to be used to get the RowData object at
     * the specified index as far as a List GUI widget is concerned.
     * This RowData object is at index 0.  Its first child is at index 1.
     * If the first child has a child, then that child is at index 2.
     * If the first child does not have a child, then the second child
     * is at index 2.  (I.e. the first child's sibling.)
     *
     * A simple "picture" of RowData objects and their "indexes" as
     * far as a List widget is concerned, makes this more obvious:
     *
     *      RowData 0
     *          RowData 1
     *          RowData 2
     *          RowData 3
     *               RowData 4
     *          RowData 5
     *               RowData 6
     *               RowData 7
     *               RowData 8
     *          RowData 9
     *
     * So, just to repeat, "this" RowData object is returned if
     * you pass an index of 0.  This RowData's first child is at
     * index 1.
     */
    public RowData getChild(int index) {

        if (index == 0)
            return(this);


        for (RowData childRow : childRows) {

            index--;
            RowData rd = childRow.getChild(index);
            if (rd != null)
                return(rd);

            index -= childRow.getDescendentCount();

            if (index == 0)
                return(childRow);
        }

        return(null);
    }


    /**
     * Get this RowData and all its descendents as an ArrayList of
     * RowData objects.
     */
    public ArrayList<RowData> getRows() {

        int count = getDescendentCount()+1;

        ArrayList<RowData> rows = new ArrayList<RowData>();
        for (int index = 0; index < count; index++)
            rows.add(getChild(index));

        return(rows);
    }


    /**
     * Add a listener to this RowData.
     * The listener will be modified when anything in this RowData
     * or any of its descendents changes.  So, you only need to
     * listen to the root RowData of an expression tree to be notified
     * if anything anywhere in the tree changes.
     */
    public void addRowDataListener(RowDataListener listener) {
        rowDataListenerList.add(RowDataListener.class, listener);
    }


    /**
     * Remove a listener from this RowData.
     */
    public void removeRowDataListener(RowDataListener listener) {
        rowDataListenerList.remove(RowDataListener.class, listener);
    }


    /**
     * Notify all listeners that have registered interest for
     * notification on this event type.
     */
    private void fireRowDataEvent() {

        //System.out.println("Enter fireRowDataEvent for row:"+getRowString());

        /**
         * Create a RowDataEvent that will tell the listener
         * about this event.  Currently, there is only one
         * type of event and all we give the listener is
         * a reference to this RowData object.
         */
        RowDataEvent rowDataEvent = new RowDataEvent(this);

        RowDataListener listenerList[] =
            rowDataListenerList.getListeners(RowDataListener.class);
        for (RowDataListener listener : listenerList) {
            listener.rowDataChanged(rowDataEvent);
        }
    }


    /**
     * This method is called when one of our direct children RowData
     * objects is changed.  Please note, because each row listens
     * to its children, the message eventually gets passed up to
     * the root row that the GUI is listening to.
     *
     * If, at some point in the future, the GUI will need to keep
     * track of individual rows, we might want to restructure the
     * way RowDataEvents are passed up the listener "tree" in order
     * to tell the root listener which row actually changed.
     * Perhaps "chain" the RowDataEvents together as a list.
     * No need for this sort of granularity/complication at the
     * present time.
     */
    public void rowDataChanged(RowDataEvent rowData) {
        fireRowDataEvent();
    }


    /**
     * Remove the specified child RowData object from this RowData object's
     * list of direct children.
     */
    public void removeChildRow(RowData childRow) {

        childRows.remove(childRow);
        childRow.removeRowDataListener(this);
        fireRowDataEvent();
    }


    /**
     * Remove this RowData object from its parent's list of direct children.
     * This is functionally equivalent to calling:
     *
     *      getParentRow().removeChildRow(this);
     * 
     * Please note, removing row "A" from its parent row "B" is NOT considered
     * a change to row "A", (as far as notifying listeners is concerned),
     * but is considered a change to the parent row "B".
     */
    public void removeFromParent() {

        if ((getParentRow() == null) ||
            (getParentRow().getChildRows() == null)) {
            System.err.println("ERROR:  removeFromParent called on a row\n"+
                "without a parent, or whose parent doesn't have children.\n"+
                "This is a coding error of some sort.");
            return;
        }

        getParentRow().removeChildRow(this);
    }


    /**
     * Create a child row for this row that is of type Compound Row.
     */
    public void createCompoundRow() {

        //System.out.println("createCompoundRow rowData: "+this.getRowString());

        RowData compoundRow = new RowData();
        compoundRow.setParentRow(this);
        compoundRow.setCollectionOperator(CollectionOperator.ANY);
        addChildRow(compoundRow);
    }


    /**
     * Create a child row for this row that is of type Attribute Row.
     */
    public void createAttributeRow() {

        //System.out.println("createAttributeRow rowData: "+this.getRowString());

        ClassDescription classDescription = null;

        if ((this != getRootRow()) && isSimpleCompoundRow()) {
            /**
             * TODO:  Not sure I like this mucking around.
             */
            RowData parent = getParentRow();
            while (classDescription == null) {
                Attribute attribute = parent.getChildmostAttribute();
                if (attribute != null)
                    classDescription = attribute.getClassDescription();
                parent = parent.getParentRow();
                if (parent == null)
                    classDescription = getClassUnderQualification();
            }
        }
        else {
            Attribute attribute = getChildmostAttribute();
            if (this == getRootRow())
                classDescription = getClassUnderQualification();
            else
                classDescription = attribute.getClassDescription();
        }


        if (classDescription == null) {
            System.err.println("ERROR: In createAttributeRow "+
                "classDescription == null.  This should never happen.");
            return;
        }

        ArrayList<Attribute> attributes = classDescription.getAllAttributes();
        if (attributes.isEmpty()) {
            System.err.println("ERROR: In createAttributeRow "+
                "attributes.isEmpty == true.  This should never happen.");
            return;
        }

        RowData attributeRow = new RowData();
        attributeRow.addAttribute(Attribute.SELECT_ATTRIBUTE);
        addChildRow(attributeRow);
    }


    /**
     * Returns true if this row is a Compound Row, (simple or not).
     * I.e. this means the row ends with a "compound" Collection Operator
     * Any, All, or None.  A row that ends with Count is not a Compound Row.
     *
     * For example, a row like one of these:
     *
     *      epochGroups.epochs Any of the following
     *      resources None of the following
     *      Any of the following
     *      None of the following
     */
    public boolean isCompoundRow() {

        if (collectionOperator == null)
            return(false);

        return(collectionOperator.isCompoundOperator());
    }


    /**
     * Returns true if this is a "simple" Compound Row.
     * I.e. a row that contains ONLY a Collection Operator comboBox.
     *
     * For example, a row like one of these:
     *
     *      Any of the following
     *      All of the following
     *      None of the following
     */
    public boolean isSimpleCompoundRow() {

        if (isCompoundRow() == false)
            return(false);

        if ((attributePath == null) || attributePath.isEmpty())
            return(true);

        return(false);
    }


    /**
     */
    public void setClassUnderQualification(
        ClassDescription classUnderQualification) {

        if (this != getRootRow()) {
            System.err.println(
                "WARNING:  This RowData object is not the \"root\"\n"+
                "RowData object.  Are you confused?\n"+
                "Setting the Class Under Qualification really only makes\n"+
                "sense on the root RowData object.\n"+
                "I will assume that is what you meant to do, and do that.");
        }

        getRootRow().classUnderQualification = classUnderQualification;

        if (!getRootRow().getChildRows().isEmpty()) {
            System.out.println("INFO:  Clearing all childRows.");
            getRootRow().getChildRows().clear();
        }
    }


    public ClassDescription getClassUnderQualification() {
        //return(classUnderQualification);
        return(getRootRow().classUnderQualification);
    }


    /**
     * Get the current rootRow.
     */
    public RowData getRootRow() {

        RowData rootRow = this;
        while (rootRow.getParentRow() != null)
            rootRow = rootRow.getParentRow();
        return(rootRow);
    }


    /*
    public static void setRootRow(RowData rowData) {
        rootRow = rowData;
    }
    */


    public boolean isRootRow() {
        return(this == getRootRow());
    }


    public void setParentRow(RowData parentRow) {
        this.parentRow = parentRow;
    }


    private RowData getParentRow() {
        return(parentRow);
    }


    public void setCollectionOperator(CollectionOperator collectionOperator) {

        this.collectionOperator = collectionOperator;

        /**
         * Blank out the attribute operator if the user
         * is selecting Any, All, or None.
         *
         * Set the attribute operator and attribute value
         * to legal values if the user is selecting Count.
         */
        if ((collectionOperator != null) &&
            collectionOperator.isCompoundOperator()) {
            setAttributeOperator(null);
            setAttributeValue(null);
        }
        else if ((collectionOperator != null) &&
                 !collectionOperator.isCompoundOperator()) {
            setAttributeOperator(DataModel.OPERATORS_ARITHMATIC[0]);
            setAttributeValue("");
        }
    }


    public CollectionOperator getCollectionOperator() {
        return(collectionOperator);
    }


    /**
     * Set the attributeOperator of this row.
     *
     * TODO: Cleanup/reorganize where these assorted operator strings
     * are stored.  Is creating enums overkill?
     *
     * @param attributeOperator - A value such as, but not limited to:
     * ==, !=, >, <, ~=, is null, is not null, is true, is false.
     */
    public void setAttributeOperator(String attributeOperator) {

        //System.out.println("Setting attributeOperator to "+attributeOperator);
        this.attributeOperator = attributeOperator;

        /**
         * Blank out the attributeValue if the attributeOperator
         * is being set to the operator "is true", "is false", "is null"
         * "is not null".  (If the operator is set to one of those
         * values, the attributeValue member data is meaningless.)
         *
         * TODO: I don't like the fact that the user *chooses* the
         * "is null" or "is not null" operator using an Attribute comboBox,
         * but we save the operator in the RowData's attributeOperator member
         * data.  That is the way the user sees it in the GUI though.
         * I need to disentangle the data model from the GUI's view.
         */
        if (DataModel.OPERATOR_TRUE.equals(attributeOperator) ||
            DataModel.OPERATOR_FALSE.equals(attributeOperator) ||
            DataModel.OPERATOR_IS_NULL.equals(attributeOperator) ||
            DataModel.OPERATOR_IS_NOT_NULL.equals(attributeOperator)) {
            setAttributeValue(null);
        }

        fireRowDataEvent();
    }


    public String getAttributeOperator() {
        return(attributeOperator);
    }


    public void setAttribute(int index, Attribute attribute) {

        System.out.println("Enter setAttribute("+index+", "+attribute+")");
        /**
         * Make sure the attributePath is long enough.
         */
        while (index >= getAttributeCount())
            addAttribute(null);

        getAttributePath().set(index, attribute);
        fireRowDataEvent();
    }


    /**
     * Remove Attributes from the attributePath that are
     * after the passed in index.  So, the index you
     * pass in is the index of the last attribute you
     * want in the list.  Attributes after that index are
     * removed from the list.  For example, if you
     * pass 3 for the lastAttributeIndex parameter,
     * this method will remove, (if they exist), the
     * Attributes at index 4, 5, 6...
     */
    public void trimAttributePath(int lastAttributeIndex) {

        /**
         * If the attributePath isn't even as long as the
         * caller thinks it is, just return.
         */
        if (getAttributeCount() <= (lastAttributeIndex+1))
            return;

        getAttributePath().subList(lastAttributeIndex+1,
                                   getAttributeCount()).clear();
        fireRowDataEvent();
    }


    /**
     * Returns the number of Attribute objects in the attributePath.
     * Please note, a "special" Attribute such as the "is null" Attribute
     * is part of this count.
     */
    public int getAttributeCount() {
        return(getAttributePath().size());
    }


    /**
     * Add an Attribute to the end of this RowData's attributePath.
     *
     * In addition to simply adding the Attribute to the attributePath,
     * this method also makes any other necessary changes to the
     * other member data associated with this RowData object.
     * For example, if the user adds the Attribute.IS_NULL to the
     * attributePath, this method also sets the attributeOperator
     * to "is null".
     */
    public void addAttribute(Attribute attribute) {

        System.out.println("Enter addAttribute("+attribute+")");

        if (attributePath == null)
            attributePath = new ArrayList<Attribute>();

        attributePath.add(attribute);

        if (attribute.equals(Attribute.IS_NULL)) {
            setAttributeOperator(DataModel.OPERATOR_IS_NULL);
        }
        else if (attribute.equals(Attribute.IS_NOT_NULL)) {
            setAttributeOperator(DataModel.OPERATOR_IS_NOT_NULL);
        }
        fireRowDataEvent();
    }


    /**
     * This returns a COPY of the Attribute at the specified index.
     * So, any changes you make to it will have no effect on this
     * RowData's attributePath.
     */
    public Attribute getAttribute(int index) {
        return(new Attribute(getAttributePath().get(index)));
    }


    /**
     * This returns this RowData's ArrayList of Attribute objects.
     * This method creates an empty ArrayList if the attributePath
     * is currently null.
     *
     * Please note, it is returning this RowData's member data.
     * It is NOT returning a copy, so if you make changes to
     * it, you are affecting this RowData object.
     *
     * TODO:  Make the RowData.getAttributePath() method private and
     * force engineers to use the setAttribute(), addAttribute(), and
     * removeAttribute() methods to access the attributePath.  This
     * will let the RowData object impose whatever rules it needs to
     * on the manipulation of the attributePath.  I have written
     * the addAttribute() method, but that is just a start.
     */
    private ArrayList<Attribute> getAttributePath() {

        if (attributePath == null)
            attributePath = new ArrayList<Attribute>();

        return(attributePath);
    }


    /**
     * This is simply a convenience method that calls the
     * other version of this method with the debugVersion
     * flag set to false.
     */
    public String getAttributePathString() {
        return(getAttributePathString(false));
    }


    /**
     * Get a string that represents this RowData's attributePath
     * that can be used in a query string.
     * The returned value is in the format that the Objectivity
     * software would like.
     *
     * TODO: Make changes to this if necessary.
     *
     * @param debugVersion - Set this to true if you want a 
     * "debug" version of the attribute path that has the
     * isMine flag shown in it.  Set this to false if you
     * want a version of this string that can be used for
     * a query.
     */
    public String getAttributePathString(boolean debugVersion) {

        String string = "";


        boolean first = true;
        for (Attribute attribute : attributePath) {

            if (attribute != null) {
                if (!attribute.equals(Attribute.SELECT_ATTRIBUTE) &&
                    !attribute.equals(Attribute.IS_NULL) &&
                    !attribute.equals(Attribute.IS_NOT_NULL)) {
                    /**
                     * Put a dot between each attribute on the path.
                     */
                    if (first) {
                        string += " ";
                        first = false;
                    }
                    else {
                        string += ".";
                    }

                    if (debugVersion)
                        string += attribute.getDisplayName();
                    else
                        string += attribute.getQueryName();

                }
                else {
                    /**
                     * We don't display a string for this type of attribute.
                     */
                }
            }
            else {
                System.err.println("ERROR:  null Attribute in attributePath.");
                string += "ERROR: attribute == null";
            }
        }

        return(string);
    }


    public void setAttributeValue(Object attributeValue) {

        //System.out.println("setAttributeValue("+attributeValue+")");
        this.attributeValue = attributeValue;
        fireRowDataEvent();
    }


    public void setAttributeValueUsingString(String stringValue) {

        Object value = null;

        Attribute attribute = getChildmostAttribute();
        if (attribute == null) {
            setAttributeValue(null);
        }
        else {
            try {
                value = convertStringToAttributeValue(stringValue, 
                                                      attribute.getType());
                setAttributeValue(value);
            }
            catch (NumberFormatException e) {
                System.out.println("Field does not contain a legal number.");
            }
            catch (Exception e) {
                System.out.println("Field does not contain a legal value.");
                e.printStackTrace();
            }
        }
    }


    private Object convertStringToAttributeValue(String stringValue,
                                                 Type type) {

        Object value;

        if (type == null) {
            (new Exception("test")).printStackTrace();
            return(null);
        }

        switch (type) {
            case BOOLEAN:
                value = new Boolean(stringValue);
            break;
            case UTF_8_STRING:
                value = stringValue;
            break;
            case INT_16:
                value = new Short(stringValue);
            break;
            case INT_32:
                value = new Integer(stringValue);
            break;
            case FLOAT_64:
                value = new Double(stringValue);
            break;
            case DATE_TIME:
                /**
                 * We don't really need to bother with this
                 * one because it won't happen.
                 * But if this does become necessary, using
                 * SimpleDateFormat.parse() would probably be
                 * involved.  For now, just ignore the passed
                 * in value and set it to null.
                 */
                value = null;
            break;
            case PER_USER:
            case PARAMETERS_MAP:
            case PER_USER_PARAMETERS_MAP:
                value = convertStringToAttributeValue(stringValue,
                                                      getPropType());
            break;
            default:
                if (stringValue.isEmpty()) {
                    value = stringValue;
                }
                else {
                    System.err.println("ERROR: Unhandled attribute type.");
                    System.err.println("stringValue = "+stringValue);
                    System.err.println("type = "+type);
                    value = new String("ERROR");
                }
        }
        return(value);
    }


    public Object getAttributeValue() {
        return(attributeValue);
    }


    public void setPropName(String propName) {
        this.propName = propName;
        fireRowDataEvent();
    }


    public String getPropName() {
        return(propName);
    }


    public void setPropType(Type propType) {

        this.propType = propType;
        if (propType == Type.BOOLEAN) {
            setAttributeOperator(DataModel.OPERATOR_TRUE);
            setAttributeValue(null);
        }
        else if (propType == Type.DATE_TIME) {
            setAttributeOperator("==");
            setAttributeValue(null);
        }
        else {
            setAttributeOperator("==");
            setAttributeValue("");
        }
        fireRowDataEvent();
    }


    public Type getPropType() {
        return(propType);
    }


    /**
     * TODO: Do we want to make this method private so engineers
     * are forced to use addChildRow() and removeChildRow()?
     */
    /*
    private void setChildRows(ArrayList<RowData> childRows) {

        this.childRows = childRows;
        for (RowData childRow : childRows)
            childRow.setParentRow(this);
    }
    */


    private ArrayList<RowData> getChildRows() {

        if (childRows == null)
            childRows = new ArrayList<RowData>();
        return(childRows);
    }


    public void addChildRow(RowData childRow) {

        childRow.setParentRow(this);
        childRows.add(childRow);

        childRow.addRowDataListener(this);
        fireRowDataEvent();
    }


    /**
     * Get the number of "levels" that this row is indented.
     */
    public int getIndentCount() {

        int count = 0;
        for (RowData rowData = this.getParentRow(); rowData != null;
             rowData = rowData.getParentRow())
            count++;
        return(count);
    }


    /**
     * Get the amount a RowString should be indented.
     * This method will probably be unused once I switch to using
     * widgets to render a cell.
     */
    public String getIndentString() {

        String indentString = "";
        for (RowData rowData = this.getParentRow(); rowData != null;
             rowData = rowData.getParentRow()) {

            indentString += "      ";
        }
        return(indentString);
    }


    /**
     * Get a string version of this whole tree usefull for debugging.
     * I.e. a string version of this RowData and all its descendents.
     */
    public String toString() {
        return(toString(true, ""));
    }


    /**
     * Get a string version of this whole tree usefull for debugging.
     * I.e. a string version of this RowData and all its descendents.
     *
     * All rows should have the indent string prepended to the row.
     * This method calls itself recursively, increasing the indent
     * amount for each level deeper a row is nested in the tree.
     *
     * @param debubVersion - Pass true if you want the string to
     * be more useful to a human.  Pass false if you want it to
     * look more like what will be passed to query software.
     *
     * @param indent - The amount to indent this row.  E.g. "    "
     */
    public String toString(boolean debugVersion, String indent) {

        String string = getRowString(debugVersion, indent);

        for (RowData childRow : childRows)
            string += "\n"+childRow.toString(debugVersion, indent+"  ");

        return(string);
    }


    /**
     * Get the String representation of JUST THIS row.  I.e. not this
     * row and its children.
     */
    public String getRowString() {
        return(getRowString(true, ""));
    }


    /**
     * Get the String representation of JUST THIS row.  I.e. not this
     * row and its children.
     */
    /*
    public String getRowString(boolean debugVersion) {
        return(getRowString(debugVersion, ""));
    }
    */


    /**
     * Get the String representation of JUST THIS row.  I.e. not this
     * row and its children.  The row will have the passed in indent
     * string prepended to it.
     *
     * @param indent - The amount to indent this row.  E.g. "    "
     */
    public String getRowString(boolean debugVersion, String indent) {
        
        String string = indent;

        if ((debugVersion) || (this.isRootRow())) {
            if (getParentClass() != null) 
                string += getParentClass().getName()+" |";
            else 
                string += "ERROR: No Parent Class"+" |";
        }

        /**
         * Do a quick sanity check.
         */
        if (collectionOperator != null &&
            (collectionOperator != CollectionOperator.COUNT) &&
            attributeOperator != null) {
            string += "ERROR: RowData is in an inconsistent state.";
            string += "\ncollectionOperator = "+collectionOperator;
            string += "\nattributeOperator = "+attributeOperator;
            return(string);
        }

        string += getAttributePathString(debugVersion);

        if (collectionOperator != null) {
            string += " "+collectionOperator;
        }

        if (propName != null) {
            string += "."+propName;
        }
        if (propType != null) {
            string += "("+propType+")";
        }

        if (attributeOperator != null)
            string += " "+attributeOperator;

        /**
         * If the user specified the "is null" or "is not null" operator,
         * then s/he cannot also specify a value.
         */
        if ((attributeOperator != null) &&
            (attributeOperator.equals(Attribute.IS_NULL.toString()) ||
             attributeOperator.equals(Attribute.IS_NOT_NULL.toString())) &&
            (attributeValue != null)) {
            string += "ERROR: RowData is in an inconsistent state.";
            string += "\nattributeOperator = "+attributeOperator;
            string += "\nattributeValue = "+attributeValue;
            return(string);
        }

        if (attributeValue != null) {
            string += " \""+attributeValue+"\"";
        }

        return(string);
    }


    /**
     * Get the "childmost" or "leaf" Attribute that is specified
     * by this row.
     *
     * TODO:  Change this to return a COPY of the Attribute.
     */
    public Attribute getChildmostAttribute() {

        if (getParentRow() == null) {
            /**
             * This is the root row, which does not have an attribute path.
             */
            //return(classUnderQualification);
            //System.out.println("getParentRow() == null");
            return(null);
        }
        else if (attributePath.isEmpty()) {
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
                return(getClassUnderQualification());
            }
            else {
                //System.out.println("parentRow.getChildmostAttribute != null");
                //System.out.println("parentRow.getChildmostAttribute().getClassDescription() = "+parentRow.getChildmostAttribute().getClassDescription());
                //System.out.println("parentRow.getChildmostAttribute() = "+parentRow.getChildmostAttribute());
                return(parentRow.getChildmostAttribute().getClassDescription());
            }
        }
    }


    /**
     * This method creates a RowData initialized with a few values
     * for testing purposes.
     */
    public static RowData createTestRowData() {

        ClassDescription entityBaseCD =
            DataModel.getClassDescription("EntityBase");
        ClassDescription taggableEntityBaseCD =
            DataModel.getClassDescription("TaggableEntityBase");
        ClassDescription userCD =
            DataModel.getClassDescription("User");
        ClassDescription keywordTagCD =
            DataModel.getClassDescription("KeywordTag");
        ClassDescription timelineElementCD =
            DataModel.getClassDescription("TimelineElement");
        ClassDescription epochCD =
            DataModel.getClassDescription("Epoch");
        ClassDescription epochGroupCD =
            DataModel.getClassDescription("EpochGroup");
        ClassDescription sourceCD =
            DataModel.getClassDescription("Source");
        ClassDescription resourceCD =
            DataModel.getClassDescription("Resource");
        ClassDescription derivedResponseCD =
            DataModel.getClassDescription("DerivedResponse");

        /**
         * Now create some RowData values.
         */

        /**
         * Create the "root" RowData object.  All of the other
         * rows are children of this row.
         */
        RowData rootRow = RowData.createRootRow();

        Attribute attribute;
        RowData rowData;
        //ArrayList<RowData> childRows = new ArrayList<RowData>();

        /**
         * Start creating child rows of the rootRow.
         */

        /**
         * Create a row:
         *
         *      epochGroup.source is null
         */
        rowData = new RowData();
        attribute = new Attribute("epochGroup", Type.REFERENCE,
                                  epochGroupCD, Cardinality.TO_ONE);
        rowData.addAttribute(attribute);
        attribute = new Attribute("source", Type.REFERENCE,
                                  sourceCD, Cardinality.TO_ONE);
        rowData.addAttribute(attribute);
        rowData.addAttribute(Attribute.IS_NULL);
        rootRow.addChildRow(rowData);
        //rootRow.setChildRows(childRows);


        /**
         * Create a couple "Date/Time" rows.
         */
        rowData = new RowData();
        attribute = new Attribute("startTime", Type.DATE_TIME);
        rowData.addAttribute(attribute);
        rowData.setAttributeOperator(">=");
        rowData.setAttributeValue(new GregorianCalendar(2011, 0, 1).getTime());
        //childRows.add(rowData);
        rootRow.addChildRow(rowData);

        rowData = new RowData();
        attribute = new Attribute("endTime", Type.DATE_TIME);
        rowData.addAttribute(attribute);
        rowData.setAttributeOperator("<=");
        rowData.setAttributeValue(new Date());
        rootRow.addChildRow(rowData);

        //rootRow.setChildRows(childRows);

        /**
         * Create a "My Property" row.
         */
        rowData = new RowData();
        attribute = new Attribute("nextEpoch", Type.REFERENCE,
                                  epochCD, Cardinality.TO_ONE);
        rowData.addAttribute(attribute);
        attribute = new Attribute("properties", "Property",
                                  Type.PER_USER_PARAMETERS_MAP,
                                  null, Cardinality.TO_MANY, true);
        rowData.addAttribute(attribute);

        rowData.setPropName("animalID");
        rowData.setPropType(Type.INT_32);
        rowData.setAttributeOperator("<=");
        rowData.setAttributeValue("123");

        //childRows.add(rowData);
        rootRow.addChildRow(rowData);
        //rootRow.setChildRows(childRows);

        /**
         * Create a "Parameters Map" row of type int.
         */
        rowData = new RowData();
        attribute = new Attribute("protocolParameters", Type.PARAMETERS_MAP,
                                  null, Cardinality.N_A);
        rowData.addAttribute(attribute);

        rowData.setPropName("stimulusFrequency");
        rowData.setPropType(Type.INT_32);
        rowData.setAttributeOperator("==");
        rowData.setAttributeValue("27");

        //childRows.add(rowData);
        rootRow.addChildRow(rowData);
        //rootRow.setChildRows(childRows);

        /**
         * Create a "Parameters Map" row of type string.
         */
        rowData = new RowData();
        attribute = new Attribute("protocolParameters", Type.PARAMETERS_MAP,
                                  null, Cardinality.N_A);
        rowData.addAttribute(attribute);

        rowData.setPropName("stimulusName");
        rowData.setPropType(Type.UTF_8_STRING);
        rowData.setAttributeOperator("~~=");
        rowData.setAttributeValue("caffeine");

        rootRow.addChildRow(rowData);
        //childRows.add(rowData);
        //rootRow.setChildRows(childRows);

        /**
         * Create a "Per User" derivedResponse row.
         */
        rowData = new RowData();
        attribute = new Attribute("derivedResponses", null, Type.PER_USER,
                                  derivedResponseCD, Cardinality.TO_MANY, true);
        rowData.addAttribute(attribute);

        rowData.setCollectionOperator(CollectionOperator.ALL);

        //childRows.add(rowData);
        rootRow.addChildRow(rowData);
        //rootRow.setChildRows(childRows);

        /**
         * Create a row that ends with a string value.
         */
        rowData = new RowData();
        attribute = new Attribute("epochGroup", Type.REFERENCE,
                                  epochGroupCD, Cardinality.TO_ONE);
        rowData.addAttribute(attribute);
        attribute = new Attribute("source", Type.REFERENCE,
                                  sourceCD, Cardinality.TO_ONE);
        rowData.addAttribute(attribute);
        attribute = new Attribute("label", Type.UTF_8_STRING);
        rowData.addAttribute(attribute);

        rowData.setAttributeOperator("==");
        rowData.setAttributeValue("Test 27");

        //childRows.add(rowData);
        rootRow.addChildRow(rowData);
        //rootRow.setChildRows(childRows);

        /**
         * Create another child row.
         */
        rowData = new RowData();
        attribute = new Attribute("resources", Type.REFERENCE,
                                  resourceCD, Cardinality.TO_MANY);
        rowData.addAttribute(attribute);
        rowData.setCollectionOperator(CollectionOperator.NONE);

        //childRows.add(rowData);
        rootRow.addChildRow(rowData);
        //rootRow.setChildRows(childRows);

        /**
         * Create another child row.
         */
        rowData = new RowData();
        rowData.setCollectionOperator(CollectionOperator.ALL);

        attribute = new Attribute("epochGroup", Type.REFERENCE,
                                  epochGroupCD, Cardinality.TO_ONE);
        rowData.addAttribute(attribute);
        attribute = new Attribute("epochs", Type.REFERENCE,
                                  epochCD, Cardinality.TO_MANY);
        rowData.addAttribute(attribute);

        //childRows.add(rowData);
        rootRow.addChildRow(rowData);
        //rootRow.setChildRows(childRows);

        RowData rowData2 = new RowData();
        attribute = new Attribute("startTime", Type.DATE_TIME);
        rowData2.addAttribute(attribute);

        rowData2.setAttributeOperator(">=");
        rowData2.setAttributeValue(new GregorianCalendar(2010, 0, 1).getTime());

        //ArrayList<RowData> childRows2 = new ArrayList<RowData>();
        //childRows2.add(rowData2);
        //rowData.setChildRows(childRows2);
        rowData.addChildRow(rowData2);

        System.out.println("rootRow:\n"+rootRow.toString());

        return(rootRow);
    }


    /**
     * This method returns true if the current value of this RowData
     * expression tree is valid.  I.e. this RowData object and all
     * its children are valid.
     *
     * A few notes on what is valid and not valid:
     *
     *      A tree that has no rows in it, not even a root row,
     *      is considered not valid.  (This case should never actually
     *      occur in the present GUI.)
     *
     *      A root row that has no children is considered not valid.
     *      For example, the user has selected Epoch as the Class
     *      Under Qualification, but has not set any children for it.
     *
     *      A compound row that has no children is considered not valid.
     *      For example, the user has created a compound row as a
     *      child of another row, and has selected Any/All/None
     *      as the collection operator, but has not created any child
     *      rows that are in that collection.
     *
     * TODO: Do we want to change what is considerd valid?
     * We probably want to make this code more clever at catching
     * settings that don't make sense.
     */
    public boolean containsLegalValue() {

        if (getRootRow() == null)
            return(true);

        /**
         * First check that the values in this row are valid.
         */

        /**
         * If we are a compound row, then we should have at least
         * one child row.
         */
        if ((isCompoundRow()) && (getChildRows().size() < 1))
            return(false);

        /**
         * Make sure all of the attributes on our attributePath
         * are legal.
         */ 
        if (attributePathIsLegal() == false)
            return(false);

        /**
         * If we get here, this row is legal.  Now recursively
         * check all of our descendent rows.
         * If any are illegal, immediately return false.
         */
        for (RowData childRow : getChildRows()) {
            if (childRow.containsLegalValue() == false)
                return(false);
        }

        return(true);
    }


    /**
     * This returns true if the attributePath is legal.
     * Please note, as of October 2011, all we are doing
     * is making sure that there are no "Select Attribute"
     * attributes in the path.
     */
    private boolean attributePathIsLegal() {

        for (Attribute attribute : getAttributePath()) {
            if (attribute.equals(Attribute.SELECT_ATTRIBUTE))
                return(false);
        }

        return(true);
    }


    /**
     * This is a simple test program for this class.
     */
    public static void main(String[] args) {

        System.out.println("RowData test is starting...");

        RowData rootRow = createTestRowData();
        System.out.println(rootRow);

        System.out.println("RowData test is ending.");
    }
}
