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

    //�������Ͷ���
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

    //���ʶ���:�����������������
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
    ����˵��:
     line		���ն˶�����ַ���;	      ��ǰ��ָλ���ڼ����� iCurPos, �ַ�Ϊch
     token		����ʶ��ĵ����ַ�������ǰ��ָλ���ڼ����� iIndex
     sym		����ʶ��ĵ��ʷ�������
     word		�����ֱ� �����ֵ���������
     Symlist 	���ű����ʶ����ĵ��ʼ����࣬���磺  (token, sym)

     Err		������Ŀ����Err=0�����ɹ�
    */
    String line;
    int iCurPos;
    char[] token;
    symbol sym;
    String[] word = {"begin","boolean","class", "do", "else","end","extends","false", "if", "int","length","main","new","public",
            "return","static","String","System.out.println", "then","this","true","void", "while"};
    ArrayList<aWord> Symlist;
    int Err;

    //��������
    public static void main(String[] args) throws FileNotFoundException {
        Scanner in = new Scanner(new FileInputStream("in.txt"));
        while (in.hasNextLine()) {
            lex lex = new lex();
            lex.Symlist = new ArrayList<aWord>();
            lex.Symlist.clear();
            System.out.print("�������򴮣���. ���� ��");
            lex.line = in.nextLine().trim();
            System.out.println(lex.line);
            lex.iCurPos = 0;
            lex.Err = 0;


            //==========�ʷ�&�﷨������һ�����==========
            lex.getSym();
            lex.block();
            if (lex.Err == 0) {
//          ��ûʹ�õ�
//            ������
                for (StringBuffer sb : lex.resultList) {
                    System.out.print(sb);
                }
                System.out.print("\n����" + (lex.identList.size() - lex.usedIdent.size()) + "������δʹ��:");
                for (String s : lex.identList) {
                    if (!lex.usedIdent.contains(s)) {
                        System.out.print(s + " ");
                    }
                }
                //������ű�
                System.out.print("\n");
                for (int i = 0; i < lex.Symlist.size(); i++) {
                    System.out.print(lex.Symlist.get(i).toString().trim() + " ");
                }

                System.out.println("\nsyntax parses success!\n\n");
            } else
                System.out.println("syntax parses fail!\n\n");
        }
    }

    //���ն˶���һ�г���
//    public String getProgram() {
//            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
//            line = "";
//            try {
//                do {
//                    System.out.print("�������򴮣���. ���� ��");
//                    line = in.readLine().trim();
//                } while (line.endsWith(".") == false);
//                in.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            return line;
//    }

    //ʶ���Ƿ�Ϊһ���ؼ��֣� ���ض�Ӧ���͡��ö��ַ����ҷ�
    public symbol getKeyWordType() {
        String strToken = (new String(token)).trim();
        symbol sym = symbol.ident;
        int IsKeyword = -1;
        //���ҹؼ����б�begin,do, end, if, then, while
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
            //enum��valueof�Ǹ���object���䵽���͡�
            sym = symbol.valueOf(strToken.concat("sym"));
        }
        return sym;
    }

    //����һ�����ʵ����ű���
    public void AddaWordtoList(symbol symtype) {
        aWord aWord;
        aWord = new aWord((new String(token)).trim(), symtype);
        Symlist.add(aWord);
    }

    //�ʷ������� ������ʶ��һ������
    public void getSym() {
        flag = false;
        tempStr = new StringBuffer();
        int iIndex = 0;
        token = new char[20];
        sym = symbol.nil;
        char ch = line.charAt(iCurPos++);
        tempStr.append(ch);
        //ɨ�����С��ո��
        while (ch == ' ' || ch == (char) 10 || ch == (char) 11) {
            ch = line.charAt(iCurPos++);
            tempStr.append(ch);
        }

        //ʶ��IDENT��ʶ����Keyword�ؼ���
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

        //ʶ��NUMBER
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
            //�����ִ�ת��Ϊ����
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
//                          ����ע��
                            iCurPos = lastPos;
                        } else {
//                          ��ע�� �Ӹ��ո��ȥ
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
        //û��ƥ�䵽���ʵķ���
        if (sym == symbol.nil && !flag) {
            System.out.println("Error 10: Position " + iCurPos + " occur the unexpected char \'" + ch + "\'.");
        } else if (flag) {
            getSym();
        } else {
            //�ѵ��ʼӵ����ű���ȥ
            AddaWordtoList(sym);
        }
    }

    //====�﷨������ڣ������====
    //TIPS����ǰ��һ��sym
    void block() {
        //b a<=10 end.
        if (sym != symbol.beginsym) {
            error(11);
        } else {
            resultList.add(tempStr);
            //��ǰ��һ��
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

    //���
    void statement() {
        /********************************************
         *************** TODO: ��䴦�� *************
         *******************************************/
        if (sym == symbol.ident) {
            newIdent = new String(token).trim();
            if (!identList.contains(newIdent)) {
                Err++;
                //�����ڱ���
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
                //�﷨����
                error(12);
        }
    }


    //��ֵ���
    void assignStatement() {
        /********************************************
         *************** TODO: ��ֵ��䴦�� *************
         *******************************************/
        //�����ǰ�� ��= ֱ�ӽ����
        if (sym == symbol.becomes) {
            getSym();
            resultList.add(tempStr);
            expression();
        } else {
            error(31);
        }
    }


    //�������
    void ifStatement() {
        /********************************************
         *************** TODO: ������䴦�� *************
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
            // ȱ�� then ����
            error(33);
        }
    }


    //ѭ�����
    void loopStatement() {
        /********************************************
         *************** TODO: ѭ����䴦�� *************
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
            //ȱ�� do
            error(32);
        }
    }

    //��������

    void varDefinition() {
        /*
         * @Description: ����Ƕ��� ��ǰ��һ�� A���Ǳ�ʶ��    1�����ھͱ��� 2�������ھʹ���
         *                                   B�����Ǳ�ʶ��  ����
         *                  ���Ƕ��� ��ǰ��һ�� A�����Ǳ�ʶ��  1���ֺŽ��� 2�����Ƿֺż���
         *                                    B���Ǳ�ʶ��  ����
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
                    //�����ظ�
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


    //���ʽ
    void expression() {
        /********************************************
         *************** TODO: ���ʽ���� *************
         *******************************************/
        if (sym == symbol.plus || sym == symbol.minus) {
            while (sym == symbol.plus || sym == symbol.minus) {
                //ֱ����һ��
                getSym();
                resultList.add(tempStr);
                //ֱ����һ�� term��������Ǹ������ǲ���+ -����while
                term();
            }
        } else {
            term();
            if (Err > 0) {
                return;
            }
            while (sym == symbol.plus || sym == symbol.minus) {
                //ֱ����һ��
                getSym();
                resultList.add(tempStr);
                //ֱ����һ�� term��������Ǹ������ǲ���+ -����while
                term();
            }
        }
    }

    //��
    void term() {
        /********************************************
         *************** TODO: ��� *************
         *******************************************/
        // ��fac�Ľ����ж�
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

    //����
    void factor() {
        /********************************************
         *************** TODO: ���Ӵ��� *************
         *******************************************/
        // ident ���� number
        if (sym == symbol.ident || sym == symbol.number) {
            if (sym == symbol.ident) {
                newIdent = new String(token).trim();
                if (!identList.contains(newIdent)) {
                    Err++;
                    //�����ڱ���
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
        // ����ǣ�
        if (sym == symbol.lparen) {
            getSym();
            resultList.add(tempStr);
            expression();
            // ��expression ������ķ��� ���ǲ���������
            if (Err > 0) {
                return;
            }
            if (sym != symbol.rparen) {
                //�����Ų�ƥ��
                error(61);
            } else {
                getSym();
                resultList.add(tempStr);
            }

        } else {
            //����
            error(60);
        }
    }


    //����
    void condition() {
        /********************************************
         *************** TODO: �������� *************
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
            //�������Ų��Ϸ�
            error(70);
        }
    }

    //���������ݴ��������������Ϣ
    /*
     * ������滮��
     * 1x	�������ʷ�����
     * 2x	�����������
     * 3x	���������
     * 4x	���������ʽ
     * 5x	��������
     * 6x	����������
     * 7x	����������
     * 10x	��������������
     * */
    int error(int i) {
        switch (i) {
            case 11:
                System.out.print("Error " + i + ": �����ȱ�ٿ�ʼ����begin.");
                break;
            /********************************************
             *************** TODO: ������ *************
             *******************************************/

            case 12:
                System.out.print("Error " + i + ": û�и��������");
                break;
            case 20:
                System.out.print("Error " + i + ": �����ȱ�ٽ�������end.");
                break;
            case 21:
                System.out.print("Error " + i + ": ������������ֹ");
                break;
            case 31:
                System.out.print("Error " + i + ": ��ֵ���ȱ��:=");
                break;
            case 32:
                System.out.print("Error " + i + ": ѭ�����ȱ��do");
                break;
            case 33:
                System.out.print("Error " + i + ": �������ȱ��then");
                break;
            case 41:
                System.out.print("Error " + i + ": ��������");
                break;
            case 42:
                System.out.print("Error " + i + ": �����ظ�");
                break;
            case 43:
                System.out.print("Error " + i + ": ����������");
                break;
            case 60:
                System.out.print("Error " + i + ": ���Ӳ��Ϸ�");
                break;
            case 61:
                System.out.print("Error " + i + ": ���������Ų�ƥ��");
                break;
            case 70:
                System.out.print("Error " + i + ": ���������ϲ��Ϸ�");
                break;
            default:
                break;
        }
        System.out.println(" [pos=" + iCurPos + "; Token=" + new String(token).trim() + "; sym=" + sym.iIndex + "] \n");
        Err++;
        return Err;
    }

}


