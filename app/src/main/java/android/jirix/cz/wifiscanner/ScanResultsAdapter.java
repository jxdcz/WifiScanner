package android.jirix.cz.wifiscanner;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JxD on 24. 1. 2017.
 */

public class ScanResultsAdapter extends BaseAdapter{

   private List<ScanResult> mList;
   private Context mContext;

   private OnItemClickedListener mListener;

   public ScanResultsAdapter(Context context){
      mList = new ArrayList<>();
      mContext = context;
   }

   public interface OnItemClickedListener{
      void onConnectClicked(String ssid);
   }

   public void setListener(OnItemClickedListener listener){
      mListener = listener;
   }

   public void setResults(List<ScanResult> list){
      if(list == null)
         mList.clear();
      else
         mList = list;
      notifyDataSetChanged();
   }


   @Override
   public int getCount(){
      return mList.size();
   }

   @Override
   public Object getItem(int position){
      return mList.get(position);
   }

   @Override
   public long getItemId(int position){
      return position;
   }

   @Override
   public View getView(int position, View convertView, ViewGroup parent){
      if(convertView == null)
         convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_wifi,parent,false);

      ScanResult result = mList.get(position);

      //TODO add a ViewHolder pattern

      convertView.setOnClickListener(new OnItemClickListener(result.SSID));

      ((TextView)convertView.findViewById(R.id.text_wifi_ssid)).setText(result.SSID);
      ((TextView)convertView.findViewById(R.id.text_wifi_bssid)).setText(result.BSSID);
      ((TextView)convertView.findViewById(R.id.text_wifi_capabilities)).setText(result.capabilities);
      ((TextView)convertView.findViewById(R.id.text_wifi_ch_width)).setText(result.channelWidth+"");
      ((TextView)convertView.findViewById(R.id.text_wifi_freq)).setText(result.frequency+"");
      ((TextView)convertView.findViewById(R.id.text_wifi_rssi)).setText(result.level+"");

      return convertView;
   }

   private class OnItemClickListener implements View.OnClickListener{
      String mSsid;

      public OnItemClickListener(String ssid){
         mSsid = ssid;
      }


      @Override
      public void onClick(View v){
         if(mListener != null)
            mListener.onConnectClicked(mSsid);
      }
   }

}
