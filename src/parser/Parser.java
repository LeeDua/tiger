package parser;

import lexer.Lexer;
import lexer.Token;
import lexer.Token.Kind;

import javax.swing.*;
import java.awt.desktop.SystemSleepEvent;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class Parser
{
  private Lexer lexer;
  private Token current;
  private boolean abortOnFirstError = false;

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
  private void parseExpList()
  {
    if (current.kind == Kind.TOKEN_RPAREN)
      return;
    parseExp();
    while (current.kind == Kind.TOKEN_COMMER) {
      advance();
      parseExp();
    }
    return;
  }

  // AtomExp -> (exp)
  // -> INTEGER_LITERAL
  // -> true
  // -> false
  // -> this
  // -> id
  // -> new int [exp]
  // -> new id ()
  private void parseAtomExp()
  {
    switch (current.kind) {
    case TOKEN_LPAREN:
      advance();
      parseExp();
      eatToken(Kind.TOKEN_RPAREN);
      return;
    case TOKEN_NUM:
      advance();
      return;
    case TOKEN_TRUE:
      advance();
      return;
    case TOKEN_FALSE:
      advance();
      return;
    case TOKEN_THIS:
      advance();
      return;
    case TOKEN_ID:
      advance();
      return;
    case TOKEN_NEW: {
      advance();
      switch (current.kind) {
      case TOKEN_INT:
        advance();
        eatToken(Kind.TOKEN_LBRACK);
        parseExp();
        eatToken(Kind.TOKEN_RBRACK);
        return;
      case TOKEN_ID:
        advance();
        eatToken(Kind.TOKEN_LPAREN);
        eatToken(Kind.TOKEN_RPAREN);
        return;
      default:
        error();
        return;
      }
    }
    default:
      error();
      return;
    }
  }

  // NotExp -> AtomExp
  // -> AtomExp .id (expList)
  // -> AtomExp [exp]
  // -> AtomExp .length
  private void parseNotExp()
  {
    parseAtomExp();
    while (current.kind == Kind.TOKEN_DOT || current.kind == Kind.TOKEN_LBRACK) {
      if (current.kind == Kind.TOKEN_DOT) {
        advance();
        if (current.kind == Kind.TOKEN_LENGTH) {
          advance();
          return;
        }
        eatToken(Kind.TOKEN_ID);
        eatToken(Kind.TOKEN_LPAREN);
        parseExpList();
        eatToken(Kind.TOKEN_RPAREN);
      } else {
        advance();
        parseExp();
        eatToken(Kind.TOKEN_RBRACK);
      }
    }
    return;
  }

  //TODO：TimesExp既然能相乘，它的值难道不是必须为一个整数吗？为什么还能!呢？

  // TimesExp -> ! NotExp
  // -> NotExp
  private void parseTimesExp()
  {
    while (current.kind == Kind.TOKEN_NOT) {
      advance();
    }
    parseNotExp();
    return;
  }

  // AddSubExp -> TimesExp * TimesExp
  // -> TimesExp
  private void parseAddSubExp()
  {
    parseTimesExp();
    while (current.kind == Kind.TOKEN_TIMES) {
      advance();
      parseTimesExp();
    }
    return;
  }

  // LtExp -> AddSubExp + AddSubExp
  // -> AddSubExp - AddSubExp
  // -> AddSubExp
  private void parseLtExp()
  {
    parseAddSubExp();
    while (current.kind == Kind.TOKEN_ADD || current.kind == Kind.TOKEN_SUB) {
      advance();
      parseAddSubExp();
    }
    return;
  }

  // AndExp -> LtExp < LtExp
  // -> LtExp
  private void parseAndExp()
  {
    parseLtExp();
    while (current.kind == Kind.TOKEN_LT) {
      advance();
      parseLtExp();
    }
    return;
  }

  // Exp -> AndExp && AndExp
  // -> AndExp
  private void parseExp()
  {
    parseAndExp();
    while (current.kind == Kind.TOKEN_AND) {
      advance();
      parseAndExp();
    }
    return;
  }

  // Statement -> { Statement* }
  // -> if ( Exp ) Statement else Statement
  // -> while ( Exp ) Statement
  // -> System.out.println ( Exp ) ;
  // -> id = Exp ;
  // -> id [ Exp ]= Exp ;
  private void parseStatement()
  {
    // Lab1. Exercise 4: Fill in the missing code
    // to parse a statement.
    //new util.Todo();
    if(current.kind == Kind.TOKEN_LBRACE){
      advance();
      parseStatements();
      eatToken(Kind.TOKEN_RBRACE);
    }
    else if(current.kind == Kind.TOKEN_IF){
      advance();
      eatToken(Kind.TOKEN_LPAREN);
      parseExp();
      eatToken(Kind.TOKEN_RPAREN);
      parseStatement();
      eatToken(Kind.TOKEN_ELSE);
      parseStatement();
    }
    else if(current.kind == Kind.TOKEN_WHILE){
      advance();
      eatToken(Kind.TOKEN_LPAREN);
      parseExp();
      eatToken(Kind.TOKEN_RPAREN);
      parseStatement();
    }
    else if(current.kind == Kind.TOKEN_SYSTEM){
      advance();
      eatToken(Kind.TOKEN_DOT);
      eatToken(Kind.TOKEN_OUT);
      eatToken(Kind.TOKEN_DOT);
      eatToken(Kind.TOKEN_PRINTLN);
      eatToken(Kind.TOKEN_LPAREN);
      parseExp();
      eatToken(Kind.TOKEN_RPAREN);
      eatToken(Kind.TOKEN_SEMI);
    }
    else if(current.kind == Kind.TOKEN_ID){
      advance();
      if(current.kind == Kind.TOKEN_ASSIGN){
        advance();
        parseExp();
        eatToken(Kind.TOKEN_SEMI);
      }
      else if(current.kind == Kind.TOKEN_LBRACK){
        advance();
        parseExp();
        eatToken(Kind.TOKEN_RBRACK);
        eatToken(Kind.TOKEN_ASSIGN);
        parseExp();
        eatToken(Kind.TOKEN_SEMI);
      }
      else{
        System.out.println("Bug at line 271");
        error();
      }
    }
    else{
      error();
    }
    return;
  }

  // Statements -> Statement Statements
  // ->
  private void parseStatements()
  {
    while (current.kind == Kind.TOKEN_LBRACE || current.kind == Kind.TOKEN_IF
        || current.kind == Kind.TOKEN_WHILE
        || current.kind == Kind.TOKEN_SYSTEM || current.kind == Kind.TOKEN_ID) {
      parseStatement();
    }
    return;
  }

  // Type -> int []
  // -> boolean
  // -> int
  // -> id
  private void parseType()
  {
    // Lab1. Exercise 4: Fill in the missing code
    // to parse a type.
    if (current.kind == Kind.TOKEN_INT){
      advance();
      if(current.kind == Kind.TOKEN_LBRACK){
        advance();
        eatToken(Kind.TOKEN_RBRACK);
      }
    }
    else if(current.kind == Kind.TOKEN_BOOLEAN  || current.kind == Kind.TOKEN_ID){
      advance();
    }
    else error();
    //new util.Todo();
  }

  // VarDecl -> Type id ;
  private void parseVarDecl()
  {
    // to parse the "Type" nonterminal in this method, instead of writing
    // a fresh one.
    Token type_backup = current;
    parseType();
    if(current.kind == Kind.TOKEN_ID){
      eatToken(Kind.TOKEN_ID);
      eatToken(Kind.TOKEN_SEMI);
    }
    else if(current.kind == Kind.TOKEN_ASSIGN){
      //if no id follows an id : should be an assign statement
      lexer.rollBackToken(current);
      lexer.rollBackToken(type_backup);
      advance();
      parseStatements();
    }
    return;
  }

  // VarDecls -> VarDecl VarDecls
  // ->
  private void parseVarDecls()
  {
    while (current.kind == Kind.TOKEN_INT || current.kind == Kind.TOKEN_BOOLEAN
        || current.kind == Kind.TOKEN_ID) {
      parseVarDecl();
    }
    return;
  }

  // FormalList -> Type id FormalRest*
  // ->
  // FormalRest -> , Type id
  private void parseFormalList()
  {
    if (current.kind == Kind.TOKEN_INT || current.kind == Kind.TOKEN_BOOLEAN
        || current.kind == Kind.TOKEN_ID) {
      parseType();
      eatToken(Kind.TOKEN_ID);
      while (current.kind == Kind.TOKEN_COMMER) {
        advance();
        parseType();
        eatToken(Kind.TOKEN_ID);
      }
    }
    return;
  }

  // Method -> public Type id ( FormalList )
  // { VarDecl* Statement* return Exp ;}
  private void parseMethod()
  {
    // Lab1. Exercise 4: Fill in the missing code
    // to parse a method.
    //new util.Todo();
    eatToken(Kind.TOKEN_PUBLIC);
    parseType();
    eatToken(Kind.TOKEN_ID);
    eatToken(Kind.TOKEN_LPAREN);
    parseFormalList();
    eatToken(Kind.TOKEN_RPAREN);
    eatToken(Kind.TOKEN_LBRACE);
    parseVarDecls();
    parseStatements();
    eatToken(Kind.TOKEN_RETURN);
    parseExp();
    eatToken(Kind.TOKEN_SEMI);
    eatToken(Kind.TOKEN_RBRACE);
  }

  // MethodDecls -> MethodDecl MethodDecls
  // ->
  private void parseMethodDecls()
  {
    while (current.kind == Kind.TOKEN_PUBLIC) {
      parseMethod();
    }
    return;
  }

  // ClassDecl -> class id { VarDecl* MethodDecl* }
  // -> class id extends id { VarDecl* MethodDecl* }
  private void parseClassDecl()
  {
    eatToken(Kind.TOKEN_CLASS);
    eatToken(Kind.TOKEN_ID);
    if (current.kind == Kind.TOKEN_EXTENDS) {
      eatToken(Kind.TOKEN_EXTENDS);
      eatToken(Kind.TOKEN_ID);
    }
    eatToken(Kind.TOKEN_LBRACE);
    parseVarDecls();
    parseMethodDecls();
    eatToken(Kind.TOKEN_RBRACE);
    return;
  }

  // ClassDecls -> ClassDecl ClassDecls
  // ->
  private void parseClassDecls()
  {
    while (current.kind == Kind.TOKEN_CLASS) {
      parseClassDecl();
    }
    return;
  }

  // MainClass -> class id
  // {
  // public static void main ( String [] id )
  // {
  // Statements
  // }
  // }
  private void parseMainClass()
  {
    // Lab1. Exercise 4: Fill in the missing code
    // to parse a main class as described by the
    // grammar above.
    //new util.Todo();
    eatToken(Kind.TOKEN_CLASS);
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
    eatToken(Kind.TOKEN_ID);
    eatToken(Kind.TOKEN_RPAREN);
    eatToken(Kind.TOKEN_LBRACE);
    parseStatements();
    eatToken(Kind.TOKEN_RBRACE);
    eatToken(Kind.TOKEN_RBRACE);
  }

  // Program -> MainClass ClassDecl*
  private void parseProgram()
  {
    parseMainClass();
    parseClassDecls();
    eatToken(Kind.TOKEN_EOF);
    return;
  }

  public ast.Ast.Program.T parse()
  {
    parseProgram();
    return null;
  }
}
