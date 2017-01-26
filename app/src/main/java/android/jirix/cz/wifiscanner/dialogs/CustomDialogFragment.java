package android.jirix.cz.wifiscanner.dialogs;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

/**
 * Created by JxD on 24. 1. 2017.
 */

public class CustomDialogFragment extends DialogFragment{


   public void show(FragmentManager fragmentManager, String string) {
      this.setShowsDialog(true);
      FragmentTransaction ft = fragmentManager.beginTransaction();
      ft.add(this, string);
      ft.commit();
   }

}
