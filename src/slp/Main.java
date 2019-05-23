package slp;

import java.util.ArrayList;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

import slp.Slp.Exp;
import slp.Slp.Exp.Eseq;
import slp.Slp.Exp.Id;
import slp.Slp.Exp.Num;
import slp.Slp.Exp.Op;
import slp.Slp.ExpList;
import slp.Slp.Stm;
import util.Bug;
import util.Todo;
import control.Control;

import slp.Linklist;

public class Main
{
  // ///////////////////////////////////////////
  // maximum number of args
  private Linklist mem = new Linklist();

  private int maxArgsExp(Exp.T exp)
  {
    /*if (exp instanceof Exp.Id){
        return 0;
      }
    else if (exp instanceof Exp.Num){
        return 0;
      }
    else if (exp instanceof Exp.Op) {
      Exp.Op e = (Exp.Op) exp;
      int n1 = maxArgsExp(e.left);
      int n2 = maxArgsExp(e.right);
      return n1+n2;
    }*/
    if (exp instanceof Exp.Eseq){
      Exp.Eseq e = (Exp.Eseq) exp;
      int n1 = maxArgsStm(e.stm);
      int n2 = maxArgsExp(e.exp);
      return n1>n2 ? n1 : n2;
    }
    else{
      return 1;
    }
  }

  private int maxArgExpList(ExpList.T expList){
    if (expList instanceof ExpList.Pair){
      ExpList.Pair elist= (ExpList.Pair) expList;
      int n1 = maxArgsExp(elist.exp);
      int n2 = maxArgExpList(elist.list);
      return n1 + n2;
    }
    if (expList instanceof ExpList.Last){
      ExpList.Last last= (ExpList.Last) expList;
      return maxArgsExp(last.exp);
    }
    else{
      throw new java.lang.Error("unmatched explist");
    }
  }

  private int maxArgsStm(Stm.T stm)
  {
    if (stm instanceof Stm.Compound) {
      Stm.Compound s = (Stm.Compound) stm;
      int n1 = maxArgsStm(s.s1);
      int n2 = maxArgsStm(s.s2);
      return n1 >= n2 ? n1 : n2;
    } else if (stm instanceof Stm.Assign) {
        Stm.Assign s = (Stm.Assign) stm;
        return maxArgsExp(s.exp);
    } else if (stm instanceof Stm.Print) {
        Stm.Print s = (Stm.Print) stm;
        //System.out.println(maxArgExpList(s.explist));
        return maxArgExpList(s.explist);
    } else{
      throw new java.lang.Error("unmatchend stm");
    }
  }

  // ////////////////////////////////////////
  // interpreter
  private int interpExp(Exp.T exp) {
    if (exp instanceof Exp.Op){
      Exp.Op e = (Exp.Op) exp;
      switch (e.op){
        case ADD:
          return interpExp(e.left) + interpExp(e.right);
        case SUB:
          return interpExp(e.left) - interpExp(e.right);
        case TIMES:
          return interpExp(e.left) * interpExp(e.right);
        case DIVIDE:
          if (interpExp(e.right) == 0){
            throw new java.lang.Error("divided by zero");
          }
          return interpExp(e.left) / interpExp(e.right);
      }
    }
    else if (exp instanceof Exp.Num){
      return ((Num) exp).num;
    }
    else if (exp instanceof Exp.Id){
      Exp.Id e = (Exp.Id) exp;
      return this.mem.lookup(e.id);
    }
    else if (exp instanceof Exp.Eseq){
      Exp.Eseq e = (Exp.Eseq) exp;
      interpStm(e.stm);
      return interpExp(e.exp);
    }
    throw new java.lang.Error("unmatched Exp class");
  }

  private void interpStm(Stm.T stm)
  {
    if (stm instanceof Stm.Compound) {
      Stm.Compound s = (Stm.Compound) stm;
      interpStm(s.s1);
      interpStm(s.s2);
    } else if (stm instanceof Stm.Assign) {
      Stm.Assign s = (Stm.Assign) stm;
      int value = interpExp(s.exp);
      this.mem.update(s.id.id, value);
    } else if (stm instanceof Stm.Print) {
      Stm.Print s = (Stm.Print) stm;
      ArrayList<Integer> valueList = interpExpList(s.explist);
      ListIterator<Integer> iter = valueList.listIterator();
      //System.out.println("start explist print out");
      while(iter.hasNext()){
        System.out.print(iter.next().toString());
        System.out.print(" ");
      }
      if(valueList.size() != 0){
        System.out.println();
      }
      //System.out.println("end explist print out");
    }

  }

  private ArrayList<Integer> interpExpList(ExpList.T expList){
    ArrayList<Integer> list = new ArrayList();
    if (expList instanceof  ExpList.Pair){
      ExpList.Pair pair = (ExpList.Pair) expList;
      list.add(interpExp((pair.exp)));
      ListIterator<Integer>iter = interpExpList(pair.list).listIterator();
      while(iter.hasNext()){
        list.add(iter.next());
      }
    }
    else if (expList instanceof ExpList.Last){
      ExpList.Last last = (ExpList.Last) expList;
      list.add(interpExp(last.exp));
    }
    return list;
  }

  // ////////////////////////////////////////
  // compile
  HashSet<String> ids;
  StringBuffer buf;

  private void emit(String s)
  {
    buf.append(s);
  }

  private void compileExp(Exp.T exp)
  {
    if (exp instanceof Id) {
      Exp.Id e = (Exp.Id) exp;
      String id = e.id;

      emit("\tmovl\t" + id + ", %eax\n");
    } else if (exp instanceof Num) {
      Exp.Num e = (Exp.Num) exp;
      int num = e.num;

      emit("\tmovl\t$" + num + ", %eax\n");
    } else if (exp instanceof Op) {
      Exp.Op e = (Exp.Op) exp;
      Exp.T left = e.left;
      Exp.T right = e.right;
      Exp.OP_T op = e.op;

      switch (op) {
      case ADD:
        compileExp(left);
        emit("\tpushl\t%eax\n");
        compileExp(right);
        emit("\tpopl\t%edx\n");
        emit("\taddl\t%edx, %eax\n");
        break;
      case SUB:
        compileExp(left);
        emit("\tpushl\t%eax\n");
        compileExp(right);
        emit("\tpopl\t%edx\n");
        emit("\tsubl\t%eax, %edx\n");
        emit("\tmovl\t%edx, %eax\n");
        break;
      case TIMES:
        compileExp(left);
        emit("\tpushl\t%eax\n");
        compileExp(right);
        emit("\tpopl\t%edx\n");
        emit("\timul\t%edx\n");
        break;
      case DIVIDE:
        compileExp(left);
        emit("\tpushl\t%eax\n");
        compileExp(right);
        emit("\tpopl\t%edx\n");
        emit("\tmovl\t%eax, %ecx\n");
        emit("\tmovl\t%edx, %eax\n");
        emit("\tcltd\n");
        emit("\tdiv\t%ecx\n");
        break;
      default:
        new Bug();
      }
    } else if (exp instanceof Eseq) {
      Eseq e = (Eseq) exp;
      Stm.T stm = e.stm;
      Exp.T ee = e.exp;

      compileStm(stm);
      compileExp(ee);
    } else
      new Bug();
  }

  private void compileExpList(ExpList.T explist)
  {
    if (explist instanceof ExpList.Pair) {
      ExpList.Pair pair = (ExpList.Pair) explist;
      Exp.T exp = pair.exp;
      ExpList.T list = pair.list;

      compileExp(exp);
      emit("\tpushl\t%eax\n");
      emit("\tpushl\t$slp_format\n");
      emit("\tcall\tprintf\n");
      emit("\taddl\t$4, %esp\n");
      compileExpList(list);
    } else if (explist instanceof ExpList.Last) {
      ExpList.Last last = (ExpList.Last) explist;
      Exp.T exp = last.exp;

      compileExp(exp);
      emit("\tpushl\t%eax\n");
      emit("\tpushl\t$slp_format\n");
      emit("\tcall\tprintf\n");
      emit("\taddl\t$4, %esp\n");
    } else
      new Bug();
  }

  private void compileStm(Stm.T prog)
  {
    if (prog instanceof Stm.Compound) {
      Stm.Compound s = (Stm.Compound) prog;
      Stm.T s1 = s.s1;
      Stm.T s2 = s.s2;

      compileStm(s1);
      compileStm(s2);
    } else if (prog instanceof Stm.Assign) {
      Stm.Assign s = (Stm.Assign) prog;
      String id = s.id.id;
      Exp.T exp = s.exp;

      ids.add(id);
      compileExp(exp);
      emit("\tmovl\t%eax, " + id + "\n");
    } else if (prog instanceof Stm.Print) {
      Stm.Print s = (Stm.Print) prog;
      ExpList.T explist = s.explist;

      compileExpList(explist);
      emit("\tpushl\t$newline\n");
      emit("\tcall\tprintf\n");
      emit("\taddl\t$4, %esp\n");
    } else
      new Bug();
  }

  // ////////////////////////////////////////
  public void doit(Stm.T prog)
  {
    // return the maximum number of arguments
    if (Control.ConSlp.action == Control.ConSlp.T.ARGS) {
      int numArgs = maxArgsStm(prog);
      System.out.println(numArgs);
    }

    // interpret a given program
    if (Control.ConSlp.action == Control.ConSlp.T.INTERP) {
     // System.out.print("Start Interpret");
      interpStm(prog);
     // System.out.print("End Interpret");
    }

    // compile a given SLP program to x86
    if (Control.ConSlp.action == Control.ConSlp.T.COMPILE) {
      ids = new HashSet<String>();
      buf = new StringBuffer();

      compileStm(prog);
      try {
        // FileOutputStream out = new FileOutputStream();
        FileWriter writer = new FileWriter("slp_gen.s");
        writer
            .write("// Automatically generated by the Tiger compiler, do NOT edit.\n\n");
        writer.write("\t.data\n");
        writer.write("slp_format:\n");
        writer.write("\t.string \"%d \"\n");
        writer.write("newline:\n");
        writer.write("\t.string \"\\n\"\n");
        for (String s : this.ids) {
          writer.write(s + ":\n");
          writer.write("\t.int 0\n");
        }
        writer.write("\n\n\t.text\n");
        writer.write("\t.globl main\n");
        writer.write("main:\n");
        writer.write("\tpushl\t%ebp\n");
        writer.write("\tmovl\t%esp, %ebp\n");
        writer.write(buf.toString());
        writer.write("\tleave\n\tret\n\n");
        writer.close();
        Process child = Runtime.getRuntime().exec("gcc slp_gen.s");
        child.waitFor();
        if (!Control.ConSlp.keepasm)
          Runtime.getRuntime().exec("rm -rf slp_gen.s");
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(0);
      }
      // System.out.println(buf.toString());
    }
  }
}
