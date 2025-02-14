package utilsPkg;

public class WaitAndNotify
{
   MonitorObject done = new MonitorObject();
   boolean wasSignaled = false;

   //  returns true if notify was received, false if timeout
   public boolean DoWait(int timeout)
   {
      boolean timedOut = false;
      synchronized(done)
      {
         long startTime = System.currentTimeMillis();
         long endTime = startTime + timeout;
         while (!wasSignaled && !timedOut)
         {
            try
            {
               long waitTime = endTime - System.currentTimeMillis();
               if (waitTime > 0)
               {
                  done.wait(waitTime);
               }
               else
               {
                 timedOut = true;
               }
            }
            catch(InterruptedException e)
            {}
         }
         wasSignaled = false;
      }
      return !timedOut;
   }
   
   public void DoNotify()
   {
      synchronized(done)
      {
         wasSignaled = true;
         done.notify();
      }
   }
}
