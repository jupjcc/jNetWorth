package mainPkg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swtchart.Chart;
import org.eclipse.swtchart.IAxis;
import org.eclipse.swtchart.IAxisSet;
import org.eclipse.swtchart.IAxisTick;
import org.eclipse.swtchart.IBarSeries;
import org.eclipse.swtchart.ILegend;
import org.eclipse.swtchart.ILineSeries;
import org.eclipse.swtchart.ISeries.SeriesType;
import org.eclipse.swtchart.ISeriesLabel;
import org.eclipse.swtchart.ISeriesSet;
import org.eclipse.swtchart.internal.series.CircularSeries;
import org.eclipse.wb.swt.SWTResourceManager;

import chartsPkg.ChartMouseListener;
import chartsPkg.ChartSelections;
import configPkg.TrkSettings;
import importPkg.DataPoint;
import importPkg.ExcelIo;
import importPkg.ImportFromHtml;
import utilsPkg.OpMsgLogger;
import utilsPkg.TextLogger;
import utilsPkg.Utils;

/*  jNetWorth maintains an excel file containing net worth investments
 *  Input is from the Empower app.
 *  Revisions:
 *  2025.02.17 let gson handle json saved account selections
 *  2025.01.16 corrected display of negative amounts
 *  2024.12.25 .mthml format changed + ported to Lenovo Win 11
 *  2024.04.15 import Empower dashboard from "Empower - Dashboard.mhtml"
 *                                saved as "Webpage, single file"
 *  2023.12.07 Output text amounts to accounts section of SetupDisplay tab
 */
public class Tracker extends Shell {
	public static String PROG_ID = "jNetWorth Java Empower Tracker v20250217";
	public static String BASE_PATH = "c:\\jNetWorth\\";
	public static String CFG_PATH = BASE_PATH + "cfg\\";
	final static String EMPOWER_IMPORT_FOLDER = BASE_PATH + "data";
	final static String IMPORT_FROM_EMPOWER_STR = "To import new Empower data:\n"
			+ "  1. Run the Empower app after logging in\n"
			+ "  2. Right click in a clear area on Dashboard (left) panel\n"
			+ "  3. Click Save As Type 'Webpage, single file (*.mhtml)' and\n"
			+ "     ensure that the file is saved in folder \n" + "     " + EMPOWER_IMPORT_FOLDER + "\n";

//   final static String HELP_TEXT_IMPORT =
//              "To Update the track file:\n" +
//              "1. Run the Empower app after logging in.\n" +
//              "2. Right click on Investments to display html.\n" +
//              "3. Click Save As and select folder \"C:\\jNetWorth\\Data\\"\n" +
//                  ** make sure you Save as type Webpage, single file (*.mhtml)
//              "4. Click the Import button";
	final static String TAB_IMPORTED_DATA = "ImportedData";
	final static String TAB_SETUP_DISPLAY = "SetupDisplay";
	final static String TAB_DATA_DISPLAY = "DataDisplay";
	public static ImportFromHtml impFromHtml;
	public static AcctSelection[] acctSelectors;
	public static OpMsgLogger OpMsgLog;
	public static TrkSettings Settings;
	public static Shell shlMain;
	protected static StyledText stxtOpMsgLog = null;
	static TextLogger sLog;
//   private static Text txtCurrentLogFile;
	public static Display MainDisplay;
	ArrayList<String> amounts;
	static boolean captureOk = false;
	static boolean xlFileAbort = false;
	static Text txtXlFile;
	static Button btnNewFile;
	static Button btnBrowse;
	static Button btnUseCurrent;
	static Button btnResetAccts;
	static Button btnSelAllAccts;
	static Combo cboSelRefDate;
	static Combo cboBeginDateRange;
	static Combo cboEndDateRange;
	static ScrolledComposite scmpAcctList;
	static private Composite cmpAcctList;
	static Label lblDateAcctList;
	static LocalDate refDate;
//   static WaitAndNotify xlFileOk = null;
	static TabFolder tabFolder;
	static TabItem tabImport;
	static TabItem tabCtrlDisp;
	static TabItem tabDataDisp;
	static String prevTabItemName;
	static boolean xlFileOk = false;
	static boolean importData = false;
	static Composite cmpCtrlDisp;
	private Group grpSnapshotCharts;
	Group grpAcctSel;
	Composite cmpChart;
	Button btnPieChart;
	Button btnBarChart;
	double[] snapshotAmts;
	String[] snapshotAcctNames;
	String snapshotLegend;
	private Composite cmpDataDisp;
	String dollarFmtStr;
	DecimalFormat dollarFmt;
	Chart chart;

	enum historyChartType {
		BALANCES, DELTA_DOLLARS, DELTA_PERCENT;
	}

	ChartMouseListener mouseListener;
	static SashForm sashForm;
	private Group grpSelectFile;
	private Label lblCurrentFie;
	FormData fdTabFolder;
	FormData fd_sash;
	FormData fd_grpOpMsg;
	private Group grpImpData;
	private Label lblRecentDate;
	static private Text txtRecentDate;
	private Label lblRecentNetWorth;
	static private Text txtRecentTotInv;
	private Label lblTotInv;
	static private Text txtRecentNetWorth;
	private Label lblRecentTrkDate;
	static private Text txtRecentTrkDate;
	private Label lblRecentTrkNetWorth;
	static private Text txtRecentTrkNetWorth;
	private Label lblTotInvTrk;
	static private Text txtRecentTotInvTrk;
	private Button btnNewTrack;
	private Button btnRefreshImport;
	private Group grpTrkFileData;
	static private Button btnRefreshTrk;
	static private Text txtHtmlFileInfo;
	private Label lblHtmlFileInfo;
	private Composite cmpImportFile;
	private static Button btnSaveList;
	private static Button btnLoadList;
	private Composite cmpAcctBtns;
	private Label lblRefDate;

//   private Text txtTopWeight;
//   private Text txtBottomWeight;
//   private Label lblTopWeight;
//   private Group grpTestTabWeights;
	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			Display display = Display.getDefault();
			TrkSettings sClass = new TrkSettings();
			MainDisplay = display;
			shlMain = new Tracker(MainDisplay);
			// Create the child shell and the dispose listener
			shlMain.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					Settings.WriteJsonObject();
				}
			});
			tabFolder.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					String itemName = tabFolder.getSelection()[0].getText();
					if (!itemName.equals("")) {
						selectTab(itemName);
					}
				}

				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
			});
			shlMain.pack();
			shlMain.open();
			shlMain.layout();
			selectTab(TAB_IMPORTED_DATA);

			OpMsgLog.LogMsg(PROG_ID + "\n");
			Settings = sClass.GetInstance(OpMsgLog);
//         OpMsgLog.LogMsg("Reading most recently imported Empower html file\n");
			readEmpowerInpFile();
			if (impFromHtml == null) {
				try {
					ExcelIo.OpenTrackXl(Settings.TrackExcelFileName);
				} catch (Exception xe) {
					MessageBox mb = new MessageBox(shlMain, SWT.ABORT);
					mb.setMessage(Utils.ExceptionString(xe) + "\nError attempting to read track file\n"
							+ "Program cannot continue without track file\n" + "or imported data file");
					mb.setText("Fatal Error - no data");
					mb.open();
					System.exit(1);
				}
			}
			boolean haveFile = !Settings.TrackExcelFileName.endsWith("none");
			btnUseCurrent.setEnabled(haveFile);
			ExcelIo.SetLog(OpMsgLog);
//         OpMsgLog.LogMsg( 
//               "\nDisplay current and historical account balances\n" +
//               "as reported by the Empower app.  By examining these charts\n" +
//               "you can observe and compare account performances.\n" +
//               "You import from the Empower app into a track file.\n" +
//               "Performing an import updates the track file, which is\n" +
//               "in Excel .xls format. jTrack uses this data to present\n" +
//               "charts, and you can also view the raw imported data\n" +
//               "by opening the track file with Excel.\n\n");
//         OpMsgLog.LogMsg( 
//               "To Update the track file (Import):\n" +
//               "  1. Run the Empower app after logging in.\n" +
//               "  2. Right click in a clear area on the left (Dashboard) panel\n" +
//               "  3. Click Save As Type Webpage, single file (*.mhtml)and enter file name \n" +
//               "      " + ExcelIo.HTML_IMPORT_FILE + "\n" +
//               "  4. Click the Import button\n" +
//               "  5. Click the SetupDisplay tab\n",
//                     OpMsgLogger.LogLevel.PROMPT);
			OpMsgLog.LogMsg("Select the track file\n",
//                         "Note that if you choose to create a new file,\n" +
//                         "the most recent imported data shown will be used.\n",
					OpMsgLogger.LogLevel.PROMPT);

			txtXlFile.setText(Settings.TrackExcelFileName);

			tabFolder.setSelection(tabImport);
			captureOk = false;
			while (!shlMain.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the shell.
	 * 
	 * @param display
	 */
	public Tracker(Display display) {
		super(display, SWT.SHELL_TRIM);
		setMinimumSize(new Point(402, 700));
		setSize(578, 527);
		setText(PROG_ID);

		createContents(display);
		OpMsgLog.SetFileLogger(sLog);
	}

	/**
	 * Create contents of the shell.
	 */
	protected void createContents(Display disp) {
		setLayout(new FillLayout(SWT.HORIZONTAL));
		sashForm = new SashForm(this, SWT.VERTICAL);
		sashForm.setLayout(new FormLayout());

		Composite cmpTabs = new Composite(sashForm, SWT.NONE);
		cmpTabs.setLayout(new FillLayout(SWT.HORIZONTAL));

		tabFolder = new TabFolder(cmpTabs, SWT.NONE);

		// ***************************************************************************
		// B E G I N I M P O R T T A B
		tabImport = new TabItem(tabFolder, SWT.NONE);
		tabImport.setText(TAB_IMPORTED_DATA);
		prevTabItemName = TAB_IMPORTED_DATA;
		Group grpImports = new Group(tabFolder, SWT.NONE);
		tabImport.setControl(grpImports);
		FormData fd_imports = new FormData();
		fd_imports.left = new FormAttachment(0, 1);
		fd_imports.right = new FormAttachment(100, 0);
		fd_imports.top = new FormAttachment(0, 1);
		grpImports.setLayoutData(fd_imports);
		grpImports.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		grpImports.setText("Imported Data");
		GridLayout gl_grpImports = new GridLayout(4, false);
		gl_grpImports.marginHeight = 1;
		gl_grpImports.verticalSpacing = 1;
		gl_grpImports.marginBottom = 1;
		gl_grpImports.horizontalSpacing = 2;
		grpImports.setLayout(gl_grpImports);

		// group for specifying Excel tracking file
		// vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
		grpSelectFile = new Group(grpImports, SWT.NONE);
		grpSelectFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		grpSelectFile.setText("Select Track File");
		GridLayout gl_grpSelectFile = new GridLayout(3, false);
		gl_grpSelectFile.marginHeight = 1;
		gl_grpSelectFile.verticalSpacing = 1;
		grpSelectFile.setLayout(gl_grpSelectFile);
		btnUseCurrent = new Button(grpSelectFile, SWT.NONE);
		btnUseCurrent.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		btnUseCurrent.setSize(108, 25);
		btnUseCurrent.setText("Use Current");
		btnUseCurrent.setToolTipText("use the file shown");
		btnUseCurrent.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Settings.TrackExcelFileName = txtXlFile.getText();
//            OpMsgLog.LogMsg("Opening track file " + 
//                              Settings.TrackExcelFileName + "\n");
				try {
					if (ExcelIo.xlHeaders == null) {
						ExcelIo.OpenTrackXl(Settings.TrackExcelFileName);
					}
					reportTrkFile();
					// compare most recent track file results with imported data file and
					// if different then update track file
					if (impFromHtml != null) {
						DateTimeFormatter df = DateTimeFormatter.ofPattern("MM/dd/yyyy");
						String impDateStr = impFromHtml.importDateTime.format(df);
						if (impFromHtml.netWorth == ExcelIo.GetMostRecentVal("NetWorth")
								&& impFromHtml.GetGroupTotal("Investment") == ExcelIo.GetMostRecentVal("Investment")
								&& impDateStr.equals(ExcelIo.GetMostRecentDate())) {
							OpMsgLog.LogMsg("Track file is up to date with " + "current import html file\n",
									OpMsgLogger.LogLevel.INFO, true);
							OpMsgLog.LogMsg(
									"Click Refresh on import data row "
											+ "to download new data from Empower web site\n",
									OpMsgLogger.LogLevel.PROMPT);
							btnRefreshImport.setEnabled(true);
						} else {
							OpMsgLog.LogMsg("Track file data doesn't match imported" + " data file\n");
							OpMsgLog.LogMsg("Click Refresh on Track file row " + "to update the track file\n",
									OpMsgLogger.LogLevel.PROMPT);
							btnRefreshTrk.setEnabled(true);
						}
					}
//               setupDisplaySelections();
					setupAcctSelections();
				} catch (Exception oe) {
					OpMsgLog.LogMsg(Utils.ExceptionString(oe) + "\nError opening Track file, choose"
							+ "\nBrowseExistingFiles or Create New Track File\n", OpMsgLogger.LogLevel.ERROR);
				}
			} // end bntUseCurrent selection
		});
		btnBrowse = new Button(grpSelectFile, SWT.NONE);
		btnBrowse.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		btnBrowse.setSize(50, 25);
		btnBrowse.setToolTipText("Browse for existing file");
		btnBrowse.setText("BrowseExistingFiles");
		btnBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String fName = Utils.GetOpenFileName(shlMain, "Track file ", "excel with macros files(*.xlsx)",
						"*.xlsx", TrkSettings.DATA_FOLDER);
				if (fName != null && !fName.isEmpty()) {
					txtXlFile.setText(fName);
					Settings.TrackExcelFileName = fName;
					btnUseCurrent.setEnabled(true);
				}
			}
		});
		btnNewTrack = new Button(grpSelectFile, SWT.NONE);
		btnNewTrack.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		btnNewTrack.setText("Create New Track File");
		btnNewTrack.setToolTipText("Create a new track file with imported data");
		btnNewTrack.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String defFileName = TrkSettings.DATA_FOLDER + "jNetWorth.xlsx";
				String fName = Utils.GetSaveFileName(shlMain, "Track Excel Compatible File",
						new String[] { "xlsx files(*.xlsx)" }, new String[] { "*.xlsx" }, defFileName, true); // true->prompt
																												// for
																												// replace
				if (fName != null) {
					txtXlFile.setText(fName);
					Settings.TrackExcelFileName = fName;
					MessageBox mb = new MessageBox(shlMain, SWT.YES | SWT.NO);
					mb.setText("Refresh Empower Data?");
					mb.setMessage("Refresh Import data from Empower Website first (Yes)\n"
							+ "or create track file using the data shown as most recent (NO)?");
					if (mb.open() == SWT.YES) {
						OpMsgLog.LogMsg(
								"1. Run the Empower app after logging in.\n"
										+ "2. Right click in empty space on the left 'Net Worth' Empower dashboard\n"
										+ "3. Click Save As and select folder c:\\jNetWorth\\data\\\n"
										+ "4. Click the Refresh button in Most Recent Data\n",
								OpMsgLogger.LogLevel.PROMPT);
						btnRefreshImport.setEnabled(true);
					}
					try {
						ExcelIo.CreateNewFile(fName);
						reportTrkFile();
//                  setupDisplaySelections();
						setupAcctSelections();
					} catch (Exception ce) {
						OpMsgLog.LogMsg(Utils.ExceptionString(ce) + "\nException creating track file",
								OpMsgLogger.LogLevel.ERROR);
					}
				}
			}
		});

		lblCurrentFie = new Label(grpSelectFile, SWT.NONE);
		lblCurrentFie.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));
		lblCurrentFie.setText("Current Track File");
		new Label(grpSelectFile, SWT.NONE);
		new Label(grpSelectFile, SWT.NONE);
		txtXlFile = new Text(grpSelectFile, SWT.BORDER);
		txtXlFile.setEditable(false);
		txtXlFile.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 5, 1));
		tabImport.setControl(grpImports);
		// group for displaying most recent data in track file
		// vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
		grpTrkFileData = new Group(grpImports, SWT.NONE);
		grpTrkFileData.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		grpTrkFileData.setText("Most Recent Track File Data");
		GridLayout gl_grpTrkFileData = new GridLayout(7, false);
		gl_grpTrkFileData.marginWidth = 1;
		gl_grpTrkFileData.marginHeight = 1;
		gl_grpTrkFileData.verticalSpacing = 1;
		grpTrkFileData.setLayout(gl_grpTrkFileData);
		lblRecentTrkDate = new Label(grpTrkFileData, SWT.NONE);
		GridData gd_lblRecentTrkDate = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
		gd_lblRecentTrkDate.widthHint = 32;
		lblRecentTrkDate.setLayoutData(gd_lblRecentTrkDate);
		lblRecentTrkDate.setText("Date");
		txtRecentTrkDate = new Text(grpTrkFileData, SWT.BORDER);
		txtRecentTrkDate.setText("no file");
		txtRecentTrkDate.setEditable(false);
		GridData gd_txtRecentTrkDate = new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1);
		gd_txtRecentTrkDate.widthHint = 56;
		txtRecentTrkDate.setLayoutData(gd_txtRecentTrkDate);
		lblRecentTrkNetWorth = new Label(grpTrkFileData, SWT.NONE);
		lblRecentTrkNetWorth.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		lblRecentTrkNetWorth.setText("NetWorth");
		txtRecentTrkNetWorth = new Text(grpTrkFileData, SWT.BORDER);
		GridData gd_txtRecentTrkNetWorth = new GridData(SWT.LEFT, SWT.BOTTOM, true, false, 1, 1);
		gd_txtRecentTrkNetWorth.widthHint = 54;
		txtRecentTrkNetWorth.setLayoutData(gd_txtRecentTrkNetWorth);
		txtRecentTrkNetWorth.setText("no file");
		txtRecentTrkNetWorth.setEditable(false);
		lblTotInvTrk = new Label(grpTrkFileData, SWT.NONE);
		lblTotInvTrk.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblTotInvTrk.setText("TotalInvestments");
		txtRecentTotInvTrk = new Text(grpTrkFileData, SWT.BORDER);
		txtRecentTotInvTrk.setText("no file");
		GridData gd_txtRecentTotInvTrk = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtRecentTotInvTrk.widthHint = 56;
		txtRecentTotInvTrk.setLayoutData(gd_txtRecentTotInvTrk);

		btnRefreshTrk = new Button(grpTrkFileData, SWT.NONE);
		btnRefreshTrk.setEnabled(false);
		btnRefreshTrk.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		btnRefreshTrk.setToolTipText("Update track file with most recent imported data");
		btnRefreshTrk.setText("Refresh");
		btnRefreshTrk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTrackFile();
			}
		});
		// group for displaying most recent imported data
		// from the 'Save As' file created from Empower dashboard
		// vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
		grpImpData = new Group(grpImports, SWT.NONE);
		grpImpData.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		grpImpData.setText("Most Recent Imported Data");
		GridLayout gl_grpImpData = new GridLayout(7, false);
		gl_grpImpData.marginWidth = 1;
		gl_grpImpData.marginHeight = 1;
		gl_grpImpData.verticalSpacing = 1;
		grpImpData.setLayout(gl_grpImpData);
		lblRecentDate = new Label(grpImpData, SWT.NONE);
		GridData gd_lblRecentDate = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
		gd_lblRecentDate.widthHint = 32;
		lblRecentDate.setLayoutData(gd_lblRecentDate);
		lblRecentDate.setText("Date");
		txtRecentDate = new Text(grpImpData, SWT.BORDER);
		txtRecentDate.setText("none");
		txtRecentDate.setEditable(false);
		GridData gd_txtRecentDate = new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1);
		gd_txtRecentDate.widthHint = 56;
		txtRecentDate.setLayoutData(gd_txtRecentDate);
		lblRecentNetWorth = new Label(grpImpData, SWT.NONE);
		lblRecentNetWorth.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		lblRecentNetWorth.setText("NetWorth");
		txtRecentNetWorth = new Text(grpImpData, SWT.BORDER);
		txtRecentNetWorth.setText("no data");
		txtRecentNetWorth.setEditable(false);
		GridData gd_txtRecentNetWorth = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_txtRecentNetWorth.widthHint = 54;
		txtRecentNetWorth.setLayoutData(gd_txtRecentNetWorth);
		lblTotInv = new Label(grpImpData, SWT.NONE);
		lblTotInv.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblTotInv.setText("TotalInvestments");
		txtRecentTotInv = new Text(grpImpData, SWT.BORDER);
		txtRecentTotInv.setText("no data");
		GridData gd_txtRecentTotInv = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_txtRecentTotInv.widthHint = 56;
		txtRecentTotInv.setLayoutData(gd_txtRecentTotInv);
		btnRefreshImport = new Button(grpImpData, SWT.NONE);
		btnRefreshImport.setEnabled(true);
		btnRefreshImport.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		btnRefreshImport.setToolTipText("Update this most recent data from web site");
		btnRefreshImport.setText("Refresh");
		btnRefreshImport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnRefreshImport.getText().equals("Refresh")) {
					OpMsgLog.LogMsg(IMPORT_FROM_EMPOWER_STR + "  Click the button now labeled Import\n",
							OpMsgLogger.LogLevel.PROMPT);
					btnRefreshImport.setText("Import");
				} else {
					readEmpowerInpFile();
					btnRefreshImport.setText("Refresh");
					updateTrackFile();
					setDateSelections();
				}
			}
		});
		cmpImportFile = new Composite(grpImpData, SWT.NONE);
		cmpImportFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 7, 1));
		GridLayout gl_cmpImportFile = new GridLayout(2, false);
		gl_cmpImportFile.horizontalSpacing = 7;
		cmpImportFile.setLayout(gl_cmpImportFile);
		lblHtmlFileInfo = new Label(cmpImportFile, SWT.NONE);
		lblHtmlFileInfo.setSize(70, 15);
		lblHtmlFileInfo.setText("Imported File");
		txtHtmlFileInfo = new Text(cmpImportFile, SWT.BORDER);
		txtHtmlFileInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtHtmlFileInfo.setSize(437, 21);
		txtHtmlFileInfo.setEditable(false);
		txtHtmlFileInfo.setText("none");
		// E N D I M P O R T T A B

		// *******************************************************************************
		// *******************************************************************************
		// T A B T O C O N T R O L W H A T ' S B E I N G D I S P L A Y E D
		// note that this tab depends on data read from excel template file
		// so we disable it here and finish populating it after reading template
		tabCtrlDisp = new TabItem(tabFolder, SWT.NONE);
		tabCtrlDisp.setText(TAB_SETUP_DISPLAY);
		cmpCtrlDisp = new Composite(tabFolder, SWT.NONE);
		tabCtrlDisp.setControl(cmpCtrlDisp);
		cmpCtrlDisp.setLayout(new FormLayout());

		// vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
		// select account(s)
		grpAcctSel = new Group(cmpCtrlDisp, SWT.NONE);
		FormData fd_grpAcctSel = new FormData();
		fd_grpAcctSel.bottom = new FormAttachment(100, -1);
		fd_grpAcctSel.right = new FormAttachment(60, -1);
		fd_grpAcctSel.top = new FormAttachment(0);
		fd_grpAcctSel.left = new FormAttachment(0, 1);
		grpAcctSel.setLayoutData(fd_grpAcctSel);
		grpAcctSel.setText("Account Selection for charts");
		grpAcctSel.setLayout(new GridLayout(2, false));

		cmpAcctBtns = new Composite(grpAcctSel, SWT.NONE);
		cmpAcctBtns.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		cmpAcctBtns.setLayout(new GridLayout(2, false));

		btnResetAccts = new Button(cmpAcctBtns, SWT.NONE);
		btnResetAccts.setEnabled(true);
		btnResetAccts.setText("Reset");
		btnResetAccts.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (int c = 0; c < acctSelectors.length; c++) {
					acctSelectors[c].SetSelection(false);
				}
				btnSaveList.setEnabled(false);
				ChartSelections.ClearChartSelections();
			}
		});

		btnSaveList = new Button(cmpAcctBtns, SWT.NONE);
		btnSaveList.setEnabled(false);
		btnSaveList.setText("Save to File");
		btnSaveList.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				String fName = Utils.GetSaveFileName(shlMain, "Account Selections", "accts files(*.accts)", "*.accts",
						CFG_PATH, true);
				if (fName != null) {
					try {
			          Settings.acctsListFileName = fName;
						File file = new File(fName);
						PrintWriter pw = new PrintWriter(new FileOutputStream(file));
						int nSelAccts = ChartSelections.accountsToChart.size();
						for (int acct = 0; acct < nSelAccts; acct++) {
							String acctName = ChartSelections.accountsToChart.get(acct).name;
							pw.println(acctName);
						}
						pw.close();
						btnLoadList.setEnabled(true);
						OpMsgLog.LogMsg("Account selections saved to " + fName + "\n");
					} catch (Exception ex) {
						String err = Utils.ExceptionString(ex) + "\nException saving account selections to " + fName
								+ "\n";
						OpMsgLog.LogMsg(err, OpMsgLogger.LogLevel.ERROR);
						MessageBox mb = new MessageBox(shlMain, SWT.ICON_ERROR | SWT.OK);
						mb.setText("ERROR");
						mb.setMessage(err);
						mb.open();
					}
				}
			}
		});

		btnSelAllAccts = new Button(cmpAcctBtns, SWT.NONE);
		btnSelAllAccts.setEnabled(true);
		btnSelAllAccts.setText("Select All");
		btnSelAllAccts.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (int c = 0; c < acctSelectors.length; c++) {
					acctSelectors[c].SetSelection(true);
					ChartSelections.SelectToChart(acctSelectors[c], true);
				}
				btnSaveList.setEnabled(true);
			}
		});

		btnLoadList = new Button(cmpAcctBtns, SWT.NONE);
		btnLoadList.setText("Load from File");
		btnLoadList.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				String fName = Utils.GetOpenFileName(shlMain, "Account selections list", "accts files(*.accts)",
						"*.accts", Settings.acctsListFileName);
				if (fName != null) {
					try {
						FileReader fr = new FileReader(fName);
						BufferedReader br = new BufferedReader(fr);
						String rline;
						while ((rline = br.readLine()) != null) {
							if (rline.length() > 0) {
								for (int a = 0; a < ExcelIo.xlHeaders.length; a++) {
									if (rline.equals(ExcelIo.xlHeaders[a].name)) {
										acctSelectors[a].SetSelection(true);
//                              ckbAcctsSel[a].setSelection(true);
										ChartSelections.SelectToChart(acctSelectors[a], true);
										break;
									}
								}
							}
						}
						scmpAcctList.layout();
						br.close();
						btnLoadList.setEnabled(true);
						OpMsgLog.LogMsg("Account selections recalled from " + fName + "\n");
					} catch (Exception ex) {
						String err = Utils.ExceptionString(ex) + "\nException recalling account selections from "
								+ fName + "\n";
						MessageBox mb = new MessageBox(shlMain, SWT.ICON_ERROR | SWT.OK);
						mb.setText("ERROR");
						mb.setMessage(err);
						mb.open();
					}
				}
			}
		});
		lblDateAcctList = new Label(grpAcctSel, SWT.NONE);
		lblDateAcctList.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, true, false, 1, 1));
		lblDateAcctList.setText("2023-12-07 Acct Totals ");

		// vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
		// select reference date and date range
		Group grpDateSelection = new Group(cmpCtrlDisp, SWT.NONE);
		grpDateSelection.setText("Date Selection");
		grpDateSelection.setLayout(new GridLayout(2, false));
		FormData fd_grpDateSelection = new FormData();
		fd_grpDateSelection.top = new FormAttachment(0, 1);
		fd_grpDateSelection.bottom = new FormAttachment(0, 108);
		fd_grpDateSelection.right = new FormAttachment(100);
		fd_grpDateSelection.left = new FormAttachment(60);
		grpDateSelection.setLayoutData(fd_grpDateSelection);
		lblRefDate = new Label(grpDateSelection, SWT.NONE);
		GridData gd_lblRefDate = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
		gd_lblRefDate.widthHint = 100;
		gd_lblRefDate.horizontalIndent = 8;
		lblRefDate.setLayoutData(gd_lblRefDate);
		lblRefDate.setText("Set Reference Date");
		cboSelRefDate = new Combo(grpDateSelection, SWT.READ_ONLY);
		cboSelRefDate.setText("Reference Date");
		GridData gd_cboSelRefDate = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_cboSelRefDate.widthHint = 64;
		cboSelRefDate.setLayoutData(gd_cboSelRefDate);
		cboSelRefDate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setRefDate();
			}
		});
		Label lblBeginDateRange = new Label(grpDateSelection, SWT.NONE);
		lblBeginDateRange.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		lblBeginDateRange.setText("Begin Date Range");
		cboBeginDateRange = new Combo(grpDateSelection, SWT.READ_ONLY);
		cboBeginDateRange.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridData gd_BeginDateRange = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_BeginDateRange.widthHint = 64;
		cboBeginDateRange.setLayoutData(gd_BeginDateRange);
		cboBeginDateRange.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Settings.DateRangeStartStr = cboBeginDateRange.getText();
			}
		});
		Label lblEndDateRange = new Label(grpDateSelection, SWT.NONE);
		lblEndDateRange.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblEndDateRange.setText("End Date Range");
		cboEndDateRange = new Combo(grpDateSelection, SWT.READ_ONLY);
		cboEndDateRange.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridData gd_EndDateRange = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_EndDateRange.widthHint = 64;
		cboEndDateRange.setLayoutData(gd_EndDateRange);

		// vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
		// create snapshot chart at reference date
		grpSnapshotCharts = new Group(cmpCtrlDisp, SWT.NONE);
		grpSnapshotCharts.setText("Snapshots at reference date");
		grpSnapshotCharts.setLayout(new GridLayout(2, false));
		FormData fd_grpSnapshotCharts = new FormData();
		fd_grpSnapshotCharts.left = new FormAttachment(grpDateSelection, 0, SWT.LEFT);
		fd_grpSnapshotCharts.right = new FormAttachment(grpDateSelection, 0, SWT.RIGHT);
		fd_grpSnapshotCharts.top = new FormAttachment(grpDateSelection, 1);
		grpSnapshotCharts.setLayoutData(fd_grpSnapshotCharts);
		btnPieChart = new Button(grpSnapshotCharts, SWT.NONE);
		btnPieChart.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		btnPieChart.setText("Pie");
		btnPieChart.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (snapshotChartSetupOk()) {
					IAxisSet axisSet = chart.getAxisSet();
					IAxis xAxis = axisSet.getXAxis(0);
					chart.getTitle().setText("Account Balances on " + refDate.toString());
					xAxis.getTitle().setText("Accounts");
					axisSet.getYAxis(0).getTitle().setText("Balance");
					CircularSeries circularSeries = (CircularSeries) chart.getSeriesSet().
//                     createSeries(SeriesType.DOUGHNUT,"pie series");
		               createSeries(SeriesType.PIE, "pie series");
					// account for negative value on pie charts 
					double []pieAmts = new double[snapshotAmts.length];
					double minPie = snapshotAmts[0];
					for (int s = 0; s < pieAmts.length; s++) {
					   if (snapshotAmts[s] < minPie) {
					      minPie = snapshotAmts[s];
					   }
					}
               for (int s = 0; s < pieAmts.length; s++) {
                  if (minPie < 0) {
	                  pieAmts[s] = snapshotAmts[s] - minPie;
	               } else {
                     pieAmts[s] = snapshotAmts[s];
	               }   
					}
					String[] legends = new String[snapshotAcctNames.length];
					for (int a = 0; a < snapshotAcctNames.length; a++) {
						legends[a] = String.format("%-13s: %-9s", snapshotAcctNames[a],
								dollarFmt.format(snapshotAmts[a]));
					}
//               circularSeries.setSeries(snapshotAcctNames,snapshotAmts);
					circularSeries.setSeries(legends, pieAmts);
					tabFolder.setSelection(tabDataDisp);
				}
			}
		});
		btnBarChart = new Button(grpSnapshotCharts, SWT.NONE);
		btnBarChart.setSelection(true);
		btnBarChart.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		btnBarChart.setText("Bar");
		btnBarChart.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (snapshotChartSetupOk()) {
					// following from https://www.javatips.net/api/swtchart-master/
					// org.swtchart.examples/src/org/swtchart/examples/BarChartExample.java
					IAxisSet axisSet = chart.getAxisSet();
					IAxis xAxis = axisSet.getXAxis(0);
					ILegend legend = chart.getLegend();
					legend.setVisible(false);
//               chart.setToolTipText("tooltip");
					chart.getTitle().setText("Account Balances on " + refDate.toString());
					xAxis.getTitle().setText("Accounts");
					xAxis.setCategorySeries(snapshotAcctNames);
					xAxis.enableCategory(true);
					axisSet.getYAxis(0).getTitle().setText("Balance");
					IBarSeries<?> series = (IBarSeries<?>) chart.getSeriesSet().createSeries(SeriesType.BAR,
							snapshotLegend);
					series.setYSeries(snapshotAmts);
					ISeriesLabel seriesLabel = series.getLabel();
					seriesLabel.setFormat(dollarFmtStr);
					Color color = new Color(Display.getDefault(), 0, 0, 0);
					seriesLabel.setForeground(color);
					seriesLabel.setVisible(true);
					// xAxis.enableCategory(true);
//               xAxis.setRange(new Range(0,1));
					axisSet.adjustRange();
					tabFolder.setSelection(tabDataDisp);
				}
			}
		});

		// vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
		// create history chart in selected data range
		Group grpHistory = new Group(cmpCtrlDisp, SWT.NONE);
		grpHistory.setText("History between begin and end dates");
		grpHistory.setLayout(new GridLayout(3, false));
		FormData fd_grpHistory = new FormData();
		fd_grpHistory.right = new FormAttachment(100);
		fd_grpHistory.top = new FormAttachment(grpSnapshotCharts, 6);
		fd_grpHistory.left = new FormAttachment(grpDateSelection, 0, SWT.LEFT);
		grpHistory.setLayoutData(fd_grpHistory);

		Button btnAmtHistory = new Button(grpHistory, SWT.NONE);
		btnAmtHistory.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, false, 1, 1));
		btnAmtHistory.setText("Balances");
		btnAmtHistory.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				historyChart(historyChartType.BALANCES);
				{
					tabFolder.setSelection(tabDataDisp);
				}
			}
		});

		Button btnHistoryDeltas = new Button(grpHistory, SWT.NONE);
		btnHistoryDeltas.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		btnHistoryDeltas.setText("Change$$");
		btnHistoryDeltas.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				historyChart(historyChartType.DELTA_DOLLARS);
				{
					tabFolder.setSelection(tabDataDisp);
				}
			}
		});

		Button btnHistoryPercent = new Button(grpHistory, SWT.NONE);
		btnHistoryPercent.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		btnHistoryPercent.setText("Change %");
		btnHistoryPercent.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				historyChart(historyChartType.DELTA_PERCENT);
				{
					tabFolder.setSelection(tabDataDisp);
				}
			}
		});

		// end group for specifying Excel tracking file
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		// *******************************************************************************
		// *******************************************************************************
		// T A B T O D I S P L A Y D A T A
		// note that this tab depends on data read from excel template file
		// so we disable it here and finish populating it after reading template
		tabDataDisp = new TabItem(tabFolder, SWT.NONE);
		tabDataDisp.setText(TAB_DATA_DISPLAY);

		cmpDataDisp = new Composite(tabFolder, SWT.NONE);
		tabDataDisp.setControl(cmpDataDisp);
		cmpDataDisp.setLayout(new FormLayout());
		cmpChart = new Composite(cmpDataDisp, SWT.NONE);
		cmpChart.setLayout(new FillLayout(SWT.HORIZONTAL));
		FormData fd_cmpChart = new FormData();
		fd_cmpChart.top = new FormAttachment(grpAcctSel);
		fd_cmpChart.bottom = new FormAttachment(100);
		fd_cmpChart.right = new FormAttachment(100);
		fd_cmpChart.left = new FormAttachment(0);
		cmpChart.setLayoutData(fd_cmpChart);

		scmpAcctList = new ScrolledComposite(grpAcctSel, SWT.BORDER | SWT.V_SCROLL);
		scmpAcctList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		scmpAcctList.setLayout(new FillLayout(SWT.VERTICAL));
		cmpAcctList = new Composite(scmpAcctList, SWT.NONE);
		cmpAcctList.setLayout(new GridLayout(1, false));
		scmpAcctList.setContent(cmpAcctList);
		scmpAcctList.setExpandHorizontal(true);
		scmpAcctList.setExpandVertical(true);
		scmpAcctList.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				org.eclipse.swt.graphics.Rectangle r = scmpAcctList.getClientArea();
				scmpAcctList.setMinSize(grpAcctSel.computeSize(r.width, SWT.DEFAULT));
			}
		});

		// *******************************************************************************
		// S A S H between tab folder and operator log
//      final Composite sash = new Composite(sashForm, SWT.NONE);
//      FormData fd_sash = new FormData();
//      fd_sash.left = new FormAttachment(0, 1);
//      fd_sash.right = new FormAttachment(100);
//      fd_sash.top = new FormAttachment(50, 0);
//      sash.setLayoutData(fd_sash);
////      final FormData sashData = new FormData();
////      final int sashPercentFromTop = 0; //
//      final int sashMinPixFromEdge = 60;
////      sashData.left = new FormAttachment(0, 0);
////      sashData.top = new FormAttachment(sashPercentFromTop, 0);
////      sashData.right = new FormAttachment(100, 0);
////      sash.setLayoutData(sashData);
//      sash.addListener(SWT.Selection, new Listener() {
//        public void handleEvent(Event e) {
//          Rectangle sashRect = sash.getBounds();
//          Rectangle formRect = sashForm.getClientArea();
//          int  bottom = formRect.height - sashRect.height - sashMinPixFromEdge;
//          e.y = Math.max(Math.min(e.y, bottom), sashMinPixFromEdge);
////          e.x = Math.max(Math.min(e.x, right), sashMinPixFromEdge);
//          if (e.y != sashRect.y) {
//            fdTabFolder.bottom = new FormAttachment(0, e.y);
//            fd_grpOpMsg.top =  new FormAttachment(0, e.y);
//            sashForm.layout();
//          }
//        }
//      });

		// *******************************************************************************
		// B O T T O M S A S H - always displayed below tab folder
		// display messages to operator
		// *******************************************************************************
		Group grpOpMsg = new Group(sashForm, SWT.NONE);
		fd_grpOpMsg = new FormData();
		fd_grpOpMsg.left = new FormAttachment(cmpTabs, 0, SWT.LEFT);
//      fd_grpOpMsg.top = new FormAttachment(sash, 1);
//      sash.setLayout(new FillLayout(SWT.HORIZONTAL));
		fd_grpOpMsg.bottom = new FormAttachment(100, 0);
		grpOpMsg.setLayoutData(fd_grpOpMsg);
		grpOpMsg.setLayout(new FillLayout(SWT.HORIZONTAL));

		stxtOpMsgLog = new StyledText(grpOpMsg, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
//      stxtOpMsgLog.setSize(553, 297);
		stxtOpMsgLog.setFont(SWTResourceManager.getFont("Courier New", 9, SWT.NORMAL));
		OpMsgLog = new OpMsgLogger(disp, stxtOpMsgLog);
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		sLog = new TextLogger("", BASE_PATH + "logs");

	} // end createContents(Display disp)

	void setupFileSelection() {
		OpMsgLog.LogMsg("Choose Track Excel file from above and click 'AcceptFile'\n", OpMsgLogger.LogLevel.PROMPT);
		btnNewFile.setEnabled(true);
		btnBrowse.setEnabled(true);
		btnUseCurrent.setEnabled(true);
	}

	static LocalDate getSelectedDate(Combo cbo) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withLocale(Locale.ENGLISH);
		String dateStr = cbo.getItem(cbo.getSelectionIndex());
		return LocalDate.parse(dateStr, formatter);
	}

	boolean snapshotChartSetupOk() {
//      dollarFmtStr = "##.0";
		dollarFmtStr = "###,###";
		dollarFmt = new DecimalFormat(dollarFmtStr);
		refDate = getSelectedDate(cboSelRefDate);
		if (refDate == null) {
			OpMsgLog.LogMsg("No Reference date is set\n", OpMsgLogger.LogLevel.ERROR);
			return false;
		} else if (ChartSelections.accountsToChart.size() == 0) {
			OpMsgLog.LogMsg("No acccounts are selected\n", OpMsgLogger.LogLevel.ERROR);
			return false;
		} else {
			if (!(chart == null)) {
				chart.dispose();
			}
			chart = new Chart(cmpChart, SWT.NONE);
		}
		snapshotAmts = ExcelIo.GetDataPoint(refDate).GetAmtsArray(ChartSelections.accountsToChart);
		snapshotAcctNames = new String[ChartSelections.accountsToChart.size()];
		for (int a = 0; a < snapshotAcctNames.length; a++) {
			snapshotAcctNames[a] = ChartSelections.accountsToChart.get(a).name;
		}
		ILegend legend = chart.getLegend();
		Font font = new Font(Display.getDefault(), "Courier", 8, SWT.NORMAL);
		legend.setFont(font);
		snapshotLegend = "";
		for (int a = 0; a < snapshotAcctNames.length; a++) {
			snapshotLegend += String.format("%-13s: %-9s\n", snapshotAcctNames[a], dollarFmt.format(snapshotAmts[a]));
		}
		return true;
	} // end setupSnapshotOk

	void historyChart(historyChartType cType) {
		// set up xSeries, an array of java.util.date to define x axis
		if (!(chart == null)) {
			chart.dispose();
		}
		chart = new Chart(cmpChart, SWT.NONE);
		String title = "";
		String yAxisLabel = "?";
		switch (cType) {
		case BALANCES:
			title = "Account Balances";
			yAxisLabel = "Balances";
			break;
		case DELTA_DOLLARS:
			title = "Balance Historys";
			yAxisLabel = "$ Change From Reference on " + refDate.toString();
			break;
		case DELTA_PERCENT:
			title = "Balance Historys";
			yAxisLabel = "% Change From Reference on " + refDate.toString();
			break;
		default:
		}
		chart.getTitle().setText(
				title + ": " + getSelectedDate(cboBeginDateRange) + " thru " + getSelectedDate(cboEndDateRange));
		ISeriesSet sSet = chart.getSeriesSet();
		IAxisSet axisSet = chart.getAxisSet();
		IAxis xAxis = axisSet.getXAxis(0);
		xAxis.getTitle().setText("Date");
		IAxis yAxis = axisSet.getYAxis(0);
		yAxis.getTitle().setText(yAxisLabel);
		ILineSeries<?>[] scatterSeries;
		int selectedDateIndices[] = ExcelIo.GetSelectedImportIndices(getSelectedDate(cboBeginDateRange),
				getSelectedDate(cboEndDateRange));
		// jcc 20230626 - after trying many approaches to labeling
		// x axis with dates, finally used deprecated java.util.Date
		// approach
//      xAxis.enableCategory(true);    // puts labels in wrong place
		int nSelAccts = ChartSelections.accountsToChart.size();
		scatterSeries = new ILineSeries<?>[nSelAccts];
		for (int acct = 0; acct < nSelAccts; acct++) {
			String acctName = ChartSelections.accountsToChart.get(acct).name;
			scatterSeries[acct] = (ILineSeries<?>) sSet.createSeries(SeriesType.LINE, acctName);
			scatterSeries[acct].setSymbolSize(2);
		}
		IAxisTick xTick = axisSet.getXAxis(0).getTick();
		xTick.setFormat(new SimpleDateFormat("MM/dd"));
		// double epoch time is seconds since 1970-01-01T00:00:00Z
		// double []epochTimes = new double[selectedDateIndices.length];
		String[] axisTimeStrs = new String[selectedDateIndices.length];
		java.util.Date[] uDates = new java.util.Date[selectedDateIndices.length];
		double[][] ys = new double[nSelAccts][selectedDateIndices.length];
		for (int se = 0; se < selectedDateIndices.length; se++) {
			DataPoint dp = ExcelIo.importData.get(selectedDateIndices[se]);
			// epochTimes[se] = dp.GetEpochSec();
			// axisTimeStrs[se] = dp.GetAxisLabelStr();
			uDates[se] = dp.GetUtilDate();
			// switch statement on cType here
			double[] amts = new double[nSelAccts];
			amts = dp.GetAmtsArray(ChartSelections.accountsToChart);
			switch (cType) {
			case BALANCES:
				title = "Account Balances";
				yAxisLabel = "Balances";
				for (int acct = 0; acct < nSelAccts; acct++) {
					ys[acct][se] = amts[acct];
				}
				break;
			case DELTA_DOLLARS:
				refDate = getSelectedDate(cboSelRefDate);
				if (refDate == null) {
					OpMsgLog.LogMsg("No Reference date is set\n", OpMsgLogger.LogLevel.ERROR);
				} else if (ChartSelections.accountsToChart.size() == 0) {
					OpMsgLog.LogMsg("No acccounts are selected\n", OpMsgLogger.LogLevel.ERROR);
				} else {
					double[] refAmts = ExcelIo.GetDataPoint(refDate).GetAmtsArray(ChartSelections.accountsToChart);
					title = "Balance Change From Reference";
					yAxisLabel = "$ Change";
					for (int acct = 0; acct < nSelAccts; acct++) {
						ys[acct][se] = amts[acct] - refAmts[acct];
					}
				}
				break;
			case DELTA_PERCENT:
				refDate = getSelectedDate(cboSelRefDate);
				if (refDate == null) {
					OpMsgLog.LogMsg("No Reference date is set\n", OpMsgLogger.LogLevel.ERROR);
				} else if (ChartSelections.accountsToChart.size() == 0) {
					OpMsgLog.LogMsg("No acccounts are selected\n", OpMsgLogger.LogLevel.ERROR);
				} else {
					double[] refAmts = ExcelIo.GetDataPoint(refDate).GetAmtsArray(ChartSelections.accountsToChart);
					title = "% Change From Reference of " + refDate.toString();
					yAxisLabel = "% Change";
					for (int acct = 0; acct < nSelAccts; acct++) {
						if (refAmts[acct] == 0) {
							ys[acct][se] = 0;
						} else {
							ys[acct][se] = 100 * (amts[acct] - refAmts[acct]) / refAmts[acct];
						}
					}
				}
				break;
			default:
			}
		}
		// xAxis.enableCategory(true);
		for (int acct = 0; acct < nSelAccts; acct++) {
			xAxis.setCategorySeries(axisTimeStrs);
			// scatterSeries[acct].setXSeries(epochTimes);
			scatterSeries[acct].setXDateSeries(uDates);
			scatterSeries[acct].setYSeries(ys[acct]);
		}
		axisSet.adjustRange();
		mouseListener = new ChartMouseListener(chart);
	}

	private static void readEmpowerInpFile() {
		impFromHtml = null;
		final String IMPORT_FOLDER = BASE_PATH + "data";
		OpMsgLog.LogMsg("Reading current import file from folder " + IMPORT_FOLDER + "\n", OpMsgLogger.LogLevel.INFO,
				true);
		while (impFromHtml == null) {
			try {
				impFromHtml = new ImportFromHtml(BASE_PATH + "data", ".mhtml", OpMsgLog);
				txtRecentNetWorth.setText(Utils.AmountStr(impFromHtml.netWorth));
				txtRecentTotInv.setText(Utils.AmountStr(impFromHtml.GetGroupTotal("Investment")));
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
				String impDateStr = formatter.format(impFromHtml.importDateTime);
				txtRecentDate.setText(impDateStr);
				txtHtmlFileInfo.setText(impFromHtml.htmlFileInfo);
			} catch (Exception impe) {
				OpMsgLog.LogMsg(Utils.ExceptionString(impe) + "\nException reading import html file\n",
						OpMsgLogger.LogLevel.ERROR);
			}
			if (impFromHtml == null) {
				MessageBox mb = new MessageBox(Tracker.shlMain, SWT.YES | SWT.NO);
				mb.setText("No Import data file found");
				mb.setMessage("Click Yes to retry or No to continue with existing track data");
				if (mb.open() != SWT.YES) {
					break;
				}
			}
		}
	}

	private static void reportTrkFile() {
		try {
			txtRecentTrkNetWorth.setText(Utils.AmountStr(ExcelIo.GetMostRecentVal("NetWorth")));
			txtRecentTotInvTrk.setText(Utils.AmountStr(ExcelIo.GetMostRecentVal("Investment")));
			txtRecentTrkDate.setText(ExcelIo.GetMostRecentDate());
		} catch (Exception impe) {
			OpMsgLog.LogMsg(Utils.ExceptionString(impe) + "\nException reading track file\n",
					OpMsgLogger.LogLevel.ERROR);
		}
	}

	private static void updateTrackFile() {
		try {
			ExcelIo.AddRow();
			reportTrkFile();
		} catch (Exception are) {
			OpMsgLog.LogMsg(Utils.ExceptionString(are) + "\nException adding data to track file",
					OpMsgLogger.LogLevel.ERROR);
		}
	}

	private static void selectTab(String tabName) {
		if (tabName.equals(TAB_IMPORTED_DATA)) {
			sashForm.setWeights(new int[] { 30, 50 });
		} else if (tabName.equals(TAB_SETUP_DISPLAY)) {
			sashForm.setWeights(new int[] { 40, 30 });
//         sashForm.setWeights(new int[] {70, 20});
		} else if (tabName.equals(TAB_DATA_DISPLAY)) {
			sashForm.setWeights(new int[] { 60, 10 });
		}
		if (!tabName.equals(prevTabItemName)) {
			sashForm.layout();
			prevTabItemName = tabName;
		}
	}

//   private void addAcctSelection(String aName) {
//      for (int c = 0; c < ckbAcctsSel.length; c++) {
//         if (aName.equals(ExcelIo.xlHeaders[c].name)) {
//            ckbAcctsSel[c].setSelection(true);                     
//            if (ExcelIo.xlHeaders[c].groupHdr) {
//               ckbAcctsSel[c].setFont(BOLD_HDR_FONT);                     
//            }
//         }
//         btnSaveList.setEnabled(true);
//      }
//   }
	private static void setupAcctSelections() {
		GridData gd_acctList = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
		gd_acctList.heightHint = 18;
		cmpAcctList.setLayoutData(gd_acctList);

		acctSelectors = new AcctSelection[ExcelIo.xlHeaders.length];
		for (int acct = 0; acct < acctSelectors.length; acct++) {
			acctSelectors[acct] = new AcctSelection(cmpAcctList, SWT.NONE, ExcelIo.xlHeaders[acct].groupHdr,
					ExcelIo.xlHeaders[acct].name, acct);
		}
		setDateSelections();

	}

	static void setRefDate() {
		refDate = getSelectedDate(cboSelRefDate);
		// update account list table
		double[] refAmts = ExcelIo.GetDataPoint(refDate).GetAmtsArray(null);
		for (int acct = 0; acct < acctSelectors.length; acct++) {
			acctSelectors[acct].SetAmt(refAmts[acct]);
		}
		lblDateAcctList.setText(refDate.toString() + " Acct Totals");
	}

	static public void EnableSaveAcctList() {
		btnSaveList.setEnabled(true);
	}
//   private static void setupDisplaySelections() {
//      ckbAcctsSel = new Button[ExcelIo.xlHeaders.length];
//      for (int c = 0; c < ckbAcctsSel.length; c++) {
//         final int cIndex = c;
//         ckbAcctsSel[c] = new Button(cmpAcctList, SWT.CHECK);
//         ckbAcctsSel[c].setText(ExcelIo.xlHeaders[c].name);
//         if (ExcelIo.xlHeaders[c].groupHdr) {
//            ckbAcctsSel[c].setFont(BOLD_HDR_FONT);                     
//         }
//         ckbAcctsSel[c].addSelectionListener(new SelectionAdapter() {
//            @Override
//            public void widgetSelected(SelectionEvent e) {
//               int nsel = ChartSelections.SelectToChart(cIndex,
//                           ckbAcctsSel[cIndex].getSelection());
//               btnSaveList.setEnabled(nsel > 0);
//            }  
//         });
//      }
//      setDateSelections();
//   }

	private static void setDateSelections() {
		String importDates[] = ExcelIo.GetImportDates();
		cboSelRefDate.setItems(importDates);
		cboSelRefDate.select(importDates.length - 1);
		setRefDate();
		cboBeginDateRange.setItems(importDates);
		if (Settings.DateRangeStartStr.equals("")) {
			cboBeginDateRange.select(0);
		} else {
			for (int d = 0; d < importDates.length; d++) {
				if (Settings.DateRangeStartStr.equals(importDates[d].toString())) {
					cboBeginDateRange.select(d);
					break;
				}
			}
		}
		cboEndDateRange.setItems(importDates);
		cboEndDateRange.select(importDates.length - 1);
		btnRefreshTrk.setEnabled(true);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
