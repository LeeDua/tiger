package ast;

import ast.Ast.Dec;
import ast.Ast.Exp;
import ast.Ast.Exp.Call;
import ast.Ast.Exp.Id;
import ast.Ast.Exp.Lt;
import ast.Ast.Exp.NewObject;
import ast.Ast.Exp.Num;
import ast.Ast.Exp.Sub;
import ast.Ast.Exp.This;
import ast.Ast.Exp.Times;
import ast.Ast.Exp.Add;
import ast.Ast.MainClass;
import ast.Ast.MainClass.MainClassSingle;
import ast.Ast.Method;
import ast.Ast.Program;
import ast.Ast.Program.ProgramSingle;
import ast.Ast.Stm;
import ast.Ast.Stm.Assign;
import ast.Ast.Stm.If;
import ast.Ast.Stm.Block;
import ast.Ast.Stm.While;
import ast.Ast.Stm.Print;
import ast.Ast.Type;


/*
class Sum {
	public static void main(String[] a) {
        System.out.println(new Doit().doit(101));
    }
}

class Doit {
    public int doit(int n) {
        int sum;
        int i;

        i = 0;
        sum = 0;
        while (i<n){
        	sum = sum + i;
        	i = i+1;
        }
        return sum;
    }
}
 */

public class Sum_tree_repre {
    static MainClass.T sum_main = new MainClassSingle(
            "Sum","a",
            new Print(
                 new Call(
                         new NewObject("Doit"), "doit",
                                 new util.Flist<Exp.T>().list(new Num(101)))
                 )
            );
    static ast.Ast.Class.T Doit_class = new ast.Ast.Class.ClassSingle(
            "Doit", null,
            new util.Flist<Dec.T>().list(),
            new util.Flist<Method.T>().list(
                    new Method.MethodSingle(
                            new Type.Int(), "doit", new util.Flist<Dec.T>()
                            .list(new Dec.DecSingle(new Type.Int(), "n")),
                            new util.Flist<Dec.T>().list(new Dec.DecSingle(
                                    new Type.Int(), "sum"
                            ), new Dec.DecSingle(new Type.Int(), "i")),
                            new util.Flist<Stm.T>().list(
                                    new Assign("i", new Num(0)),
                                    new Assign("sum", new Num(0)),
                                    new While(new Lt(new Id("i"),new Id("n")),
                                            new Block(new util.Flist<Stm.T>().list(
                                                    new Assign("sum", new Add(new Id("sum"),new Id("i"))),
                                                    new Assign("i", new Add(new Id("i"), new Num(1)))
                                            )          )
                                        )
                            ),
                            new Id("sum")
                    )
            )
    );
    public static Program.T prog = new ProgramSingle(sum_main,
            new util.Flist<ast.Ast.Class.T>().list(Doit_class));
}
