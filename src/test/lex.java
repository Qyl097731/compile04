package test;


import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

public class lex {

    class MethodInfo {
        private String returnType;
        private String methodName;
        private ArrayList<Object> params = new ArrayList<>();
        private Integer paramsNum = 0;
    }

    private MethodInfo methodInfo = new MethodInfo();

    private String className = "";

    private String argeName = "";

    private String newIdent = "";

    private StringBuffer resultStr = new StringBuffer();

    private StringBuffer tempStr = new StringBuffer();

    private Stack<StringBuffer> identStack = new Stack<>();

    private List<String> identList = new ArrayList<>(), usedIdent = new ArrayList<>();

    private List<MethodInfo> methodList = new ArrayList<>();

    private Boolean flag = false;

    private Integer cnt = 0;

    private File file = new File("out.txt");
    private BufferedWriter bw = null;


    //�������Ͷ���
    public enum symbol {
        period(".", 0), plus("+", 1), minus("-", 2), times("*", 3), slash("/", 4),
        eql("=", 5), neq("<>", 6), lss("<", 7), leq("<=", 8), gtr(">", 9),
        geq(">=", 10), lparen("(", 11), rparen(")", 12), semicolon(";", 13), becomes(":=", 14),
        beginsym("begin", 15), endsym("end", 16), ifsym("if", 17), thensym("then", 18),
        whilesym("while", 19), dosym("do", 20), ident("IDENT", 21), number("number", 22),
        nil("nil", 23), intsym("int", 24), commasym(",", 25), classsym("class", 26), lbracket("[", 27), rbracket("]", 28), publicsym("public", 29), staticsym("static", 30),
        voidsym("void", 31), mainsym("main", 32), Stringsym("String", 33), lbraces("{", 34), rbraces("}", 35),
        returnsym("return", 36), newsym("new", 37), booleansym("boolean", 38), andsym("&&", 39),
        truesym("true", 40), falsesym("false", 41), thissym("this", 42),
        notsym("!", 43), lengthsym("length", 44), sysosym("System.out.println", 45), elsesym("else", 46),
        quotation("\"", 47), privatesym("private", 48);

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
    String[] psvm = {"(", "String", "[", "]"};
    String[] word = {"String", "System.out.println", "begin", "boolean", "class", "do", "else", "end",
            "extends", "false", "if", "int", "length", "main", "new", "private", "public",
            "return", "static", "then", "this", "true", "void", "while"};
    ArrayList<aWord> Symlist;
    int Err;

    //��������
    public static void main(String[] args) throws IOException {
        System.out.println("����������룺");
        InputStream is = new FileInputStream("in.txt");
        int iAvail = is.available();
        byte[] bytes = new byte[iAvail];
        is.read(bytes);
        lex lex = new lex();
        lex.line = new String(bytes);
        lex.Symlist = new ArrayList<aWord>();
        System.out.println(lex.line);
        lex.iCurPos = 0;
        lex.Err = 0;
        if (!lex.file.exists()) {
            lex.file.createNewFile();
        }
        lex.bw = new BufferedWriter(new FileWriter(lex.file));
        //==========�ʷ�&�﷨������һ�����==========
        lex.resultStr.append("����ļ�:\n");
        lex.getSym();
        lex.block();
        if (lex.Err == 0) {
            //������ű�
            System.out.println(lex.resultStr);
            for (int i = 0; i < lex.Symlist.size(); i++) {
                System.out.print(lex.Symlist.get(i).toString().trim() + " ");
            }
            System.out.println("\nsyntax parses success!\n\n");
            lex.bw.write(new String(lex.resultStr));
        } else {
            System.out.println("syntax parses fail!\n\n");
        }
        lex.bw.close();
    }


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
            if (strToken.equals("System.out.println")) {
                sym = symbol.sysosym;
            } else {
                sym = symbol.valueOf(strToken.concat("sym"));
            }
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
    public void getSym() throws IOException {
        flag = false;
        tempStr = new StringBuffer();
        int iIndex = 0;
        token = new char[20];
        sym = symbol.nil;
        char ch = line.charAt(iCurPos++);
        tempStr.append(ch);
        //ɨ�����С��ո��
        while (ch == ' ' || ch == (char) 10 || ch == (char) 11 || ch == '\r' || ch == '\t') {
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
            if (ch == '.') {
                while (ch >= 'a' && ch <= 'z' || ch >= '0' && ch <= '9' || ch == '.') {
                    token[iIndex++] = ch;
                    ch = line.charAt(iCurPos++);
                    tempStr.append(ch);
                }
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
                case '\"': {
                    token[iIndex++] = ch;
                    sym = symbol.quotation;
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
                case '{': {
                    token[iIndex++] = ch;
                    sym = symbol.lbraces;
                    break;
                }
                case '}': {
                    token[iIndex++] = ch;
                    sym = symbol.rbraces;
                    break;
                }
                case '[': {
                    token[iIndex++] = ch;
                    sym = symbol.lbracket;
                    break;
                }
                case ']': {
                    token[iIndex++] = ch;
                    sym = symbol.rbracket;
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
                case '!': {
                    token[iIndex++] = ch;
                    sym = symbol.notsym;
                    break;
                }
                case '=': {
                    token[iIndex++] = ch;
                    sym = symbol.eql;
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
            if (sym == symbol.returnsym) {
                resultStr.append("\n\t\tSystem.out.println(\"Exit Function " + className + "::" + methodInfo.methodName + "(\"");
                for (int i = 0; i < methodInfo.params.size(); i++) {
                    resultStr.append(methodInfo.params.get(i) + "=\"+" + methodInfo.params.get(i) +
                            (i + 1 == methodInfo.params.size() ? "\"" : "+\","));
                }
                resultStr.append("+\")!\");");
            }
            //�ѵ��ʼӵ����ű���ȥ
            AddaWordtoList(sym);
            if (sym != symbol.rbraces) {
                resultStr.append(tempStr);
            }

        }
    }

    //====�﷨������ڣ������====
    //TIPS����ǰ��һ��sym
    void block() throws IOException {
        ClassDefinition();
        if (Err > 0) {
            return;
        }
    }

    void ClassDefinition() throws IOException {

        if (sym == symbol.publicsym || sym == symbol.privatesym) {
            getSym();
        }
        if (sym == symbol.classsym) {
            getSym();
            Identifier();
            if (Err > 0) {
                return;
            }
            if (sym == symbol.lbraces) {
                resultStr.append(new String(token).trim());
                getSym();
                while (iCurPos < line.length()) {
                    statement();
                    if (Err > 0) {
                        return;
                    }
                }
                if (sym != symbol.rbraces) {
                    Err++;
                    System.out.println("classȱ��}");
                } else {
                    resultStr.append((tempStr));
                }
            }

        }
    }

    void MainClass() throws IOException {
        String strToken = (new String(token)).trim();
        for (int i = 0; i < psvm.length; i++) {
            if (strToken.equals(psvm[i])) {
                getSym();
                strToken = (new String(token)).trim();
            } else {
                System.out.println("MainClass psvm error");
                Err++;
                return;
            }
        }
        Identifier();
        if (Err > 0) {
            return;
        }
        if (sym == symbol.rparen) {
            getSym();
            if (sym == symbol.lbraces) {
                resultStr.append("\n\t\tSystem.out.println(\"Enter Function " + className + "::main(" + argeName + "=\"+" + argeName + ".toString()+\")!\");");
                getSym();
                while (iCurPos < line.length()) {
                    statement();
                    if (Err > 0) {
                        return;
                    }
                    if (sym == symbol.rbraces) {
                        resultStr.append("\n\t\tSystem.out.println(\"Exit Function " + className + "::main(" + argeName + "=\"+" + argeName + ".toString()+\")!\");");
                        resultStr.append(tempStr);
                        break;
                    }
                }
                if (Err == 0) {
                    if (sym != symbol.rbraces) {
                        System.out.println("MainClass ȱ�� }");
                        Err++;
                    } else {
                        getSym();
                    }
                }
            }
        }
    }

    void MethodDeclaration(String className) throws IOException {
        if (sym == symbol.lparen) {
            getSym();
            paramDefinition();
            methodList.add(methodInfo);
            if (Err > 0) {
                return;
            }
            if (sym == symbol.rparen) {
                getSym();
                if (Err > 0) {
                    return;
                }
                resultStr.append("\n\t\tSystem.out.println(\"Enter Function " + className + "::" + methodInfo.methodName + "(\"");
                for (int i = 0; i < methodInfo.params.size(); i++) {
                    resultStr.append(methodInfo.params.get(i) + "=\"+" + methodInfo.params.get(i) +
                            (i + 1 == methodInfo.params.size() ? "\"" : "+\","));
                }
                resultStr.append("+\")!\");");
                if (sym == symbol.lbraces) {
                    getSym();
                    if (sym == symbol.rbraces) {
                        resultStr.append(tempStr);
                        getSym();
                        return;
                    }
                    while (iCurPos < line.length()) {
                        statement();
                        if (Err > 0) {
                            return;
                        }
                        if (sym == symbol.rbraces) {
                            resultStr.append(tempStr);
                            getSym();
                            return;
                        }
                    }
                    Err++;
                    System.out.println("��������ȱ��}");
                } else {
                    Err++;
                    System.out.println("��������ȱ��{");
                }
            }
        } else {
            Err++;
            System.out.println("��������ȱ�٣�");
        }
    }

    //���
    void statement() throws IOException {
        /********************************************
         *************** TODO: ��䴦�� *************
         *******************************************/
        if (sym == symbol.ident) {
            newIdent = new String(token).trim();
            if (!identList.contains(newIdent) && !methodInfo.params.contains(newIdent)) {
                Err++;
                //�����ڱ���
                System.out.println("����������");
                return;
            }
            if (!usedIdent.contains(newIdent))
                usedIdent.add(newIdent);
            getSym();
            assignStatement();
        } else if (sym == symbol.ifsym) {
            getSym();
            ifStatement();
        } else if (sym == symbol.whilesym) {
            getSym();
            loopStatement();
        } else if (sym == symbol.intsym || sym == symbol.booleansym) {
            getSym();
            varDefinition();
        } else if (sym == symbol.sysosym) {
            getSym();
            printStatement();
        } else if (sym == symbol.rbraces) {
            resultStr.append(tempStr);
            if (iCurPos < line.length()) {
                getSym();
            }
        } else if (sym == symbol.publicsym) {
            //�洢������Ϣ
            getSym();
            if (sym == symbol.staticsym) {
                getSym();
                if (sym == symbol.intsym || sym == symbol.booleansym) {
                    methodInfo.returnType = new String(token).trim();
                    getSym();
                    if (sym == symbol.ident) {
                        methodInfo.methodName = new String(token).trim();
                        getSym();
                        MethodDeclaration(className);
                    } else {
                        Err++;
                        System.out.println("����������");
                    }
                } else if (sym == symbol.voidsym) {
                    methodInfo.returnType = new String(token).trim();
                    getSym();
                    if (sym == symbol.mainsym) {
                        if (cnt == 0) {
                            getSym();
                            MainClass();
                            cnt++;
                        } else {
                            Err++;
                            System.out.println("��������main");
                        }
                    } else if (sym == symbol.ident) {
                        methodInfo.methodName = new String(token).trim();
                        getSym();
                        MethodDeclaration(className);
                    } else {
                        Err++;
                        System.out.println("û�и������");
                    }
                } else {
                    Err++;
                    System.out.println("û�и÷���ֵ���͵����");

                }
            } else {
                Err++;
                System.out.println("ȱ��public");
            }
        } else if (sym == symbol.returnsym) {
            if (methodInfo.returnType.equals("void")) {
                Err++;
                System.out.println("�������Ͳ�ƥ��");
            } else {
                getSym();
                returnStatement();
            }
        } else {
            if (!flag)
                //�﷨����
                System.out.println("�﷨����");
        }
    }

    void returnStatement() throws IOException {
        expression();
    }

    void printStatement() throws IOException {
        if (sym == symbol.lparen) {
            getSym();
            expression();
            if (Err > 0) {
                return;
            }
            if (sym == symbol.rparen) {
                getSym();
                if (sym == symbol.semicolon) {
                    getSym();
                } else {
                    System.out.println("��������ʽȱ�٣�");
                    Err++;
                }
            } else {
                System.out.println("��������ʽȱ�٣�");
                Err++;
            }
        } else {
            Err++;
            System.out.println("��������ʽȱ�٣�");
        }
    }

    //��ֵ���
    void assignStatement() throws IOException {
        /********************************************
         *************** TODO: ��ֵ��䴦�� *************
         *******************************************/
        //�����ǰ�� ��= ֱ�ӽ����
        if (sym == symbol.eql) {
            getSym();
            expression();
        } else {
            error(31);
        }
    }


    //�������
    void ifStatement() throws IOException {
        /********************************************
         *************** TODO: ������䴦�� *************
         *******************************************/
        if (sym == symbol.lparen) {
            getSym();
            condition();
            if (Err > 0) {
                return;
            }
            if (sym == symbol.rparen) {
                getSym();
                if (sym == symbol.thensym) {
                    getSym();
                    if (sym == symbol.lbraces) {
                        getSym();
                        statement();
                        if (Err > 0) {
                            return;
                        }
                    } else {
                        Err++;
                        System.out.println("thenȱ��{");
                    }
                } else {
                    // ȱ�� then ����
                    Err++;
                    error(33);
                }
            } else {
                Err++;
                System.out.println("�������ȱ�٣�");
            }
        } else {
            Err++;
            System.out.println("�������ȱ��(");
        }

    }


    //ѭ�����
    void loopStatement() throws IOException {
        /********************************************
         *************** TODO: ѭ����䴦�� *************
         *******************************************/
        if (sym == symbol.lparen) {
            getSym();

            condition();
            if (Err > 0) {
                return;
            }
            if (sym == symbol.rparen) {
                getSym();
                if (sym == symbol.dosym) {
                    getSym();
                    if (sym == symbol.lbraces) {
                        getSym();
                        statement();
                    } else {
                        Err++;
                        System.out.println("while doȱ��{");
                    }
                } else {
                    //ȱ�� do
                    Err++;
                    error(32);
                }
            } else {
                Err++;
                System.out.println("while �������ȱ�٣�");
            }
        }else{
            Err++;
            System.out.println("while �������ȱ�٣�");
        }
    }


    void paramDefinition() throws IOException {
        methodInfo.paramsNum = 0;
        methodInfo.params.clear();
        if (sym == symbol.intsym || sym == symbol.booleansym) {
            getSym();
            if (sym == symbol.ident) {
                methodInfo.paramsNum++;
                methodInfo.params.add(new String(token).trim());
                getSym();
            } else {
                Err++;
                System.out.println("���������������");
            }
            while (sym == symbol.commasym) {
                getSym();
                if (sym == symbol.intsym || sym == symbol.booleansym) {
                    getSym();
                    if (sym == symbol.ident) {
                        methodInfo.paramsNum++;
                        methodInfo.params.add(new String(token).trim());
                        getSym();
                    } else {
                        Err++;
                        System.out.println("���������������");
                    }
                } else {
                    Err++;
                    System.out.println("��������ȱ������");
                }
            }
        }
    }
    //��������


    void varDefinition() throws IOException {
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
                if (sym == symbol.semicolon) {
                    getSym();
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
                if (sym == symbol.semicolon) {
                    getSym();
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
    void expression() throws IOException {
        /********************************************
         *************** TODO: ���ʽ���� *************
         *******************************************/
        if (sym == symbol.plus || sym == symbol.minus) {
            while (sym == symbol.plus || sym == symbol.minus) {
                //ֱ����һ��
                getSym();
                //ֱ����һ�� term��������Ǹ������ǲ���+ -����while
                term();
            }
            if (sym == symbol.semicolon) {
                getSym();
            } else {
                Err++;
                System.out.println("���ʽȱ�ٷֺ�");
            }
        } else if (sym == symbol.truesym) {
            getSym();
        } else if (sym == symbol.falsesym) {
            getSym();
        } else if (sym == symbol.quotation) {
            getSym();
            while (sym != symbol.quotation) {
                getSym();
            }
            if (sym == symbol.quotation) {
                getSym();
                if (sym == symbol.plus) {
                    getSym();
                    if (sym == symbol.ident) {
                        String strToken = new String(token).trim();
                        if (identList.contains(strToken)) {
                            getSym();
                            return;
                        }
                        for (int i = 0; i < methodList.size(); i++) {
                            if (methodList.get(i).methodName.equals(strToken)) {
                                getSym();
                                transmitParams(methodList.get(i).paramsNum);
                                if (Err > 0) {
                                    return;
                                }
                                return;
                            }
                        }
                    }
                }
            } else {
                Err++;
                System.out.println("������ȱ�ٷֺ�");
            }
        } else {
            term();
            if (Err > 0) {
                return;
            }
            while (sym == symbol.plus || sym == symbol.minus) {
                //ֱ����һ��
                getSym();
                //ֱ����һ�� term��������Ǹ������ǲ���+ -����while
                term();
                if (Err > 0) {
                    return;
                }
            }
            if (sym == symbol.semicolon) {
                getSym();
            } else if (sym == symbol.neq || sym == symbol.eql || sym == symbol.lss || sym == symbol.leq || sym == symbol.gtr || sym == symbol.geq) {
                getSym();
                expression();
            } else if (sym == symbol.rparen) {
            } else {
                Err++;
                System.out.println("���ʽȱ�ٷֺ�");
            }
        }
    }

    private void transmitParams(Integer paramsNum) throws IOException {
        if (sym == symbol.lparen) {
            getSym();
            factor();
            if (Err > 0) {
                return;
            }
            for (int j = 1; j < paramsNum; j++) {
                if (sym == symbol.commasym) {
                    getSym();
                    factor();
                } else {
                    Err++;
                    System.out.println("��������ȱ�٣�");
                    return;
                }
                if (Err > 0) {
                    return;
                }
            }
        }
        if (Err > 0) {
            return;
        }
        if (sym == symbol.rparen) {
            getSym();
        } else {
            Err++;
            System.out.println("���ú���ȱ�٣�");
        }
    }


    //��
    void term() throws IOException {
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
            factor();
            if (Err > 0) {
                return;
            }
        }
    }

    //����
    void factor() throws IOException {
        /********************************************
         *************** TODO: ���Ӵ��� *************
         *******************************************/
        // ident ���� number
        if (sym == symbol.ident || sym == symbol.number) {
            if (sym == symbol.ident) {
                newIdent = new String(token).trim();
                if (!identList.contains(newIdent) && !methodInfo.params.contains(newIdent)) {
                    Err++;
                    //�����ڱ���
                    error(43);
                    return;
                }
                if (!usedIdent.contains(newIdent))
                    usedIdent.add(newIdent);
            }
            getSym();
            return;
        }
        // ����ǣ�
        if (sym == symbol.lparen) {
            getSym();
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
            }
        } else {
            //����
            error(60);
        }
    }

    void Identifier() throws IOException {
        if (sym == symbol.ident) {
            argeName = new String(token).trim();
            if (className.equals("")) {
                className = new String(token).trim();
            }
            getSym();
        } else {
            Err++;
            System.out.println("������������");
        }
    }

    //����
    void condition() throws IOException {
        /********************************************
         *************** TODO: �������� *************
         *******************************************/
        expression();
        if (Err > 0) {
            return;
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


