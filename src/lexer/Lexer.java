package lexer;

import static control.Control.ConLexer.dump;

import java.awt.desktop.SystemEventListener;
import java.awt.desktop.SystemSleepEvent;
import java.io.InputStream;
import java.io.PushbackInputStream;

import java.util.Stack;
import java.util.function.ToDoubleBiFunction;

import lexer.Token.Kind;
import util.Bug;
import util.Todo;

public class Lexer
{
  private String fname; // the input file name to be compiled
  private PushbackInputStream fstream;
  private int current_line_num = 1;
  private Stack<Token> rollBackStack = new Stack<>();

  private Token current_line_token(Kind kind){
      return new Token(kind, this.current_line_num);
  }

  private boolean is_char_legal(int c){
      return ( c == '_' || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0'  && c <= '9'));
  }

  public Lexer(String fname, InputStream fstream)
  {
    this.fname = fname;
    this.fstream = new PushbackInputStream(fstream);
  }

  // When called, return the next token (refer to the code "Token.java")
  // from the input stream.
  // Return TOKEN_EOF when reaching the end of the input stream.
  private Token nextTokenInternal() throws Exception
  {
    int c = this.fstream.read();
    if (-1 == c)    //end of the file
      // The value for "lineNum" is now "null",
      // you should modify this to an appropriate
      // line number for the "EOF" token.
      return new Token(Kind.TOKEN_EOF, this.current_line_num);

    // skip all kinds of "blanks"
    while (' ' == c || '\t' == c || '\n' == c) {
        if(c == '\n'){
            this.current_line_num += 1;
        }
        c = this.fstream.read();
    }

    if(c == '/'){//ignore line comment
        c = this.fstream.read();
        if(c == '/'){
            do{
                c = this.fstream.read();
            }while(c != '\n');
            this.current_line_num += 1;
            return null;
            //return new Token(Kind.TOKEN_COMMENT_LINE, this.current_line_num-1);
        }
        else if( c == '*' ){
            //System.out.println(new Token(Kind.TOKEN_COMMENT_BLOCK_START,this.current_line_num).toString());
            c = fstream.read();
            if(c == '\n')this.current_line_num += 1;
            int prev_c = 0;
            while(!((c=='/')&&(prev_c=='*'))){
                prev_c = c;
                c = fstream.read();
                if(c == -1) return current_line_token(Kind.TOKEN_EOF);
                if(c == '\n')this.current_line_num += 1;
            }
            //return current_line_token(Kind.TOKEN_COMMENT_BLOCK_END);
            return null;
        }
        else{
            throw new java.lang.Error("Illegal IDENTIFIER: /");
        }
    }


    switch (c) {
        case -1: return current_line_token(Kind.TOKEN_EOF);
        case '+': return current_line_token(Kind.TOKEN_ADD);
        case '-': return current_line_token(Kind.TOKEN_SUB);
        case '*': return current_line_token(Kind.TOKEN_TIMES);
        case '<': return current_line_token(Kind.TOKEN_LT);
        case '=': return current_line_token(Kind.TOKEN_ASSIGN);
        case ',': return current_line_token(Kind.TOKEN_COMMER);
        case '.': return current_line_token(Kind.TOKEN_DOT);
        case '{': return current_line_token(Kind.TOKEN_LBRACE);
        case '[': return current_line_token(Kind.TOKEN_LBRACK);
        case '(': return current_line_token(Kind.TOKEN_LPAREN);
        case '!': return current_line_token(Kind.TOKEN_NOT);
        case '}': return current_line_token(Kind.TOKEN_RBRACE);
        case ']': return current_line_token(Kind.TOKEN_RBRACK);
        case ')': return current_line_token(Kind.TOKEN_RPAREN);
        case ';': return current_line_token(Kind.TOKEN_SEMI);
        case '&':
            c = this.fstream.read();
            if(c == '&') return current_line_token(Kind.TOKEN_AND);
            else throw new java.lang.Error("Illegal sign: &");

        default:
          // Lab 1, exercise 2: supply missing code to
          // lex other kinds of tokens.
          // Hint: think carefully about the basic
          // data structure and algorithms. The code
          // is not that much and may be less than 50 lines. If you
          // find you are writing a lot of code, you
          // are on the wrong way.

          StringBuilder whole_token = new StringBuilder();
          while(is_char_legal(c)){
              whole_token.append((char)c);
              c = this.fstream.read();
          }
          if(whole_token.toString().equals("")){
              throw new java.lang.Error("Illegal char: " + ((char)c));
          }
          //TODO: roll back the extra read char here
            this.fstream.unread(c);

          String whole_token_string = whole_token.toString();
          switch (whole_token_string) {
              case "boolean":
                  return current_line_token(Kind.TOKEN_BOOLEAN);
              case "class":
                  return current_line_token(Kind.TOKEN_CLASS);
              case "else":
                  return current_line_token(Kind.TOKEN_ELSE);
              case "extends":
                  return current_line_token(Kind.TOKEN_EXTENDS);
              case "false":
                  return current_line_token(Kind.TOKEN_FALSE);
              case "if":
                  return current_line_token(Kind.TOKEN_IF);
              case "int":
                  return current_line_token(Kind.TOKEN_INT);
              case "length":
                  return current_line_token(Kind.TOKEN_LENGTH);
              case "main":
                  return current_line_token(Kind.TOKEN_MAIN);
              case "new":
                  return current_line_token(Kind.TOKEN_NEW);
              case "public":
                  return current_line_token(Kind.TOKEN_PUBLIC);
              case "return":
                  return current_line_token(Kind.TOKEN_RETURN);
              case "static":
                  return current_line_token(Kind.TOKEN_STATIC);
              case "this":
                  return current_line_token(Kind.TOKEN_THIS);
              case "true":
                  return current_line_token(Kind.TOKEN_TRUE);
              case "void":
                  return current_line_token(Kind.TOKEN_VOID);
              case "while":
                  return current_line_token(Kind.TOKEN_WHILE);
              case "out":
                  return current_line_token(Kind.TOKEN_OUT);
              case "println":
                  return current_line_token(Kind.TOKEN_PRINTLN);
              case "String":
                  return current_line_token(Kind.TOKEN_STRING);
              case "System":
                  return current_line_token(Kind.TOKEN_SYSTEM);
              default:
                  if (((whole_token_string.charAt(0) >= 'a' && whole_token_string.charAt(0) <= 'z')
                          || (whole_token_string.charAt(0) >= 'A' && whole_token_string.charAt(0) <= 'Z'))) {
                      return new Token(Kind.TOKEN_ID, this.current_line_num, whole_token_string);
                  } else {
                      try {
                          Integer.parseInt(whole_token_string);
                          return new Token(Kind.TOKEN_NUM, this.current_line_num, whole_token_string);
                      } catch (NumberFormatException e) {
                          throw new java.lang.Error("Illegal IDENTIFIER: " + whole_token_string);
                      }
                  }
            }
          }
  }


  public Token nextToken()
  {
    Token t = null;

    if(!rollBackStack.empty()){
        return rollBackStack.pop();
    }

    try {
        do{
            t = this.nextTokenInternal();
            if(t != null){
                System.out.println(t.toString());
            }
        }
        while(t == null);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    if (dump)
      System.out.println(t.toString());
    return t;
  }

  public void rollBackToken(Token t)
  {
      rollBackStack.push(t);
  }
}
