package com.physion.ovation.gui.ebuilder.datatypes;


/**
 * 
 */
public class Attribute {

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
     * we reference.
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


    public String toString() {

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
