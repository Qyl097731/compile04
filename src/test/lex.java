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

    private String newIdent = "";

    private StringBuffer tempStr = new StringBuffer();

    private Stack<StringBuffer> identStack = new Stack<>();

    private List<StringBuffer> resultList = new ArrayList<>();

    private List<String> identList = new ArrayList<>(), usedIdent = new ArrayList<>();

    private List<MethodInfo> methodList = new ArrayList<>();

    private Boolean flag = false;

    private Integer cnt = 0;

    //符号类型定义
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
    String[] psvm = {"(", "String", "[", "]"};
    String[] word = {"String", "System.out.println", "begin", "boolean", "class", "do", "else", "end",
            "extends", "false", "if", "int", "length", "main", "new", "private", "public",
            "return", "static", "then", "this", "true", "void", "while"};
    ArrayList<aWord> Symlist;
    int Err;

    //主函数：
    public static void main(String[] args) throws IOException {
        System.out.println("请读入程序代码：");
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
        File file = new File("out.txt");
        if(!file.exists()){
            file.createNewFile();
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(file,false));
        bw.write("输出文件:\n");
        bw = new BufferedWriter(new FileWriter(file,true));

        //==========词法&语法分析：一趟完成==========
        lex.getSym();
        lex.block();
        if (lex.Err == 0) {
            //输出符号表
//            for (int i = 0; i < lex.Symlist.size(); i++) {
//                System.out.print(lex.Symlist.get(i).toString().trim() + " ");
//            }
            bw.write("\nsyntax parses success!\n\n");
//            System.out.println("\nsyntax parses success!\n\n");
        } else {
            bw.write("syntax parses fail!\n\n");
//            System.out.println("syntax parses fail!\n\n");
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
            if (strToken.equals("System.out.println")) {
                sym = symbol.sysosym;
            } else {
                sym = symbol.valueOf(strToken.concat("sym"));
            }
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
        while (ch == ' ' || ch == (char) 10 || ch == (char) 11 || ch == '\r' || ch == '\t') {
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
        ClassDefinition();
    }

    void ClassDefinition() {

        if (sym == symbol.publicsym || sym == symbol.privatesym) {
            getSym();
        }
        if (sym == symbol.classsym) {
            getSym();
            Identifier();
            if (sym == symbol.lbraces) {
                getSym();
                while (iCurPos < line.length()) {
                    statement();
                }
            }

        }
    }

    void MainClass() {
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
        if (sym == symbol.rparen) {
            getSym();
            if (sym == symbol.lbraces) {
                getSym();
                while (iCurPos < line.length()) {
                    statement();
                    if (Err > 0) {
                        return;
                    }
                }
                if (Err == 0) {
                    if (sym != symbol.rbraces) {
                        System.out.println("MainClass 缺少 }");
                        Err++;
                    }
                }
            }
        }
    }

    void MethodDeclaration(String className) {
        if (sym == symbol.lparen) {
            getSym();
            paramDefinition();

            methodList.add(methodInfo);
            if (sym == symbol.rparen) {
                getSym();
                if (sym == symbol.lbraces) {
                    getSym();
                    if (sym == symbol.rbraces) {
                        getSym();
                        return;
                    }
                    while (iCurPos < line.length()) {
                        statement();
                        if (sym == symbol.rbraces) {
                            getSym();
                            return;
                        }
                    }
                    Err++;
                    System.out.println("方法定义缺少}");
                } else {
                    Err++;
                    System.out.println("方法定义缺少{");
                }
            }
        } else {
            Err++;
            System.out.println("方法定义缺少（");
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
        } else if (sym == symbol.intsym || sym == symbol.booleansym) {
            getSym();
            resultList.add(tempStr);
            varDefinition();
        } else if (sym == symbol.sysosym) {
            getSym();
            printStatement();
        } else if (sym == symbol.rbraces) {
            if (iCurPos < line.length()) {
                getSym();
            }
        } else if (sym == symbol.publicsym) {
            //存储方法信息
            getSym();
            if (sym == symbol.staticsym) {
                getSym();
                if (sym == symbol.intsym || sym == symbol.booleansym) {
                    methodInfo.returnType = new String(token).trim();
                    getSym();
                    if (sym == symbol.ident) {
                        methodInfo.methodName = new String(token).trim();
                        getSym();
                        MethodDeclaration();
                    } else {
                        Err++;
                        System.out.println("方法名出错");
                    }
                } else if (sym == symbol.voidsym) {
                    getSym();
                    if (sym == symbol.mainsym) {
                        if (cnt == 0) {
                            getSym();
                            MainClass();
                            cnt++;
                        } else {
                            Err++;
                            System.out.println("出现两次main");
                        }
                    } else if (sym == symbol.ident) {
                        getSym();
                        MethodDeclaration();
                    } else {
                        Err++;
                        System.out.println("没有该类语句");
                    }
                } else {
                    Err++;
                    System.out.println("没有该返回值类型的语句");

                }
            } else {
                Err++;
                System.out.println("缺少public");
                return;
            }
        } else if (sym == symbol.returnsym) {
            getSym();
            returnStatement();
        } else {
            if (!flag)
                //语法错误
                error(12);
        }
    }

    void returnStatement() {
        expression();
    }

    void printStatement() {
        if (sym == symbol.lparen) {
            getSym();
            expression();
            if (sym == symbol.rparen) {
                getSym();
                if (sym == symbol.semicolon) {
                    getSym();
                } else {
                    System.out.println("输出语句表达式缺少；");
                    Err++;
                }
            } else {
                System.out.println("输出语句表达式缺少）");
                Err++;
            }
        } else {
            Err++;
            System.out.println("输出语句表达式缺少（");
        }
    }

    //赋值语句
    void assignStatement() {
        /********************************************
         *************** TODO: 赋值语句处理 *************
         *******************************************/
        //如果当前是 ：= 直接进入表单
        if (sym == symbol.eql) {
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


    void paramDefinition() {
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
                System.out.println("参数变量定义出错");
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
                        System.out.println("参数变量定义出错");
                    }
                } else {
                    Err++;
                    System.out.println("参数定义缺少类型");
                }
            }
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
                    //变量重复
                    error(42);
                    return;
                } else {
                    identList.add(newIdent);
                }
                getSym();
                resultList.add(tempStr);
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
            if(sym == symbol.semicolon){
                getSym();
            }else{
                Err++;
                System.out.println("表达式缺少分号");
                return;
            }
        } else if(sym == symbol.truesym){
            getSym();
            return;
        }else if(sym == symbol.falsesym){
            getSym();
        }else if (sym == symbol.quotation) {
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
                                return;
                            }
                        }
                    }
                }
            } else {
                Err++;
                System.out.println("输出语句缺少分号");
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
            if(sym == symbol.semicolon){
                getSym();
            }else{
                Err++;
                System.out.println("表达式缺少分号");
            }
        }
    }

    private void transmitParams(Integer paramsNum) {
        if (sym == symbol.lparen) {
            getSym();
            factor();
            for (int j = 1; j < paramsNum; j++) {
                if (sym == symbol.commasym) {
                    getSym();
                    factor();
                } else {
                    Err++;
                    System.out.println("参数传递缺少，");
                    return;
                }
            }
        }
        if (sym == symbol.rparen) {
            getSym();
        } else {
            Err++;
            System.out.println("调用函数缺少）");
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
                if (!identList.contains(newIdent) && !methodInfo.params.contains(newIdent)) {
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

    void Identifier() {
        if (sym == symbol.ident) {
            className = new String(token).trim();
            getSym();
        } else {
            Err++;
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


