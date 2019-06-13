package parser;

import ast.Ast;
import lexer.Lexer;
import lexer.Token;
import lexer.Token.Kind;

import javax.swing.*;
import java.awt.desktop.SystemSleepEvent;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;

import ast.Ast.Type;
import ast.Ast.Type.Boolean;
import ast.Ast.Type.ClassType;
import ast.Ast.Type.Int;
import ast.Ast.Type.IntArray;
import ast.Ast.Dec;
import ast.Ast.Dec.DecSingle;
import ast.Ast.Exp;
import ast.Ast.Exp.Add;
import ast.Ast.Exp.And;
import ast.Ast.Exp.ArraySelect;
import ast.Ast.Exp.Call;
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
import ast.Ast.Method;
import ast.Ast.Method.MethodSingle;
import ast.Ast.Program;
import ast.Ast.Stm;
import ast.Ast.Stm.Assign;
import ast.Ast.Stm.AssignArray;
import ast.Ast.Stm.Block;
import ast.Ast.Stm.If;
import ast.Ast.Stm.Print;
import ast.Ast.Stm.While;
import ast.Ast.Type.Boolean;
import ast.Ast.Type.ClassType;
import ast.Ast.Type.Int;
import ast.Ast.Type.IntArray;
import ast.Ast.Program;
import ast.Ast.Class;
import ast.Ast.Class.ClassSingle;
import ast.Ast.Program.ProgramSingle;
import util.Flist;

public class Parser
{
  private Lexer lexer;
  private Token current;
  private boolean abortOnFirstError = true;

  public Parser(String fname, java.io.InputStream fstream)
  {
    lexer = new Lexer(fname, fstream);
    current = lexer.nextToken();
  }

  // /////////////////////////////////////////////
  // utility methods to connect the lexer
  // and the parser.

  private void advance()
  {
    current = lexer.nextToken();
  }

  private void eatToken(Kind kind)
  {
    if (kind == current.kind)
      advance();
    else {
      System.out.println("Expects: " + kind.toString());
      System.out.println("But got: " + current.kind.toString());
      error();
      if(!abortOnFirstError){
        eatToken(kind);
      }
    }
  }

  private void error()
  {
    System.out.println("Syntax error: compilation abort\n" +
            "Filename: " + lexer.fname + "\n" +
            "Illegal token: " + current.toString());
    System.out.println("Source code:");
    try {
      InputStream source_code_fstream = new BufferedInputStream(new FileInputStream(lexer.fname));
      Lexer lexer_to_find_source = new Lexer(lexer.fname,source_code_fstream);
      System.out.print(lexer_to_find_source.find_code(current));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      InputStream source_code_pos_fstream = new BufferedInputStream(new FileInputStream(lexer.fname));
      Lexer lexer_to_find_source_pos = new Lexer(lexer.fname,source_code_pos_fstream);
      System.out.print(lexer_to_find_source_pos.find_token_pos(current));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }

    if(abortOnFirstError){
      System.exit(1);
    }
    else{
      System.out.println("\n");
      advance();
    }


    return;
  }

  // ////////////////////////////////////////////////////////////
  // below are method for parsing.

  // A bunch of parsing methods to parse expressions. The messy
  // parts are to deal with precedence and associativity.

  // ExpList -> Exp ExpRest*
  // ->
  // ExpRest -> , Exp
  private LinkedList<Exp.T> parseExpList()
  {
  	//DONE
    LinkedList<Exp.T> exp_list = new Flist<Exp.T>().list();
	  if (current.kind == Kind.TOKEN_RPAREN)
		  return exp_list;
	  Exp.T exp = parseExp();
	  exp_list.addLast(exp);
    while (current.kind == Kind.TOKEN_COMMER) {
      advance();
      exp = parseExp();
      exp_list.addLast(exp);
    }
    return exp_list;
  }

  // AtomExp -> (exp)
  // -> INTEGER_LITERAL
  // -> true
  // -> false
  // -> this
  // -> id
  // -> new int [exp]
  // -> new id ()
  private Exp.T parseAtomExp()
  {
    //DONE
  	Exp.T exp = null;
  	switch (current.kind) {
    case TOKEN_LPAREN:
      advance();
      exp = parseExp();
      eatToken(Kind.TOKEN_RPAREN);
      break;
    case TOKEN_NUM:
      exp = new Num(Integer.parseInt(current.lexeme));
    	advance();
    	break;
    case TOKEN_TRUE:
    	exp = new True();
      advance();
      break;
    case TOKEN_FALSE:
    	exp = new False();
      advance();
      break;
    case TOKEN_THIS:
    	exp = new This();
      advance();
      break;
    case TOKEN_ID:
    	//TODO: how do i get the type of this id here? just ignore now
	    exp = new Id(current.lexeme);
      advance();
      break;
    case TOKEN_NEW:
      advance();
      switch (current.kind) {
      case TOKEN_INT:
        advance();
        eatToken(Kind.TOKEN_LBRACK);
        Exp.T e = parseExp();
        eatToken(Kind.TOKEN_RBRACK);
        exp = new NewIntArray(e);
        return exp;
      case TOKEN_ID:
        String id = current.lexeme;
      	advance();
        eatToken(Kind.TOKEN_LPAREN);
        eatToken(Kind.TOKEN_RPAREN);
        exp = new NewObject(id);
        return exp;
      default:
        error();
      }
    default:
      error();
    }
    return exp;
  }

  // NotExp -> AtomExp
  // -> AtomExp .id (expList)
  // -> AtomExp [exp]
  // -> AtomExp .length
  private Exp.T parseNotExp()
  {
    //DONE
  	Exp.T exp = parseAtomExp();
    if (current.kind == Kind.TOKEN_DOT || current.kind == Kind.TOKEN_LBRACK) {
      if (current.kind == Kind.TOKEN_DOT) {
        advance();
        if (current.kind == Kind.TOKEN_LENGTH) {
          advance();
          exp = new Length(exp);
          return exp;
        }
        String id = current.lexeme;
        eatToken(Kind.TOKEN_ID);
        eatToken(Kind.TOKEN_LPAREN);
        LinkedList<Exp.T> args = parseExpList();
        eatToken(Kind.TOKEN_RPAREN);
        exp = new Call(exp, id, args);
      } else {
        advance();
        Exp.T index = parseExp();
        eatToken(Kind.TOKEN_RBRACK);
        exp = new ArraySelect(exp, index);
      }
    }
    return exp;
  }

  //TODO：TimesExp既然能相乘，它的值难道不是必须为一个整数吗？为什么还能呢？ 在语法分析再做类型分析吗？

  // TimesExp -> ! NotExp
  // -> NotExp
  private Exp.T parseTimesExp()
  {
  	//DONE
    Exp.T exp = null;
  	int not_cnt = 0;
    while (current.kind == Kind.TOKEN_NOT) {
  		advance();
  		not_cnt += 1;
    }
	  exp = parseNotExp();
  	if(not_cnt == 0){

	  }
  	else{
  		for (int i=0;i<not_cnt;i++){
  			exp = new Not(exp);
		  }
	  }
    return exp;
  }

  // AddSubExp -> TimesExp * TimesExp
  // -> TimesExp
  private Exp.T parseAddSubExp()
  {
    //DONE
  	Exp.T exp = parseTimesExp();
    while (current.kind == Kind.TOKEN_TIMES) {
      advance();
      Exp.T right_exp = parseTimesExp();
      exp = new Times(exp, right_exp);
    }
    return exp;
  }

  // LtExp -> AddSubExp + AddSubExp
  // -> AddSubExp - AddSubExp
  // -> AddSubExp
  private Exp.T parseLtExp()
  {
    //DONE
  	Exp.T exp = parseAddSubExp();
    while (current.kind == Kind.TOKEN_ADD || current.kind == Kind.TOKEN_SUB) {
	    Token operator = current;
      advance();
	    Exp.T right = parseAddSubExp();
    	if(operator.kind == Kind.TOKEN_ADD){
	      exp = new Add(exp, right);
      }
      else{
      	exp = new Sub(exp, right);
      }

    }
    return exp;
  }

  // AndExp -> LtExp < LtExp
  // -> LtExp
  private Exp.T parseAndExp()
  {
  	//DONE
  	Exp.T exp = parseLtExp();
    while (current.kind == Kind.TOKEN_LT) {
      advance();
      Exp.T right = parseLtExp();
      exp = new Lt(exp, right);
    }
    return exp;
  }

  // Exp -> AndExp && AndExp
  // -> AndExp
  private Exp.T parseExp()
  {
    //DONE
  	Exp.T exp = parseAndExp();
    while (current.kind == Kind.TOKEN_AND) {
      advance();
      Exp.T right = parseAndExp();
      exp = new And(exp,right);
    }
    return exp;
  }

  // Statement -> { Statement* }
  // -> if ( Exp ) Statement else Statement
  // -> while ( Exp ) Statement
  // -> System.out.println ( Exp ) ;
  // -> id = Exp ;
  // -> id [ Exp ]= Exp ;
  // -> ;
  private Stm.T parseStatement()
  {
    //DONE
    //new util.Todo();
	  Stm.T stm = null;
	  if(current.kind == Kind.TOKEN_SEMI)
	    advance();
    else if(current.kind == Kind.TOKEN_LBRACE){
      advance();
      if(current.kind != Kind.TOKEN_RBRACE){
        stm = new Block(parseStatements());
      }
      else{
        stm = new Block();
      }
      eatToken(Kind.TOKEN_RBRACE);
    }
    else if(current.kind == Kind.TOKEN_IF){
      advance();
      eatToken(Kind.TOKEN_LPAREN);
      Exp.T condition = parseExp();
      eatToken(Kind.TOKEN_RPAREN);
      Stm.T if_statements = parseStatement();
      eatToken(Kind.TOKEN_ELSE);
      Stm.T else_statements = parseStatement();
      stm = new If(condition, if_statements, else_statements);
    }
    else if(current.kind == Kind.TOKEN_WHILE){
      advance();
      eatToken(Kind.TOKEN_LPAREN);
      Exp.T condition = parseExp();
      eatToken(Kind.TOKEN_RPAREN);
      Stm.T loop_stms = parseStatement();
      stm = new While(condition, loop_stms);
    }
    else if(current.kind == Kind.TOKEN_SYSTEM){
      advance();
      eatToken(Kind.TOKEN_DOT);
      eatToken(Kind.TOKEN_OUT);
      eatToken(Kind.TOKEN_DOT);
      eatToken(Kind.TOKEN_PRINTLN);
      eatToken(Kind.TOKEN_LPAREN);
      Exp.T print_exp = parseExp();
      eatToken(Kind.TOKEN_RPAREN);
      eatToken(Kind.TOKEN_SEMI);
      stm = new Print(print_exp);
    }
    else if(current.kind == Kind.TOKEN_ID){
      String id = current.lexeme;
    	advance();
      if(current.kind == Kind.TOKEN_ASSIGN){
        advance();
        Exp.T assign_exp = parseExp();
        eatToken(Kind.TOKEN_SEMI);
        stm = new Assign(new Id(id), assign_exp);
      }
      else if(current.kind == Kind.TOKEN_LBRACK){
        advance();
        Exp.T index = parseExp();
        eatToken(Kind.TOKEN_RBRACK);
        eatToken(Kind.TOKEN_ASSIGN);
        Exp.T assign_exp = parseExp();
        eatToken(Kind.TOKEN_SEMI);
        stm = new AssignArray(new Id(id),index,assign_exp);
      }
      else{
        error();
      }
    }
    else{
      error();
    }
    return stm;
  }

  // Statements -> Statement Statements
  // ->
  private LinkedList<Stm.T> parseStatements()
  {
    //DONE
	  //For filed usage?
  	LinkedList<Stm.T> stms = new Flist<Stm.T>().list();
  	while (current.kind == Kind.TOKEN_LBRACE || current.kind == Kind.TOKEN_IF
        || current.kind == Kind.TOKEN_WHILE
        || current.kind == Kind.TOKEN_SYSTEM || current.kind == Kind.TOKEN_ID
        || current.kind == Kind.TOKEN_SEMI) {
      Stm.T stm = parseStatement();
      if(stm!=null)
        stms.addLast(stm);
    }
    return stms;
  }

  // Type -> int []
  // -> boolean
  // -> int
  // -> id
  private Type.T parseType()
  {
		//DONE
  	Type.T type = null;
    if (current.kind == Kind.TOKEN_INT){
      advance();
      type = new Int();
      if(current.kind == Kind.TOKEN_LBRACK){
        advance();
        eatToken(Kind.TOKEN_RBRACK);
        type = new IntArray();
      }
    }
    else if(current.kind == Kind.TOKEN_BOOLEAN){
      advance();
      type = new Boolean();
    }
    else if(current.kind == Kind.TOKEN_ID){
    	String id = current.lexeme;
    	advance();
    	type = new ClassType(id);
    }
    else error();
    //new util.Todo();
	  return type;
  }

  // VarDecl -> Type id ;
  private Dec.T parseVarDecl()
  {
  	//DONE
  	Dec.T dec = null;
  	// to parse the "Type" nonterminal in this method, instead of writing
    // a fresh one.
    Token type_backup = current;
    Type.T type = parseType();
    if(current.kind == Kind.TOKEN_ID){
      String id = current.lexeme;
    	eatToken(Kind.TOKEN_ID);
      eatToken(Kind.TOKEN_SEMI);
      dec = new DecSingle(type, id);
    }
	  return dec;
  }

  // VarDecls -> VarDecl VarDecls
  // ->
  private LinkedList<Dec.T> parseVarDecls()
  {
    //DONE
  	LinkedList<Dec.T> decs = new Flist<Dec.T>().list();
  	while (current.kind == Kind.TOKEN_INT || current.kind == Kind.TOKEN_BOOLEAN
        || current.kind == Kind.TOKEN_ID) {
  	  if(current.kind == Kind.TOKEN_ID){
        Token id_backup = current;
  	    advance();
        //read forward 1 more token to check whether is class var decl or assign
        if(current.kind == Kind.TOKEN_ID){
          lexer.rollBackToken(current);
          current = id_backup;
          Dec.T dec = parseVarDecl();
          decs.addLast(dec);
        }
        else{
          lexer.rollBackToken(current);
          current = id_backup;
          break;
        }
      }
      else{
        Dec.T dec = parseVarDecl();
        decs.addLast(dec);
      }
    }
    return decs;
  }

  // FormalList -> Type id FormalRest*
  // ->
  // FormalRest -> , Type id
  private LinkedList<Dec.T> parseFormalList()
  {
  	//DONE
  	LinkedList<Dec.T> formalist = new Flist<Dec.T>().list();
  	Type.T type = null;
  	String id = null;
  	if (current.kind == Kind.TOKEN_INT || current.kind == Kind.TOKEN_BOOLEAN
        || current.kind == Kind.TOKEN_ID) {
      type = parseType();
      id = current.lexeme;
      eatToken(Kind.TOKEN_ID);
      formalist.addLast(new DecSingle(type, id));
      while (current.kind == Kind.TOKEN_COMMER) {
        advance();
        type = parseType();
        id = current.lexeme;
        eatToken(Kind.TOKEN_ID);
        formalist.addLast(new DecSingle(type, id));
      }
    }
    return formalist;
  }

  // Method -> public Type id ( FormalList )
  // { VarDecl* Statement* return Exp ;}
  private Method.T parseMethod()
  {
  	// DONE
  	// Lab1. Exercise 4: Fill in the missing code
    // to parse a method.
    //new util.Todo();
    eatToken(Kind.TOKEN_PUBLIC);
    Type.T return_type = parseType();
    String id = current.lexeme;
    eatToken(Kind.TOKEN_ID);
    eatToken(Kind.TOKEN_LPAREN);
    LinkedList<Dec.T> formalist = parseFormalList();
    eatToken(Kind.TOKEN_RPAREN);
    eatToken(Kind.TOKEN_LBRACE);
    LinkedList<Dec.T> decs = parseVarDecls();
    LinkedList<Stm.T> stms = parseStatements();
    eatToken(Kind.TOKEN_RETURN);
    Exp.T return_exp = parseExp();
    eatToken(Kind.TOKEN_SEMI);
    eatToken(Kind.TOKEN_RBRACE);
    return new MethodSingle(return_type, id, formalist, decs, stms, return_exp);
  }

  // MethodDecls -> MethodDecl MethodDecls
  // ->
  private LinkedList<Method.T> parseMethodDecls()
  {
    //DONE
  	LinkedList<Method.T> methods = new Flist<Method.T>().list();
  	while (current.kind == Kind.TOKEN_PUBLIC) {
      Method.T method = parseMethod();
      methods.addLast(method);
    }
    return methods;
  }

  // ClassDecl -> class id { VarDecl* MethodDecl* }
  // -> class id extends id { VarDecl* MethodDecl* }
  private Class.T parseClassDecl()
  {
  	//DONE
  	eatToken(Kind.TOKEN_CLASS);
  	String id = current.lexeme;
  	String extend_class = null;
    eatToken(Kind.TOKEN_ID);
    if (current.kind == Kind.TOKEN_EXTENDS) {
      eatToken(Kind.TOKEN_EXTENDS);
      extend_class = current.lexeme;
      eatToken(Kind.TOKEN_ID);
    }
    eatToken(Kind.TOKEN_LBRACE);
    LinkedList<Dec.T> decs = parseVarDecls();
    LinkedList<Method.T> methods = parseMethodDecls();
    eatToken(Kind.TOKEN_RBRACE);
    return new ClassSingle(id, extend_class, decs, methods);
  }

  // ClassDecls -> ClassDecl ClassDecls
  // ->
  private LinkedList<Class.T> parseClassDecls()
  {
    //DONE
  	LinkedList<Class.T> classes = new Flist<Class.T>().list();
  	while (current.kind == Kind.TOKEN_CLASS) {
      Class.T class_single = parseClassDecl();
      classes.addLast(class_single);
    }
    return classes;
  }

  // MainClass -> class id
  // {
  // public static void main ( String [] id )
  // {
  // Statements
  // }
  // }
  private MainClass.T parseMainClass()
  {
    // Lab1. Exercise 4: Fill in the missing code
    // to parse a main class as described by the
    // grammar above.
    //new util.Todo();
	  eatToken(Kind.TOKEN_CLASS);
    String id = current.lexeme;
	  eatToken(Kind.TOKEN_ID);
    eatToken(Kind.TOKEN_LBRACE);
    eatToken(Kind.TOKEN_PUBLIC);
    eatToken(Kind.TOKEN_STATIC);
    eatToken(Kind.TOKEN_VOID);
    eatToken(Kind.TOKEN_MAIN);
    eatToken(Kind.TOKEN_LPAREN);
    eatToken(Kind.TOKEN_STRING);
    eatToken(Kind.TOKEN_LBRACK);
    eatToken(Kind.TOKEN_RBRACK);
	  String arg = current.lexeme;
	  eatToken(Kind.TOKEN_ID);
    eatToken(Kind.TOKEN_RPAREN);
    eatToken(Kind.TOKEN_LBRACE);
    LinkedList<Stm.T> stms = parseStatements();
    eatToken(Kind.TOKEN_RBRACE);
    eatToken(Kind.TOKEN_RBRACE);
    return new MainClass.MainClassSingle(id, arg, stms);
  }

  // Program -> MainClass ClassDecl*
  private Program.T parseProgram()
  {

  	MainClass.T main_class = parseMainClass();
    LinkedList<Class.T> classes = parseClassDecls();
    eatToken(Kind.TOKEN_EOF);
    return new ProgramSingle(main_class, classes);
  }

  public Program.T parse()
  {
    return parseProgram();
  }
}
