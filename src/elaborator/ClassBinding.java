package elaborator;

import java.util.Hashtable;

import ast.Ast.Type;

public class ClassBinding
{
  public String extendss; // null for non-existing extends
  public java.util.Hashtable<String, Type.T> fields;
  public java.util.Hashtable<String, SingleMethodTable> methods;

  public ClassBinding(String extendss)
  {
    this.extendss = extendss;
    this.fields = new Hashtable<String, Type.T>();
    this.methods = new Hashtable<String,SingleMethodTable>();
  }

  public ClassBinding(String extendss,
      java.util.Hashtable<String, Type.T> fields,
      java.util.Hashtable<String, SingleMethodTable> methods)
  {
    this.extendss = extendss;
    this.fields = fields;
    this.methods = methods;
  }

  public void put(String xid, Type.T type)
  {
    if (this.fields.get(xid) != null) {
      System.out.println("duplicated class field: " + xid);
      System.exit(1);
    }
    this.fields.put(xid, type);
  }

  public void put(String mid, MethodType method)
  {
    if (this.methods.get(mid) != null) {
      System.out.println("duplicated class method: " + mid);
      System.exit(1);
    }
    this.methods.put(mid, new SingleMethodTable(method));
  }

  @Override
  public String toString()
  {
    String class_info = "";
    class_info += "extends: ";
    if (this.extendss != null)
      class_info += this.extendss;
    else
      class_info += "<>";
    class_info += "\nfields:";
    class_info += fields.toString();
    class_info += "\nmethods:";
    class_info += methods.toString();
    class_info += "\n\n";

    return class_info;
  }

}
