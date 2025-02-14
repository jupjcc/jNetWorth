package utilsPkg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import mainPkg.Tracker;

public class TextLogger
{  
   public String  LogFileName="";
   public String  CreationErrStr="";
   File           logFile = null;
   boolean        logFileEmpty = true;
   BufferedWriter logBw=null;
   FileWriter     logFw=null;
   String         logName;
   String logPath = "";

   public TextLogger(String logName)
   {
      this.logName = logName;
   }

   public TextLogger(String logName, String dPath)
   {
      this.logName = logName;
      logPath = dPath;
   }

   public File GetLogFile()
   {
      return logFile;
   }
   
   //  close the current file for user viewing and open a new one
   public void OpenNewLogFile()
   {
      if (logFile != null)
      {
         CloseLogFile();
      }
      String ts;
      ts=TimeStamps.FileNameTimeStamp();
      LogFileName = logPath + "\\" + ts + "_" + logName + ".log";
      try
      {
         logFile = new File(LogFileName);
         logFw = new FileWriter(logFile);
         logBw = new BufferedWriter(logFw);
         logBw.write(Tracker.PROG_ID + " " + logName +
                  " Log Created " + TimeStamps.TimeStamp() + "\n\n");
         logBw.flush();
         logFileEmpty = true;
         Tracker.OpMsgLog.LogMsg("New log file opened:\n" + LogFileName + "\n");
      }
      catch (Exception er)
      {
         Tracker.OpMsgLog.LogMsg(Utils.ExceptionString(er) +
                "\nUnable to create " + LogFileName + "\n", OpMsgLogger.LogLevel.WARN);
      }
   }
   
   public String GetLogFileName()
   {
      return LogFileName;
   }
   
   public void WriteToLog(String entry)
   {
      if (logBw != null)
      {
         try
         {
            logBw.write(entry);
            logBw.flush();
            logFileEmpty = false;
         }
         catch (Exception er)
         {
        	 Tracker.OpMsgLog.LogMsg(Utils.ExceptionString(er)+
               "\nexception writing to " + logName + " log file "+LogFileName+"\n",
               OpMsgLogger.LogLevel.WARN);
         }
      }
//      else
//      {
//         System.out.println(logName + " no log file open:" + entry);
//      }
   }

   public void CloseLogFile()
   {
      if (logBw != null)
      {
         try
         {
            logBw.close();
            logFw.close();
            if (logFileEmpty)
            {
               logFile.delete();
            }
         }
         catch (Exception e)
         {}
      }
   }
}
