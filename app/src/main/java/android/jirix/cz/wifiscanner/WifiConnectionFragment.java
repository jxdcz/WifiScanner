package android.jirix.cz.wifiscanner;


import android.jirix.cz.wifiscanner.dialogs.ConfirmDialogFragment;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;


public class WifiConnectionFragment extends Fragment{

   JxWifiManager mWifiManager;

   private View mRootView;
   private View mContentView;

   private NetworkInfoViewHolder mHolder;
   private TextView mTextIp;
   private TextView mTextMac;

   private boolean mConnected;

   private WifiActionsListener mListener;
   private ConfirmDialogFragment.DialogCallbacks mDialogListener = new DisconnectDialogCallbacks();


   public WifiConnectionFragment(){
      // Required empty public constructor
   }

public static WifiConnectionFragment create(){
      WifiConnectionFragment fragment = new WifiConnectionFragment();
      Bundle args = new Bundle();

      fragment.setArguments(args);
      return fragment;
   }

   @Override
   public void onCreate(Bundle savedInstanceState){
      super.onCreate(savedInstanceState);

      mListener = new WifiActionsListener();
      mWifiManager = JxWifiManager.getInstance(getActivity());

   }

   private void setVisibility(){
      mRootView.setVisibility(mWifiManager.isWifiEnabled() ? View.VISIBLE : View.GONE);
   }

   @Override
   public void onStart(){
      super.onStart();
      mWifiManager.start();
   }

   @Override
   public void onResume(){
      super.onResume();
      mWifiManager.addListener(mListener);
      setVisibility();
   }

   @Override
   public void onPause(){
      super.onPause();
      mWifiManager.removeListener(mListener);
   }

   @Override
   public void onStop(){
      super.onStop();
      mWifiManager.stop();
   }


   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
      mRootView = inflater.inflate(R.layout.fragment_wifi_connection,container,false);
      mContentView = mRootView.findViewById(R.id.layout_wifi_connection_content);
      mContentView.setOnClickListener(new View.OnClickListener(){
         @Override
         public void onClick(View v){
            ConfirmDialogFragment dialog = ConfirmDialogFragment.create(
                    getString(R.string.label_title_disconnecting),
                    getString(R.string.label_message_areyousure),
                    getString(R.string.misc_cancel),
                    getString(R.string.misc_disconnect));
            dialog.setListener(mDialogListener);
            dialog.show(getFragmentManager(),"dialog");
         }
      });

      mHolder = new NetworkInfoViewHolder();
      mHolder.setViews(
              (TextView)mRootView.findViewById(R.id.text_wifi_ssid),
              (TextView)mRootView.findViewById(R.id.text_wifi_bssid),
              (TextView)mRootView.findViewById(R.id.text_wifi_capabilities),
              (TextView)mRootView.findViewById(R.id.text_wifi_ch_width),
              (TextView)mRootView.findViewById(R.id.text_wifi_freq),
              (TextView)mRootView.findViewById(R.id.text_wifi_rssi)
      );
      mTextIp = (TextView) mRootView.findViewById(R.id.text_wifi_ip);
      mTextMac = (TextView) mRootView.findViewById(R.id.text_wifi_mac_addr);


      WifiInfo connectionInfo = mWifiManager.getConnectionInfo();
      onNetworkConnectionChanged(connectionInfo,(connectionInfo.getBSSID() == null || connectionInfo.getBSSID().isEmpty()) ? false : true);


      return mRootView;
   }

   public void onNetworkConnectionChanged(WifiInfo info, boolean connected){
      setVisible(connected);
      setData(info);
   }

   public void setData(WifiInfo info){
      String freq = "";
      if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
         freq = info.getFrequency()+"";
      }
      mHolder.updateData(info.getSSID(),info.getBSSID(),"","",freq,info.getRssi()+"");
      String IP = getString(R.string.misc_unknown);
      try{
         IP = InetAddress.getByAddress(BigInteger.valueOf(info.getIpAddress()).toByteArray()).getHostAddress();
      }catch(UnknownHostException e){

      }
      mTextIp.setText(IP);
      mTextMac.setText(info.getMacAddress());
   }

   public void setVisible(boolean visible){
      mConnected = visible;
      mContentView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
   }

   private void wifiStateChanged(int state){
      switch(state){
         //case JxWifiManager.STATE_ENABLED:
         //   mContentView.setVisibility(View.VISIBLE);
         //   break;
         case JxWifiManager.STATE_DISABLED:
            mContentView.setVisibility(View.INVISIBLE);
            break;
      }
   }

   private class WifiActionsListener implements JxWifiManager.WifiManagerCallbacks{

      @Override
      public void onScanResultsReceived(List<ScanResult> list){
         // other fragments job
      }

      @Override
      public void onWifiStateChanged(int state){
         wifiStateChanged(state);
      }

      @Override
      public void onConnectionStateChanged(WifiInfo info, boolean connected, int status){
         onNetworkConnectionChanged(info, connected);
      }
   }

   private class DisconnectDialogCallbacks implements ConfirmDialogFragment.DialogCallbacks{

      @Override
      public void onNegativeButtonPressed(){
         //nothing
      }

      @Override
      public void onPositiveButtonPressed(){
         //disconnect at ONCE
         mWifiManager.disconnect();
      }
   }

}
