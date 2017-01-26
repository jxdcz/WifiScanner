package android.jirix.cz.wifiscanner;

import android.app.Fragment;
import android.content.Context;
import android.jirix.cz.wifiscanner.dialogs.NetworkConnectDialogFragment;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;

public class WifiResultsFragment extends Fragment implements NetworkConnectDialogFragment.DialogCallbacks, ScanResultsAdapter.OnItemClickedListener{

   private JxWifiManager mWifiManager;

   private static final int REQ_CODE = 55354;

   private View mRootView;
   private ScanResultsAdapter mAdapter;
   private ListView mListView;

   private String mSelectedNetwork;

   private WifiResultsFragmentCallbacks mCallback;

   private Snackbar mSnackbar;

   private WifiActionsListener mWifiListener = new WifiActionsListener();

   public WifiResultsFragment(){
   }

   @Override
   public void onNegativeButtonPressed(){

   }

   @Override
   public void onPositiveButtonPressed(HashMap<String, String> input){
      String passkey = input.get(NetworkConnectDialogFragment.INPUT_PASSKEY);
      Snackbar.make(mRootView,R.string.label_title_connecting,Snackbar.LENGTH_SHORT).show();
      mWifiManager.connect(mSelectedNetwork,passkey);
   }

   @Override
   public void onConnectClicked(String ssid){
      mSelectedNetwork = ssid;
      NetworkConnectDialogFragment dialog = NetworkConnectDialogFragment.create(
              mWifiManager.getNetworkSecurity(ssid),
              getString(R.string.label_wifi_connectingto) + " " + ssid,
              getString(R.string.msic_password),
              getString(R.string.misc_cancel),
              getString(R.string.misc_connect));
      dialog.setListener(this);
      dialog.show(getFragmentManager(),"dialog");
   }


   public interface WifiResultsFragmentCallbacks{
      void onWifiEnabled(boolean enabled);
      void onWifiConnect(String ssid, String key);
   }

   @Override
   public void onCreate(Bundle savedInstanceState){
      super.onCreate(savedInstanceState);

      mWifiManager = JxWifiManager.getInstance(getActivity());

   }

   @Override
   public void onStart(){
      super.onStart();
      mWifiManager.start();
   }

   @Override
   public void onResume(){
      super.onResume();
      mWifiManager.addListener(mWifiListener);
   }

   @Override
   public void onPause(){
      super.onPause();
      mWifiManager.removeListener(mWifiListener);
   }

   @Override
   public void onStop(){
      super.onStop();
      mWifiManager.stop();
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
      mRootView = inflater.inflate(R.layout.fragment_wifi_results, container, false);



      ListView listView = (ListView) mRootView.findViewById(R.id.list_wifis);
      mAdapter = new ScanResultsAdapter(getActivity());
      mAdapter.setListener(this);

      //final TextView emptyText = new TextView(getActivity());
      //emptyText.setText("Wifi disabled or no results found");
      //listView.setEmptyView(emptyText);

      listView.setAdapter(mAdapter);



      return mRootView;
   }

   @Override
   public void onAttach(Context context){
      super.onAttach(context);
      if(context instanceof WifiResultsFragmentCallbacks)
         mCallback = (WifiResultsFragmentCallbacks) context;
      else{
         // support it without callbacks
      }
   }

   @Override
   public void onDetach(){
      super.onDetach();
      mCallback = null;
   }


   public void notifyScanButtonPressed(){
      if(!mWifiManager.isWifiEnabled()){
         Toast.makeText(getActivity(),getString(R.string.label_message_plsenablewifi),Toast.LENGTH_SHORT).show();
         return;
      }
      if(handlePermissions()){
         showSnackbar(getString(R.string.label_title_scanning));
         mWifiManager.scan();
      }
   }


   private void showSnackbar(String text){
      mSnackbar = Snackbar.make(mRootView,text,Snackbar.LENGTH_INDEFINITE);
      mSnackbar.show();
   }

   private void hideSnackbar(){
      if(mSnackbar != null && mSnackbar.isShown())
         mSnackbar.dismiss();
      mSnackbar = null;
   }


   private boolean handlePermissions(){
      if(!mWifiManager.checkPermissions()){
         requestPermissions(mWifiManager.getRequiredPermissions(), REQ_CODE);
         return false;
      }else{
         return true;
      }
   }

   private void scanResultsReceived(List<ScanResult> list){
      hideSnackbar();
      mAdapter.setResults(list);
   }


   private class WifiActionsListener implements JxWifiManager.WifiManagerCallbacks{

      @Override
      public void onScanResultsReceived(List<ScanResult> list){
         scanResultsReceived(list);
      }

      @Override
      public void onWifiStateChanged(int state){}

      @Override
      public void onConnectionStateChanged(WifiInfo info, boolean connected, int status){
         if(status == JxWifiManager.STATUS_WRONG_CREDENTIALS)
            Toast.makeText(getActivity(),R.string.error_wrong_pass,Toast.LENGTH_LONG).show();

      }
   }

}
