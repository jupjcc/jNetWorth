package importPkg;

public class SidebarGroup {
   String name;
   double value = 0;
   int htmlIndex;    // file index where name starts
   int lastHtmlIndex = 0;
   
   public SidebarGroup(String n, int i) {
      name = n;
      htmlIndex = i;
   }
}
