package com.physion.ovation.gui.ebuilder.datatypes;

import java.util.ArrayList;

/**
 * 
 */
public class ClassDescription {

    /**
     */
    private String name = "ERROR: name not set.";
    private ArrayList<Attribute> attributes = new ArrayList<Attribute>();
    private ClassDescription parentClass = null;


    /**
     * Create a ClassDescription with no attributes.
     * You will probably need to call addAttribute to add some attributes.
     */
    public ClassDescription(String name, ClassDescription parentClass) {
        this.name = name;
        this.parentClass = parentClass;
    }


    public String getName() {
        return(name);
    }


    public void setParentClass(ClassDescription parentClass) {
        this.parentClass = parentClass;
    }


    public void addAttribute(Attribute attribute) {
        attributes.add(attribute);
    }


    /**
     * Returns the list of all the attributes that this class has.
     * I.e. it returns this class's direct attributes, plus all the
     * attributes of its ancestor classes.
     */
    public ArrayList<Attribute> getAllAttributes() {

        ArrayList<Attribute> allAttributes = new ArrayList<Attribute>();

        /**
         * First add our attributes to the list we will return.
         */
        allAttributes.addAll(attributes);

        /**
         * Now recursively add our parent attributes.
         */
        if (parentClass != null)
            allAttributes.addAll(parentClass.getAllAttributes());

        return(allAttributes);
    }


    public String toString() {
        return(toString(""));
    }


    public String toString(String indent) {

        String string;

        string = indent+name;

        for (Attribute attribute : attributes) {
            string += "\n"+indent;
            string += " "+attribute;
        }

        if (parentClass != null) {
            string += "\n"+indent+" Parent Class:\n";
            string += parentClass.toString(indent+"  ");
        }

        return(string);
    }


    /**
     * This is a simple test program for this class.
     */
    public static void main(String[] args) {

        System.out.println("ClassDescription test is starting...");

        /**
         * Create a ClassDescription for all the classes we will
         * need.  Please note, when we first create them, they
         * aren't valid because we might not have not set the parent class
         * or attributes.
         */
        ClassDescription entityBaseCD =
            new ClassDescription("EntityBase", null);
        ClassDescription taggableEntityBaseCD =
            new ClassDescription("TaggableEntityBase", entityBaseCD);
        ClassDescription userCD =
            new ClassDescription("User", taggableEntityBaseCD);
        ClassDescription keywordTagCD =
            new ClassDescription("KeywordTag", entityBaseCD);

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
         * Initialize values of the TaggableEntityBase class.
         */
        attribute = new Attribute("keywords", Type.REFERENCE,
                                  keywordTagCD, Cardinality.TO_MANY);
        taggableEntityBaseCD.addAttribute(attribute);

        /**
         * Initialize values of the User class.
         */
        attribute = new Attribute("userName", Type.UTF_8_STRING);
        userCD.addAttribute(attribute);

        /**
         * Initialize values of the KeywordTag class.
         */
        attribute = new Attribute("tag", Type.UTF_8_STRING);
        keywordTagCD.addAttribute(attribute);

        /**
         * Print out values.
         */
        //System.out.println("\nentityBaseCD.toString() below:\n"+entityBaseCD);
        System.out.println("\nuserCD.toString() below:\n"+userCD);
        System.out.println("\nkeywordTagCD.toString() below:\n"+keywordTagCD);
        //System.out.println("\ntaggableEntityBaseCD.toString() below:\n"+
        //                   taggableEntityBaseCD);
        System.out.println("ClassDescription test is ending.");
    }
}