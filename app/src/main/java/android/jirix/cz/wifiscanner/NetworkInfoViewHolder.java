package android.jirix.cz.wifiscanner;

import android.widget.TextView;

/**
 * Created by JxD on 25. 1. 2017.
 */
public class NetworkInfoViewHolder{
   public TextView SSID;
   public TextView RSSID;
   public TextView capabilities;
   public TextView chWidth;
   public TextView freq;
   public TextView RSSI;

   public NetworkInfoViewHolder(){

   }

   public void setViews(TextView ssid, TextView rssid, TextView capabilities, TextView chWidth, TextView freq, TextView rssi){
      SSID = ssid;
      RSSID = rssid;
      this.capabilities = capabilities;
      this.chWidth = chWidth;
      this.freq = freq;
      this.RSSI = rssi;
   }

   public void updateData(String ssid, String rssid, String capabilities, String chWidth, String freq, String rssi){
      this.SSID.setText(ssid);
      this.RSSID.setText(rssid);
      this.capabilities.setText(capabilities);
      this.chWidth.setText(chWidth);
      this.freq.setText(freq);
      this.RSSI.setText(rssi);
   }


}
