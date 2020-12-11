package test;


import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

public class lex {

    private String newIdent = "";
    private StringBuffer tempStr = new StringBuffer();
    private Stack<StringBuffer> identStack = new Stack<>();
    private List<StringBuffer> resultList = new ArrayList<>();
    private List<String> identList = new ArrayList<>(),
            usedIdent = new ArrayList<>();
    private Boolean flag = false;

    //符号类型定义
    public enum symbol {
        period(".", 0), plus("+", 1), minus("-", 2), times("*", 3), slash("/", 4),
        eql("=", 5), neq("<>", 6), lss("<", 7), leq("<=", 8), gtr(">", 9),
        geq(">=", 10), lparen("(", 11), rparen(")", 12), semicolon(";", 13), becomes(":=", 14),
        beginsym("begin", 15), endsym("end", 16), ifsym("if", 17), thensym("then", 18),
        whilesym("while", 19), dosym("do", 20), ident("IDENT", 21), number("number", 22),
        nil("nil", 23), intsym("int", 24), commasym(",", 25),classsym("class",26)
        ,lbracket("{",27),rbracket("}",28),publicsym("public",29),staticsym("static",30),
        voidsym("void",31),mainsym("main",32),stringsym("String",33),lbraces("{",34),rbraces("}",35),
        returnsym("return",36),newsym("new",37),booleansym("boolean",38),andsym("&&",39),
        truesym("true",40),falsesym("false",41),thissym("this",42),
        notsym("!",43),lengthsym("length",44),sysosym("System.out.println",45),elsesym("else",46),
        ;

        private String strName;
        private int iIndex;

        private symbol(String name, int index) {
            this.strName = name;
            this.iIndex = index;
        }

        public static symbol getType(char[] strlist) {
            String str = (new String(strlist)).trim();
            return symbol.valueOf(str);
        }

        public static symbol getType(String str) {
            return symbol.valueOf(str);
        }

        public String toString() {
            return this.iIndex + "_" + this.strName;
        }
    }

    ;

    //单词定义:（单词名，单词类别）
    public class aWord {
        String name;
        symbol symtype;

        private aWord(char[] name, symbol symtype) {
            this.name = new String(name);
            this.symtype = symtype;
        }

        private aWord(String name, symbol symtype) {
            this.name = name;
            this.symtype = symtype;
        }

        public String toString() {
            return "(" + this.symtype.iIndex + "," + this.name.trim() + ")";
        }
    }

    /*
    变量说明:
     line		从终端读入的字符串;	      当前所指位置在计数器 iCurPos, 字符为ch
     token		正在识别的单词字符串；当前所指位置在计数器 iIndex
     sym		正在识别的单词符号种类
     word		保留字表 ，按字典升序排序
     Symlist 	符号表，存放识别出的单词及种类，形如：  (token, sym)

     Err		错误数目。当Err=0解析成功
    */
    String line;
    int iCurPos;
    char[] token;
    symbol sym;
    String[] word = {"begin","boolean","class", "do", "else","end","extends","false", "if", "int","length","main","new","public",
            "return","static","String","System.out.println", "then","this","true","void", "while"};
    ArrayList<aWord> Symlist;
    int Err;

    //主函数：
    public static void main(String[] args) throws FileNotFoundException {
        Scanner in = new Scanner(new FileInputStream("in.txt"));
        while (in.hasNextLine()) {
            lex lex = new lex();
            lex.Symlist = new ArrayList<aWord>();
            lex.Symlist.clear();
            System.out.print("请读入程序串，以. 结束 ：");
            lex.line = in.nextLine().trim();
            System.out.println(lex.line);
            lex.iCurPos = 0;
            lex.Err = 0;


            //==========词法&语法分析：一趟完成==========
            lex.getSym();
            lex.block();
            if (lex.Err == 0) {
//          有没使用的
//            都可以
                for (StringBuffer sb : lex.resultList) {
                    System.out.print(sb);
                }
                System.out.print("\n还有" + (lex.identList.size() - lex.usedIdent.size()) + "个变量未使用:");
                for (String s : lex.identList) {
                    if (!lex.usedIdent.contains(s)) {
                        System.out.print(s + " ");
                    }
                }
                //输出符号表
                System.out.print("\n");
                for (int i = 0; i < lex.Symlist.size(); i++) {
                    System.out.print(lex.Symlist.get(i).toString().trim() + " ");
                }

                System.out.println("\nsyntax parses success!\n\n");
            } else
                System.out.println("syntax parses fail!\n\n");
        }
    }

    //从终端读入一行程序
//    public String getProgram() {
//            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
//            line = "";
//            try {
//                do {
//                    System.out.print("请读入程序串，以. 结束 ：");
//                    line = in.readLine().trim();
//                } while (line.endsWith(".") == false);
//                in.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            return line;
//    }

    //识别是否为一个关键字： 返回对应类型。用二分法查找法
    public symbol getKeyWordType() {
        String strToken = (new String(token)).trim();
        symbol sym = symbol.ident;
        int IsKeyword = -1;
        //查找关键字列表：begin,do, end, if, then, while
        int iLbound = 0;
        int iUBound = word.length - 1;

        while (iLbound <= iUBound) {
            int iMid = (iLbound + iUBound) / 2;
            IsKeyword = strToken.compareTo(word[iMid]);
            if (IsKeyword == 0) {
                break;
            } else if (IsKeyword < 0) {
                iUBound = iMid - 1;
            } else if (IsKeyword > 0) {
                iLbound = iMid + 1;
            }
        }
        if (IsKeyword == 0) {
            //enum的valueof是根据object反射到类型。
            sym = symbol.valueOf(strToken.concat("sym"));
        }
        return sym;
    }

    //增加一个单词到符号表里
    public void AddaWordtoList(symbol symtype) {
        aWord aWord;
        aWord = new aWord((new String(token)).trim(), symtype);
        Symlist.add(aWord);
    }

    //词法分析主 函数：识别一个符号
    public void getSym() {
        flag = false;
        tempStr = new StringBuffer();
        int iIndex = 0;
        token = new char[20];
        sym = symbol.nil;
        char ch = line.charAt(iCurPos++);
        tempStr.append(ch);
        //扫过空行、空格等
        while (ch == ' ' || ch == (char) 10 || ch == (char) 11) {
            ch = line.charAt(iCurPos++);
            tempStr.append(ch);
        }

        //识别IDENT标识符或Keyword关键字
        if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z') {
            token[iIndex++] = ch;
            ch = line.charAt(iCurPos++);
            tempStr.append(ch);
            while (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9' || ch == '_') {
                token[iIndex++] = ch;
                ch = line.charAt(iCurPos++);
                tempStr.append(ch);
            }
            iCurPos--;
            tempStr.deleteCharAt(tempStr.length() - 1);
            sym = getKeyWordType();
        }

        //识别NUMBER
        else if ((ch >= '0') && (ch <= '9')) {
            token[iIndex++] = ch;
            ch = line.charAt(iCurPos++);
            tempStr.append(ch);
            while (ch >= '0' && ch <= '9') {
                token[iIndex++] = ch;
                ch = line.charAt(iCurPos++);
                tempStr.append(ch);
            }
            iCurPos--;
            tempStr.deleteCharAt(tempStr.length() - 1);
            //将数字串转化为数字
            sym = symbol.number;
        } else {
            switch (ch) {
                case ':': {
                    token[iIndex++] = ch;
                    ch = line.charAt(iCurPos++);
                    tempStr.append(ch);
                    if (ch == '=') {
                        token[iIndex++] = ch;
                        sym = symbol.becomes;
                        identStack.push(new StringBuffer(Symlist.get(Symlist.size() - 1).name.trim()));
                    } else {
                        System.out.println("program occured the unexpected ch: " + ch);
                    }
                    break;
                }
                case '=': {
                    token[iIndex++] = ch;
                    sym = symbol.eql;
                    break;
                }
                case '>': {
                    token[iIndex++] = ch;
                    ch = line.charAt(iCurPos++);
                    tempStr.append(ch);
                    if (ch == '=') {
                        sym = symbol.geq;
                    } else {
                        iCurPos--;
                        tempStr.deleteCharAt(tempStr.length() - 1);
                        sym = symbol.gtr;
                    }
                    break;
                }
                case '<': {
                    token[iIndex++] = ch;
                    ch = line.charAt(iCurPos++);
                    tempStr.append(ch);
                    if (ch == '=') {
                        sym = symbol.leq;
                    } else {
                        iCurPos--;
                        tempStr.deleteCharAt(tempStr.length() - 1);
                        sym = symbol.lss;
                    }
                    break;
                }
                case '+': {
                    token[iIndex++] = ch;
                    sym = symbol.plus;
                    break;
                }
                case '-': {
                    token[iIndex++] = ch;
                    sym = symbol.minus;
                    break;
                }
                case '*': {
                    token[iIndex++] = ch;
                    sym = symbol.times;
                    break;
                }
                case '/': {
                    token[iIndex++] = ch;
                    ch = line.charAt(iCurPos++);
                    int lastPos = iCurPos;
                    tempStr.append(ch);
                    if (ch != '*') {
                        sym = symbol.slash;
                        tempStr.deleteCharAt(tempStr.length() - 1);
                        iCurPos--;
                    } else {
                        ch = line.charAt(iCurPos++);
                        while ((ch != '*' || line.charAt(iCurPos) != '/') && ch != '.') {
                            ch = line.charAt(iCurPos++);
                        }
                        if (ch == '.') {
//                          不是注释
                            iCurPos = lastPos;
                        } else {
//                          是注释 加个空格进去
                            flag = true;
                            resultList.add(new StringBuffer(" "));
                            iCurPos += 2;
                        }
                    }

                    break;
                }
                case '(': {
                    token[iIndex++] = ch;
                    sym = symbol.lparen;
                    break;
                }
                case ')': {
                    token[iIndex++] = ch;
                    sym = symbol.rparen;
                    break;
                }
                case ';': {
                    token[iIndex++] = ch;
                    sym = symbol.semicolon;

                    break;
                }
                case ',': {
                    token[iIndex++] = ch;
                    sym = symbol.commasym;
                    break;
                }
                case '.': {
                    token[iIndex++] = ch;
                    sym = symbol.period;
                    break;
                }
                default:
            }
        }
        //没有匹配到合适的符号
        if (sym == symbol.nil && !flag) {
            System.out.println("Error 10: Position " + iCurPos + " occur the unexpected char \'" + ch + "\'.");
        } else if (flag) {
            getSym();
        } else {
            //把单词加到符号表中去
            AddaWordtoList(sym);
        }
    }

    //====语法分析入口：程序块====
    //TIPS：往前看一个sym
    void block() {
        //b a<=10 end.
        if (sym != symbol.beginsym) {
            error(11);
        } else {
            resultList.add(tempStr);
            //往前看一个
            getSym();
            resultList.add(tempStr);
            statement();
            if (Err > 0) {
                return;
            }
            while (sym == symbol.semicolon && Err == 0) {
                if (identStack.size() > 0) {
                    StringBuffer printStr = new StringBuffer(" print(" + identStack.peek() + ") ");
                    identStack.pop();
                    resultList.add(printStr);
                }
                getSym();
                resultList.add(tempStr);
                statement();
            }
            if (Err > 0) {
                return;
            }
            //begin a := 10 end end.
            if (sym == symbol.endsym) {
                StringBuffer beforeStr = resultList.get(resultList.size() - 1);
                resultList.remove(resultList.size() - 1);
                if (identStack.size() > 0) {
                    StringBuffer printStr = new StringBuffer("; print(" + identStack.peek() + ") ");
                    identStack.pop();
                    resultList.add(printStr);
                }
                resultList.add(beforeStr);
                getSym();
                resultList.add(tempStr);
                if (sym != symbol.period)
                    error(21);
            } else
                // begin a := 10 .
                error(20);

        }
    }

    //语句
    void statement() {
        /********************************************
         *************** TODO: 语句处理 *************
         *******************************************/
        if (sym == symbol.ident) {
            newIdent = new String(token).trim();
            if (!identList.contains(newIdent)) {
                Err++;
                //不存在变量
                error(43);
                return;
            }
            if (!usedIdent.contains(newIdent))
                usedIdent.add(newIdent);
            getSym();
            resultList.add(tempStr);
            assignStatement();
        } else if (sym == symbol.ifsym) {
            getSym();
            resultList.add(tempStr);
            ifStatement();
        } else if (sym == symbol.whilesym) {
            getSym();
            resultList.add(tempStr);
            loopStatement();
        } else if (sym == symbol.intsym) {
            getSym();
            resultList.add(tempStr);
            varDefinition();
        } else {
            if (!flag)
                //语法错误
                error(12);
        }
    }


    //赋值语句
    void assignStatement() {
        /********************************************
         *************** TODO: 赋值语句处理 *************
         *******************************************/
        //如果当前是 ：= 直接进入表单
        if (sym == symbol.becomes) {
            getSym();
            resultList.add(tempStr);
            expression();
        } else {
            error(31);
        }
    }


    //条件语句
    void ifStatement() {
        /********************************************
         *************** TODO: 条件语句处理 *************
         *******************************************/
        condition();
        if (Err > 0) {
            return;
        }
        if (sym == symbol.thensym) {
            getSym();
            resultList.add(tempStr);
            statement();
        } else {
            // 缺少 then 错误
            error(33);
        }
    }


    //循环语句
    void loopStatement() {
        /********************************************
         *************** TODO: 循环语句处理 *************
         *******************************************/
        condition();
        if (Err > 0) {
            return;
        }
        if (sym == symbol.dosym) {
            getSym();
            resultList.add(tempStr);
            statement();
        } else {
            //缺少 do
            error(32);
        }
    }

    //变量定义

    void varDefinition() {
        /*
         * @Description: 如果是逗号 向前看一个 A、是标识符    1、存在就报错 2、不存在就存入
         *                                   B、不是标识符  报错
         *                  不是逗号 向前看一个 A、不是标识符  1、分号结束 2、不是分号继续
         *                                    B、是标识符  报错
         *
         * @Param: []
         * @return: void
         * @Author: Mr.Qiu
         * @Date: 2020/11/27
         */
        while (true) {
            if (sym == symbol.commasym) {
                getSym();
                resultList.add(tempStr);
                if (sym == symbol.semicolon) {
                    return;
                }
                if (sym != symbol.ident) {
                    Err++;
                    error(41);
                    return;
                }
            } else if (sym == symbol.ident) {
                newIdent = new String(token).trim();
                if (identList.contains(newIdent)) {
                    Err++;
                    //变量重复
                    error(42);
                    return;
                } else {
                    identList.add(newIdent);
                }
                getSym();
                resultList.add(tempStr);
                if (sym == symbol.semicolon) {
                    return;
                }
                if (sym != symbol.commasym) {
                    Err++;
                    error(41);
                    return;
                }
            }
        }
    }


    //表达式
    void expression() {
        /********************************************
         *************** TODO: 表达式处理 *************
         *******************************************/
        if (sym == symbol.plus || sym == symbol.minus) {
            while (sym == symbol.plus || sym == symbol.minus) {
                //直接下一个
                getSym();
                resultList.add(tempStr);
                //直接下一项 term不满足的那个符号是不是+ -继续while
                term();
            }
        } else {
            term();
            if (Err > 0) {
                return;
            }
            while (sym == symbol.plus || sym == symbol.minus) {
                //直接下一个
                getSym();
                resultList.add(tempStr);
                //直接下一项 term不满足的那个符号是不是+ -继续while
                term();
            }
        }
    }

    //项
    void term() {
        /********************************************
         *************** TODO: 项处理 *************
         *******************************************/
        // 用fac的结束判断
        factor();
        if (Err > 0) {
            return;
        }
        while (sym == symbol.times || sym == symbol.slash) {
            getSym();
            resultList.add(tempStr);
            factor();
        }
    }

    //因子
    void factor() {
        /********************************************
         *************** TODO: 因子处理 *************
         *******************************************/
        // ident 或者 number
        if (sym == symbol.ident || sym == symbol.number) {
            if (sym == symbol.ident) {
                newIdent = new String(token).trim();
                if (!identList.contains(newIdent)) {
                    Err++;
                    //不存在变量
                    error(43);
                    return;
                }
                if (!usedIdent.contains(newIdent))
                    usedIdent.add(newIdent);
            }
            getSym();
            resultList.add(tempStr);
            return;
        }
        // 如果是（
        if (sym == symbol.lparen) {
            getSym();
            resultList.add(tempStr);
            expression();
            // 用expression 不满足的符号 看是不是右括号
            if (Err > 0) {
                return;
            }
            if (sym != symbol.rparen) {
                //右括号不匹配
                error(61);
            } else {
                getSym();
                resultList.add(tempStr);
            }

        } else {
            //其他
            error(60);
        }
    }


    //条件
    void condition() {
        /********************************************
         *************** TODO: 条件处理 *************
         *******************************************/
        expression();
        if (Err > 0) {
            return;
        }
        if (sym == symbol.neq || sym == symbol.eql || sym == symbol.lss || sym == symbol.leq || sym == symbol.gtr || sym == symbol.geq) {
            getSym();
            resultList.add(tempStr);
            expression();
        } else {
            //条件符号不合法
            error(70);
        }
    }

    //函数：根据错误码输出错误信息
    /*
     * 错误码规划：
     * 1x	－－－词法分析
     * 2x	－－－程序块
     * 3x	－－－语句
     * 4x	－－－表达式
     * 5x	－－－项
     * 6x	－－－因子
     * 7x	－－－条件
     * 10x	－－－其它错误
     * */
    int error(int i) {
        switch (i) {
            case 11:
                System.out.print("Error " + i + ": 程序块缺少开始符号begin.");
                break;
            /********************************************
             *************** TODO: 错误处理 *************
             *******************************************/

            case 12:
                System.out.print("Error " + i + ": 没有该语句类型");
                break;
            case 20:
                System.out.print("Error " + i + ": 程序块缺少结束符号end.");
                break;
            case 21:
                System.out.print("Error " + i + ": 程序块非正常终止");
                break;
            case 31:
                System.out.print("Error " + i + ": 赋值语句缺少:=");
                break;
            case 32:
                System.out.print("Error " + i + ": 循环语句缺少do");
                break;
            case 33:
                System.out.print("Error " + i + ": 条件语句缺少then");
                break;
            case 41:
                System.out.print("Error " + i + ": 变量错误");
                break;
            case 42:
                System.out.print("Error " + i + ": 变量重复");
                break;
            case 43:
                System.out.print("Error " + i + ": 变量不存在");
                break;
            case 60:
                System.out.print("Error " + i + ": 因子不合法");
                break;
            case 61:
                System.out.print("Error " + i + ": 因子右括号不匹配");
                break;
            case 70:
                System.out.print("Error " + i + ": 条件语句符合不合法");
                break;
            default:
                break;
        }
        System.out.println(" [pos=" + iCurPos + "; Token=" + new String(token).trim() + "; sym=" + sym.iIndex + "] \n");
        Err++;
        return Err;
    }

}


