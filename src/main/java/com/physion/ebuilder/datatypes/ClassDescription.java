/**
 * Copyright (c) 2011. Physion Consulting LLC
 * All rights reserved.
 */
package com.physion.ebuilder.datatypes;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * This is the description of a class.  The DataModel object creates
 * all of these for the system.
 */
public class ClassDescription
    implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
     * The name of this class.  E.g. "Epoch", "Response".
     */
    private String name = "ERROR: name not set.";

    /**
     * The list of Attributes this class has.
     */
    private ArrayList<Attribute> attributes = new ArrayList<Attribute>();

    /**
     * The parent class of this class, if it has one.
     * Please note that the EntityBase class does not have a parent class.
     */
    private ClassDescription parentClass = null;


    /**
     * Create a ClassDescription with no attributes.
     * You will probably need to call addAttribute to add some attributes.
     */
    public ClassDescription(String name, ClassDescription parentClass) {
        this.name = name;
        this.parentClass = parentClass;
    }


    /**
     * Override the equals() method so we can do some extra checking.
     */
    @Override
    public boolean equals(Object rhs) {

        if (rhs == null)
            return(false);

        if (!(rhs instanceof ClassDescription))
            return(false);

        ClassDescription other = (ClassDescription)rhs;

        if (this == rhs)
            return(true);

        if (!this.name.equals(other.name))
            return(false);

        if ((this.parentClass == null) && (other.parentClass != null))
            return(false);
        if ((this.parentClass != null) && (other.parentClass == null))
            return(false);
        if ((this.parentClass != null) && (other.parentClass != null))
            if (!this.parentClass.equals(other.parentClass))
                return(false);

        if (this.attributes.size() != other.attributes.size())
            return(false);
        for (int index = 0; index < this.attributes.size(); index++) {
            if (!this.attributes.get(index).equals(
                other.attributes.get(index))) {
                return(false);
            }
        }

        return(true);
    }


    /**
     * Get the name of this class.  E.g. "Epoch", "Response".
     */
    public String getName() {
        return(name);
    }


    /**
     * Set the parent class of this class.
     * As of November 2011, only the EntityBase class does
     * not have a parent class.
     */
    public void setParentClass(ClassDescription parentClass) {
        this.parentClass = parentClass;
    }


    /**
     * Get the parent class of this class.  Possibly null.
     * As of November 2011, only the EntityBase class does
     * not have a parent class.
     */
    public ClassDescription getParentClass() {
        return(parentClass);
    }


    /*
    public boolean isInstanceof(ClassDescription classDescription) {

        if (classDescription == null) {
            return(false);
        }

        while (classDescription != null) {
            if (this.equals(classDescription))
                return(true);
            classDescription = classDescription.getParentClass();
        }

        return(false);
    }
    */


    /**
     * Add an Attribute to this class's list of attributes.
     *
     * Note, you do not need to add our parent class's attributes
     * explicitly.  We get those from our parent.
     */
    public void addAttribute(Attribute attribute) {
        attributes.add(attribute);
    }


    /**
     * Returns the list of ALL the attributes that this class has.
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


    /**
     * Please make sure you want this method and not the
     * method getAllAttributes().
     *
     * Please note, this method returns a reference to this
     * ClassDescription's internal list, so don't mess with it.
     */
    /*
    public ArrayList<Attribute> getAttributeList() {
        return(attributes);
    }
    */


    /**
     * Returns true if this class, or any of its ancestors,
     * contains the passed in attribute.
     */
    public boolean containsAttribute(Attribute attribute) {

        for (Attribute att : getAllAttributes()) {
            if (att.equals(attribute))
                return(true);
        }

        return(false);
    }


    /**
     * Get a COPY of the Attribute with the passed in queryName
     * if such an attribute exists in this ClassDescription.
     * Returns null if it doesn't.
     *
     * Please note, we are checking this ClassDescription's
     * "direct" Attributes and all our parent class Attributes.
     *
     * @param queryName A string like:  "incomplete", "protocolID",
     * "properties".  Not a displayName like "My Property".
     *
     * @return The Attribute, if it exists, that has the passed
     * in queryName.  Please note, we return a COPY of this ClassDescription's
     * Attribute, so you are free to do with it what you want.
     * Returns null if the Attribute does not exist in the passed
     * in ClassDescription.
     */
    public Attribute getAttribute(String queryName) {

        for (Attribute att : getAllAttributes()) {
            if (att.getQueryName().equals(queryName))
                return(new Attribute(att));
        }

        return(null);
    }


    /**
     * This returns the name of this Attribute.  This string is
     * what is displayed in the GUI.
     */
    public String toString() {
        return(name);
    }


    /**
     * Get a String version of this class useful for testing/debugging.
     */
    public String toStringDebug() {
        return(toStringDebug(""));
    }


    /**
     * Get a string version of this class useful for debugging and
     * indent the string using the passed in indent string.
     * We do this to show a nested hierarchy of a class and its
     * child classes and their attributes.
     */
    public String toStringDebug(String indent) {

        String string;

        string = indent+name;

        for (Attribute attribute : attributes) {
            string += "\n"+indent;
            string += " "+attribute;
        }

        if (parentClass != null) {
            string += "\n"+indent+" Parent Class:\n";
            string += parentClass.toStringDebug(indent+"  ");
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
        System.out.println("\nuserCD.toString() below:\n"+
            userCD.toStringDebug());
        System.out.println("\nkeywordTagCD.toString() below:\n"+
            keywordTagCD.toStringDebug());
        //System.out.println("\ntaggableEntityBaseCD.toString() below:\n"+
        //                   taggableEntityBaseCD);
        System.out.println("ClassDescription test is ending.");
    }
}
