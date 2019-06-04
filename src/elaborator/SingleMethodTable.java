package elaborator;

import java.time.format.DecimalStyle;
import java.util.LinkedList;

import ast.Ast.Dec;
import ast.Ast.Type;
import util.Todo;

public class SingleMethodTable
{
  private java.util.Hashtable<String, Type.T> dec_table;
  private LinkedList<Dec.T> formals;
  private Type.T retType;

  public SingleMethodTable()
  {
    this.retType = null;
    this.dec_table = new java.util.Hashtable<String, Type.T>();
  }

  // Duplication is not allowed
  private void SingleMethodTable(LinkedList<Dec.T> formals,
      LinkedList<Dec.T> locals, Type.T retType)
  {
    for (Dec.T dec : formals) {
      Dec.DecSingle decc = (Dec.DecSingle) dec;
      if (this.dec_table.get(decc.id) != null) {
        System.out.println("duplicated parameter: " + decc.id);
        System.exit(1);
      }
      this.dec_table.put(decc.id, decc.type);
    }

    for (Dec.T dec : locals) {
      Dec.DecSingle decc = (Dec.DecSingle) dec;
      if (this.dec_table.get(decc.id) != null) {
        System.out.println("duplicated variable: " + decc.id);
        System.exit(1);
      }
      this.dec_table.put(decc.id, decc.type);
    }
    this.retType = retType;
    this.formals = formals;
  }

  public SingleMethodTable(MethodType methodType){
    this.SingleMethodTable(methodType.formals,methodType.locals,methodType.retType);
  }

  public Type.T getVar(String var){
    return this.dec_table.get(var);
  }

  public Type.T getRetType(){
    return this.retType;
  }

  public void dump()
  {
    System.out.print(this.toString());
  }

  @Override
  public String toString()
  {
    String method_string = "";
    for (Dec.T formal:this.formals
         ) {
      Dec.DecSingle f = (Dec.DecSingle) formal;
      method_string += f.type.toString();
      method_string += " * ";
    }
    method_string += "->";
    method_string += this.retType.toString();
    return method_string;
  }
}
