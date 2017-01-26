package android.jirix.cz.wifiscanner.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;

/**
 * Created by JxD on 22. 1. 2017.
 */

public class TextInputDialogFragment extends DialogFragment{

   private static final String DATA_TITLE = "title";
   private static final String DATA_MSG = "message";
   private static final String DATA_BTN_POSITIVE = "btn_positive";
   private static final String DATA_BTN_NEGATIVE = "btn_negative";
   private static final String DATA_LISTENER = "listener";

   private String mTitle;
   private String mHint;
   private String mBtnPos;
   private String mBtnNeg;

   private EditText mEditText;

   private DialogCallbacks mListener;

   public interface DialogCallbacks{
      void onNegativeButtonPressed();
      void onPositiveButtonPressed(String input);
   }



   public static TextInputDialogFragment create(String title, String hint, String btnNeg, String btnPos){
      TextInputDialogFragment dialog = new TextInputDialogFragment();

      Bundle B = new Bundle();
      B.putString(DATA_TITLE,title);
      B.putString(DATA_MSG,hint);
      B.putString(DATA_BTN_POSITIVE,btnPos);
      B.putString(DATA_BTN_NEGATIVE,btnNeg);
      //B.putSerializable(DATA_LISTENER,listener);

      dialog.setArguments(B);

      return dialog;
   }

   public void setListener(DialogCallbacks listener){
      mListener = listener;
   }

   @Override
   public void onCreate(Bundle savedInstanceState){
      super.onCreate(savedInstanceState);
      Bundle B = getArguments();
      if(B != null){
         mTitle = B.getString(DATA_TITLE);
         mHint = B.getString(DATA_MSG);
         mBtnPos = B.getString(DATA_BTN_POSITIVE);
         mBtnNeg = B.getString(DATA_BTN_NEGATIVE);
      }
   }




   @Override
   public Dialog onCreateDialog(Bundle savedInstanceState){

      final EditText input = new EditText(getActivity());
      input.setHint(mHint);

      mEditText = input;

      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      builder.setTitle(mTitle);
      builder.setView(input);

      builder.setNegativeButton(mBtnNeg, new DialogInterface.OnClickListener(){
         @Override
         public void onClick(DialogInterface dialog, int which){
            if(mListener != null) mListener.onNegativeButtonPressed();
         }
      });
      builder.setPositiveButton(mBtnPos, new DialogInterface.OnClickListener(){
         @Override
         public void onClick(DialogInterface dialog, int which){
            if(mListener != null) mListener.onPositiveButtonPressed(mEditText.getText().toString());
         }
      });

      return builder.create();
   }



   public void show(FragmentManager fragmentManager, String string) {
      this.setShowsDialog(true);
      FragmentTransaction ft = fragmentManager.beginTransaction();
      ft.add(this, string);
      ft.commit();
   }


}
