package codegen.C;

import codegen.C.Ast.Dec;
import codegen.C.Ast.Type;

public class ClassTable
{
  private java.util.Hashtable<String, ClassBinding> table;

  public ClassTable()
  {
    this.table = new java.util.Hashtable<String, ClassBinding>();
  }

  public void init(String current, String extendss)
  {
    this.table.put(current, new ClassBinding(extendss));
    return;
  }

  public void initDecs(String current,
      java.util.LinkedList<Dec.T> decs)
  {
    ClassBinding cb = this.table.get(current);
    for (Dec.T dec : decs) {
      Dec.DecSingle decc = (Dec.DecSingle) dec;
      cb.put(current, decc.type, decc.id);
    }
    this.table.put(current, cb);
  }

  public void initMethod(String current, Type.T ret,
      java.util.LinkedList<Dec.T> args, String mid)
  {
    ClassBinding cb = this.table.get(current);
    cb.putm(current, ret, args, mid);
    return;
  }

  public void inherit(String c)
  {
    // build inherit field and method list from class C (leaf node on inherit tree)
    // foreach class after inherit scan done, mark visited as true
    // 不断向上递归，first build p node then child node
    ClassBinding cb = this.table.get(c);

    //对所有类进行继承处理时需要遍历所有叶节点，每个叶节点向上递归一次，下一个叶节点可能会和之前的叶节点有公共父节点
    //此时父节点已构建完成，无需再次处理，所以直接return
    if (cb.visited)
      return;

    //if have no parent, keep method and fields as current and return
    if (cb.extendss == null) {
      cb.visited = true;
      return;
    }

    inherit(cb.extendss);

    ClassBinding pb = this.table.get(cb.extendss);
    // this tends to be very slow...
    // need a much fancier data structure.
    java.util.LinkedList<Tuple> newFields = new java.util.LinkedList<Tuple>();
    newFields.addAll(pb.fields);
    newFields.addAll(cb.fields);
    cb.update(newFields);
    // methods;
    java.util.ArrayList<Ftuple> newMethods = new java.util.ArrayList<Ftuple>();
    newMethods.addAll(pb.methods);
    for (codegen.C.Ftuple t : cb.methods) {
      int index = newMethods.indexOf(t);
      //if parent binding does not contain child method, add child method
      if (index == -1) {
        newMethods.add(t);
        continue;
      }
      //else override parent method
      newMethods.set(index, t);
    }
    cb.update(newMethods);
    // set the mark
    cb.visited = true;
    return;
  }

  // return null for non-existing keys
  public ClassBinding get(String c)
  {
    return this.table.get(c);
  }

  @Override
  public String toString()
  {
    return this.table.toString();
  }
}
