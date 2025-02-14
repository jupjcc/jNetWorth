package importPkg;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
//import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import chartsPkg.ChartSelections;
import mainPkg.AcctSelection;

public class DataPoint {
   public LocalDateTime dateTime;
   public ArrayList<BigDecimal> amounts;
   
   public DataPoint(LocalDateTime dt) {
      dateTime = dt;
      amounts = new ArrayList<BigDecimal>();
   }
   public void AddAmount(double amt) {
      amounts.add(new BigDecimal(amt));
   }
   public double GetEpochSec() {
      return dateTime.toEpochSecond(ZoneOffset.UTC);
   }
   public String GetAxisLabelStr() {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
      return dateTime.toLocalDate().format(formatter);
   }
   public java.util.Date GetUtilDate() {
      Instant instant = dateTime.toInstant(ZoneOffset.UTC);
      return Date.from(instant);
   }
   // if acctList is null then return values for all accounts
   public double[] GetAmtsArray(List<AcctSelection>acctList) {
//   public double[] GetAmtsArray() {
      double ret[] = null;
      if (amounts.size() > 0) {
         int nAccounts;
         if (acctList == null) {
            nAccounts = ExcelIo.xlHeaders.length;
         } else {
            nAccounts = ChartSelections.accountsToChart.size(); 
         }
         ret = new double[nAccounts];
         int amtIndex = 0;
         if (acctList == null) {
            // get all the amounts
            Iterator<BigDecimal> ita = amounts.iterator();
            while (ita.hasNext()) {
               ret[amtIndex++] = ita.next().doubleValue();
            }
         } else {
            // just get amts in acct list
            for (int acct = 0; acct < nAccounts; acct++) {
               amtIndex = ChartSelections.accountsToChart.get(acct).amtIndex;
               ret[acct] = amounts.get(amtIndex).doubleValue();
            }
         }
//               // convert selected amounts to double and return
//               ret = new double[ChartSelections.accountsToChart.size()];
//               for (idbl = 0; idbl < ret.length; idbl++) {
//                  ret[idbl] = bigDec[ChartSelections.accountsToChart.get(idbl)].doubleValue();
//               }
      }
      return ret;
   }
}
