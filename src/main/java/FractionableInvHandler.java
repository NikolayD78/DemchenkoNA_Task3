import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.sleep;

public class FractionableInvHandler implements InvocationHandler {

    private Object obj;

    // Значение каждой функции записывается в строго конкретное время.
    // не может быть одновременной записи в кэш двух разных значений,
    // разница должна быть хотя бы в 1 милисекунду
    private HashMap <Long,String> functionMap; // название кэшируемой функции и значение времени, в которое ее вызвали
    private HashMap <Long,String> paramMap; // время и значение параметров класса (состояние строкой - это значения переменных класса, с которыми вызвана функция в данный момент),
                                            // по состоянию параметров понимается какое значение отдать из кэша в момент вызова кэшируемой функции
    private HashMap <Long,Object> cacheMap; //значения кэшируемой функции
    private HashMap <Long,Long> timeMap; // время вызова функции, и временная задержка перед удалением из кэша
    private int workActive; // показывает что идет работа с HashMap, либо в потоке сборки мусора, либо в основном потоке

    public int getWorkActive()
    {return workActive;}

    synchronized public void setWorkActive(int value)
    {workActive=value;}

    public FractionableInvHandler(Object obj)
        {
            this.obj = obj;
            functionMap=new HashMap<>();
            paramMap=new HashMap<>();
            cacheMap=new HashMap<>();
            timeMap=new HashMap<>();

            // создаем поток-демон для сборки мусора
            Thread thr1=new Thread(new CheckTime("Поток очистки кэша",this));
            thr1.setDaemon(true);
            thr1.start();

        }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //System.out.println("It works");

        // сначала дождемся когда поток очистки мусора освободит HashMap
        while (getWorkActive()==1)
        {
            try
            {sleep(50);}
            catch (InterruptedException e) {}
        }

        setWorkActive(1); // начинаем работу

        Object retValue;
        Method m = obj.getClass().getMethod(method.getName(), method.getParameterTypes());
        // временные отметки
        long t1; // время в которое была вызвана функция (сейчас)
        long t2; // время жизни результата функции
        long t3; // текущая временная отметка (при просмотре кэша)
        // строка с параметрами (для поиска в кэше)
        String p1;
        // название метода который мы ищем в кэше
        String mName=method.getName();
        int isTimeUsed;

        Annotation[] anns = m.getAnnotationsByType(Mutator.class);
        for (Annotation a: anns)
        {
            //cacheMap.clear(); // сбрасываем кэш, сразу для всех кэшируемых функций, сколько бы их ни было
            //System.out.println("Сброшен кэш, по факту вызова функции с аннотацией @Mutator: "+m.getName());

            setWorkActive(0); // заканчиваем работу
            return m.invoke(obj, args);
        }
        anns = m.getAnnotationsByType(Cache.class);
        for (Annotation a: anns)
            {
                // шаг 0: отфиксируем время в которое вызвана операция t1
                // шаг 0: отфиксируем время (из аннотации cache), в течении которого должно жить в кэше значение
                // по данной функции t2
                // шаг 1: определяем конфигурацию класса (т.е. значения полей), значение кэшируемой функции,
                // соответствующее которой надо будет искать (из рефлексии)
                // шаг 2: определим было ли запомнено такое значение функции в кэше
                // шаг 3: если такого значения не было запомнено, то:
                // шаг 4: запомним время, в которое пришло значение на обработку
                // шаг 5: вычислим значение функции и запомним его в кэше со значениями полей класса и временем появления

                // если значение было, то путем нахождения разницы t1-t2 поймем не истекло ли время его
                // жизни в кэше. Если истекло, то обновим время прописав ему значение t1
                // и вернем значение из кэша
                // если значение не истекло, то просто вернем значение из кэша

                t2=m.getAnnotation(Cache.class).time(); // время жизни значения в кэше

                // надо исключить хотя и очень маловероятную ситуацию, что в одну и ту же милисекунду придут два значения в кэш
                do {
                    isTimeUsed = 0;
                    t1=System.currentTimeMillis();
                    for (Map.Entry<Long, String> entry : functionMap.entrySet())
                        if (t1 == entry.getKey())
                            isTimeUsed = 1;
                }while(isTimeUsed == 1);

                Method mState=obj.getClass().getMethod("getStateForCache");
                p1=(String)mState.invoke(obj);
                //System.out.println(p1);

                // Первая HashMap по функции, ищем все отметки времени, в который она вызывалась
                // вторая HashMap по параметрам - ищем нужные параметры, среди найденных отметок времени
                // если нужная отметка времени найдена, то
                // в третьей HashMap по отсечке времени ищем нужный результат

                t3=0;
                for (Map.Entry<Long,String> entry : functionMap.entrySet()) {
                    //System.out.println("mName.equals(entry.getValue()) "+mName.equals(entry.getValue()));
                    //System.out.println("entry.getKey() "+entry.getKey());
                    if(mName.equals(entry.getValue())) // если это нужная функция (по названию)
                        if(t3<=entry.getKey()) // если записей в кэше по такой функции много, будем находить наиболее позднее
                        {
                            if (p1.equals(paramMap.get(entry.getKey())))// если такие параметры есть
                            {
                                t3=entry.getKey();
                            }
                        }
                }// for (Map.Entry<Long,String>
                // в этой точке - если значение в кэше есть, то его можно получить, а значит
                // надо так же проверить не истекло ли его время, и если истекло, то обновить
                // время. Если не истекло, то ничего не делаем, а просто возвращаем
                retValue=null;
                //System.out.println("t3="+t3);
                if(t3>0)
                {
                    retValue=cacheMap.get(t3);
                    System.out.println("Значение из кэша");
                    if(t1>(t2+t3))
                    {
                        functionMap.put(t1,mName);
                        paramMap.put(t1,p1);
                        cacheMap.put(t1,retValue);
                        timeMap.put(t1,t2);
                    }

                }
                if(t3==0) // если запись в кэше не нашли, то вычисляем ее и заносим
                {
                    retValue=m.invoke(obj, args);
                    System.out.println("Значение вычисляем");
                    functionMap.put(t1,mName);
                    paramMap.put(t1,p1);
                    cacheMap.put(t1,retValue);
                    timeMap.put(t1,t2);
                }
                System.out.println(functionMap.toString());
                System.out.println(paramMap.toString());
                System.out.println(cacheMap.toString());
                System.out.println(timeMap.toString());
                setWorkActive(0); // заканчиваем работу
                return retValue;

            } // методы с аннотацией Cache

          setWorkActive(0); // заканчиваем работу
          return m.invoke(obj, args);


    }

    public void clearMusor()
    {
        long t1; // время в которое была вызвана функция (сейчас)
        long t2; // сколько в кэше должно было жить конкретное значение
        long t3; // какую запись мы анализируем сейчас (временная отметка в кэше)
        long [] timeToDel; //массив ключей HashMap, а именно временных отсечек, которые уже надо удалить
        timeToDel=new long[functionMap.size()]; // предположим, что все надо удалить
        int counter=0;

    // сначала дождемся когда основной поток освободит HashMap

        while (getWorkActive()==1)
        {
            try
            {sleep(50);}
            catch (InterruptedException e) {}
        }

        setWorkActive(1); // начинаем процесс очистки мусора
        System.out.println("Запустился поток очистки мусора");
        t1=System.currentTimeMillis();

        // проинициализируем
        for (int f=0;f<timeToDel.length;f++)
            timeToDel[f]=0;

        for (Map.Entry<Long,String> entry : functionMap.entrySet())
        {
            t3=entry.getKey();
            t2=timeMap.get(t3);
            if(t1>(t3+t2)) // время хранения вышло
                {
                    timeToDel[counter]=t3;
                    counter++;
                }
        }// цикл по functionMap

        // удаляем во всех 4-х HashMap
        for (int f=0;f<timeToDel.length;f++)
        {
            if(timeToDel[f]>0)
            {
                functionMap.remove(timeToDel[f]);
                paramMap.remove(timeToDel[f]);
                cacheMap.remove(timeToDel[f]);
                timeMap.remove(timeToDel[f]);
            }
        }
        setWorkActive(0); // все, закончили работу

    }


}
