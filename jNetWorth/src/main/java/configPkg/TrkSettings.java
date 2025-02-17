package configPkg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;

import mainPkg.Tracker;
import utilsPkg.OpMsgLogger;
import utilsPkg.Utils;

public class TrkSettings {
   public static String DefaultRuntimeFolder = "c:\\jNetWorth\\bin\\";
   public static String DATA_FOLDER = Tracker.BASE_PATH + "data\\";
   static String settingsFileName;
   public String []savedAccountsSelected = null;
   public String TrackExcelFileName = DATA_FOLDER + "none";
   public String acctsListFileName = Tracker.CFG_PATH + "volatileInvestments.accts";
   public String DateRangeStartStr = "";
   //^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
   
   private static OpMsgLogger st;
   private static TrkSettings instance;
   
   public TrkSettings() throws Exception {
//      String os = System.getProperty("os.name");
//      DefaultRuntimeFolder = os.toLowerCase().contains("win")?
//              "c:\\InvTrack\\bin" : "/jtrack/runtime/bin";
   }
   
   // init to selection status from last session
   public boolean SavedAccountSelection(String name) {
      if (instance.savedAccountsSelected != null) {
         int nSaved = instance.savedAccountsSelected.length;
         if (nSaved > 0) {
            for (int sel = 0; sel < nSaved; sel++) {
               String sName = instance.savedAccountsSelected[sel]; 
               if (name.equals(sName)) {
                  return true;
               }
            }
         }
      }
      return false;
   }
   
   public TrkSettings GetInstance(OpMsgLogger s)
   {
      st = s;
      try {
         readJsonObjectFromFile();
      } catch (Exception re) {
         s.LogMsg("Error reading settings, using program defaults\n",
               OpMsgLogger.LogLevel.WARN);
      }
      return instance;
   }
   
   public void readJsonObjectFromFile() throws Exception {
      Gson gson = new Gson();
      InputStreamReader isReader;
      instance = this;
      settingsFileName = DefaultRuntimeFolder +  "Settings.json";
//      st.LogMsg("Reading program settings from " + settingsFileName + "\n");
      isReader = new InputStreamReader(new FileInputStream(settingsFileName));
      try {
         JsonReader jr = new JsonReader(isReader);
         try
         {
            instance = gson.fromJson(isReader, TrkSettings.class);
         }
         catch (JsonParseException je)
         {
            st.LogMsg(Utils.ExceptionString(je) + 
                     "\nException parsing Settings file:" +
                     "\n" + settingsFileName + "\nUsing defaults\n", OpMsgLogger.LogLevel.WARN);
            instance = this;
         }
         isReader.close();
         jr.close();
         if (instance == null)
         {
            st.LogMsg("\nError reading Settings file:" +
                     "\n" + settingsFileName + "\nUsing defaults\n", OpMsgLogger.LogLevel.WARN);
            instance = this;
         }
      }
      catch (Exception je)
      {
         st.LogMsg(Utils.ExceptionString(je) + 
                "\nException reading Settings file:" +
                "\n" + settingsFileName + "\nUsing defaults\n", OpMsgLogger.LogLevel.WARN);
      }
   }
   
   public void WriteJsonObject() {
      String jSelStr = "";
      int nSel = 0;
      ArrayList<String> acctsList = new ArrayList<String>();
      for (int a = 0; a < Tracker.acctSelectors.length; a++) {
         if (Tracker.acctSelectors[a].IsSelected()) {
            if (++nSel == 1) {
               jSelStr = "{\"selected_accounts\" : [ ";
            }
            acctsList.add(Tracker.acctSelectors[a].name);
            jSelStr += "\"" + Tracker.acctSelectors[a].name + "\" ,";
         }
      }
      instance.savedAccountsSelected = new String[nSel];
      if (nSel > 0) {
         for (int s = 0; s < nSel; s++) {
            instance.savedAccountsSelected[s] = acctsList.get(s);
         }
      }
      Gson gson = new Gson();
      try
      {
         File f = new File(settingsFileName);
         f.createNewFile();
         FileWriter fw = new FileWriter(f);
         gson.toJson(this, fw);
         fw.close();
      }
      catch (Exception je)
      {
         st.LogMsg(Utils.ExceptionString(je) + 
                  "\nException saving to settings file:" +
                  "\n" + settingsFileName + "\n", OpMsgLogger.LogLevel.WARN);

      }
   }
   
}
