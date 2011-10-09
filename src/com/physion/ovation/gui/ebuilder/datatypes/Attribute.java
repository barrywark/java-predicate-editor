package com.physion.ovation.gui.ebuilder.datatypes;


/**
 * This class describes an attribute of a class.
 * Normally an attribute of a class is a "primitive"
 * such as "owner = 'bwark'", or a "reference" to
 * a class or a collection of classes.  (There are
 * other, more complicated attributes also.)
 *
 * The list of attributes are displayed in a comboBox
 * that lets the user select a particular attribute.
 * An attribute comboBox shows the value
 * "Select Attribute" when it is first displayed.
 * In addition, it often has other special values
 * the user can select such as "is null" or
 * "is not null".  In order to make the code more
 * simple, I have made the Attribute class capable
 * of holding these "special" values also.  This way
 * can always treat the selected value in an attribute
 * comboBox as an instance of an Attribute object.
 * This makes the code a bit more simple, but does
 * make the Attribute class a bit less "clean".
 */
public class Attribute {

    /**
     * These are special values that appear in comboBoxes that
     * display a list of attributes.  They are not really attributes
     * though.  See notes at the start of this class.
     *
     * I have used the Operator.IS_NULL.toString() and
     * Operator.IS_NOT_NULL.toString() methods to get
     * the "is null" and "is not null" strings.  Doing it this
     * way, as opposed to simply putting in the string makes
     * sure these strings used in an attribute comboBox and
     * an operator comboBox are consistent.  But, it really
     * doesn't matter to the code if they are consistent, so
     * change it if you think it is unweildy.
     */
    public static final Attribute SELECT_ATTRIBUTE =
        new Attribute("Select Attribute", Type.REFERENCE);
    public static final Attribute IS_NULL =
        //new Attribute("is null", Type.REFERENCE);
        new Attribute(Operator.IS_NULL.toString(), Type.REFERENCE);
    public static final Attribute IS_NOT_NULL =
        //new Attribute("is not null", Type.REFERENCE);
        new Attribute(Operator.IS_NOT_NULL.toString(), Type.REFERENCE);


    /**
     * The name of this attribute used in queries.
     * E.g. "owner", "uuid", "keywords".
     *
     * This is the string that is used when creating an
     * attribute path that contains this Attribute.
     *
     * As of September 2011, the only time this string and the
     * displayName are different is in the case of the
     * EntityBase.properties attribute.  In that case the displayName
     * is "Property" and the queryName is "properties".
     */
    private String queryName;

    /**
     * The "display" name of this attribute.  I.e. the string that
     * will be displayed in a comboBox dropdown list.
     *
     * As of September 2011, the only time this string and the
     * queryName are different is in the case of the
     * EntityBase.properties attribute.
     *
     * Because the displayName is almost always the same as the
     * queryName, we only set the displayName to a non-null value if it
     * is different from the queryName.  The getDisplayName() method
     * will return the value stored in our queryName member data if
     * displayName is null.
     */
    private String displayName;

    /**
     * This value only matters to Attributes of Type.PER_USER and
     * Type.PER_USER_PARAMETERS_MAP.  In addition, it only matters
     * when this Attribute is being used to generate a label to
     * be used in a GUI or when creating a query string.  It is NOT
     * used when setting up the list of Attribute objects in a
     * ClassDescription.
     *
     * If this flag is true, this Attribute is used to create a
     * query string that is looking for records
     * associated with attributes the current user created.
     * E.g. "my" keywords.  The attribute is prefaced with "My"
     * when it is displayed to the user.  If this flag is false,
     * then the attribute is prefaced with "Any" or "All" when it
     * is displayed.
     */
    private boolean isMine;

    /**
     * The type of this attribute.  E.g. BOOLEAN, INT, REFERENCE.
     */
    private Type type;

    /**
     * If our type is "REFERENCE", then this is the class that
     * we reference.  Otherwise, this is null.
     */
    private ClassDescription classDescription;

    /**
     * The cardinality of the relationship between this attribute
     * and the class that "contains" this attribute.
     * This does not apply if this attribute is a "primitive" type
     * such as boolean, int, float, string, time/date.
     */
    private Cardinality cardinality;


    public Attribute(Attribute other) {
        this(other.queryName, other.displayName, other.type,
             other.classDescription, other.cardinality, other.isMine);
    }


    /**
     * The base constructor that sets all the member data values
     * to the passed in values.
     */
    public Attribute(String queryName, String displayName, Type type,
                     ClassDescription classDescription,
                     Cardinality cardinality, boolean isMine) {
        this.queryName = queryName;
        this.displayName = displayName;
        this.type = type;
        this.classDescription = classDescription;
        this.cardinality = cardinality;
        this.isMine = isMine;

        if ((this.type == Type.PARAMETERS_MAP) &&
            (this.cardinality != Cardinality.N_A)) {

            System.err.println("WARNING:  type is PARAMETERS_MAP but "+
                               "cardinality is not N_A.  "+
                               "The code MIGHT need to be updated to handle "+
                               "that.  Attribute = "+this);
        }
    }


    /**
     * A constructor with some values defaulted:
     * 
     *      displayName = null
     *      isMine = false;
     *
     * Use this constructor to create most Attributes that are
     * a reference to a class.
     */
    public Attribute(String queryName, Type type,
                     ClassDescription classDescription,
                     Cardinality cardinality) {
        this(queryName, null, type, classDescription, cardinality, false);
    }


    /**
     * A constructor with some values defaulted:
     *
     *      displayName = null
     *      classDescription = null
     *      cardinality = Cardinality.N_A
     *      isMine = false;
     *
     * Use this constuctor to create a "primitive" Attribute such as
     * boolean, int, float, string, time/date.
     */
    public Attribute(String queryName, Type type) {
        this(queryName, null, type, null, Cardinality.N_A, false);
    }


    /**
     * Returns true if this Attribute is equivalent to
     * the passed in Attribute.
     */
    @Override
    public boolean equals(Object rhs) {

        if (rhs == null)
            return(false);

        if (!(rhs instanceof Attribute))
            return(false);

        Attribute other = (Attribute)rhs;

        if (this == rhs)
            return(true);

        if (!this.queryName.equals(other.queryName))
            return(false);

        /**
         * The displayName is not relevant for determining whether
         * two Attribute objects are equivalent.
         */
        //if (!this.getDisplayName().equals(other.getDisplayName()))
        //    return(false);

        if (this.isMine != other.isMine)
            return(false);

        if (this.type != other.type)
            return(false);

        if (this.cardinality != other.cardinality)
            return(false);

        if ((this.classDescription == null) &&
            (other.classDescription != null))
            return(false);

        if ((this.classDescription != null) &&
            (!this.classDescription.equals(other.classDescription)))
            return(false);

        return(true);
    }


    /**
     * Set the "query" name of this attribute that should be used
     * in query strings.  E.g. "uuid", "owner", "properties".
     *
     * If you want to set the string that should be displayed in a
     * comboBox dropdown list, you should use the getDisplayName()
     * method.
     */
    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }


    /**
     * Get the "query" name of this attribute that should be used
     * in query strings.  E.g. "uuid", "owner", "properties".
     *
     * If you want the string that should be displayed in a comboBox
     * dropdown list, you should use the getDisplayName() method.
     */
    public String getQueryName() {
        return(queryName);
    }


    /**
     * If the displayName and queryName for this attribute are the
     * same strings, you don't need to call this method.
     * The getDisplayName() method will return the queryName value
     * in that case.
     *
     * But if the displayName and queryName are different values,
     * you need to call this method to set the displayName.
     * As of September 2011, the displayName and the queryName
     * are the same for all attributes except for the
     * EntityBase.properties attribute.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }


    /**
     * Get the "display" name of this attribute.  I.e. this
     * is the string that is displayed in a comboBox.
     *
     * As of September 2011, the displayName and the queryName
     * are the same for all attributes except for the
     * EntityBase.properties attribute.  So, we only bother
     * to set the displayName to a non-null value for that
     * one attribute.  In the case of the EntityBase.properties
     * attribute, the displayName is "Property".  In addition,
     * the string "My " or "Any " is prepended to the displayName
     * depending upon the isMine flag.
     *
     * @return The "display" name for this Attribute.  Please
     * note, this method never returns null even if our
     * displayName member data is null.
     */
    public String getDisplayName() {

        String string = displayName;
        if (string == null)
            string = getQueryName();

        /**
         * For most attributes, we are done at this point, but
         * for a few of them, we need to prepend My, Any, or All
         * to the name.  E.g. EntityBase.properties becomes
         * "My Property" or "Any Property".  Epoch.derivedResponses
         * becomes "My derivedResponses" or "All derivedResponses".
         */

        if (type == Type.PER_USER) {
            if (isMine)
                string = "My "+string;
            else
                string = "All "+string;
        }
        else if (type == Type.PER_USER_PARAMETERS_MAP) {
            if (isMine)
                string = "My "+string;
            else
                string = "Any "+string;
        }

        return(string);
    }


    /**
     * Get the string that is displayed in comboBox dropdown lists
     * when this Attribute object is in a list.
     * I.e. this is the method the Swing framework calls when it
     * turns a DefaultComboBoxModel of Attribute objects into strings
     * that are displayed to the user.
     */
    public String toString() {
        return(getDisplayName());
    }


    /**
     * This value only matters to Attributes of Type.PER_USER and
     * Type.PER_USER_PARAMETERS_MAP.
     */
    public void setIsMine(boolean isMine) {
        this.isMine = isMine;
    }


    public boolean getIsMine() {
        return(isMine);
    }


    public Type getType() {
        return(type);
    }


    public ClassDescription getClassDescription() {
        return(classDescription);
    }


    public Cardinality getCardinality() {
        return(cardinality);
    }


    /**
     * This returns true if this Attribute is a "primitive"
     * attribute such as an int, float, string, date etc.
     */
    public boolean isPrimitive() {

        if (this.equals(Attribute.SELECT_ATTRIBUTE) ||
            this.equals(Attribute.IS_NULL) ||
            this.equals(Attribute.IS_NOT_NULL)) {
            return(false);
        }

        return(type.isPrimitive());
    }


    /**
     * This returns true if this Attribute is a "special" attribute.
     * By "special" we mean it is not really an attribute, but is
     * instead a special value the GUI uses.
     */
    public boolean isSpecial() {

        if (this.equals(Attribute.SELECT_ATTRIBUTE) ||
            this.equals(Attribute.IS_NULL) ||
            this.equals(Attribute.IS_NOT_NULL)) {
            return(true);
        }
        return(false);
    }


    /**
     * Get a String version of this Attribute that is useful for
     * debugging.
     */
    public String toStringDebug() {

        String string;

        string = queryName;
        if (displayName != null)
            string += "("+displayName+")("+isMine+")";
        string += " "+type;

        if ((type == Type.REFERENCE) && !isSpecial()) {
            if (classDescription == null)
                string += "ERROR: classDescription == null";
            else
                string += "<"+classDescription.getName()+">";
        }

        if (cardinality != Cardinality.N_A)
            string += " "+cardinality;

        return(string);
    }


    /**
     * This is a simple test program for this class.
     */
    /*
    public static void main(String[] args) {

        System.out.println("Attribute test is starting...");
        System.out.println("Attribute test is ending.");
    }
    */
}
