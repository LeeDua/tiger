package elaborator;

import java.util.LinkedList;

import ast.Ast.Class;
import ast.Ast.Exp.False;
import ast.Ast.Exp.Id;
import ast.Ast.Exp.Length;
import ast.Ast.Exp.Lt;
import ast.Ast.Exp.NewIntArray;
import ast.Ast.Exp.NewObject;
import ast.Ast.Exp.Not;
import ast.Ast.Exp.Num;
import ast.Ast.Exp.Sub;
import ast.Ast.Exp.This;
import ast.Ast.Exp.Times;
import ast.Ast.Exp.True;
import ast.Ast.MainClass;
import ast.Ast.Class.ClassSingle;
import ast.Ast.Dec;
import ast.Ast.Dec.DecSingle;
import ast.Ast.Exp;
import ast.Ast.Exp.Add;
import ast.Ast.Exp.And;
import ast.Ast.Exp.ArraySelect;
import ast.Ast.Exp.Call;
import ast.Ast.Method;
import ast.Ast.Method.MethodSingle;
import ast.Ast.Program.ProgramSingle;
import ast.Ast.Stm;
import ast.Ast.Stm.Assign;
import ast.Ast.Stm.AssignArray;
import ast.Ast.Stm.Block;
import ast.Ast.Stm.If;
import ast.Ast.Stm.Print;
import ast.Ast.Stm.While;
import ast.Ast.Type;
import ast.Ast.Type.ClassType;
import control.Control.ConAst;

public class ElaboratorVisitor implements ast.Visitor
{
  public ClassTable classTable; // symbol table for class
  public MethodTable methodTable; // symbol table for each method
  public String currentClass; // the class name being elaborated
  public Type.T type; // type of the expression being elaborated

  public ElaboratorVisitor()
  {
    this.classTable = new ClassTable();
    this.methodTable = new MethodTable();
    this.currentClass = null;
    this.type = null;
  }

  private void error(String message)
  {
    throw new java.lang.Error(message);
  }

  private void error()
  {
    throw new java.lang.Error("Type miss match");
  }

  private void error(Type.T type_1, Type.T type_2)
  {
    throw new java.lang.Error(type_1.toString() + " does not match " + type_2.toString());
  }

  // /////////////////////////////////////////////////////
  // expressions
  @Override
  public void visit(Add e)
  {
    //check left and right are both int
    e.left.accept(this);
    Type.T leftty = this.type;
    e.right.accept(this);
    Type.T rightty = this.type;
    if (!(leftty instanceof Type.Int && rightty instanceof Type.Int)){
      error("Add operator type error:" + leftty.toString() + "~" + rightty.toString());
    }
    this.type = new Type.Int();
  }

  @Override
  public void visit(And e)
  {
    e.left.accept(this);
    Type.T leftty = this.type;
    e.right.accept(this);
    Type.T rightty = this.type;
    if(!(leftty instanceof Type.Boolean && rightty instanceof Type.Boolean)){
      error("And operator type error: " + leftty.toString() + "~" + rightty.toString());
    }
    this.type = new Type.Boolean();
  }

  @Override
  public void visit(ArraySelect e)
  {
    e.array.accept(this);
    e.index.accept(this);
    Type.T index_type = this.type;
    if(!(index_type instanceof Type.Int)){
      error("ArraySelect operator index type error");
    }
    this.type = new Type.Int();
  }

  @Override
  public void visit(Call e)
  {
    Type.T leftty;
    Type.ClassType ty = null;

    e.exp.accept(this);
    leftty = this.type;
    if (leftty instanceof ClassType) {
      ty = (ClassType) leftty;
      e.type = ty.id;
      //如果exp是class type, call的type设置为class type
      //TODO: WHAT'S Call.type for?
    } else
      error("Call operator exp.method_id(args) :: 'exp' should be of class type!");
    MethodType mty = this.classTable.getm(ty.id, e.id);
    java.util.LinkedList<Type.T> argsty = new LinkedList<Type.T>();
    for (Exp.T a : e.args) {
      a.accept(this);
      argsty.addLast(this.type);
    }
    if (mty.argsType.size() != argsty.size())
      error("method call args type miss match:\n" + mty.toString()+ "\n" + argsty.toString());
    for (int i = 0; i < argsty.size(); i++) {
      Dec.DecSingle dec = (Dec.DecSingle) mty.argsType.get(i);
      if (dec.type.toString().equals(argsty.get(i).toString()))
        ;
      else
        error();
    }
    this.type = mty.retType;
    e.at = argsty;
    e.rt = this.type;
    return;
  }

  @Override
  public void visit(False e)
  {
    this.type = new Type.Boolean();
  }

  @Override
  public void visit(True e)
  {
    this.type = new Type.Boolean();
  }

  @Override
  public void visit(Id e)
  {
    // first look up the id in method table
    Type.T type = this.methodTable.get(e.id);
    // if search failed, then s.id must be a class field.
    if (type == null) {
      type = this.classTable.get(this.currentClass, e.id);
      // mark this id as a field id, this fact will be
      // useful in later phase.
      e.isField = true;
    }
    if (type == null)
      error();
    this.type = type;
    // record this type on this node for future use.
    e.type = type;
    return;
  }

  @Override
  public void visit(Length e)
  {
    //array must be int[]
    e.array.accept(this);
    if(! (this.type instanceof Type.IntArray)){
      error(".length operator must be applied on int array");
    }
    this.type = new Type.Int();
  }

  @Override
  public void visit(Lt e)
  {
    //should be the same type, but does not need to be both int?
    e.left.accept(this);
    Type.T ty = this.type;
    e.right.accept(this);
    /*if (!this.type.toString().equals(ty.toString()))
      error();*/
    if(!(this.type instanceof Type.Int && ty instanceof Type.Int)){
      error("Lt operator should only apply on two ints");
    }
    this.type = new Type.Boolean();
    return;
  }

  @Override
  public void visit(NewIntArray e)
  {
    //int[e] e should be int
    e.exp.accept(this);
    if(!(this.type instanceof Type.Int)){
      error("New int array size should be integer");
    }
    this.type = new Type.IntArray();
  }

  @Override
  public void visit(NewObject e)
  {
    this.type = new Type.ClassType(e.id);
    return;
  }

  @Override
  public void visit(Not e)
  {
    //! exp should be boolean
    e.exp.accept(this);
    if((this.type instanceof Type.Boolean)){
      error("Not operation should only apply on boolean");
    }
    this.type = new Type.Boolean();
  }

  @Override
  public void visit(Num e)
  {
    this.type = new Type.Int();
    return;
  }

  @Override
  public void visit(Sub e)
  {
    e.left.accept(this);
    Type.T leftty = this.type;
    e.right.accept(this);
    /*if (!this.type.toString().equals(leftty.toString()))
      error("sub operator should apply on two int");*/
    if(!(this.type instanceof Type.Int && leftty instanceof Type.Int)){
      error("sub operator should apply on two int");
    }
    this.type = new Type.Int();
    return;
  }

  @Override
  public void visit(This e)
  {
    this.type = new Type.ClassType(this.currentClass);
    return;
  }

  @Override
  public void visit(Times e)
  {
    e.left.accept(this);
    Type.T leftty = this.type;
    e.right.accept(this);
    //if (!this.type.toString().equals(leftty.toString()))
    if(!(this.type instanceof Type.Int && leftty instanceof Type.Int))
      error();
    this.type = new Type.Int();
    return;
  }



  // statements
  @Override
  public void visit(Assign s)
  {
    // first look up the id in method table
    Type.T type = this.methodTable.get(s.id);
    // if search failed, then s.id must be a class field
    if (type == null)
      type = this.classTable.get(this.currentClass, s.id);
    if (type == null)
      error("Cant assign to var that has not declared");
    s.exp.accept(this);
    if(!this.type.toString().equals(type.toString())){
      error("Assign statement type miss match");
    }
    return;
  }

  @Override
  public void visit(AssignArray s)
  {
    Type.T type = this.methodTable.get(s.id);
    if(type == null){
      type = this.classTable.get(this.currentClass, s.id);
    }
    if(type == null){
      error("Cant assign int array to var that has not declared");
    }
    else{
      if(!(type instanceof Type.IntArray)){
        error("Assign array should operates only on int array var");
      }
      else{
        //check index of type Int
        s.index.accept(this);
        if(!(this.type instanceof Type.Int))
          error("Assign array index should be int");
        //check exp is of IntArray type
        s.exp.accept(this);
        if(!(this.type instanceof Type.IntArray))
          error("Assign array value should be int array");
      }
    }
  }

  @Override
  public void visit(Block s)
  {
    //TODO: how do i deal with block ?
    //when enter block, should build a new class table
    //when exit, should determine what inblock modification on class table to keep
    LinkedList<Stm.T> stms = s.stms;
    for (Stm.T stm : stms) {
      stm.accept(this);
    }
  }

  @Override
  public void visit(If s)
  {
    s.condition.accept(this);
    if (!this.type.toString().equals("@boolean"))
      error("If clause condition should be boolean");
    s.thenn.accept(this);
    s.elsee.accept(this);
    return;
  }

  @Override
  public void visit(Print s)
  {
    //TODO: can support string also
    s.exp.accept(this);
    if (!this.type.toString().equals("@int"))
      error();
    return;
  }

  @Override
  public void visit(While s)
  {
    s.condition.accept(this);
    if(!(this.type instanceof Type.Boolean)){
      error("While clause condition should be boolean");
    }
    s.body.accept(this);
  }

  // type
  @Override
  public void visit(Type.Boolean t)
  {
    this.type = t;
  }

  @Override
  public void visit(Type.ClassType t)
  {
    this.type = t;
  }

  @Override
  public void visit(Type.Int t)
  {
    this.type = t;
  }

  @Override
  public void visit(Type.IntArray t)
  {
    this.type = t;
  }

  private void visit(Type.T t){
    if(t instanceof Type.Boolean){
      this.visit(t);
    }
    else if(t instanceof Type.ClassType){
      this.visit(t);
    }
    else if(t instanceof Type.Int){
      this.visit(t);
    }
    else if(t instanceof Type.IntArray){
      this.visit(t);
    }
    else{
      System.out.println("reach undefined Type");
      System.exit(-1);
    }
  }

  // dec
  @Override
  public void visit(Dec.DecSingle d)
  {
    //TODO: Class field 和 method filed有同名变量时怎么处理？默认没有吗
    //TODO:should not declare again for the same id either in class field or method field
    //if dec is used in formals? should this be considered the same?
    /*
    Type.T type = this.methodTable.get(d.id);
    if(type != null){
      error("Error: Var " + d.id + " redeclared");
    }
    else{
      this.classTable.get(this.currentClass,d.id);
      if(type != null){
        error("Error: Var " + d.id + " redeclared");
      }
      else{
        this.type = d.type;
      }
    }*/

  }

  // method
  @Override
  public void visit(Method.MethodSingle m)
  {
    // construct the method table
    //do not allow method overloading
    //but should check whether formals are exactly the same
    MethodType method = this.classTable.getm(this.currentClass, m.id);
    if(method != null){

    }
    else{
      this.methodTable.put(m.formals, m.locals);

      if (ConAst.elabMethodTable)
        this.methodTable.dump();

      for (Stm.T s : m.stms)
        s.accept(this);

      m.retExp.accept(this);
    }

    return;
  }

  // class
  @Override
  public void visit(Class.ClassSingle c)
  {
    this.currentClass = c.id;
    for (Dec.T dec: c.decs) {
      DecSingle d = (DecSingle) dec;
      Type.T type = this.classTable.get(this.currentClass,d.id);
      if(type!=null){
        error("Error: class field " + d.id + " redeclared");
      }
      else{

      }
    }


    for (Method.T m : c.methods) {
      m.accept(this);
    }
    return;
  }

  // main class
  @Override
  public void visit(MainClass.MainClassSingle c)
  {
    this.currentClass = c.id;
    // "main" has an argument "arg" of type "String[]", but
    // one has no chance to use it. So it's safe to skip it...

    for (Stm.T s:c.stms
         ) {
      s.accept(this);
    }
    return;
  }

  // ////////////////////////////////////////////////////////
  // step 1: build class table
  // class table for Main class
  private void buildMainClass(MainClass.MainClassSingle main)
  {
    this.classTable.put(main.id, new ClassBinding(null));
  }

  // class table for normal classes
  private void buildClass(ClassSingle c)
  {
    this.classTable.put(c.id, new ClassBinding(c.extendss));
    for (Dec.T dec : c.decs) {
      Dec.DecSingle d = (Dec.DecSingle) dec;
      this.classTable.put(c.id, d.id, d.type);
    }
    for (Method.T method : c.methods) {
      MethodSingle m = (MethodSingle) method;
      this.classTable.put(c.id, m.id, new MethodType(m.retType, m.formals));
    }
  }

  // step 1: end
  // ///////////////////////////////////////////////////

  // program
  @Override
  public void visit(ProgramSingle p)
  {
    // ////////////////////////////////////////////////
    // step 1: build a symbol table for class (the class table)
    // a class table is a mapping from class names to class bindings
    // classTable: className -> ClassBinding{extends, fields, methods}
    buildMainClass((MainClass.MainClassSingle) p.mainClass);
    for (Class.T c : p.classes) {
      buildClass((ClassSingle) c);
    }

    // we can double check that the class table is OK!
    if (control.Control.ConAst.elabClassTable) {
      this.classTable.dump();
    }

    // ////////////////////////////////////////////////
    // step 2: elaborate each class in turn, under the class table
    // built above.
    p.mainClass.accept(this);
    for (Class.T c : p.classes) {
      c.accept(this);
    }

  }
}
