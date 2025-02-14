package utilsPkg;

/**
 * Title:       TimeStamps
 * Description: Provide a consistent time stamp for files
 *              created by the P&E SIL Test Manager
 * Copyright:   Copyright (c) 2002 - 2005
 * Company:     SAIC
 * @author      Cockerham
 * @version 2.0 jcc 10/18/05 - create new string each call
 *              tm can generated multiple system logs since
 *              acquisition list can be modified on the fly by
 *              custom display list + these functions now used
 *              to time stamp control data files
 */
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TimeStamps {

   /*****************************************************************
    * year goes first in file name - makes for better sorting
    ****************************************************************/
   public static String FileNameTimeStamp(Date d)
   {
      SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
      return df.format(d);
   }
   /*****************************************************************
    * return stamp based on current time
    ****************************************************************/
   public static String FileNameTimeStamp()
   {
      Date d = new Date();
      return FileNameTimeStamp(d);
   }
   /*****************************************************************
    * return stamp based on current time
    ****************************************************************/
   public static Date DateFromFileNameTimeStamp(String s)
   {
      SimpleDateFormat df;
      try
      {
         df = new SimpleDateFormat("yyyyMMdd_HHmmss");
         return df.parse(s);
      }
      catch (java.text.ParseException pe)
      {
         //  ensure that this one isn't chosen as most recent
         return new Date(0);
      }
   }

   public static String TimeOnly()
   {
      SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
      Date d = new Date();
      return df.format(d);
   }
   
   /*****************************************************************
    * return user friendly date+time from UTC seconds (since 1/1/1970)
    ****************************************************************/
   public static String DateTime(double secUtc,boolean utcSecFmt)
   {
      if ((secUtc<1000000.) || utcSecFmt)
      {
         // assume seconds since boot
         return String.format("%13.1f",secUtc);
      }
      // assume sec since 1/1/1970
      Date dt = new Date((long)(1000. * secUtc));
      SimpleDateFormat df = new SimpleDateFormat("MM/dd HH:mm:ss.SS");
      df.setTimeZone(TimeZone.getTimeZone("UTC"));
      return df.format(dt);
   }
   
   // In order to Excel to display time stamp properly, it must be enclosed in ''
   public static String CsvTimeStamp()
   {
      return "\'" + TimeStamp() + "\'";
   }
   
   /*****************************************************************
    * more human-readable for embedding text in files
    ****************************************************************/
   public static String TimeStamp(Date d)
   {
      SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
      return df.format(d);
   }
   
   /*****************************************************************
    * return user friendly date+time from UTC seconds (since 1/1/1970)
    ****************************************************************/
   public static String TimeStr(double secUtc,boolean utcSecFmt)
   {
      if ((secUtc<1000000.) || utcSecFmt)
      {
         // assume seconds since boot
         return String.format("%13.1f",secUtc);
      }
      // assume sec since 1/1/1970
      long mSec=1000*(long)secUtc;
      Date dt=new Date(mSec);
      SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SS");
      df.setTimeZone(TimeZone.getTimeZone("UTC"));
      return df.format(dt);
   }
   
   /*****************************************************************
    * return stamp based on current
    ****************************************************************/
   public static String TimeStamp()
   {
      Date d = new Date();
      return TimeStamp(d);
   }
   
}