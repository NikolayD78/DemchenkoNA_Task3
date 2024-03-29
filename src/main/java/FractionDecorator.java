//Декоратор нужен для того, чтобы ввести переменные для кэшированных значений
import java.lang.reflect.*;
import java.util.Arrays;

public class FractionDecorator implements Fractionable{

    private Fractionable baseFraction;


    private FractionDecorator(){}

    public FractionDecorator(Fractionable baseFraction) {
        this.baseFraction = baseFraction;
    }

    public  Object getProxy()
    {
        Class cls = this.getClass();
        return  Proxy.newProxyInstance(cls.getClassLoader(),
                new Class[]{Fractionable.class},
                new FractionableInvHandler(this));
    }

    @Mutator
    public void setNum(int num)
    {
        baseFraction.setNum(num);
    }

    public int getNum() {return baseFraction.getNum();}

    @Mutator
    public void setDenum(int denum)
    {
        baseFraction.setDenum(denum);
    }

    public int getDenum() {return baseFraction.getDenum();}

    @Override
    @Cache(time=10000)
    public double doubleValue() {

            return baseFraction.doubleValue();

    }

    public String getStateForCache()
    {
        String ResultString;

        ResultString="num#"+getNum()+"#denum"+getDenum();

        return ResultString;

    }

}
