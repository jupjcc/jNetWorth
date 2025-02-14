package utilsPkg;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;

import dataEnumerations.Colors;

public class OpMsgLogger
{
   public enum LogLevel
   {
      INFO, WARN, ERROR, PROMPT
   }
   
   private StyledText stxt;
   private Display myDisplay;
   private TextLogger fileLogger = null;
   
   public OpMsgLogger(Display disp, StyledText stxt)
   {
      this.myDisplay = disp;
      this.stxt = stxt;
   }
   
   public void SetFileLogger(TextLogger tlog)
   {
      fileLogger = tlog;
   }
   
   public void LogMsg(final String msgArg, final LogLevel level, boolean timeStamp)
   {
      final String msg;
      if (timeStamp)
      {
         msg = TimeStamps.TimeOnly() + ": " + msgArg;
      }
      else
      {
         msg = msgArg;
      }
      myDisplay.syncExec(new Runnable()
      {
         public void run()
         {
            if (level != LogLevel.INFO)
            {
               int cPos = stxt.getText().length();
               stxt.append(msg);
               StyleRange sr=new StyleRange();
               sr.start=cPos;
               sr.length=msg.length();
               if (level == LogLevel.ERROR)
               {
                  sr.foreground = Colors.RED;
               }
               else if (level == LogLevel.PROMPT)
               {
                  sr.foreground = Colors.BLUE;
               }
               else
               {
                  sr.foreground = Colors.ORANGE;
               }
               sr.fontStyle=SWT.BOLD;
               stxt.setStyleRange(sr);
            }
            else
            {
               stxt.append(msg);
            }     // end if lvl1=info
            if (fileLogger != null)
            {
               fileLogger.WriteToLog(msg);
            }
            // move caret to end
            stxt.setSelection(stxt.getCharCount());
         }
      });
   }
   public void LogToFileOnly(final String msg) {
      if (fileLogger != null)
      {
         fileLogger.WriteToLog(msg);
      }      
   }
   public void LogMsg(final String msg, final LogLevel level)
   {
      LogMsg(msg, level, false);
   }
   
   public void LogMsg(final String msg)
   {
      LogMsg(msg, LogLevel.INFO, false);
   }
   public void CloseLog()
   {
       if (fileLogger != null)
       {
          fileLogger.CloseLogFile();
       }	   
   }
   
}
