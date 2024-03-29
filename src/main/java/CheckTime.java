public class CheckTime implements Runnable{

    private final String str;
    private FractionableInvHandler o;

        public CheckTime(String str,FractionableInvHandler o)
        {
            this.str = str;
            this.o=o;

        }
        @Override
        public void run() {
            try {
                while(true) {
                    o.clearMusor();

                    Thread.sleep(1000L);

                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }

