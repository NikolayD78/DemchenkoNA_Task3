import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static java.lang.Thread.sleep;

public class UnitTest {

    static ByteArrayOutputStream baos;
    static PrintStream ps;
    static PrintStream oldPrintStream;
    static PrintStream newPrintStream;
    static String res;

    @BeforeAll
    static void preparing() {


        baos = new ByteArrayOutputStream();
        ps = new PrintStream(baos);
        oldPrintStream=System.out;
        newPrintStream=ps;
        System.setOut(ps);
    }

    static void checkResultOutput(String outputStr) // повторяемые фрагменты вынесены в отдельную ф-ю
    {
        res = baos.toString();
        System.setOut(oldPrintStream);// для визуального контроля - выведем еще и на "реальную" консоль
        System.out.println(res);
        baos.reset();
        System.setOut(newPrintStream);
        if (res.indexOf(outputStr,0)==-1)
            throw new AssertionError("Нет строки: "+outputStr);
    }

    public static void sleep5000ms()
    {
        try
        {sleep(5000);}
        catch (InterruptedException e) {}
    }

    @org.junit.jupiter.api.Test
    public void testCache()
    {
        double result;

        // время жизни значения в кэше 10 секунд
        // очистка мусора запускается каждую секунду, удаляет если прошло > 10 секунд
        System.out.println("Hello World!");
        Fraction fr=new Fraction(2,3);
        Fractionable num= new FractionDecorator(fr);
        Object pr;
        pr = ((FractionDecorator)num).getProxy();

        result=((Fractionable)pr).doubleValue();
        Assertions.assertEquals(String.valueOf(result),"0.6666666666666666");
        checkResultOutput("Значение вычисляем");
        sleep5000ms();
        result=((Fractionable)pr).doubleValue();
        Assertions.assertEquals(String.valueOf(result),"0.6666666666666666");
        checkResultOutput("Значение из кэша"); // прошло только 5 секунд, значение сохраняется
        sleep5000ms();
        ((Fractionable)pr).setNum(20);
        result=((Fractionable)pr).doubleValue();
        Assertions.assertEquals(String.valueOf(result),"6.666666666666667");
        checkResultOutput("Значение вычисляем"); // новое значение, добавляем в кэш
        sleep5000ms();
        result=((Fractionable)pr).doubleValue();
        Assertions.assertEquals(String.valueOf(result),"6.666666666666667");
        checkResultOutput("Значение из кэша"); // прошло 5 секунд, значение сохранилось
        sleep5000ms();
        ((Fractionable)pr).setDenum(15);
        result=((Fractionable)pr).doubleValue();
        Assertions.assertEquals(String.valueOf(result),"1.3333333333333333");
        checkResultOutput("Значение вычисляем"); // 3-е новое значение
        sleep5000ms();
        result=((Fractionable)pr).doubleValue();
        Assertions.assertEquals(String.valueOf(result),"1.3333333333333333");
        checkResultOutput("Значение из кэша"); // прошло 5 секунд, значение сохранено
        sleep5000ms();
        // тестируем предыдущие значения
        ((Fractionable)pr).setNum(2);
        ((Fractionable)pr).setDenum(3);
        result=((Fractionable)pr).doubleValue();
        Assertions.assertEquals(String.valueOf(result),"0.6666666666666666");
        checkResultOutput("Значение вычисляем"); // прошло > 10 сек с момента вычисл, сборщик мусора уже удалил прошлое
        ((Fractionable)pr).setNum(20);
        result=((Fractionable)pr).doubleValue();
        Assertions.assertEquals(String.valueOf(result),"6.666666666666667");
        result=((Fractionable)pr).doubleValue();
        Assertions.assertEquals(String.valueOf(result),"6.666666666666667");
        checkResultOutput("Значение вычисляем"); // то же самое, прошло > 10 секунд
        result=((Fractionable)pr).doubleValue();
        Assertions.assertEquals(String.valueOf(result),"6.666666666666667");
        checkResultOutput("Значение из кэша"); // без задержки
        sleep5000ms();
        sleep5000ms();
        sleep5000ms();
        result=((Fractionable)pr).doubleValue();
        Assertions.assertEquals(String.valueOf(result),"6.666666666666667");
        checkResultOutput("Значение вычисляем"); // прошло 15 секунд, все значения удалены из кэша
        //********************************
        //********************************
        //********************************

    }
}
