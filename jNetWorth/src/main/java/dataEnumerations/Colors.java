package dataEnumerations;


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.graphics.Color;

public class Colors
{
   public static Color BLACK = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
   public static Color BLUE = Display.getCurrent().getSystemColor(SWT.COLOR_BLUE);
   public static Color DARK_BLUE = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE);
   public static Color PALE_BLUE = new Color(Display.getCurrent(), 136, 255, 255);
   public static Color DARK_GRAY = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
   public static Color GRAY = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
   public static Color PALE_GRAY = new Color(Display.getCurrent(), 210,210,210);
   public static Color GREEN = Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);
   public static Color PALE_GREEN = new Color(Display.getCurrent(), 180,255, 180);
   public static Color ORANGE = new Color(Display.getCurrent(), 255, 127, 0);
   public static Color RED = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
   public static Color PALE_RED = new Color(Display.getCurrent(), 255, 100,100);
   public static Color PALER_RED = new Color(Display.getCurrent(), 255, 200,200);
   public static Color YELLOW = Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
   public static Color PALE_YELLOW = new Color(Display.getCurrent(), 255, 255, 114);
   public static Color WHITE = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
   public static Color INFO_BACKGROUND = Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
   public static Color INFO_FOREGROUND = Display.getCurrent().getSystemColor(SWT.COLOR_INFO_FOREGROUND);
   
   //  swt system resources are normally freed when program exits
   //  but if a problem occurs during exit we might have a resouce leak
   //  this code may help in such a situation
   public static void Close()
   {
      BLACK.dispose();
      BLUE.dispose();
      DARK_BLUE.dispose();
      PALE_BLUE.dispose();
      DARK_GRAY.dispose();
      GRAY.dispose();
      PALE_GRAY.dispose();
      GREEN.dispose();
      PALE_GREEN.dispose();
      ORANGE.dispose();
      RED.dispose();
      PALE_RED.dispose();
      PALER_RED.dispose();
      YELLOW.dispose();
      PALE_YELLOW.dispose();
      WHITE.dispose();
      INFO_BACKGROUND.dispose();
      INFO_FOREGROUND.dispose();
   }
}