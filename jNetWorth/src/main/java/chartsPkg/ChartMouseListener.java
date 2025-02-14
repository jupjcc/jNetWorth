package chartsPkg;

import java.time.Instant;
//import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swtchart.Chart;
import org.eclipse.swtchart.IAxis;
import org.eclipse.swtchart.ISeries;

import utilsPkg.Utils;

public class ChartMouseListener {
   
   protected static boolean highlight;
   /* Used to remember the location of the highlight point */   
   private static int highlightX;
   private static int highlightY;
   
   public ChartMouseListener(final Chart chart) {
      // Get System Time Zone Offset
      ZoneOffset zoneOffset = ZoneId.systemDefault().getRules().getOffset(Instant.now());
      final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd");
      
      /* Get the plot area and add the mouse listeners */
      final Composite plotArea = (Composite)chart.getPlotArea();
      
      plotArea.addListener(SWT.MouseHover, new Listener() {
         @Override
         public void handleEvent(Event event) {
            IAxis xAxis = chart.getAxisSet().getXAxis(0);
            IAxis yAxis = chart.getAxisSet().getYAxis(0);
            double x = xAxis.getDataCoordinate(event.x);
            double y = yAxis.getDataCoordinate(event.y);
            ISeries[] series = chart.getSeriesSet().getSeries();
            double closestX = 0;
            double closestY = 0;
            double minDist = Double.MAX_VALUE;
            String yId = "";
           /* over all series */
           for (ISeries serie : series) {
              double[] xS = serie.getXSeries();
              double[] yS = serie.getYSeries();
              /* check all data points */
              for (int i = 0; i < xS.length; i++) {
                 /* compute distance to mouse position */
                 double newDist = Math.sqrt(Math.pow((x - xS[i]), 2)
                           + Math.pow((y - yS[i]), 2));
   
                 /* if closer to mouse, remember */
                 if (newDist < minDist) {
                    minDist = newDist;
                    closestX = xS[i];
                    closestY = yS[i];
                    yId = serie.getId();
                 }
              }
           }
           long epochSec = (long)closestX / 1000;
           int nanosOfSec = 0;
           // Convert Epoch Seconds to LocalDate object
           LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(epochSec, nanosOfSec, zoneOffset);
//           LocalDate localDate = localDateTime.toLocalDate();
           String dateStr = localDateTime.toLocalDate().format(dateFormatter);
           /* set tooltip of closest data point */
           String dollars = Utils.AmountStr(closestY);
           String toolTip = dateStr + ": " + yId + " " + dollars;
           plotArea.setToolTipText(toolTip);
           /* remember closest data point */
           highlightX = xAxis.getPixelCoordinate(closestX);
           highlightY = yAxis.getPixelCoordinate(closestY);   
           highlight = true;
           /* trigger repaint (paint highlight) */
           plotArea.redraw();
         }     // end handleEvent
      });     // end plotArea.addListener
      plotArea.addListener(SWT.MouseMove, new Listener() {   
         @Override
         public void handleEvent(Event arg0) {
            highlight = false;
            plotArea.redraw();
         }
      });
      plotArea.addListener(SWT.Paint, new Listener() {
         @Override
         public void handleEvent(Event event) {
            if (highlight) {
               GC gc = event.gc;
               gc.setBackground(Display.getDefault().getSystemColor(
                       SWT.COLOR_RED));
               gc.setAlpha(128);   
               gc.fillOval(highlightX - 5, highlightY - 5, 10, 10);
           }
         }    // end handleEvent
      });
   }

}