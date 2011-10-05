package com.physion.ovation.gui.ebuilder.expression;

public interface ExpressionFactory {
    Expression fromPQLString(String pql);
}

/*
The class ooExpression is the base class for all three types of expression nodes in the expression tree:
ooOperatorExpression: represents an operator node. ooAttributeValueExpression: represents an attribute value node of either a basic type member (boolean, int, unsigned int, float, string, date, time, datatime, interval), a reference or an embedded member, or a member of query-collection type (fixed or variable array, fixed array of variable arrays, to-many relationship, or persistent collection).
ooLiteralValueExpression: base class for all literal value expression classes to represent all supported types of literal value nodes.
o ooBoolLiteralValueExpression: represents a Boolean literal value node. o ooStringLiteralValueExpression: represents a string literal value node. o ooIntLiteralValueExpression: represents an integer literal value node. o ooUIntLiteralValueExpression: represents an unsigned integer literal
value node.
o ooFloatLiteralValueExpression represents a float literal value node.
o ooDateLiteralValueExpression represents a date literal value node.
o ooTimeLiteralValueExpression represents a time literal value node.
o ooDateTimeLiteralValueExpression represents a datetime literal value
node.
￼￼￼￼5
o ooIntervalLiteralValueExpression represents an interval literal value node.
o oocOrderedListLiteralValueExpression represents an ordered-list literal value node.
o ooClassTypeLiteralValueExpression represents a class-type literal value node.
All expression types are captured by the enumeration ooExpressionType. The virtual function ooExpression::getExpressionType() lets you get an ooExpression node's type.

An expression tree is made of expression nodes in such a way that:
The head expression node's result type is Boolean.
Intermediate level nodes (non leaves) are ooOperatorExpression nodes, which can have a number of other expression nodes as their operands.
Leaf nodes are value expression nodes of type ooAttributeValueExpression or ooLiteralValueExpression.

*/

public interface Expression {
    String toPQLString();
    
    ExpressionResultSpec getExpressionResultSpec();
    
    void accept(ExpressionVisitor visitor);
}


/*
An ooOperatorExpression object can be constructed with or without a specified token name. If constructed with a token name, as is typically done by parsers, the actual operator used by the operator expression is selected later during the setup process. The specified token name can be retrieved with the getTokenName() method. If not constructed with a token name, the operator must be specified using the function setOperator(const ooOperator*). In either case, the selected operator can be obtained using getOperator().
*/
public interface OperatorExpression extends Expression {
    String getTokenName();
    List<Expression> getOperandList();
}


/*
ooLiteralValueExpression is an abstract base class for all literal value expression classes. Use the constructors of its derived classes to create literal value instances of particular types. When creating a particular type of literal value object, the literal value should be provided to the constructor, and it can be reset by setValue() or retrieved by getValue().
*/
public interface LiteralValueExpression extends Expression {
    Object getValue();
}

/*
An ooAttributeValueExpression object represents the value of an attribute of the qualification object or of a related object. An ooAttributeValueExpression object can be used to represent an attribute value of:
A basic type, such as the name or age for an instance of the employee class.
A reference or embedded object type, such as the employer for an instance of the employee class.
An attribute value of a query collection type, such as the children of an instance of the employee class.
The ooAttributeValueExpression constructor takes the attribute name as an argument. In case the same attribute name is defined in both the base class and the class being qualified (or is referenced from the class being qualified), the following syntax must be used in order to express the attribute of the base class: baseClassName::AttributeName.
The attribute name can be reset or retrieved by the setAttributeName() and getAttributeName() methods.
*/

public interface AttributeExpression extends Expression {
    String getAttributeName();
}

public interface LiteralValueExpressionFactory extends ExpressionFactory {
    LiteralValueExpression expressionForValue(Object value); //constructs appropriate subclass for value's type (bool, string, int, UInt, Float, Date, Time, DateTime, Interval, OrderedList, Class)
}

/*
The visitor pattern1 is used to provide the extensibility for adding new operations on the ooExpression tree structure. The visitor pattern allows you to define a new operation to be performed on elements in a class structure without changing and recompiling the classes. The class ooExpressionVisitor is the base class for user-defined expression visitor classes that add operations to be performed on the expression nodes. In order for the expression classes to perform the user-added operation, the accept(ooExpressionVisitor* visitor) method, which takes a visitor (a concrete ooExpressionVisitor instance) as an argument, is provided on all expression node classes.
To add and perform a new operation on all nodes in the expression tree structure:
Define a concrete expression-visitor class derived from ooExpressionVisitor (called specificExpressionVisitor from this point on), and implement the new operation on each type of expression by implementing all the virtual visit methods defined in ooExpressionVisitor:
o visitOperator()
o visitAtributeValue()
o visitIntLiteralValue()
o visitUIntLiteralValue()
o visitFloatLiteralValue()
o visitStringLiteralValue()
o visitBoolLiteralValue(), and so forth.
The visitOperator() method returns a boolean type to indicate whether or not the operation needs to be populated to its operands (to call the accept() method of its operands from the accept() method of the operator expression).
Create an instance of the specificExpressonVisitor (called specificVisitorPtr from this point on).
1 Erich Gamma, Richard Helm, Ralph Johnson, John M. Vlissides, Design Patterns, Elements of Reusable Object-Oriented Software, Addison-Wesley Professional Computing Series
￼￼￼￼￼17
Perform the operation implemented by specificExpressionVisitor on the expression tree nodes by calling the function accept(specificVisitorPtr) from the head predicate expression, and passing in the reference to the specific visitor instance as an argument.
The operation will be performed on the head node first, then populated to all its operands as the operator expression's accept() method calls its operands' accept() method (if visitOperator() returns true). The whole tree will be traversed, and the new defined operation will be performed on each expression node automatically as it is visited.
*/

public interface ExpressionVisitor {
    boolean visitOperator(); //return true to indicate visitor needs to see operands
    void visitAttributeValue();
    void visitIntLiteralValue();
    void visitUIntLiteralValue();
    void visitFloatLiteralValue();
    void visitStringLiteralValue();
    void visitBoolLiteralValue();
    ...
}


/*
ooExpressionResultSpec represents the result type of an ooExpression node in an expression tree. ooExpressionResultSpec has one member for the data type and one member for the element type. Note that the element type is only valid if the data type is a query collection (oocQCollection in ooQDataType).

ooExpressionResultSpec is also used to represent valid operand types of an operator. The ooQDataType enum field values correspond to bit positions so that they can be combined to represent types of a certain category or grouping. For example, the following shows a combined numerical type for an operand and result type of an arithmetic operator such as multiplication.

oocQNumeric = oocQUInt | oocQUInt |oocFloat
*/
public interface ExpressionResultSpec {
    public enum DataType {
        NoDataType,
        UInt,
        Int,
        WholeNumer = UInt | Int,
        Float32,
        Float64,
        Float = Float32 | Float64,
        Numeric = UInt | Int | Float,
        Bool,
        String8Bit,
        Utf8String,
        Utf16String,
        Utf32String,
        UtfString = Utf8String | Utf16String | Utf32String,
        String = String8Bit | UtfString,
        DateTime,
        Interval,
        Simple = Numeric | Bool | String | DateTime | Interval,
        Class,
        Reference,
        OrderedArray,
        OrderedList,
        OrderedSet,
        OrderedMap,
        OrderedCollection = OrderedArray | ... | OrderedMap,
        UnorderedArray,
        ...,
        UnorderedMap,
        UnorderedCollection = UnorderedArray | ... | UnorderedMap,
        Collection = OrderedCollection | UnorderedCollection
    }
    
    DataType getDataType();
    ExpressionResultSpec getElementSpec(); //for result specs of type Collection (isCollection==true), specifies the spec of each element of the collection. Throws an execption if dataType is not Collection type
    Class<? extends ooObj> getKeyClass(); //for keyed-collections only
    Class<? extends ooObj> getAssociatedClass();
    boolean isLiteral();
    boolean isCollection();
    boolean isKeyedCollection();
}