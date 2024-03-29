
public class Fraction implements Fractionable{
    private int num;
    private int denum;

    private Fraction(){}

    public Fraction(int num,int denum)
        {
            this.num=num;
            this.denum=denum;
        }


    public void setNum(int num)
    {
        this.num=num;
    }

    public int getNum() {return num;}

    public void setDenum(int denum)
    {
        this.denum=denum;
    }

    public int getDenum() {return denum;}

    @Override
    public double doubleValue() {
        //System.out.println("invoke double value");
        //System.out.println("num"+num);
        //System.out.println("denum"+denum);
        return (double) num/denum;
    }

}
