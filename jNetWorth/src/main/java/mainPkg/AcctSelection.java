package mainPkg;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import chartsPkg.ChartSelections;
import utilsPkg.Utils;

public class AcctSelection extends Composite {

   public String name;
   public double amt = 0;
   public int amtIndex;
   
   AcctSelection thisInstance;
   String amtStr = "";
   Button btnSelected;
   boolean selected = false;
   private Text txtAmount;
   final static Font BOLD_HDR_FONT = SWTResourceManager.getFont("Courier New", 10, SWT.BOLD);
   
   /**
    * Create the composite.
    * @param parent
    * @param style
    */
   public AcctSelection(Composite parent, int style,
                        boolean isGroupHdr, String acctName,
                        int acctIndex) {
      super(parent, style);
      thisInstance = this;
      GridLayout gridLayout = new GridLayout(2, true);
      gridLayout.verticalSpacing = 0;
      gridLayout.marginHeight = 0;
      setLayout(gridLayout);
      name = acctName;
      amtIndex = acctIndex;
      selected = Tracker.Settings.SavedAccountSelection(name);
      if (selected) {
         ChartSelections.SelectToChart(thisInstance, true);
      }
      btnSelected = new Button(this, SWT.CHECK);
      GridData gd_btnSelected = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
      gd_btnSelected.heightHint = 14;
      gd_btnSelected.widthHint = 120;
      btnSelected.setLayoutData(gd_btnSelected);
      btnSelected.setText(name);
      if (isGroupHdr) {
         btnSelected.setFont(BOLD_HDR_FONT);
      }
      btnSelected.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            selected = btnSelected.getSelection();
            ChartSelections.SelectToChart(thisInstance, selected);
            if (selected) {
               Tracker.EnableSaveAcctList();
            }
//          ckbAcctsSel[cIndex].getSelection());
//btnSaveList.setEnabled(nsel > 0);
         }  
      });     
      btnSelected.setSelection(selected);
      txtAmount = new Text(this, SWT.READ_ONLY | SWT.RIGHT);
      GridData gd_txtAmount = new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1);
      gd_txtAmount.heightHint = 12;
      gd_txtAmount.widthHint = 60;
      txtAmount.setLayoutData(gd_txtAmount);
      txtAmount.setEditable(false);
      txtAmount.setText(amtStr);     
   }
   
   public boolean IsSelected() {
      return btnSelected.getSelection();
   }
   public void SetSelection(boolean sel) {
      btnSelected.setSelection(sel);
   }
   public void SetAmt(double amt64) {
      amt = amt64;
      txtAmount.setText(Utils.AmountStr(amt));
   }

   @Override
   protected void checkSubclass() {
      // Disable the check that prevents subclassing of SWT components
   }

}
