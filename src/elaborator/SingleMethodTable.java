package elaborator;

import java.awt.*;
import java.time.format.DecimalStyle;
import java.util.Enumeration;
import java.util.LinkedList;

import ast.Ast.Dec;
import ast.Ast.Type;
import util.Todo;

public class SingleMethodTable
{
  private java.util.Hashtable<String, Type.T> dec_table;
  private java.util.Hashtable<String, Boolean> var_usage_table;
  private LinkedList<Type.T> formal_type_list;
  private Type.T retType;

  public SingleMethodTable()
  {
    this.retType = null;
    this.dec_table = new java.util.Hashtable<String, Type.T>();
    this.var_usage_table = new java.util.Hashtable<String,Boolean>();
  }

  // Duplication is not allowed
  private void SingleMethodTable(LinkedList<Dec.T> formals,
      LinkedList<Dec.T> locals, Type.T retType)
  {
    this.dec_table = new java.util.Hashtable<String, Type.T>();
    this.var_usage_table = new java.util.Hashtable<String,Boolean>();
    for (Dec.T dec : formals) {
      Dec.DecSingle decc = (Dec.DecSingle) dec;
      if (this.dec_table.get(decc.id) != null) {
        System.out.println("duplicated parameter: " + decc.id);
        System.exit(1);
      }
      this.dec_table.put(decc.id, decc.type);
      this.var_usage_table.put(decc.id,false);
    }

    for (Dec.T dec : locals) {
      Dec.DecSingle decc = (Dec.DecSingle) dec;
      if (this.dec_table.get(decc.id) != null) {
        System.out.println("duplicated variable: " + decc.id);
        System.exit(1);
      }
      this.dec_table.put(decc.id, decc.type);
      this.var_usage_table.put(decc.id, false);
    }

    LinkedList<Type.T> formal_type_list = new LinkedList<>();
    for (Dec.T d: formals
    ) {
      formal_type_list.addLast(((Dec.DecSingle)d).type);
    }
    this.formal_type_list = formal_type_list;
    this.retType = retType;
  }

  public SingleMethodTable(MethodType methodType){
    this.SingleMethodTable(methodType.formals,methodType.locals,methodType.retType);
  }

  public Type.T get(String var){
    return this.dec_table.get(var);
  }

  public Type.T getRetType(){
    return this.retType;
  }

  public LinkedList<Type.T> getFormals(){
    return this.formal_type_list;
  }

  public void mark_var_used(String id){
    this.var_usage_table.put(id, true);
  }

  public String NotifyNotUsedVars(){
    String str = "";
    Enumeration iterator = this.var_usage_table.keys();
    while(iterator.hasMoreElements()){
      String current_key = (String) iterator.nextElement();
      if(!this.var_usage_table.get(current_key)){
        str += current_key + " ";
      }
    }
    return str;
  }

  public void dump()
  {
    String method_string = "( ";
    for (Type.T formal:this.formal_type_list
    ) {
      method_string += formal.toString();
      method_string += " * ";
    }
    method_string += ")->";
    method_string += this.retType.toString();
    method_string += "\n";
    method_string += "formal and local decls:";
    method_string += dec_table.toString();
    System.out.println(method_string);
  }

  @Override
  public String toString()
  {
    String method_string = "( ";
    for (Type.T formal:this.formal_type_list
         ) {
      method_string += formal.toString();
      method_string += " * ";
    }
    method_string += ")->";
    method_string += this.retType.toString();
    return method_string;
  }


}