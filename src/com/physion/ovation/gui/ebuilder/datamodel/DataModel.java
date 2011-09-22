package com.physion.ovation.gui.ebuilder.datamodel;

import java.util.ArrayList;

import com.physion.ovation.gui.ebuilder.datatypes.ClassDescription;
import com.physion.ovation.gui.ebuilder.datatypes.Attribute;
import com.physion.ovation.gui.ebuilder.datatypes.Type;
import com.physion.ovation.gui.ebuilder.datatypes.Cardinality;


/**
 * This class manages all the data types/classes that are part
 * of the system.
 *
 * TODO:  Have this class read values from a configuration file.
 */
public class DataModel {

    /**
     * Please note, we are using Attribute.IS_NULL and
     * IS_NOT_NULL for the OPERATOR_IS_NULL
     * and OPERATOR_IS_NOT_NULL operators.
     */
    public static final String OPERATOR_TRUE = "is true";
    public static final String OPERATOR_FALSE = "is false";
    public static final String[] OPERATORS_BOOLEAN = {OPERATOR_TRUE,
                                                       OPERATOR_FALSE};
    public static final String[] OPERATORS_ARITHMATIC = {"==", "!=", "<", "<=",
        ">", ">="};
    public static final String[] OPERATORS_STRING = {"==", "!=", "<", "<=",
        ">", ">=", "~=", "~~="};
    public static final String OPERATOR_IS_NULL = Attribute.IS_NULL.getName();
    public static final String OPERATOR_IS_NOT_NULL =
        Attribute.IS_NOT_NULL.getName();

    /**
     * TODO: What are the possible types?
     */
    public static final String PROP_TYPE_INT = "int";
    public static final String PROP_TYPE_FLOAT = "float";
    public static final String PROP_TYPE_STRING = "string";
    public static final String PROP_TYPE_TIME = "time";
    public static final String PROP_TYPE_BOOLEAN = "boolean";
    public static final String[] PROP_TYPES = {PROP_TYPE_INT,
                                               PROP_TYPE_FLOAT,
                                               PROP_TYPE_STRING,
                                               PROP_TYPE_TIME,
                                               PROP_TYPE_BOOLEAN};

    /**
     * The one an only instance of this DataModel.
     * We are a singleton.
     */
    private static DataModel instance;

    /**
     * These are all the ClassDescriptions that exists in the system.
     */
    private static ArrayList<ClassDescription> allClassDescriptions;

    /**
     * These are the subset of all ClassDescriptions that are possible
     * choices in the very first/topmost row.  I.e. these are possible
     * Class Under Qualification choices.
     */
    private static ArrayList<ClassDescription> possibleCUQs;


    public static DataModel getInstance() {

        if (instance == null) {
            instance = new DataModel();
        }
        return(instance);
    }


    private DataModel() {

        allClassDescriptions = new ArrayList<ClassDescription>();
        possibleCUQs = new ArrayList<ClassDescription>();

        initialize();
    }



    /**
     * This method creates hard coded values at the moment.
     * Eventually, these values will come from a configuration file.
     */
    private static void initialize() {

        ClassDescription entityBaseCD =
            new ClassDescription("EntityBase", null);
        allClassDescriptions.add(entityBaseCD);

        ClassDescription taggableEntityBaseCD =
            new ClassDescription("TaggableEntityBase", entityBaseCD);
        allClassDescriptions.add(taggableEntityBaseCD);

        ClassDescription timelineElementCD =
            new ClassDescription("TimelineElement", taggableEntityBaseCD);
        allClassDescriptions.add(timelineElementCD);

        ClassDescription epochCD =
            new ClassDescription("Epoch", timelineElementCD);
        allClassDescriptions.add(epochCD);
        possibleCUQs.add(epochCD);

        ClassDescription sourceCD =
            new ClassDescription("Source", taggableEntityBaseCD);
        allClassDescriptions.add(sourceCD);
        possibleCUQs.add(sourceCD);

        ClassDescription userCD =
            new ClassDescription("User", taggableEntityBaseCD);
        allClassDescriptions.add(userCD);

        ClassDescription keywordTagCD =
            new ClassDescription("KeywordTag", entityBaseCD);
        allClassDescriptions.add(keywordTagCD);

        ClassDescription epochGroupCD =
            new ClassDescription("EpochGroup", timelineElementCD);
        allClassDescriptions.add(epochGroupCD);
        possibleCUQs.add(epochGroupCD);

        ClassDescription resourceCD =
            new ClassDescription("Resource", taggableEntityBaseCD);
        allClassDescriptions.add(resourceCD);

        ClassDescription iOBaseCD =
            new ClassDescription("IOBase", taggableEntityBaseCD);
        allClassDescriptions.add(iOBaseCD);

        ClassDescription responseBaseCD =
            new ClassDescription("ResponseBase", iOBaseCD);
        allClassDescriptions.add(responseBaseCD);

        ClassDescription derivedResponseCD =
            new ClassDescription("DerivedResponse", responseBaseCD);
        allClassDescriptions.add(derivedResponseCD);

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

        attribute = new Attribute("resources", Type.REFERENCE,
                                  resourceCD, Cardinality.TO_MANY);
        entityBaseCD.addAttribute(attribute);

        /**
         * Initialize values of the Epoch class.
         */
        attribute = new Attribute("protocolID", Type.UTF_8_STRING);
        epochCD.addAttribute(attribute);

        attribute = new Attribute("protocolParameters", Type.PARAMETERS_MAP,
                                  null, Cardinality.N_A);
        epochCD.addAttribute(attribute);

        attribute = new Attribute("excludeFromAnalysis", Type.BOOLEAN);
        epochCD.addAttribute(attribute);

        attribute = new Attribute("nextEpoch", Type.REFERENCE,
                                  epochCD, Cardinality.TO_ONE);
        epochCD.addAttribute(attribute);

        attribute = new Attribute("derivedResponses", Type.PER_USER,
                                  derivedResponseCD, Cardinality.TO_MANY);
        epochCD.addAttribute(attribute);

        attribute = new Attribute("epochGroup", Type.REFERENCE,
                                  epochGroupCD, Cardinality.TO_ONE);
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
        attribute = new Attribute("source", Type.REFERENCE,
                                  sourceCD, Cardinality.TO_ONE);
        epochGroupCD.addAttribute(attribute);

        attribute = new Attribute("epochs", Type.REFERENCE,
                                  epochCD, Cardinality.TO_MANY);
        epochGroupCD.addAttribute(attribute);

        attribute = new Attribute("label", Type.UTF_8_STRING);
        epochGroupCD.addAttribute(attribute);

        /**
         * Initialize values of the TimelineElement class.
         */
        attribute = new Attribute("startTime", Type.DATE_TIME);
        timelineElementCD.addAttribute(attribute);

        attribute = new Attribute("endTime", Type.DATE_TIME);
        timelineElementCD.addAttribute(attribute);

        attribute = new Attribute("startTimeZone", Type.UTF_8_STRING);
        timelineElementCD.addAttribute(attribute);

        attribute = new Attribute("endTimeZone", Type.UTF_8_STRING);
        timelineElementCD.addAttribute(attribute);

        /**
         * Initialize values of the Resource class.
         */
        attribute = new Attribute("uti", Type.UTF_8_STRING);
        resourceCD.addAttribute(attribute);

        attribute = new Attribute("notes", Type.UTF_8_STRING);
        resourceCD.addAttribute(attribute);

        attribute = new Attribute("name", Type.UTF_8_STRING);
        resourceCD.addAttribute(attribute);
    }


    /**
     * Get a ClassDescription object using its name member data.
     *
     * @param name - The name of the ClassDescription.  For example,
     * "Epoch", "Source", "TaggableEntityBase".
     */
    public static ClassDescription getClassDescription(String name) {

        /**
         * Be sure we are initialized.
         */
        getInstance();

        for (ClassDescription classDescription : allClassDescriptions)
            if (classDescription.getName().equals(name))
                return(classDescription);

        System.out.println("ERROR:  In getClassDescription().  "+
            "Caller asked for unknown class with name \""+name+"\".");
        return(null);
    }


    public static ArrayList<ClassDescription> getAllClassDescriptions() {
        return(allClassDescriptions);
    }


    public static ArrayList<ClassDescription> getPossibleCUQs() {
        return(possibleCUQs);
    }

    
    /**
     * This is a simple test program for this class.
     */
    public static void main(String[] args) {

        System.out.println("DataModel test is starting...");

        DataModel dataModel = DataModel.getInstance();

        System.out.println("DataModel test is ending.");
    }
}
