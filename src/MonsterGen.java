import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class MonsterGen
{
    public static void print(String s)
    {
        System.out.println (s);
    }

    public static void usage()
    {
        print ("Monster generator.\nUsage: java MonsterGen <num>");
        System.exit(1);
    }

    public static void main(String[] args)
    {

        if (args.length<2)
            usage();

        int num = 0;
        try{
            num = Integer.parseInt(args[0]);
        }
        catch (Exception e){
            System.out.println("Expects an integer");
            usage();
        }

        try{
            PrintStream ps=new PrintStream(new FileOutputStream("./test/"+args[1]));
            System.setOut(ps);
            print ("class Monster\n{");
            print ("\tstatic void main (String[] args)");
            print ("\t{\n\t\tSystem.out.println (new Foo().foo());");
            print ("\t}\n}\n");

            print ("class Foo\n{");
            print ("\tpublic int foo()");
            print ("\t{");
            print ("\t\tint sum;\n");
            print ("\n\t\tsum = 0;");
            for (int i=0; i<num; i++)
                print ("\t\tsum = sum + 1;");
            print ("\t\treturn sum;");
            print ("\t}");
            print ("}");
            return;
        }
        catch (IOException e){
            System.out.println("Illegal filename" + args[1]);
        }

    }
}
