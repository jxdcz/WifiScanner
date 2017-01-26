package android.jirix.cz.wifiscanner.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.jirix.cz.wifiscanner.JxWifiManager;
import android.jirix.cz.wifiscanner.R;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;

/**
 * Created by JxD on 26. 1. 2017.
 */

public class NetworkConnectDialogFragment extends CustomDialogFragment{

   private static final String DATA_TYPE = "type";
   private static final String DATA_TITLE = "title";
   private static final String DATA_MSG = "message";
   private static final String DATA_BTN_POSITIVE = "btn_positive";
   private static final String DATA_BTN_NEGATIVE = "btn_negative";
   private static final String DATA_LISTENER = "listener";

   public static final String INPUT_IDENTITY = "identity";
   public static final String INPUT_PASSKEY = "password";

   private int mType;

   private String mTitle;
   private String mHint;
   private String mBtnPos;
   private String mBtnNeg;

   private View mRootView;

   private TextView mTextSecurityType;
   private EditText mEditIdentity;
   private EditText mEditPassword;

   private DialogCallbacks mListener;

   public interface DialogCallbacks{
      void onNegativeButtonPressed();
      void onPositiveButtonPressed(HashMap<String, String> input);
   }



   public static NetworkConnectDialogFragment create(int type, String title, String hint, String btnNeg, String btnPos){
      NetworkConnectDialogFragment dialog = new NetworkConnectDialogFragment();

      Bundle B = new Bundle();
      B.putInt(DATA_TYPE,type);
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
         mType = B.getInt(DATA_TYPE);
         mTitle = B.getString(DATA_TITLE);
         mHint = B.getString(DATA_MSG);
         mBtnPos = B.getString(DATA_BTN_POSITIVE);
         mBtnNeg = B.getString(DATA_BTN_NEGATIVE);
      }
   }

   private void onPositiveClicked(){
      HashMap<String, String> results = new HashMap<>();
      results.put(INPUT_IDENTITY,mEditIdentity.getText().toString());
      results.put(INPUT_PASSKEY,mEditPassword.getText().toString());

      mListener.onPositiveButtonPressed(results);
   }

   @Override
   public Dialog onCreateDialog(Bundle savedInstanceState){

      mRootView = getActivity().getLayoutInflater().inflate(R.layout.dialog_wifi_connect,null,false);
      mTextSecurityType = (TextView) mRootView.findViewById(R.id.text_wifi_security);
      mEditIdentity = (EditText) mRootView.findViewById(R.id.edit_name);
      mEditPassword = (EditText) mRootView.findViewById(R.id.edit_password);

      String text = "";
      switch(mType){
         case JxWifiManager.NETSEC_EAP: text = getString(R.string.label_wifi_type_eap);break;
         case JxWifiManager.NETSEC_PSK: text = getString(R.string.label_wifi_type_psk);break;
         case JxWifiManager.NETSEC_WEP: text = getString(R.string.label_wifi_type_wep);break;
         case JxWifiManager.NETSEC_OPEN: text = getString(R.string.label_wifi_type_open);break;
      }
      mTextSecurityType.setText(text);

      mEditIdentity.setVisibility(mType == JxWifiManager.NETSEC_EAP ? View.VISIBLE : View.GONE);
      mEditPassword.setVisibility(mType == JxWifiManager.NETSEC_OPEN ? View.GONE : View.VISIBLE);

      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      builder.setTitle(mTitle);
      builder.setView(mRootView);

      builder.setNegativeButton(mBtnNeg, new DialogInterface.OnClickListener(){
         @Override
         public void onClick(DialogInterface dialog, int which){
            if(mListener != null) mListener.onNegativeButtonPressed();
         }
      });
      builder.setPositiveButton(mBtnPos, new DialogInterface.OnClickListener(){
         @Override
         public void onClick(DialogInterface dialog, int which){
            if(mListener != null) onPositiveClicked();
         }
      });

      return builder.create();
   }


}
