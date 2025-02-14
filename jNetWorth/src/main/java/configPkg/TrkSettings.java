package configPkg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import mainPkg.Tracker;
import utilsPkg.OpMsgLogger;
import utilsPkg.Utils;

public class TrkSettings {
   public static String DefaultRuntimeFolder;
   public static String DATA_FOLDER = Tracker.BASE_PATH + "data\\";
   static String settingsFileName;
   public String []accountsSelected = null;
   //  the following are populated by jsontoGson
//   public String TrackExcelFileName = DATA_FOLDER + "jNetWorth.x";
   public String TrackExcelFileName = DATA_FOLDER + "none";
//   public String RefDateStr = "";
   public String DateRangeStartStr = "";
//   public String DateRangeEndStr = "";
//   public float StopWatchSec = 0;
   public JsonObject AcctsSelGson = null;
   //^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
   
   private static OpMsgLogger st;
   private static TrkSettings instance;
   
   public TrkSettings() throws Exception {
//      String os = System.getProperty("os.name");
//      DefaultRuntimeFolder = os.toLowerCase().contains("win")?
//              "c:\\InvTrack\\bin" : "/jtrack/runtime/bin";
      DefaultRuntimeFolder = "c:\\jNetWorth\\bin\\";
   }
   
   public TrkSettings GetInstance(OpMsgLogger s)
   {
      st = s;
      try {
         readJsonObjectFromFile();
         if (instance.AcctsSelGson != null) {
            JsonArray accts = instance.AcctsSelGson.getAsJsonArray("selected_accounts");
            instance.accountsSelected = new String[accts.size()];
            for (int a = 0; a < instance.accountsSelected.length; a++) {
               instance.accountsSelected[a] = accts.get(a).toString();
            }
         }
      } catch (Exception re) {
         s.LogMsg("Error reading settings, using program defaults\n",
               OpMsgLogger.LogLevel.WARN);
      }
      return instance;
   }
   
   // init to selection status from last session
   public boolean AccountWasSelected(String name) {
      if (instance.accountsSelected != null) {
         String cmp = "\"" + name + "\"";
         for (int a = 0; a < instance.accountsSelected.length; a++) {
            if (cmp.equals(instance.accountsSelected[a])) {
               return true;
            }
         }
      }
      return false;
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
   
//   // remember which accounts were selected & make default next time
//   public void UpdateAcctsSel() {
//   }
   
   
   public void WriteJsonObject() {
      String jSelStr = "";
      int nSel = 0;
      for (int a = 0; a < Tracker.acctSelectors.length; a++) {
         if (Tracker.acctSelectors[a].IsSelected()) {
            if (++nSel == 1) {
               jSelStr = "{\"selected_accounts\" : [ ";
            }
            jSelStr += "\"" + Tracker.acctSelectors[a].name + "\" ,";
         }
      }
      if (nSel > 0) {
         int lastCommaPos = jSelStr.lastIndexOf(",");
         jSelStr = jSelStr.substring(0, lastCommaPos-1) + "]}";
         Object obj = JsonParser.parseString(jSelStr);
         AcctsSelGson = (JsonObject)obj;
      } else {
         AcctsSelGson = null;
      }
      int bpoint=1;
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
