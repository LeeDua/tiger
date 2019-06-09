package codegen.C;

import codegen.C.Ast.Class.ClassSingle;
import codegen.C.Ast.Dec;
import codegen.C.Ast.Dec.DecSingle;
import codegen.C.Ast.Exp;
import codegen.C.Ast.Exp.Add;
import codegen.C.Ast.Exp.And;
import codegen.C.Ast.Exp.ArraySelect;
import codegen.C.Ast.Exp.Call;
import codegen.C.Ast.Exp.Id;
import codegen.C.Ast.Exp.Length;
import codegen.C.Ast.Exp.Lt;
import codegen.C.Ast.Exp.NewIntArray;
import codegen.C.Ast.Exp.NewObject;
import codegen.C.Ast.Exp.Not;
import codegen.C.Ast.Exp.Num;
import codegen.C.Ast.Exp.Sub;
import codegen.C.Ast.Exp.This;
import codegen.C.Ast.Exp.Times;
import codegen.C.Ast.MainMethod.MainMethodSingle;
import codegen.C.Ast.Method;
import codegen.C.Ast.Method.MethodSingle;
import codegen.C.Ast.Program.ProgramSingle;
import codegen.C.Ast.Stm;
import codegen.C.Ast.Stm.Assign;
import codegen.C.Ast.Stm.AssignArray;
import codegen.C.Ast.Stm.Block;
import codegen.C.Ast.Stm.If;
import codegen.C.Ast.Stm.Print;
import codegen.C.Ast.Stm.While;
import codegen.C.Ast.Type.ClassType;
import codegen.C.Ast.Type.Int;
import codegen.C.Ast.Type.IntArray;
import codegen.C.Ast.Vtable;
import codegen.C.Ast.Vtable.VtableSingle;
import control.Control;

public class PrettyPrintVisitor implements Visitor
{
  private int indentLevel;
  private java.io.BufferedWriter writer;
  private boolean inMethod;

  public PrettyPrintVisitor()
  {
    this.indentLevel = 2;
    inMethod = false;
  }

  private void indent()
  {
    this.indentLevel += 2;
  }

  private void unIndent()
  {
    this.indentLevel -= 2;
  }

  private void printSpaces()
  {
    int i = this.indentLevel;
    while (i-- != 0)
      this.say(" ");
  }

  private void sayln(String s)
  {
    say(s);
    try {
      this.writer.write("\n");
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private void say(String s)
  {
    try {
      this.writer.write(s);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  // /////////////////////////////////////////////////////
  // expressions
  @Override
  public void visit(Add e)
  {
    e.left.accept(this);
    this.say(" + ");
    e.right.accept(this);
  }

  @Override
  public void visit(And e)
  {
    e.left.accept(this);
    this.say(" && ");
    e.right.accept(this);
  }

  @Override
  public void visit(ArraySelect e)
  {
    e.array.accept(this);
    this.say("[");
    e.index.accept(this);
    this.say("]");
  }

  //assign stand for a new var to store the Class ptr
  //assign = exp.id(args)
  //e.g: this.ComputeFac(num - 1);
  //(x_1=this, x_1->vptr->ComputeFac(x_1, num - 1));
  @Override
  public void visit(Call e)
  {
    this.say("(" + e.assign + "=");
    e.exp.accept(this);
    this.say(", ");
    this.say(e.assign + "->vptr->" + e.id + "(" + e.assign);
    int size = e.args.size();
    if (size == 0) {
      this.say("))");
      return;
    }
    for (Exp.T x : e.args) {
      this.say(", ");
      x.accept(this);
    }
    this.say("))");
    return;
  }

  @Override
  public void visit(Id e)
  {
    if(this.inMethod){
      if(e.isField){
        if(e.classId == null){
          System.out.println("!!!!!field should have classId!!!!!");
        }
        else{
          this.say("this->");
        }
      }
    }
    this.say(e.id);
  }

  @Override
  public void visit(Length e)
  {
    //(sizeof(array)/sizeof(int))
    this.say("(sizeof(");
    e.array.accept(this);
    this.say(")/sizeof(int))");
  }

  @Override
  public void visit(Lt e)
  {
    e.left.accept(this);
    this.say(" < ");
    e.right.accept(this);
    return;
  }

  @Override
  public void visit(NewIntArray e)
  {
    this.say("(int*)malloc(sizeof(int)*");
    e.exp.accept(this);
    this.say(")");

  }

  //e.g: new ClassId()
  //((struct ClassId *)(Tiger_new (get_ClassId_vtable_(),sizeof(struct ClassId))))
  @Override
  public void visit(NewObject e)
  {
    this.say("((struct " + e.id + "*)(Tiger_new (get_" + e.id
        + "_vtable_(), sizeof(struct " + e.id + "))))");
    return;
  }

  @Override
  public void visit(Not e)
  {
    this.say("!(");
    e.exp.accept(this);
    this.say(")");
  }

  @Override
  public void visit(Num e)
  {
    this.say(Integer.toString(e.num));
    return;
  }

  @Override
  public void visit(Sub e)
  {
    e.left.accept(this);
    this.say(" - ");
    e.right.accept(this);
    return;
  }

  @Override
  public void visit(This e)
  {
    this.say("this");
  }

  @Override
  public void visit(Times e)
  {
    e.left.accept(this);
    this.say(" * ");
    e.right.accept(this);
    return;
  }

  // statements
  @Override
  public void visit(Assign s)
  {
    this.printSpaces();
    s.id.accept(this);
    this.say(" = ");
    s.exp.accept(this);
    this.sayln(";");
    return;
  }

  @Override
  public void visit(AssignArray s)
  {
    this.printSpaces();
    s.id.accept(this);
    this.say("[");
    s.index.accept(this);
    this.say("] = ");
    s.exp.accept(this);
    this.sayln(";");
  }

  @Override
  public void visit(Block s)
  {
    this.printSpaces();
    this.sayln("{");
    this.indent();
    for (Stm.T stm : s.stms){
      stm.accept(this);
    }
    this.unIndent();
    this.printSpaces();
    this.sayln("}");
  }

  @Override
  public void visit(If s)
  {
    this.printSpaces();
    this.say("if (");
    s.condition.accept(this);
    this.sayln(")");
    this.indent();
    s.thenn.accept(this);
    this.unIndent();
    this.printSpaces();
    this.sayln("else");
    this.indent();
    s.elsee.accept(this);
    this.unIndent();
    return;
  }

  @Override
  public void visit(Print s)
  {
    this.printSpaces();
    this.say("System_out_println (");
    s.exp.accept(this);
    this.sayln(");");
    return;
  }

  @Override
  public void visit(While s)
  {
    this.printSpaces();
    this.say("while(");
    s.condition.accept(this);
    this.sayln(")");
    this.indent();
    s.body.accept(this);
    this.unIndent();
  }

  // type
  @Override
  public void visit(ClassType t)
  {
    this.say("struct " + t.id + " *");
  }

  @Override
  public void visit(Int t)
  {
    this.say("int");
  }

  @Override
  public void visit(IntArray t)
  {
    this.say("int*");
  }

  // dec
  @Override
  public void visit(DecSingle d)
  {
    d.type.accept(this);
    this.say(" " + d.id);
    this.sayln( ";");
  }

  // method
  //e.g
  //public int ComputeFac(int num)
  //    {
  //      int num_aux;

  //      return num_aux;
  //    }
  //------------------------------
  //int Fac_ComputeFac(struct Fac * this, int num)
  //{
  //  int num_aux;
  //  struct Fac * x_1;
  //
  //  return num_aux;
  //}
  @Override
  public void visit(MethodSingle m)
  {
    m.retType.accept(this);
    this.say(" " + m.classId + "_" + m.id + "(");
    int size = m.formals.size();
    for (Dec.T d : m.formals) {
      DecSingle dec = (DecSingle) d;
      size--;
      dec.type.accept(this);
      this.say(" " + dec.id);
      if (size > 0)
        this.say(", ");
    }
    this.sayln(")");
    this.sayln("{");
    this.inMethod = true;
    for (Dec.T d : m.locals) {
      DecSingle dec = (DecSingle) d;
      this.say("  ");
      dec.type.accept(this);
      this.say(" " + dec.id);
      this.sayln(";");
    }
    this.sayln("");
    for (Stm.T s : m.stms)
      s.accept(this);
    this.say("  return ");
    m.retExp.accept(this);
    this.sayln(";");
    this.sayln("}");
    this.inMethod = false;
    return;
  }

  @Override
  public void visit(MainMethodSingle m)
  {
    this.sayln("int Tiger_main ()");
    this.sayln("{");
    this.inMethod = true;
    for (Dec.T dec : m.locals) {
      this.say("  ");
      DecSingle d = (DecSingle) dec;
      d.type.accept(this);
      this.say(" ");
      this.say(d.id);
      this.sayln(";");
    }
    m.stm.accept(this);
    this.sayln("}\n");
    this.inMethod = false;
    return;
  }


  //e.g Fac has 1 func ComputeFac
  //------------------------
  // struct Fac_vtable
  //{
  //  int (*ComputeFac)();
  //};
  //------------------------
  //struct Fac_vtable Fac_vtable_ =
  //{
  //  Fac_ComputeFac,
  //};

  // vtables
  @Override
  public void visit(VtableSingle v)
  {
    this.sayln("struct " + v.id + "_vtable");
    this.sayln("{");
    for (codegen.C.Ftuple t : v.ms) {
      this.say("  ");
      t.ret.accept(this);
      this.sayln(" (*" + t.id + ")();");
    }
    this.sayln("};\n");
    return;
  }

  private void outputVtable(VtableSingle v)
  {
    this.sayln("struct " + v.id + "_vtable " + v.id + "_vtable_ = ");
    this.sayln("{");
    for (codegen.C.Ftuple t : v.ms) {
      this.say("  ");
      this.sayln(t.classs + "_" + t.id + ",");
    }
    this.sayln("};\n");
    return;
  }

  private void declVtable(VtableSingle v)
  {
    this.sayln("struct " + v.id + "_vtable " + v.id + "_vtable_;");
    this.sayln("struct " + v.id + "_vtable * " + v.id + "_vtable_ptr;");
    this.sayln("void* get_" + v.id + "_vtable_();");
    return;
  }

  private void getVtableFunc(VtableSingle v)
  {
    this.sayln("void* get_" + v.id + "_vtable_(){");
    this.printSpaces();
    this.sayln("if(" + v.id + "_vtable_ptr" + "== NULL){");
    this.indent();
    this.printSpaces();
    this.sayln("struct " + v.id + "_vtable temp = ");
    this.indent();
    this.printSpaces();
    this.sayln("{");
    this.indent();
    for (codegen.C.Ftuple t : v.ms) {
      this.printSpaces();
      this.sayln(t.classs + "_" + t.id + ",");
    }
    this.unIndent();
    this.printSpaces();
    this.sayln("};");
    this.unIndent();
    this.printSpaces();
    this.sayln("memcpy(&" + v.id + "_vtable_, &temp, sizeof(temp));");
    this.printSpaces();
    this.sayln(v.id + "_vtable_ptr = &" + v.id + "_vtable_;");
    this.unIndent();
    this.printSpaces();
    this.sayln("}");
    this.printSpaces();
    this.sayln("return (void*)(" + v.id + "_vtable_ptr);");
    this.sayln("}");
    this.sayln("");
    return;
  }

  // class
  //e.g:
  //-----------------
  //struct Fac
  //{
  //  struct Fac_vtable *vptr;
  //  int a;
  //  int[] b;
  //};
  @Override
  public void visit(ClassSingle c)
  {
    this.sayln("struct " + c.id);
    this.sayln("{");
    this.sayln("  struct " + c.id + "_vtable *vptr;");
    for (codegen.C.Tuple t : c.decs) {
      this.say("  ");
      t.type.accept(this);
      this.say(" ");
      this.say(t.id);
      this.sayln(";");
    }
    this.sayln("};");
    return;
  }

  // program
  @Override
  public void visit(ProgramSingle p)
  {
    // we'd like to output to a file, rather than the "stdout".
    try {
      String outputName = null;
      if (Control.ConCodeGen.outputName != null)
        outputName = Control.ConCodeGen.outputName;
      else if (Control.ConCodeGen.fileName != null)
        outputName = Control.ConCodeGen.fileName + ".c";
      else
        outputName = "a.c";

      this.writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(
          new java.io.FileOutputStream(outputName)));
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }


    this.sayln("// This is automatically generated by the Tiger compiler.");
    this.sayln("// Do NOT modify!");
    this.sayln("#include <stdio.h>");
    this.sayln("#include <stdlib.h>");
    this.sayln("#include <string.h>");
    this.sayln("");

    this.sayln("void *Tiger_new (void *vtable, int size);");
    this.sayln("int System_out_println (int i);");
    this.sayln("");

    this.sayln("// structures");
    for (codegen.C.Ast.Class.T c : p.classes) {
      c.accept(this);
    }

    this.sayln("// vtables structures");
    for (Vtable.T v : p.vtables) {
      v.accept(this);
    }
    this.sayln("");

    this.sayln("// vtable ptr and init method decls");
    for (Vtable.T v : p.vtables) {
      declVtable((VtableSingle) v);
    }
    this.sayln("");

    this.sayln("// methods");
    for (Method.T m : p.methods) {
      m.accept(this);
    }
    this.sayln("");


    this.sayln("// vtable get methods");
    for (Vtable.T v : p.vtables) {
      getVtableFunc((VtableSingle) v);
    }
    this.sayln("");


    this.sayln("// main method");
    p.mainMethod.accept(this);
    this.sayln("");

    this.say("\n\n");

    try {
      this.writer.close();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

  }

}
