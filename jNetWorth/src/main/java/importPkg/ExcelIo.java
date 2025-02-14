package importPkg;

/*
 * Manage data exchange with Tracker xlsx file
 */
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;

import configPkg.TrkSettings;
import mainPkg.Tracker;
import utilsPkg.OpMsgLogger;
import utilsPkg.Utils;
/*
 * Handles data to/from the tracking file workbook
 * We have to make sure that the amounts on captures after the
 * first one are listed in the order of the 'Accounts' worksheet 
 */
public class ExcelIo
{
   final static int IMPORTS_HEADERS_ROW = 2;
   public static ExcelHeader []xlHeaders = null;
   public static String inputFileName="";
   public static int NumSheets=0;
   public static String SheetNames[]=null;
   public static XSSFWorkbook Wkbook=null;
   public static ArrayList<DataPoint> importData = new ArrayList<DataPoint>();
   static ArrayList<String> acctNames = new ArrayList<String>();
   static XSSFSheet acctsSht = null;
   static XSSFSheet importSht = null;
   static int acctsHeaderRow; 
   static String outputFileName = "?";
   static int inputSheetNum = -1;
   static OpMsgLogger stLog;
   static CellStyle dateStyle;
   static CellStyle amountStyle;
   static CellStyle boldStyle;
   static TrkSettings settings = null;
   public String initErr = "";
   static XSSFRow mostRecentRow; 
   static boolean creatingNewFile = false;
   static CreationHelper createHelper;

   public static void SetLog(OpMsgLogger statusLog) {
      stLog = statusLog;
   }

   static void initWorkbook() {
      boldStyle = Wkbook.createCellStyle();
      Font f = Wkbook.createFont();
      f.setBold(true);
      boldStyle.setFont(f);
      amountStyle = Wkbook.createCellStyle();
      createHelper = Wkbook.getCreationHelper();
      amountStyle.setDataFormat(
            createHelper.createDataFormat().getFormat("#,##0"));
      amountStyle.setAlignment(HorizontalAlignment.CENTER);      
      importSht = Wkbook.getSheet("Imports");
   }
   
   public static void OpenTrackXl(String inputFile) throws Exception {
      inputFileName = inputFile;
      Wkbook = null;
      FileInputStream fis = new FileInputStream(inputFileName);
//      stLog.LogMsg("Opening workbook for [" + inputFileName + "]\n");
      Wkbook = XSSFWorkbookFactory.createWorkbook(fis);
      fis.close();
      initWorkbook();
      // working with workbook from existing gile
      // if tracker file is new, first account name will not be there 
      XSSFRow namesRow = importSht.getRow(IMPORTS_HEADERS_ROW);
      xlHeaders = new ExcelHeader[namesRow.getLastCellNum() - 1];
      for (int n = 1; n <= xlHeaders.length; n++) {
         Cell c = namesRow.getCell(n);
         CellStyle st = c.getCellStyle();
         int fontIndex = st.getFontIndexAsInt();
         Font f = Wkbook.getFontAt(fontIndex);
         String name = c.getStringCellValue();
         xlHeaders[n-1] = new ExcelHeader(name, f.getBold());
      }
      importData.clear();
      try {
         Cell firstAcctNameCell = namesRow.getCell(1);
         String firstAcctName = firstAcctNameCell.getStringCellValue();
         if (firstAcctName.equals(xlHeaders[0].name)) {
            // build memory imports struct
            int rowNum;
            XSSFRow row = null;
            for (rowNum = IMPORTS_HEADERS_ROW + 1; rowNum < 1048576; rowNum++) {
               row = importSht.getRow(rowNum);
               if (row == null) {
                  // retrieve most recent data from last row
                  mostRecentRow = importSht.getRow(rowNum - 1);
                  break;
               }
               try {
                  Cell cell = row.getCell(0);
                  if (cell == null || 
                        cell.getCellType() == CellType.BLANK) {
                     break;
                  }
                  CellType cType = cell.getCellType();
                  if (!DateUtil.isCellDateFormatted(cell)) {
                     throw new Exception("Workbook row is missing date");
                  }
                  else {
                     DataPoint dp = new DataPoint(cell.getLocalDateTimeCellValue());
                     // build data point with imported amounts
                     int amtNum = 1;
                     while (amtNum <= xlHeaders.length) {
                        double amt = 0;
                        cell = row.getCell(amtNum);
                        if (cell != null) {
                           cType = cell.getCellType();
                           if (cType == CellType.NUMERIC) {
                              amt = cell.getNumericCellValue();
                           }
                        }
                        dp.AddAmount(amt);
                        amtNum++;
                     } // end while (amtNum <= xlHeaders.length) {
                     if (dp.amounts.size() == 0) {
                        throw new Exception("Row #" + rowNum + " has no amounts");
                     }
                     importData.add(dp);
                  }
               } catch (Exception bce) {
                  stLog.LogMsg(Utils.ExceptionString(bce) ,
                                    OpMsgLogger.LogLevel.ERROR);
                  break;
               }
            }
            int bpoint=1;
         }
      } catch (Exception ee) {}
   }     // OpentrackXl

   public static void CreateNewFile(String filePath) throws Exception {
      stLog.LogMsg("Creating new Excel workbook\n");
      Wkbook = new XSSFWorkbook();
      initWorkbook();
      importSht = Wkbook.createSheet("Imports");
      CellRangeAddress mrgRow0 = new CellRangeAddress(0, 0, 0, 5);
      importSht.addMergedRegion(mrgRow0);
      
      XSSFRow row = importSht.createRow(0);
      Cell cell = row.createCell(0);
      cell.setCellValue("jNetWorth Track File");
      XSSFRow rowName = importSht.createRow(IMPORTS_HEADERS_ROW);
      XSSFRow rowAmt = importSht.createRow(IMPORTS_HEADERS_ROW + 1);
      mostRecentRow = rowAmt;
      LocalDateTime ldt = setDateCell(rowAmt);
      
      importSht.setDefaultColumnWidth(11);
      DataPoint dp = new DataPoint(ldt);
      xlHeaders = new ExcelHeader[Tracker.impFromHtml.ImpItems.length];
      for (int a = 0; a < Tracker.impFromHtml.ImpItems.length; a++) {
         AcctImportItem item = Tracker.impFromHtml.ImpItems[a];
         cell = rowName.createCell(a + 1);
         if (item.isGroupHeader) {
            cell.setCellStyle(boldStyle);
         }
         cell.setCellValue(item.name);
         cell = rowAmt.createCell(a + 1);
         cell.setCellStyle(amountStyle);
         double amt = item.value;
         dp.AddAmount(amt);
         cell.setCellValue(amt);
         importSht.autoSizeColumn(a + 1);
         xlHeaders[a] = new ExcelHeader(item.name, item.isGroupHeader);
      }
      importData.clear();
      importData.add(dp);
      writeWorkbook();
      stLog.LogMsg("Track file created with current imported data\n");
   }     // end createNewRorkbook
   
   public static double GetMostRecentVal(String colHdr) throws Exception {
      for (int h = 0; h < xlHeaders.length; h++) {
         if (xlHeaders[h].name.equals(colHdr)) {
            return mostRecentRow.getCell(h + 1).getNumericCellValue();
         }
      }
      return 0;
   }
   public static String GetMostRecentDate() throws Exception {
      DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
      String ret = df.format(mostRecentRow.getCell(0).getDateCellValue());
      return ret;
   }
   
   static LocalDateTime setDateCell(XSSFRow row) {
      Cell newDateCell = row.createCell(0);
      CellStyle dateStyle = Wkbook.createCellStyle();
      dateStyle.setDataFormat(
            createHelper.createDataFormat().getFormat("m/d/yy"));
      newDateCell.setCellValue(Tracker.impFromHtml.importDateTime);
      newDateCell.setCellStyle(dateStyle); 
      return newDateCell.getLocalDateTimeCellValue();
   }
   
   
   public static void AddRow() throws Exception {
//      stLog.LogMsg("Updating Imports sheet with new data row\n");
	   // add row
	   int rowNum = mostRecentRow.getRowNum() + 1; 
      mostRecentRow = importSht.createRow(rowNum);
      stLog.LogMsg("inserting imported data on row #" +
                     (rowNum + 1) + "\n"); // user row num is 1 based
      // alignment and other properties cloned from template xlsx
      CreationHelper createHelper = Wkbook.getCreationHelper();
      Cell newDateCell = mostRecentRow.createCell(0);
      CellStyle dateStyle = Wkbook.createCellStyle();
      dateStyle.setDataFormat(
            createHelper.createDataFormat().getFormat("m/d/yy"));
      newDateCell.setCellValue(Tracker.impFromHtml.importDateTime);
      newDateCell.setCellStyle(dateStyle);
      System.out.println("filling amounts row, netWorth=" +
            Tracker.impFromHtml.GetImport(xlHeaders[0].name).value + "\n");
      // add the new row to importData
      LocalDateTime ldt = setDateCell(mostRecentRow);
      DataPoint dp = new DataPoint(ldt);
      // filling existing workbook with imported values
      AcctImportItem item = null;
      int colNum;
      for (int v = 0; v < xlHeaders.length; v++) {
         colNum = 1 + v;
         item = Tracker.impFromHtml.GetImport(xlHeaders[v].name);
         if (item == null) {
            System.out.println("import item [" + v + "], "+ xlHeaders[v].name + " not found, v=" + v);
            dp.AddAmount(0);
         } else {
            Cell cell = mostRecentRow.getCell(colNum);
            if (cell == null) {
               cell = mostRecentRow.createCell(colNum);
            }
            cell.setCellStyle(amountStyle);
            cell.setCellValue(item.value);
            cell.setCellValue(item.value);
            importSht.autoSizeColumn(colNum);
            dp.AddAmount(item.value);
         }
      }
      importData.add(dp);
      writeWorkbook();
   }     // end AddRow
   
   static void writeWorkbook() throws Exception {
      String outName = Tracker.Settings.TrackExcelFileName;
      FileOutputStream fileOut = new FileOutputStream(outName);
      Wkbook.write(fileOut);
      fileOut.close();
      stLog.LogMsg("Tracking file updated with new data:" +
             "\n" + outName + "\n", OpMsgLogger.LogLevel.INFO, true);
   }
   
   public static LocalDate firstDate() {
      return importData.get(0).dateTime.toLocalDate();
   }
   public static LocalDate lastDate() {
      int lastIndex = importData.size() - 1;
      return importData.get(lastIndex).dateTime.toLocalDate();
   }

   public static DataPoint GetDataPoint(LocalDate lDate) {
      Iterator<DataPoint> idp = importData.iterator();
      while (idp.hasNext()) {
         DataPoint d = idp.next();
         if (lDate.equals(d.dateTime.toLocalDate())) {
            return d;
         }
      }
      return null;
   }

   public static String[] GetImportDates() {
      String dates[] = new String[importData.size()];
      Iterator<DataPoint> idp = importData.iterator();
      int count = 0;
      while (idp.hasNext()) {
         DataPoint d = idp.next();
         dates[count++] = d.dateTime.toLocalDate().toString();
      }
      return dates;
   }

   public static int[] GetSelectedImportIndices(LocalDate firstDate, LocalDate lastDate) {
      ArrayList<Integer> indices = new ArrayList<Integer>();
      int importIndex = 0;
      while (importIndex <= importData.size()) {
         LocalDate thisDate = importData.get(importIndex).dateTime.toLocalDate();
         if (indices.size() > 0) {
            // already started selection, test for end
            indices.add(importIndex);
            if (thisDate.equals(lastDate)) {
               break;
            }
         } else {
            // looking for first date
            if (thisDate.equals(firstDate)) {
               indices.add(importIndex);                  
            }
         }
         importIndex++;
      }
      int ret[] = new int[indices.size()];
      
      for (int i = 0; i < ret.length; i++) {
         ret[i] = indices.get(i); 
      }
      return ret;
   }

}