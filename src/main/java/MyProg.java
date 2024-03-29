import java.util.*;

import static java.lang.Thread.sleep;

public class MyProg {
    public static void main(String[] args) {

        double result;

        System.out.println("Hello World!");
        Fraction fr=new Fraction(2,3);
        Fractionable num= new FractionDecorator(fr);

        Object pr;
        pr = ((FractionDecorator)num).getProxy();
        result=((Fractionable)pr).doubleValue();
        System.out.println("result:"+result);
        sleep5000ms();
        result=((Fractionable)pr).doubleValue();
        System.out.println("result:"+result);
        sleep5000ms();
        ((Fractionable)pr).setNum(20);
        result=((Fractionable)pr).doubleValue();
        System.out.println("result:"+result);
        sleep5000ms();
        result=((Fractionable)pr).doubleValue();
        System.out.println("result:"+result);
        sleep5000ms();
        ((Fractionable)pr).setDenum(15);

        result=((Fractionable)pr).doubleValue();
        System.out.println("result:"+result);
        sleep5000ms();
        result=((Fractionable)pr).doubleValue();
        System.out.println("result:"+result);
        sleep5000ms();
        // тестируем предыдущие значения
        ((Fractionable)pr).setNum(2);
        ((Fractionable)pr).setDenum(3);
        result=((Fractionable)pr).doubleValue();
        System.out.println("result:"+result);
        ((Fractionable)pr).setNum(20);
        result=((Fractionable)pr).doubleValue();
        System.out.println("result:"+result);
        result=((Fractionable)pr).doubleValue();
        System.out.println("result:"+result);
        result=((Fractionable)pr).doubleValue();
        System.out.println("result:"+result);
        sleep5000ms();
        sleep5000ms();
        sleep5000ms();
        result=((Fractionable)pr).doubleValue();
    }

    public static void sleep5000ms()
    {
        try
            {sleep(5000);}
        catch (InterruptedException e) {}
    }

}
