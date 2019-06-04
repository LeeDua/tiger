package elaborator;

import java.util.LinkedList;

import ast.Ast.Dec;
import ast.Ast.Type;

public class MethodType
{
  public Type.T retType;
  public LinkedList<Dec.T> locals;
  public LinkedList<Dec.T> formals;

  public MethodType(Type.T retType, LinkedList<Dec.T> formals, LinkedList<Dec.T> locals)
  {
    this.retType = retType;
    this.locals = locals;
    this.formals = formals;
  }

  @Override
  public String toString()
  {
    String s = "";
    for (Dec.T dec : this.formals) {
      Dec.DecSingle decc = (Dec.DecSingle) dec;
      s = decc.type.toString() + "*" + s;
    }
    s = s + " -> " + this.retType.toString();
    return s;
  }

}
