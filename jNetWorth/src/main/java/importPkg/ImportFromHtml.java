package importPkg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import utilsPkg.OpMsgLogger;
import utilsPkg.Utils;

public class ImportFromHtml {
   final String ASSETS_STR =
         "net-worth-bar__name qa-net-worth-bar-name";
   final String LIABILITIES_STR =
         "js-sidebar-liabilities pc-u-mb--";
   final String NET_WORTH_STR = 
         "span class=3D\"sidebar__networth-amount";         
   final String INVESTMENT_GROUP_STR = 
         "qa-investment-accounts";
   final String ACCOUNT_VALUE_STR =
         "qa-sidebar-account-value";
   final String SIDEBAR_ACCOUNT_DETAIL_NAME = 
         "qa-sidebar-account-detail-name";
   final String SIDEBAR_SECOND_COL_STR = 
         "sidebar-account__second-col";
   final String ACCOUNT_DETAIL_STR =
         "qa-sidebar-account-detail-name";
   final String ACCOUNT_TITLE_STR = 
         "qa-sidebar-account-title";
   final String ACCOUNT_GROUP_LABEL_STR = 
         "qa-sidebar-account-group-label ";
   final String ACCOUNT_GROUP_VALUE_STR =
         "qa-account-group-value tabular-numbers";
   final String CREDIT_CARD_GROUP_STR = 
         "qa-credit-card-accounts";
   final String OTHER_ASSETS_GROUP_STR = 
         "qa-other-assets-accounts";
   final String LINK_ANOTHER_STR = 
         "sidebar__add-account-button-container";
   public double netWorth = 0;
   public double assets = 0;
   public double liabilities = 0;
   public LocalDateTime importDateTime;
   public int investmentItemsStart;
   public String htmlFileInfo = "none";
   int lastInvestmentIndex;
   int lastImportIndex;

   ArrayList<SidebarGroup> sidebarGroups = new ArrayList<SidebarGroup>();
   ArrayList<SidebarGroup> valuedGroups = new ArrayList<SidebarGroup>();
   
   // import items is built as a list then converted to array
   public AcctImportItem []ImpItems;
   ArrayList<AcctImportItem> impItems = new ArrayList<AcctImportItem>();
   ArrayList<Double> insVals = new ArrayList<Double>();
   double []insertVals;  // in order of xl file
   
   static OpMsgLogger stLog;
   String htmlStr;  
   int fileIndex = 0;
   
   public ImportFromHtml(String folder,
                         String nameEndsWith,
                         OpMsgLogger log) throws Exception {
      stLog = log;
      File htmlFile = Utils.GetMostRecentFile(folder, nameEndsWith);
      importDateTime = Utils.GetLocalDateTime(htmlFile.lastModified());
      long lastModified = htmlFile.lastModified(); 
      htmlFileInfo = htmlFile.getPath() + " last modified " + 
                                       Utils.FileDateStr(lastModified);
      ArrayList<SidebarAcct> sidebarAccts = new ArrayList<SidebarAcct>();  
//      Date date = new Date(lastModified);
      String importFileName = htmlFile.getAbsolutePath();
//      MessageBox mb = new MessageBox(Tracker.shlMain, SWT.YES | SWT.NO);
//      mb.setMessage(
//            "The import data was last updated on " + date);
//      mb.setText("Is this import file the one you want?");
//      if (mb.open() != SWT.YES) {
//         return;
//      }
      BufferedReader reader = new BufferedReader(new FileReader(importFileName));
      StringBuilder sb = new StringBuilder();
      String line = null;
      String ls = System.getProperty("line.separator");
      while ((line = reader.readLine()) != null) {
         sb.append(line);
         sb.append(ls);
      }
      // delete the last new line separator
      sb.deleteCharAt(sb.length() - 1);
      reader.close();
      htmlStr = sb.toString(); 
      /* jcc 20240415 v3.0 use .mhtml files (mhtml is
       *   mime-enabled html). This avoids having to export
       *   (save as) multiple web page files from Empower app
       *   But we have to kludge the string read from .mhtml
       *   file to eliminate lines ending with =, which is
       *   apparently an artifact of exporting as .mhtml
       */
       htmlStr = htmlStr.replaceAll("=\r\n", "");
//      // try writing new text file with all the =\r\n removed
//      try
//      {
//         File mhtmlFile = new File("c:\\jNetWorth\\data\\remEquals.mhtml");
//         PrintWriter pw = new PrintWriter(
//                                    new FileOutputStream(mhtmlFile));
//         pw.println(mhtml);
//         pw.close();
//      } catch (Exception ex) {
//         System.out.println(Utils.ExceptionString(ex)
//                  + "\nException saving mhtml file");
//      }
      
      impItems.clear();
      
      lastImportIndex = htmlStr.indexOf("Link another account");
      netWorth = getAmt(NET_WORTH_STR);
      assets = getAmt(ASSETS_STR);
      liabilities = getAmt(LIABILITIES_STR);
      impItems.add(new AcctImportItem("NetWorth", netWorth, true));
      impItems.add(new AcctImportItem("Assets", assets, true));
      impItems.add(new AcctImportItem("Liabilities", liabilities, true));
      investmentItemsStart = impItems.size();
//      stLog.LogMsg(String.format(
//            "\nImports from file " + importFileName + ":\n" +
//            "NetWorth $%1$,.0f Assets $%2$,.0f Liabilities $%3$,.0f\n\n",
//            netWorth, assets, liabilities));
      
      // find all group labels - for each one check if group value /= 0
      fileIndex = 0;
//      int fileIndexPrev = 0;
      while (true) {
         fileIndex = htmlStr.indexOf(ACCOUNT_GROUP_LABEL_STR, fileIndex);
         if (fileIndex < 0) {
//            String lastStr = htmlStr.substring(fileIndexPrev);
            break;
         }
//         fileIndexPrev = fileIndex;
         int beginLabel = htmlStr.indexOf(">", fileIndex) + 1;
         fileIndex = htmlStr.indexOf("</", beginLabel) - 1;
         String label = htmlStr.substring(beginLabel, fileIndex).trim();
         sidebarGroups.add(new SidebarGroup(label, beginLabel));
      }
      SidebarGroup []groups = new SidebarGroup[sidebarGroups.size()];
      for (int g = 0; g < groups.length; g++) {
         groups[g] = sidebarGroups.get(g);
      }
      // if a value matcb is found then find the group containing that
      //  html index
      for (int g = 0; g < groups.length - 1; g++) {
         groups[g].lastHtmlIndex = groups[g + 1].htmlIndex;
      }
      groups[groups.length-1].lastHtmlIndex = lastImportIndex;
      
      // build valuedGroups, sidebar groups with a non-zero value
      int groupNum = 0;
      while (groupNum < groups.length) {
         fileIndex = groups[groupNum].htmlIndex;
         fileIndex = htmlStr.indexOf(ACCOUNT_GROUP_VALUE_STR, fileIndex);
         if (fileIndex < 0) {
            break;
         }
         if (fileIndex > groups[groupNum].lastHtmlIndex) {
            if (groupNum > groups.length) {
               throw new Exception("search for enclosing group failed");
            }
         } else {
            // groupNum is the enclosing group
            // find the group's value
            int beg = htmlStr.indexOf("\">", fileIndex) + 2;
            int end = htmlStr.indexOf("</", beg);
            String amtStr = htmlStr.substring(beg, end);
            amtStr = amtStr.replace("$", "");
            double val = valFromAmtStr(amtStr);
            // if the value is zero then we ignore it
            if (val != 0) {
               groups[groupNum].value = val;
               valuedGroups.add(groups[groupNum]);
               // group name and value are added as AccImportItems
               AcctImportItem newItem = new AcctImportItem(groups[groupNum].name, val);
               newItem.isGroupHeader = true;
               impItems.add(newItem);
            }
         }
         groupNum++;
      }
      fileIndex = 0;
      // search for sidebar accounts
      while (true) {
         //  get sidebar details data
         SidebarAcct sba = new SidebarAcct(htmlStr, fileIndex);
         if (sba.htmlIndex < 0) {
            break;
         }
         if (!sba.amount.equals("$0.00")) {
            sidebarAccts.add(sba);
            AcctImportItem newItem = new AcctImportItem(sba.name, valFromAmtStr(sba.amount));
            impItems.add(newItem);
         }
         fileIndex = sba.htmlIndex;
         if (fileIndex > lastImportIndex) {
            break;
         }
      }  // end while true
      ImpItems = new AcctImportItem[impItems.size()];
      int investmentsIndex = -1;
      int stopSortIndex = -1;
      for (int i = 0; i < impItems.size(); i++) {
         ImpItems[i] = impItems.get(i);
         if (ImpItems[i].name.equals("Investment")) {
            investmentsIndex = i;
         } else if (ImpItems[i].isGroupHeader &&
                     investmentsIndex >= 0 &&
                     stopSortIndex < 0) {
            stopSortIndex = i;
         }
      }
      Arrays.sort(ImpItems, investmentsIndex + 1, stopSortIndex);
   }

   double valFromAmtStr(String amtStr) {
      String valStr = amtStr.replace("$", "");
      return Double.parseDouble(valStr.replaceAll(",", ""));
   }
   
   public AcctImportItem GetImport(String nam) {
      for (int n = 0; n < ImpItems.length; n++) {
         if (ImpItems[n].name.equals(nam)) {
            return ImpItems[n];
         }
      }
      return null;
   }
      
   public double GetGroupTotal(String grpName) {
      Iterator<SidebarGroup> itg = valuedGroups.iterator();
      while (itg.hasNext()) {
         SidebarGroup sg = itg.next();
         String nam = sg.name;
         if (nam.equals(grpName)) {
            return sg.value;
         }
      }
      return 0;
   }
      
   double getAmt(String name) {
      fileIndex = htmlStr.indexOf(name);
      int beginAmt = htmlStr.indexOf(">$", fileIndex) + 2;
      fileIndex = htmlStr.indexOf("<", beginAmt);
      String amtStr = htmlStr.substring(beginAmt, fileIndex);
      return Double.parseDouble(amtStr.replaceAll(",", ""));
   }
   
}
