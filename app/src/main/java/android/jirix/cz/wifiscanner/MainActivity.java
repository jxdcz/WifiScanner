package android.jirix.cz.wifiscanner;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.util.List;

public class MainActivity extends AppCompatActivity{

   WifiResultsFragment mResultFragment;

   private JxWifiManager mWifiManager;

   private FloatingActionButton mFab;
   private Switch mWifiStateSwitch;

   private WifiStateSwitchHelper mSwitchHelper;


   @Override
   protected void onCreate(Bundle savedInstanceState){
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);

      mResultFragment = (WifiResultsFragment) getFragmentManager().findFragmentById(R.id.fragment);

      mWifiManager = JxWifiManager.getInstance(this);

      mWifiStateSwitch = (Switch) findViewById(R.id.switch_wifi_state);
      mSwitchHelper = new WifiStateSwitchHelper(mWifiManager, mWifiStateSwitch.getRootView(), mWifiStateSwitch);

      mFab = (FloatingActionButton) findViewById(R.id.fab);
      mFab.setOnClickListener(new View.OnClickListener(){
         @Override
         public void onClick(View view){
            if(mResultFragment != null && mResultFragment.isResumed())
               mResultFragment.notifyScanButtonPressed();
         }
      });

   }

   @Override
   public void onResume(){
      super.onResume();
      mSwitchHelper.onResume();
   }

   @Override
   public void onPause(){
      super.onPause();
      mSwitchHelper.onPause();
   }




   @Override
   public boolean onCreateOptionsMenu(Menu menu){
      getMenuInflater().inflate(R.menu.menu_main, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item){
      int id = item.getItemId();

      return super.onOptionsItemSelected(item);
   }

   private static class WifiStateSwitchHelper implements JxWifiManager.WifiManagerCallbacks{

      private JxWifiManager mWifi;
      private Switch mSwitch;
      private View mRootView;
      private Snackbar mSnackbar;

      private int mLastWifiState;

      public WifiStateSwitchHelper(JxWifiManager wifi, View rootView, Switch switchView){
         mSwitch = switchView;
         mRootView = rootView;
         mWifi = wifi;
         init();
      }


      private void init(){

         mSwitch.setChecked(mWifi.isWifiEnabled());
         mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
               mWifi.setWifi(isChecked);
            }
         });
      }

      public void onResume(){
         mWifi.addListener(this);
      }

      public void onPause(){
         mWifi.removeListener(this);
      }

      @Override
      public void onScanResultsReceived(List<ScanResult> list){

      }

      private void wifiStateChanged(int state){
         switch(state){
            case JxWifiManager.STATE_DISABLING:
               if(mLastWifiState == JxWifiManager.STATE_ENABLED){
                  showSnackbar(mRootView.getContext().getString(R.string.label_title_disabling_wifi));
               }
               break;
            case JxWifiManager.STATE_ENABLING:
               if(mLastWifiState == JxWifiManager.STATE_DISABLED){
                  showSnackbar(mRootView.getContext().getString(R.string.label_title_enabling_wifi));
               }
               break;
            case JxWifiManager.STATE_DISABLED:
               mSwitch.setChecked(false);
               hideSnackbar();
               break;
            case JxWifiManager.STATE_ENABLED:
               mSwitch.setChecked(true);
               hideSnackbar();
               break;

         }
         mLastWifiState = state;
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


      @Override
      public void onWifiStateChanged(int state){
         wifiStateChanged(state);

      }

      @Override
      public void onConnectionStateChanged(WifiInfo info, boolean connected, int status){

      }
   }

}
