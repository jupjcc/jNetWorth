package utilsPkg;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.time.Instant;
//import java.time.LocalDate;
import java.time.LocalDateTime;
//import java.time.ZoneId;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TimeZone;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import dataEnumerations.DataTypes;

/**
 * Title:        utillities package
 * Description:  collection of general purpose utilities
 * Copyright:    Copyright (c) 2002
 * Company:      SAIC
 * @author       John Cockerham
 * @version      2.0
 */


public class Utils {

   static float MIN_FLOAT_VALUE = (float) 0.001;
   static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
   static final char[] HEX_DIGITS_LC = "0123456789abcdef".toCharArray();
   
   public static String AmountStr(double val) {
      String pfx = "$";
      double dspVal = val;
      if (val < 0) {
         pfx = "-$";
         dspVal = -val;
      }
      return pfx + String.format("%1$,.0f", dspVal);
   }
   public static int Uint16(ByteBuffer bb)
   {
      int u16 = bb.getShort();
      if (u16 < 0)
      {
         u16 += 65536;
      }
      return u16;
   }
   public static int Uint32(ByteBuffer bb)
   {
      int u32 = bb.getInt();
      if (u32 < 0)
      {
         u32 += 0x100000000L;
      }
      return u32;
   }
   public static int[] BytesToInts(byte[] byts, int bOffs)
   {
      int ret[] = null;
      ByteBuffer bb = ByteBuffer.wrap(byts);
      bb.order(ByteOrder.LITTLE_ENDIAN);
      int nInts = (byts.length - bOffs) / 4;
      if (nInts > 0)
      {
         ret = new int[nInts];
         bb.position(bOffs);
         for (int i = 0; i < nInts; i++)
         {
            ret[i] = bb.getInt();
         }
      }
      return ret;
   }
   public static int BytesToInt(byte[] byts, int bOffs)
   {
      int ret = 0;
      ByteBuffer bb = ByteBuffer.wrap(byts);
      bb.order(ByteOrder.LITTLE_ENDIAN);
      int nInts = (byts.length - bOffs) / 4;
      if (nInts > 0)
      {
         bb.position(bOffs);
         ret = bb.getInt();
      }
      return ret;
   }
   
   public static String AscizToString(byte []buf, int bufOffs)
   {
      for (int b = 0; b < 1024; b++)
      {
         if (buf[b + bufOffs] == 0)
         {
            byte strBytes[] = new byte[b];
            System.arraycopy(buf, bufOffs, strBytes, 0, b);
            return new String(strBytes);
         }
      }
      return null;
   }
   
   //--------------------------------------------------------------------------
   static String ZeroFill2(int i)
   {
      if ( i<10 )
      {
         return "0" + String.valueOf(i);
      }
      else
      {
         return String.valueOf(i);
      }
   }
   
   // fast way to convert byte array to hex string without using String.format
   public static String BytesToHex(byte b[], int offs, int nBytes, char delim)
   {
      char hex[];
      if (delim == 0)
      {
         hex = new char[2*nBytes];
         int v;
         for (int i=0; i<nBytes; i++)
         {
            v = b[i + offs] & 0xFF;
            hex[i*2] = HEX_DIGITS[v>>4];
            hex[i*2+1] = HEX_DIGITS[v & 0x0F];
         }
      }
      else
      {
         hex = new char[3*nBytes];
         int v;
         for (int i=0; i<nBytes; i++)
         {
            v = b[i + offs] & 0xFF;
            hex[i*3] = HEX_DIGITS[v>>4];
            hex[i*3+1] = HEX_DIGITS[v & 0x0F];
            hex[i*3+2] = delim;
         }
      }
      return new String(hex);
   }
   // fast means to convert byte array to hex string without using String.format
   public static String BytesToHex(byte b[], int nBytes, char delim)
   {
      return BytesToHex(b, 0, nBytes, delim);
   }
   public static String BytesToHex(byte b[], int offs, int nBytes)
   {
      return BytesToHex(b, offs, nBytes, ',');
   }
   public static String BytesToHex(byte b[], int nBytes)
   {
      return BytesToHex(b, nBytes, ',');
   }
   public static String BytesToHex(byte b[])
   {
      return BytesToHex(b, b.length, ' ');
   }
   public static String BreakByteStringIntoLines(String str, int bytesPerLine, boolean showOffs)
   {
      String ret = "";
      int offs = 0;
      if (str.length() > 0)
      {
         int nFullStrs = str.length() / (3 * bytesPerLine);
         int nStrs;
         if ((3 * bytesPerLine) * nFullStrs < str.length())
         {
            nStrs = nFullStrs + 1;
         }
         else
         {
            nStrs = nFullStrs;
         }
         for (int f=0; f<nFullStrs; f++)
         {
            if (showOffs)
            {
               ret += String.format("%04d: ", offs);
               offs += bytesPerLine;
            }
            int pos = 3 * f * bytesPerLine;
            ret += str.substring(pos, pos + 3 * bytesPerLine) + "\n";
         }
         if (nStrs > nFullStrs)
         {
            if (showOffs)
            {
               ret += String.format("%04d: ", offs);
            }
            ret += str.substring(nFullStrs * 3 * bytesPerLine) + "\n";
         }
      }
      return ret;
   }
  
   public static int StringToInt(String str)
   {
      if (str==null)
      {
         return 0;
      }
      else
      {
         String s=str.trim();
         if (s.length()==0)
         {
            return 0;
         }
         else if ((s.length()>1) &&
                  (s.substring(0,2).equals("0x")))
         {
            return Integer.parseInt(s.substring(2),16);
         }
         else
         {
            return Integer.parseInt(s);
         }
      }
   }

   //  return true if chr is a legitimate character in a numeric field
   public static boolean LegitNumericField(String txt,char chr,int pos,int pType)
   {
      boolean haveDecPt = false;
      String  testString;
      if (chr==0x7f||
          chr==0x8)         // always allow backspace or delete chars
      {
         return true;
      }
      if (chr==0x20)
      {
         return false;     // disallow spaces
      }
      if (chr==0)
      {
         // null char; validate entire field
         testString=txt;
      }
      else
      {
         testString=txt.substring(0,pos)+chr+txt.substring(pos,txt.length());
      }
      boolean signed=pType==(DataTypes.INT)||(pType==DataTypes.FLT);
      for (int i=0; i<testString.length (); i++)
      {
         char ch = testString.charAt(i); 
         if (ch=='.')
         {
            if (haveDecPt||(pType!=DataTypes.FLT))
            {
               return false;
            }
            haveDecPt = true;
         }
         else if (ch=='-')
         {
            if (i>0||!signed)
            {
               return false;
            }
         }
         else if (ch<'0'||ch>'9')
         {
            if (pType==DataTypes.HEX)
            {
               if (!((ch>='a'&&ch<='f') ||
                     (ch>='A'&&ch<='F')))
               {
                  return false;
               }
            }
            else
            {
               return false;
            }
         }
      }        // end loop thru chars
      return true; 
   }
   
   //  return true if chr is a legitimate character in a numeric field
   //   this form is used for PLEM simulation cells
   public static boolean LegitNumericField(String txt,    char chr,       int pos,
                                           boolean signed,boolean float_t)
   {
      boolean haveDecPt = false;
      String  testString;
      if (chr==0x7f||
          chr==0x8)         // always allow backspace or delete chars
      {
         return true;
      }
      if (chr==0x20)
      {
         return false;     // disallow spaces
      }
      if (chr==0)
      {
         // null char; validate entire field
         testString=txt;
      }
      else
      {
         testString=txt.substring(0,pos)+chr+txt.substring(pos,txt.length());
      }
      for (int i=0; i<testString.length (); i++)
      {
         char ch = testString.charAt(i); 
         if (ch=='.')
         {
            if (haveDecPt||!float_t)
            {
               return false;
            }
            haveDecPt = true;
         }
         else if (ch=='-')
         {
            if (i>0||!signed)
            {
               return false;
            }
         }
         else if (ch<'0'||ch>'9')
         {
            return false;
         }
      }        // end loop thru chars
      return true; 
   }
  
   //-------------------------------------------------------------------------
   //  chop floating pt string to reasonable string for display
   public static String DispFltStr(float val, int len)
   {
      String str = String.valueOf(val);
      int length = str.length();

      if ( (val < MIN_FLOAT_VALUE) && (val > -MIN_FLOAT_VALUE))
      {
         switch (len)
         {
            case 0: return "0";
            case 1: return "0.0";
            case 2: return "0.00";
            case 3: return "0.000";
            default: return "0.00";
         }
      }
      int pointPosition = str.indexOf('.');
      int expPosition = str.indexOf('e');
      if ( expPosition<0 )
      {
         expPosition = str.indexOf('E');
      }
      if ( pointPosition >= 0 )
      {
         if ((length - pointPosition) > len)
         {
            return (str.substring(0,pointPosition+len+1));
         }
      }
      else
      {
         return (str.substring(0,pointPosition+len+1) +
                              str.substring(expPosition));
      }
      return str;
   }         // end DispFltStr
   
   static public String ExceptionString(Exception ex)
   {
      if (ex.getMessage()==null)
      {
         return ex.toString();
      }
      else
      {
         return ex.getMessage();
      }
   }

   /******************************************************************
    * void PressButton
    *
    *   General purpose button pusher for use from Java code or script
    *   
    *   . checks for widget being disposed
    *   . if button is of type radio
    *     This function enforces true radio button behavior.
    *     When rdox.setSelection(true) is called, the other buttons
    *     in the same composite should be deselected, but they aren't.
    *     Calling this function instead of setSelection causes the
    *     other buttons to be deselected
    *******************************************************************/   
   public static void PressButton(Button b)
   {
      if (b==null)
      {
         return;
      }
      if ((b.getStyle()&SWT.RADIO)== 0)
      {
         //  simple pushbutton or checkbox
         b.notifyListeners(SWT.Selection, null);
         return;
      }
      //  it's a radio button
      Composite parent = b.getParent();
      Control [] children = parent.getChildren();
      for (int i=0; i<children.length; i++) {
         Control child = children [i];
         try    // in case there's a non-button in composite
         {
            Button btn = (Button)child;
            if ( btn == b )
            {
               if (!btn.getSelection())
               {
                  btn.setSelection(true);
                  b.notifyListeners(SWT.Selection, null);
               }
            }
            else if ((btn.getStyle()&SWT.RADIO) != 0)
            {
               btn.setSelection (false);
            }
         }
         catch (Exception e)
         {
         }
      }       // thru the children
   }         // end SelectRadioButton

   static public String GetOpenFileName(Shell s,
                                        String title,      String []filterNames,
                                        String []fileTypes,String defaultFileName)
   {
      FileDialog fd = new FileDialog(s, SWT.OPEN);
      fd.setText("Open "+title);
      if (!defaultFileName.equals(""))
      {
         File fx=new File(defaultFileName);
         if (fx.isFile())
         {
            fd.setFileName(defaultFileName);
         }
         else
         {
            fd.setFilterPath(defaultFileName);
         }
      }
      fd.setFilterExtensions(fileTypes);
      fd.setFilterNames(filterNames);
      String fn=fd.open();
      if (fn==null)
      {
         return "";
      }
      else
      {
         return fn;
      }
   }
   
   static public String GetOpenFileName(Shell s,
                                        String title,   String filterName,
                                        String fileType,String defaultFileName)
   {
      String[] filterExt={fileType,"*.*"};
      String[] filterNames={filterName,"all files"};
      return GetOpenFileName(s,title,filterNames,filterExt,defaultFileName);
   }

   static public String GetOpenFileName(Shell s,
                                        String title,String filterName,String fileType)
   {
      return GetOpenFileName(s,title,filterName,fileType,"");
   }
   
   static public String GetSaveFileName(Shell s,
                                        String title,      String []filterNames,
                                        String []fileTypes,String defaultFileName,
                                        boolean promptForReplace)
   {
      String  fn=null;
      boolean done=false;
      while (!done)
      {
         FileDialog fd = new FileDialog(s,SWT.SAVE);
         fd.setText("Save");
         if (!defaultFileName.equals(""))
         {
            File fx=new File(defaultFileName);
            if (fx.isDirectory())
            {
               fd.setFilterPath(defaultFileName);
            }
            else
            {
               fd.setFileName(defaultFileName);
            }
         }
         fd.setFilterExtensions(fileTypes);
         fd.setFilterNames(filterNames);
         fn = fd.open();
         if (fn == null)
         {
            return "";
         }
         File f = new File(fn);
         if (f.exists())
         {
            MessageBox mb=new MessageBox(s,SWT.ICON_WARNING|SWT.YES|SWT.NO);
            mb.setMessage(fn+" already exists. Replace it?");
            done = mb.open() == SWT.YES;
         }
         else
         {
            done=true;
         }
      }
      return fn;
   }
   
   static public String GetSaveFileName(Shell s,          String title,
                                        String filterName,String fileType,
                                        String defaultFileName,boolean promptForReplace)
   {
      String fName="";
      if (defaultFileName!=null)
      {
         fName=defaultFileName;
      }
      String filtName="";
      if (filterName!=null)
      {
         filtName=filterName;
      }
      String fType="";
      if (fileType!=null)
      {
         fType=fileType;
      }
      String[] filterExts={fType,"*.*"};
      String[] filterNames={filtName,"all files (*.*)"};
      return GetSaveFileName(s,title,filterNames,
                             filterExts,fName,promptForReplace);
   }
   
   static public String GetSaveFileName(Shell s,String title,
                                        String filterName,String fileType,
                                        boolean promptForReplace)
   {
      return GetSaveFileName(s,title,filterName,fileType,"",promptForReplace);
   }
   static public String FileDateStr(long dateLong) {
      return new SimpleDateFormat("MM/dd/yyyy HH-mm-ss").format(dateLong);
   }
   static public File GetMostRecentFile(String folder, String endsWith) throws Exception {
      File dir = new File(folder);
      final String filt = endsWith;
      File[] files = dir.listFiles(new FilenameFilter() {
         @Override
         public boolean accept(File dir, String name) {
            return name.endsWith(filt);
         }
      });
      Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
      return files[0];
   }
   
   static public LocalDateTime GetLocalDateTime(long epochMsec) {
      return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMsec), 
            TimeZone.getDefault().toZoneId());  
   }
   
}
