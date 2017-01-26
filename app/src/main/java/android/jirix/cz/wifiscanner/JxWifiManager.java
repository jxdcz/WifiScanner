package android.jirix.cz.wifiscanner;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.*;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import java.security.Permission;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by JxD on 22. 1. 2017.
 */

public class JxWifiManager{

   public static final int STATE_ENABLED = 1;
   public static final int STATE_ENABLING = 2;
   public static final int STATE_DISABLED = 3;
   public static final int STATE_DISABLING = 4;
   public static final int STATE_UNKNOWN = 5;
   public static final int STATE_CONNECTED = 6;
   public static final int STATE_DISCONNECTED = 7;


   public static final int STATUS_NORMAL = 0;
   public static final int STATUS_CONNECTED = 1;
   public static final int STATUS_DISCONNECTED = 2;
   public static final int STATUS_WRONG_CREDENTIALS = 3;

   public static final int ACTION_SCAN_RESULTS_READY = 1;
   public static final int ACTION_WIFI_STATE_CHANGED = 2;
   public static final int ACTION_CONNECTION_STATE_CHANGED = 3;

   public static final int NETSEC_UNKNOWN = -1;
   public static final int NETSEC_OPEN = 1;
   public static final int NETSEC_WEP = 2;
   public static final int NETSEC_PSK = 3;
   public static final int NETSEC_EAP = 4;


   private static JxWifiManager sInstance;

   private WifiManager mWifi;
   private Context mContext;
   private boolean mRunning;

   private List<ScanResult> mLastResults;

   private WifiBroadcastReceiver mBroadcastReceiver;

   private Set<WifiManagerCallbacks> mListeners;
   private Set<WifiManagerActionCallback> mActionListeners;

   private int mRequestsInProgress = 0;

   private JxWifiManager(Context context){
      init(context);
   }

   public static JxWifiManager getInstance(Context context){
      if(sInstance == null)
         sInstance = new JxWifiManager(context);
      return sInstance;
   }

   private void init(Context context){
      mContext = context.getApplicationContext();
      mWifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

      mBroadcastReceiver = new WifiBroadcastReceiver();
      mLastResults = new ArrayList<>();
      mRunning = false;
      mListeners = new HashSet<>();
      mActionListeners = new HashSet<>();
   }

   public interface WifiManagerCallbacks{
      void onScanResultsReceived(List<ScanResult> list);
      void onWifiStateChanged(int state);
      void onConnectionStateChanged(WifiInfo info, boolean connected, int statusCode);
   }

   public interface WifiManagerActionCallback{
      void onActionHappened(int action);
   }

   public boolean isWifiEnabled(){
      return mWifi.isWifiEnabled();
   }

   public void setWifi(boolean enabled){
      mWifi.setWifiEnabled(enabled);
   }


   public void scan(){
      mWifi.startScan();
   }

   public void start(){
      if(!mRunning){
         mRunning = true;
         registerForResponses();
      }
   }

   public void stop(){
      if(mRunning){
         mRunning = false;
         unregisterForResponses();
      }
   }

   public WifiInfo getConnectionInfo(){
      return mWifi.getConnectionInfo();
   }

   public int getNetworkSecurity(String ssid){
      ScanResult result = null;
      for(int i=0;i<mLastResults.size();i++){
         if(mLastResults.get(i).SSID.contentEquals(ssid)){
            result = mLastResults.get(i);
            break;
         }
      }
      if(result == null)
         return NETSEC_UNKNOWN;
      else if(result.capabilities.contains("WEP"))
         return NETSEC_WEP;
      else if(result.capabilities.contains("PSK"))
         return NETSEC_PSK;
      else if(result.capabilities.contains("EAP"))
         return NETSEC_EAP;
      else
         return NETSEC_OPEN;

   }

   public void connect(String ssid, String key){
      WifiConfiguration wifiConfig = new WifiConfiguration();
      wifiConfig.SSID = String.format("\"%s\"", ssid);

      switch(getNetworkSecurity(ssid)){
         case NETSEC_PSK:
            wifiConfig.preSharedKey = String.format("\"%s\"", key);
            break;
         case NETSEC_OPEN:
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            break;
         case NETSEC_WEP:
            wifiConfig.wepKeys[0] = "\"" + key + "\"";
            wifiConfig.wepTxKeyIndex = 0;
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            break;
         default:
            return;
      }


      int netId = mWifi.addNetwork(wifiConfig);
      mWifi.disconnect();
      mWifi.enableNetwork(netId, true);
      mWifi.reconnect();
   }

   public void disconnect(){
      mWifi.disconnect();
   }


   public void addListener(WifiManagerCallbacks listener){
      mListeners.add(listener);
   }

   public void removeListener(WifiManagerCallbacks listener){
      mLastResults.remove(listener);
   }


   private void onNewRequest(){
      mRequestsInProgress++;
      registerForResponses();
   }

   private void onRequestEnded(){
      mRequestsInProgress--;
      if(mRequestsInProgress == 0)
         unregisterForResponses();
   }

   public String[] getRequiredPermissions(){
      return new String[]{
              Manifest.permission.ACCESS_WIFI_STATE,
              Manifest.permission.CHANGE_WIFI_STATE,
              Manifest.permission.ACCESS_COARSE_LOCATION
      };
   }

   public boolean checkPermissions(){
      if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
         int changeWifi = mContext.checkSelfPermission(Manifest.permission.CHANGE_WIFI_STATE);
         int accessWifi = mContext.checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE);
         int coarseLoc = mContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);

         if(changeWifi != PackageManager.PERMISSION_GRANTED || accessWifi != PackageManager.PERMISSION_GRANTED || coarseLoc != PackageManager.PERMISSION_GRANTED)
            return false;
         return true;
      }else{
         return true;
      }
   }

   private void registerForResponses(){
      IntentFilter filter = new IntentFilter();
      filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
      filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
      filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
      filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);

      mContext.registerReceiver(mBroadcastReceiver,filter);
   }

   private void unregisterForResponses(){
      mContext.unregisterReceiver(mBroadcastReceiver);
   }


   private class WifiBroadcastReceiver extends BroadcastReceiver{

      @Override
      public void onReceive(Context context, Intent intent){
         String action = intent.getAction();
         switch(action){
            case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
               scanResultsReceived();
               break;
            case WifiManager.WIFI_STATE_CHANGED_ACTION:
               wifiStateChanged(mWifi.getWifiState());
               break;
            case WifiManager.NETWORK_STATE_CHANGED_ACTION:
               android.net.NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
               connectionStateChanged(netInfo.getState() == NetworkInfo.State.CONNECTED ? true : false,STATUS_NORMAL);
               break;
            case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:
               //SupplicantState supl_state=((SupplicantState)intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE));
               int supError = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR,-1);
               if(supError == WifiManager.ERROR_AUTHENTICATING)
                  connectionStateChanged(false,STATUS_WRONG_CREDENTIALS);
               break;
            default:
               break;
         }
      }
   }

   private void connectionStateChanged(boolean connected, int status){
      notifyConnStateChanged(mWifi.getConnectionInfo(),connected,status);
   }

   private void wifiStateChanged(int state){

         switch(state){
            case WifiManager.WIFI_STATE_ENABLED:
               notifyWifiStateChanged(STATE_ENABLED);
               break;
            case WifiManager.WIFI_STATE_ENABLING:
               notifyWifiStateChanged(STATE_ENABLING);
               break;
            case WifiManager.WIFI_STATE_DISABLED:
               notifyWifiStateChanged(STATE_DISABLED);
               break;
            case WifiManager.WIFI_STATE_DISABLING:
               notifyWifiStateChanged(STATE_DISABLING);
               break;
            case WifiManager.WIFI_STATE_UNKNOWN:
               notifyWifiStateChanged(STATE_UNKNOWN);
               break;
            default:
               break;
         }
   }

   private void scanResultsReceived(){
      if(!mWifi.isWifiEnabled())
         return;

      mLastResults = mWifi.getScanResults();
      notifyScanResultsReceived(mLastResults);
   }

   private void notifyConnStateChanged(WifiInfo info, boolean connected, int status){
      Iterator<WifiManagerCallbacks> it = mListeners.iterator();
      while(it.hasNext())
         it.next().onConnectionStateChanged(info, connected,status);
      notifyActionListeners(ACTION_CONNECTION_STATE_CHANGED);
   }

   private void notifyWifiStateChanged(int state){
      Iterator<WifiManagerCallbacks> it = mListeners.iterator();
      while(it.hasNext())
         it.next().onWifiStateChanged(state);
      notifyActionListeners(ACTION_WIFI_STATE_CHANGED);
   }

   private void notifyScanResultsReceived(List<ScanResult> list){
      Iterator<WifiManagerCallbacks> it = mListeners.iterator();
      while(it.hasNext())
         it.next().onScanResultsReceived(list);
      notifyActionListeners(ACTION_SCAN_RESULTS_READY);
   }

   private void notifyActionListeners(int action){
      Iterator<WifiManagerActionCallback> it = mActionListeners.iterator();
      while(it.hasNext())
         it.next().onActionHappened(action);
   }

}
