/* 
 *  find all files matching filter
 */
package importPkg;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

public class FindFiles {

   static public void ListFiles() throws Exception {
      File dir = new File("c:\\jNetWorth\\Data\\");
//      Files.list(new File("c:\\jNetWorth\\Data\\").toPath())
//            .limit(10)
//            .forEach(path -> {
//               System.out.println(path);
//            });
      File[] files = dir.listFiles(new FilenameFilter() {
         @Override
         public boolean accept(File dir, String name) {
            return name.endsWith(".html");
         }
      });
      Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
//      int bpoint=1;
   }
}
