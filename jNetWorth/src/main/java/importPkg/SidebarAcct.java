package importPkg;

public class SidebarAcct {
   public String name;
   
   public String amount;
   public int htmlIndex;
   public String acctHtml;
   
   final String SIDEBAR_ACCOUNT_TITLE = "sidebar-account__title";
   
   public SidebarAcct(String html, int begIndx) {
      htmlIndex = html.indexOf(SIDEBAR_ACCOUNT_TITLE, begIndx);
      if (htmlIndex > 0) {
         int valIndex = html.indexOf("account-value", htmlIndex);
         if (valIndex > 0) {
            int dollarIndex = html.indexOf("$", valIndex);
            boolean minus = html.substring(dollarIndex - 1, dollarIndex).equals("-");
            int beg = dollarIndex + 1;
            int end = html.indexOf(".", beg) + 3;
            amount = html.substring(beg, end);
            if (minus) {
               amount = "-" + amount;
            }
            int index = html.indexOf("account__second-col", valIndex);
            if (index > 0) {
               end = html.indexOf("/div", index) - 1;
               beg = html.lastIndexOf(">", end) + 1;
               name = html.substring(beg, end);
//            } else {
//               
            }
            int nextAcctIndex = html.indexOf(SIDEBAR_ACCOUNT_TITLE, end);
            if (nextAcctIndex < 0) {
               acctHtml = html.substring(htmlIndex);
            } else {
               acctHtml = html.substring(htmlIndex, nextAcctIndex);
            }
         }
         htmlIndex += acctHtml.length();
      }
   }
}
