import static control.Control.ConAst.dumpAst;
import static control.Control.ConAst.testFac;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ast.Ast.Program;
import lexer.Lexer;
import lexer.Token;
import parser.Parser;
import control.CommandLine;
import control.Control;

public class Tiger
{
  public static void main(String[] args)
  {
    InputStream fstream;
    Parser parser;

    // ///////////////////////////////////////////////////////
    // handle command line arguments
    CommandLine cmd = new CommandLine();
    String fname = cmd.scan(args);

    // /////////////////////////////////////////////////////
    // to test the pretty printer on the "test/Fac.java" program
    if (testFac) {
      System.out.println("Testing the Tiger compiler on Fac.java starting:");
      ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
      ast.Fac.prog.accept(pp);

      // elaborate the given program, this step is necessary
      // for that it will annotate the AST with some
      // informations used by later phase.
      elaborator.ElaboratorVisitor elab = new elaborator.ElaboratorVisitor();
      ast.Fac.prog.accept(elab);

      // Compile this program to C.
      System.out.println("code generation starting");
      // code generation
      switch (control.Control.ConCodeGen.codegen) {
      case Bytecode:
        System.out.println("bytecode codegen");
        codegen.bytecode.TranslateVisitor trans = new codegen.bytecode.TranslateVisitor();
        ast.Fac.prog.accept(trans);
        codegen.bytecode.Ast.Program.T bytecodeAst = trans.program;
        codegen.bytecode.PrettyPrintVisitor ppbc = new codegen.bytecode.PrettyPrintVisitor();
        bytecodeAst.accept(ppbc);
        break;
      case C:
        System.out.println("C codegen");
        codegen.C.TranslateVisitor transC = new codegen.C.TranslateVisitor();
        ast.Fac.prog.accept(transC);
        codegen.C.Ast.Program.T cAst = transC.program;
        codegen.C.PrettyPrintVisitor ppc = new codegen.C.PrettyPrintVisitor();
        cAst.accept(ppc);
        break;
      case Dalvik:
        // similar
        break;
      case X86:
        // similar
        break;
      default:
        break;
      }
      System.out.println("Testing the Tiger compiler on Fac.java finished.");
      System.exit(1);
    }

    if (fname == null) {
      cmd.usage();
      return;
    }
    Control.ConCodeGen.fileName = fname;

    // /////////////////////////////////////////////////////
    // it would be helpful to be able to test the lexer
    // independently.
    if (Control.ConLexer.test) {
      System.out.println("Testing the lexer. All tokens:");
      try {
        fstream = new BufferedInputStream(new FileInputStream(fname));
        Lexer lexer = new Lexer(fname, fstream);
        Token token = lexer.nextToken();

        while (token.kind != Token.Kind.TOKEN_EOF) {
          System.out.println(token.toString());
          token = lexer.nextToken();
        }
        fstream.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      System.exit(1);
    }

    // /////////////////////////////////////////////////////////
    // normal compilation phases.
    Program.T theAst = null;

    // parsing the file, get an AST.
    try {
      fstream = new BufferedInputStream(new FileInputStream(fname));
      parser = new Parser(fname, fstream);

      theAst = parser.parse();

      fstream.close();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

    // pretty printing the AST, if necessary
    if (dumpAst) {
      ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
      theAst.accept(pp);
    }

    // elaborate the AST, report all possible errors.
    elaborator.ElaboratorVisitor elab = new elaborator.ElaboratorVisitor();
    theAst.accept(elab);

    // code generation
    switch (control.Control.ConCodeGen.codegen) {
    case Bytecode:
      codegen.bytecode.TranslateVisitor trans = new codegen.bytecode.TranslateVisitor();
      theAst.accept(trans);
      codegen.bytecode.Ast.Program.T bytecodeAst = trans.program;
      codegen.bytecode.PrettyPrintVisitor ppbc = new codegen.bytecode.PrettyPrintVisitor();
      bytecodeAst.accept(ppbc);
      break;
    case C:
      codegen.C.TranslateVisitor transC = new codegen.C.TranslateVisitor();
      theAst.accept(transC);
      codegen.C.Ast.Program.T cAst = transC.program;
      codegen.C.PrettyPrintVisitor ppc = new codegen.C.PrettyPrintVisitor();
      cAst.accept(ppc);
      break;
    case Dalvik:
      // similar
      break;
    case X86:
      // similar
      break;
    default:
      break;
    }
    //compile as debug
    System.out.println("-----Compile as debug------");
		String compile_command = "gcc -g -o " + fname + ".out " + fname + ".c ./runtime/runtime.c";
		System.out.println(compile_command);
    try {
      Process p = Runtime.getRuntime().exec(compile_command);
      String s = null;
      BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
      BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
      while ((s = stdInput.readLine()) != null) {
        System.out.println(s);
      }
      while ((s = stdError.readLine()) != null) {
        System.out.println(s);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println("-----Compile end--------\n");
    //run
    System.out.println("--------Run executable file------");
    String run_command = fname + ".out ";
    System.out.println(run_command);
    try {
      Process p = Runtime.getRuntime().exec(run_command);
      String s = null;
      BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
      BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
      while ((s = stdInput.readLine()) != null) {
        System.out.println(s);
      }
      while ((s = stdError.readLine()) != null) {
        System.out.println(s);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.print("---------Run executable end----------");

    return;
  }
}
