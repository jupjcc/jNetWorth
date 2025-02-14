package chartsPkg;

import java.util.ArrayList;
import java.util.List;

import mainPkg.AcctSelection;

public class ChartSelections {
   public static List<AcctSelection> accountsToChart = new ArrayList<AcctSelection>();
   
   // returns resulting number of selections
   public static int SelectToChart(AcctSelection acct, boolean select) {
      if (select) {
         accountsToChart.add(acct);
      } else {
         accountsToChart.remove(acct);
      }
      return accountsToChart.size();
   }
   public static void ClearChartSelections() {
      accountsToChart.clear();
   }
}
