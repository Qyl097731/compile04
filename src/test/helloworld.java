package test;

/**
 * projectName:  complie04
 * packageName: test
 * date: 2020-12-16 09:43
 * copyright(c) 2020 ÄÏÏş18×¿¹¤ ÇñÒÀÁ¼
 *
 * @author ÇñÒÀÁ¼
 */
public class helloworld {
    public static int Add(int a, int b){
        System.out.println("Enter Function helloworld::Add(a="+a+",b="+b+")!");
        int ret;
        ret = a+b;
        if(a < b){
            while(a < b){
                a = a+b;
            }
        }else{
            ret =a+b;
        }
        System.out.println("Exit Function helloworld::Add(a="+a+",b="+b+")!");
        return ret;

    }
    public static void main(String[] args) {
        System.out.println("Enter Function helloworld::main(args="+args.toString()+")!");
        System.out.println("result="+Add(1,10));
        System.out.println("Exit Function helloworld::main(args="+args.toString()+")!");
    }
}