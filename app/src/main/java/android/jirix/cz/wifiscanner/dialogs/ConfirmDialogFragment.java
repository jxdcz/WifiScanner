package android.jirix.cz.wifiscanner.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by JxD on 24. 1. 2017.
 */

public class ConfirmDialogFragment extends CustomDialogFragment{

   private static final String DATA_TITLE = "title";
   private static final String DATA_MSG = "message";
   private static final String DATA_BTN_POSITIVE = "btn_positive";
   private static final String DATA_BTN_NEGATIVE = "btn_negative";
   private static final String DATA_LISTENER = "listener";

   private String mTitle;
   private String mMessage;
   private String mBtnPos;
   private String mBtnNeg;

   private DialogCallbacks mListener;

   public interface DialogCallbacks{
      void onNegativeButtonPressed();
      void onPositiveButtonPressed();
   }



   public static ConfirmDialogFragment create(String title, String message, String btnNeg, String btnPos){
      ConfirmDialogFragment dialog = new ConfirmDialogFragment();

      Bundle B = new Bundle();
      B.putString(DATA_TITLE,title);
      B.putString(DATA_MSG,message);
      B.putString(DATA_BTN_POSITIVE,btnPos);
      B.putString(DATA_BTN_NEGATIVE,btnNeg);

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
         mMessage = B.getString(DATA_MSG);
         mBtnPos = B.getString(DATA_BTN_POSITIVE);
         mBtnNeg = B.getString(DATA_BTN_NEGATIVE);
      }
   }




   @Override
   public Dialog onCreateDialog(Bundle savedInstanceState){

      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      builder.setTitle(mTitle);
      builder.setMessage(mMessage);

      builder.setNegativeButton(mBtnNeg, new DialogInterface.OnClickListener(){
         @Override
         public void onClick(DialogInterface dialog, int which){
            if(mListener != null) mListener.onNegativeButtonPressed();
         }
      });
      builder.setPositiveButton(mBtnPos, new DialogInterface.OnClickListener(){
         @Override
         public void onClick(DialogInterface dialog, int which){
            if(mListener != null) mListener.onPositiveButtonPressed();
         }
      });

      return builder.create();
   }


}
