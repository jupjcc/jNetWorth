package importPkg;

public class AcctImportItem implements Comparable<AcctImportItem> {
   String name;
   double value;
   boolean isGroupHeader = false;
   boolean isSelected = false;
   
   public AcctImportItem(String n, double v, boolean hdr) {
      name = n;
      value = v;
      isGroupHeader = hdr;
   }
   
   public AcctImportItem(String n, double v) {
      name = n;
      value = v;
   }
   
   // performing sort in decreasing values
   public int compareTo(AcctImportItem item1) {
      if (value == item1.value) {
         return 0;
      } else if (value > item1.value) {
         return -1;
      } else {
         return 1;
      }
   }
}
