package com.physion.ovation.gui.ebuilder.datatypes;


/**
 * 
 * TODO: Write equals() method.
 */
public class Attribute {

    /**
     * TODO: I'm not happy with the fact that the Attribute class
     * is uased to hold "special" values that aren't really attributes,
     * such as SELECT_ATTRIBUTE, IS_NULL, MY_PROPERTY.
     * But, I don't see a cleaner way that doesn't create more code.
     */
    public static final Attribute SELECT_ATTRIBUTE =
        new Attribute("Select Attribute", Type.REFERENCE);
    public static final Attribute IS_NULL =
        new Attribute("is null", Type.REFERENCE);
    public static final Attribute IS_NOT_NULL =
        new Attribute("is not null", Type.REFERENCE);
    public static final Attribute MY_PROPERTY =
        new Attribute("My Property", Type.REFERENCE);
    public static final Attribute ANY_PROPERTY =
        new Attribute("Any Property", Type.REFERENCE);

    /**
     * The name of this attribute.  E.g. "owner", "uuid", "keywords".
     */
    private String name;

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


    /**
     * @param classDescription - If 
     */
    public Attribute(String name, Type type, ClassDescription classDescription,
                     Cardinality cardinality) {
        this.name = name;
        this.type = type;
        this.classDescription = classDescription;
        this.cardinality = cardinality;
    }


    /**
     * Use this constuctor to create a "primitive" Attribute such as
     * boolean, int, float, string, time/date.
     */
    public Attribute(String name, Type type) {
        this(name, type, null, Cardinality.N_A);
    }


    /*
    public Attribute(Attribute attribute) {
        this.name = attribute.name;
        this.type = attribute.type;
        this.classDescription = attribute.classDescription;
        this.cardinality = attribute.cardinality;
    }
    */


    /**
     * TODO:  Finish this.
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

        if (this.name != other.name)
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


    public String getName() {
        return(name);
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
            this.equals(Attribute.IS_NOT_NULL) ||
            this.equals(Attribute.MY_PROPERTY) ||
            this.equals(Attribute.ANY_PROPERTY)) {
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
            this.equals(Attribute.IS_NOT_NULL) ||
            this.equals(Attribute.MY_PROPERTY) ||
            this.equals(Attribute.ANY_PROPERTY)) {
            return(true);
        }
        return(false);
    }


    public String toString() {
        return(name);
    }


    public String toStringDebug() {

        String string;

        string = name+" "+type;

        if (type == Type.REFERENCE) {
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
    public static void main(String[] args) {

        System.out.println("Attribute test is starting...");

        System.out.println("Attribute test is ending.");
    }
}
