
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Lexer {
    private static final char EOF = 0;
    private Parser yyparser;            // parent parser object
    private java.io.Reader reader;      // input stream
    public int lineno;                  // line number
    public int column;                  // column
    public int realColNum;              // keeps column number track of where a word/character ends

    private static final int bufferSize = 10;
    private char[] buffer1 = new char[bufferSize];
    private char[] buffer2 = new char[bufferSize];
    private int currentIndex = 0;
    private boolean firstBuffer = true;
    int readCount = 0;

    public String[] keys = new String[]{"int", "print", "if", "else", "while", "void"};
    List keywords = Arrays.asList(keys);

    public Lexer(java.io.Reader reader, Parser yyparser) throws Exception {
        this.reader = reader;
        this.yyparser = yyparser;
        lineno = 1;
        column = 0;
        realColNum = 0;
        readFile();
    }

    public void readFile() throws IOException {
        if (firstBuffer) {
            readCount = reader.read(buffer1);
        } else {
            readCount = reader.read(buffer2);
        }

        if (readCount == -1) {
            reader.close();
        }
    }

    public char NextChar() throws Exception {
        if (currentIndex < readCount) {
            if (firstBuffer) {
                return buffer1[currentIndex++];
            } else {
                return buffer2[currentIndex++];
            }
        } else if (readCount != -1) {
            firstBuffer = !firstBuffer;
            currentIndex = 0;
            readFile();
            return NextChar();
        } else {
            return EOF;
        }
    }

    public int Fail() {
        return -1;
    }

    public int yylex() throws Exception {
        int state = 0;
        StringBuilder lex = new StringBuilder();
        while (true) {
            char c;
            switch (state) {
                case 0:
                    c = NextChar();
                    // checking for new line (windows)
                    if (c == '\r' && ((c = NextChar()) == '\n')) {
                        column = 0;
                        realColNum = 0;
                        lineno++;
                        continue;
                    }
                    // checking for new line (linux)
                    else if (c == '\n') {
                        column = 0;
                        realColNum = 0;
                        lineno++;
                        continue;
                    }
                    // checking for space
                    else if (c == ' ') {
                        realColNum++;
                        continue;
                    }
                    // checking for a tab key
                    else if (c == '\t') {
                        realColNum++;
                        continue;
                    } else if (c == ';') {
                        realColNum++;
                        state = 1;
                        continue;
                    } else if (c == '(') {
                        realColNum++;
                        state = 2;
                        continue;
                    } else if (c == ')') {
                        realColNum++;
                        state = 3;
                        continue;
                    } else if (c == '<') {
                        realColNum++;
                        state = 4;
                        continue;
                    } else if (c == '>') {
                        realColNum++;
                        state = 5;
                        continue;
                    } else if (c == '-') {
                        realColNum++;
                        state = 6;
                        continue;
                    } else if (c == '+') {
                        realColNum++;
                        state = 7;
                        continue;
                    } else if (c == '*') {
                        realColNum++;
                        state = 8;
                        continue;
                    } else if (c == '/') {
                        realColNum++;
                        state = 9;
                        continue;
                    } else if (c == ',') {
                        realColNum++;
                        state = 10;
                        continue;
                    } else if (c == '=') {
                        realColNum++;
                        state = 11;
                        continue;
                    } else if (c == '!') {
                        realColNum++;
                        state = 12;
                        continue;
                    } else if (c == '{') {
                        realColNum++;
                        state = 13;
                        continue;
                    } else if (c == '}') {
                        realColNum++;
                        state = 14;
                        continue;
                    } else if (c >= '0' && c <= '9') {
                        lex.append(c);
                        realColNum++;
                        state = 15;
                        continue;
                    } else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                        lex.append(c);
                        realColNum++;
                        state = 18;
                        continue;
                    } else if (c == EOF) {
                        state = 9999;
                        continue;
                    } else {
                        realColNum++;
                        column = realColNum;
                        return Fail();
                    }

                case 1:
                    column = realColNum;
                    yyparser.yylval = new ParserVal((Object) ";");
                    return Parser.SEMI;
                case 2:
                    column = realColNum;
                    yyparser.yylval = new ParserVal((Object) "(");
                    return Parser.LPAREN;
                case 3:
                    column = realColNum;
                    yyparser.yylval = new ParserVal((Object) ")");
                    return Parser.RPAREN;
                case 4:
                    c = NextChar();
                    if (c == '>') {
                        column = realColNum;
                        realColNum++;
                        yyparser.yylval = new ParserVal((Object) "<>");
                        return Parser.RELOP; // Treat "<>" as a single relational operator
                    } else if (c == '=') {
                        column = realColNum;
                        realColNum++;
                        yyparser.yylval = new ParserVal((Object) "<=");
                        return Parser.RELOP;
                    } else if (c == '-') {
                        column = realColNum;
                        realColNum++;
                        yyparser.yylval = new ParserVal((Object) "<-");
                        return Parser.ASSIGN;
                    } else {
                        column = realColNum;
                        currentIndex--;
                        yyparser.yylval = new ParserVal((Object) "<");
                        return Parser.RELOP;
                    }
                case 5:
                    c = NextChar();
                    column = realColNum;

                    if (c == '=') {
                        realColNum++;
                        yyparser.yylval = new ParserVal((Object) ">=");
                    } else {
                        currentIndex--;
                        yyparser.yylval = new ParserVal((Object) ">");
                    }
                    return Parser.RELOP;
                case 6:
                    c = NextChar();
                    column = realColNum;

                    if (c == '>') {
                        realColNum++;
                        yyparser.yylval = new ParserVal((Object) "->");
                        return Parser.FUNCRET;
                    } else {
                        currentIndex--;
                        yyparser.yylval = new ParserVal((Object) "-");
                        return Parser.OP;
                    }
                case 7:
                    column = realColNum;
                    yyparser.yylval = new ParserVal((Object) "+");
                    return Parser.OP;
                case 8:
                    column = realColNum;
                    yyparser.yylval = new ParserVal((Object) "*");
                    return Parser.OP;
                case 9:
                    column = realColNum;
                    yyparser.yylval = new ParserVal((Object) "/");
                    return Parser.OP;
                case 10:
                    column = realColNum;
                    yyparser.yylval = new ParserVal((Object) ",");
                    return Parser.COMMA;
                case 11:
                    column = realColNum;
                    yyparser.yylval = new ParserVal((Object) "=");
                    return Parser.RELOP;
                case 12:
                    c = NextChar();

                    if (c == '=') {
                        column = realColNum;
                        realColNum++;
                        yyparser.yylval = new ParserVal((Object) "!=");
                        return Parser.RELOP;
                    }

                    realColNum++;
                    column = realColNum;
                    return Fail();
                case 13:
                    column = realColNum;
                    yyparser.yylval = new ParserVal((Object) "{");
                    return Parser.BEGIN;
                case 14:
                    column = realColNum;
                    yyparser.yylval = new ParserVal((Object) "}");
                    return Parser.END;
                case 15:
                    c = NextChar();

                    if (c >= '0' && c <= '9') {
                        lex.append(c);
                        realColNum++;
                        state = 15;
                        continue;
                    }
                    if (c == '.') {
                        lex.append(c);
                        realColNum++;
                        state = 16;
                        continue;
                    }
                    column = realColNum - lex.length() + 1;
                    currentIndex--;
                    yyparser.yylval = new ParserVal(lex);
                    return Parser.NUM;
                case 16:
                    c = NextChar();
                    realColNum++;

                    if (c >= '0' && c <= '9') {
                        lex.append(c);
                        state = 17;
                        continue;
                    }

                    column = realColNum - 2;
                    return Fail();
                case 17:
                    c = NextChar();
                    if (c >= '0' && c <= '9') {
                        lex.append(c);
                        realColNum++;
                        state = 17;
                        continue;
                    }

                    currentIndex--;
                    column = realColNum - lex.length() + 1;
                    yyparser.yylval = new ParserVal(lex);
                    return Parser.NUM;
                case 18:
                    c = NextChar();
                    if (c == '_' || (c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                        lex.append(c);
                        realColNum++;

                        String z = String.valueOf(lex);
                        if (keywords.contains(z)) {
                            yyparser.yylval = new ParserVal((Object) z);
                            column = realColNum - z.length() + 1;
                            if (z.equals("int")) return Parser.INT;
                            if (z.equals("print")) return Parser.PRINT;
                            if (z.equals("if")) return Parser.IF;
                            if (z.equals("else")) return Parser.ELSE;
                            if (z.equals("while")) return Parser.WHILE;
                            if (z.equals("void")) return Parser.VOID;
                        } else {
                            state = 18;
                            continue;
                        }
                    }
                    column = realColNum - lex.length() + 1;
                    currentIndex--;
                    yyparser.yylval = new ParserVal(lex);
                    return Parser.ID;
                case 9999:
                    return EOF;
            }
        }
    }
}