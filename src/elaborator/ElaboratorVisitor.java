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
  //TODO: need a global method table for non-in-class methods
  //According to Program build logic, not supporting is fine now

  public ClassTable classTable; // symbol table for class
  public String currentClass; // the class name being elaborated
  public String currentMethod; //the method name being elaborated
  public Type.T type; // type of the expression being elaborated

  public ElaboratorVisitor()
  {
    this.classTable = new ClassTable();
    this.currentClass = null;
    this.currentMethod = null;
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
      error("ArraySelect operator index should only be int");
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
    //exp should be of classType to have method
    if (leftty instanceof ClassType) {
      ty = (ClassType) leftty;
      e.type = ty.id;
      //check whether class exists
      if(this.classTable.get(ty.id)==null)
        error("Method call error: Class "+ ty.id + " do not exists");
      else{
        SingleMethodTable remote_method = this.classTable.getm(ty.id, e.id);
        // Should check whether method exists
        if(remote_method == null)
          error("Method call error: Class" + ty.id + "do not have method named '"+ e.id + "'");
        java.util.LinkedList<Type.T> remote_formals = remote_method.getFormals();
        LinkedList<Type.T> formals = new LinkedList<>();
        for (Exp.T a : e.args) {
          a.accept(this);
          formals.addLast(this.type);
        }
        //check whether formals match exactly the same
        if (remote_formals.size() != formals.size())
          error("method call args type miss match:\n" + formals.toString()+ "\n" + remote_formals.toString());
        for (int i = 0; i < formals.size(); i++) {
          //should support extend class type
	        //e.g
	        //B extends A
	        //B b;
	        //func (A a) can be called by fun(b)
        	if(!formals.get(i).toString().equals(remote_formals.get(i).toString())){
        		if(remote_formals.get(i) instanceof ClassType && formals.get(i) instanceof ClassType){
        			String remote_type = ((ClassType) remote_formals.get(i)).id;
			        String _type = ((ClassType) formals.get(i)).id;
			        //search up in extend class until extend linklist ends
			        while(!(remote_type.equals(_type)) && _type != null){
			        	_type = this.classTable.get(_type).extendss;
			        }
			        if(!remote_type.equals(_type)){
				        error("method formal arg miss match");
			        }
		        }
        		else
			        error("method formal arg miss match");
	        }

        }
        this.type = remote_method.getRetType();
        e.at = formals;
        e.rt = this.type;
      }
    } else
      error("Call operator exp.method_id(args) :: 'exp' should be of class type!");

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
    ClassBinding cb = this.classTable.get(this.currentClass);
    SingleMethodTable method = cb.methods.get(this.currentMethod);
    if(method == null)
      error("Reach Id expression outside of method");
    Type.T type = method.get(e.id);
    // if search failed, then s.id must be a class field.
    if (type == null) {
      type = this.classTable.get(this.currentClass,e.id);
      // mark this id as a field id, this fact will be
      // useful in later phase.
      e.isField = true;
    }
    if (type == null)
      error(e.id + " not declared");
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
    if(!(this.type instanceof Type.IntArray)){
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
    if(this.classTable.get(e.id)== null){
      error("NewObject error: Class " + e.id + " does not exist");
    }
    this.type = new Type.ClassType(e.id);
    return;
  }

  @Override
  public void visit(Not e)
  {
    //! exp should be boolean
    e.exp.accept(this);
    if(!(this.type instanceof Type.Boolean)){
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
      error("Times operator do not operates on two ints: " + this.type.toString() + "~" + leftty.toString());
    this.type = new Type.Int();
    return;
  }



  // statements
  @Override
  public void visit(Assign s)
  {
    ClassBinding cb = this.classTable.get(this.currentClass);
    SingleMethodTable method = cb.methods.get(this.currentMethod);
    if(method == null)
      error("Reach assign statement outside of method");
    Type.T type = method.get(s.id);
    // if search failed, then s.id must be a class field.
    if (type == null) {
      type = this.classTable.get(this.currentClass,s.id);
    }
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
	  //id[index] = exp
    ClassBinding cb = this.classTable.get(this.currentClass);
    SingleMethodTable method = cb.methods.get(this.currentMethod);
    if(method == null)
      error("Reach assign array statement outside of method");
    Type.T type = method.get(s.id);
    // if search failed, then s.id must be a class field.
    if (type == null) {
      type = this.classTable.get(this.currentClass,s.id);
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
        if(!(this.type instanceof Type.Int))
          error("Assign array value should be int");
      }
    }
  }

  @Override
  public void visit(Block s)
  {
    //TODO: how do i deal with block ? do not support yet
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
      error("Print statement do not support exp type of " + this.type.toString() + "yet");
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
    //Class decl and method decl should be dealt with differently
    error("Should not reach single decl visit method");
  }

  // method
  @Override
  public void visit(Method.MethodSingle m)
  {
    //do not allow method overloading (has check duplicated in class binding putm)
    this.currentMethod = m.id;

    if (ConAst.elabMethodTable){
	    System.out.print("\n------Method table start: " + this.currentClass + "." + m.id + "----\n");
	    System.out.print(m.id + ": ");
    	this.classTable.getm(this.currentClass,m.id).dump();
    	System.out.println("------Method table end-------");
    }

    for (Stm.T s : m.stms)
      s.accept(this);

    m.retExp.accept(this);
    this.currentMethod = null;


    return;
  }

  // class
  @Override
  public void visit(Class.ClassSingle c)
  {
    //Check class redeclare
    //check class field redeclare
    //check class method redeclare
    //都在class table里做了
    this.currentClass = c.id;

    for (Method.T m:c.methods) {
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
    this.currentClass = c.id;

    for (Dec.T class_dec: c.decs) {
      DecSingle d = (DecSingle) class_dec;
      this.classTable.put(c.id,d.id,d.type);
    }

    for (Method.T method : c.methods) {
      MethodSingle m = (MethodSingle) method;
      this.classTable.put(this.currentClass, m.id, new MethodType(m.retType,m.formals,m.locals));
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
