package idv.tfp10101.iamin;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MyWallet;
import idv.tfp10101.iamin.merch.Merch;
import idv.tfp10101.iamin.network.RemoteAccess;

import static idv.tfp10101.iamin.member.MemberControl.memberRemoteAccess;

public class MemberCenterMyWalletFragment extends Fragment {
    private final static String TAG = "TAG_MyWallet";
    private Activity activity;
    private Member member;
    private List<MyWallet> merchs;
    private List<MySqlData> mySqlDataList;
    private List<PieEntry> resultIncomeEntries,defaultIncomeEntries;
    private PieData pieData;
    private RecyclerView rvMerch;
    private final int[] My_COLORS = {
            //紅
            0xFFB54434,
            //青
            0xFF006284,
            //橘
            0xFFFC9F4D,
            //綠
            0xFF2D6D4B,
            //皮膚
            0xFFB9887D
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        member = Member.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_member_center_my_wallet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Spinner spinner = view.findViewById(R.id.myWalletSpinner);

        rvMerch = view.findViewById(R.id.rvMyWallet);
        rvMerch.setLayoutManager(new LinearLayoutManager(activity));

        Set<String> hash_set_year = new HashSet<>();
        List<String> date = new ArrayList<>();
        resultIncomeEntries = new ArrayList<>();
        defaultIncomeEntries = new ArrayList<>();
        mySqlDataList = new ArrayList<>();

        //從mysql拿資料
        String jsonIn = memberRemoteAccess(activity,member,"getMyWallet");
        Log.d(TAG,jsonIn);

        try {
            JSONArray jsonArray = new JSONArray(jsonIn);
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject memberOrder = jsonArray.getJSONObject(i);

                int tmp_groupId = memberOrder.getInt("GROUP_ID");
//                String tmp_name = memberOrder.getString("NAME");
                String tmp_category = memberOrder.getString("CATEGORY");
                int tmp_money = memberOrder.getInt("TOTAL");
                //日期 取年
                String tmp_time = memberOrder.get("UPDATE_TIME").toString();
                //過濾重複 年
                hash_set_year.add(tmp_time.substring(0,4));
                mySqlDataList.add(new MySqlData(tmp_groupId, tmp_time, tmp_category, new PieEntry(tmp_money, tmp_category,tmp_groupId)));
            }
            List<PieEntry> entryList = new ArrayList<>();
            for(int i = 0; i < mySqlDataList.size(); i++){
                entryList.add(mySqlDataList.get(i).entries);
            }
            defaultIncomeEntries = new ArrayList<>(entryList);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        date.add("All Time");
        //排序讓alltime在最上面 java8 addAll()
        date.addAll(hash_set_year);
        Collections.sort(date, (o1, o2) -> {
            if(o1.equals(o2)) //update to make is stable
                return 0;
            if(o1.equals("All Time"))
                return -1;
            if(o2.equals("All Time"))
                return 1;
            return o1.compareTo(o2);
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,
                android.R.layout.simple_list_item_1, date);
        spinner.setAdapter(adapter);

        PieChart pieChart = view.findViewById(R.id.pieChart);
        /* 設定可否旋轉 */
        pieChart.setRotationEnabled(false);
        /* 設定圓心文字 */
        pieChart.setCenterText("Income");
        /* 設定圓心文字大小 */
        pieChart.setCenterTextSize(25);

        Description description = new Description();
        description.setText("");
        description.setTextSize(25);
        pieChart.setDescription(description);
        pieChart.getLegend().setEnabled(false);

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry entry, Highlight highlight) {
                PieEntry pieEntry = (PieEntry) entry;
                showMerchList(merchs);
            }
            @Override
            public void onNothingSelected() {
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //spinner選項 select all 選擇所有時間
                if(position == 0){
//                    Log.d(TAG,"defaultIncomeEntries " + defaultIncomeEntries);
                    pieData = setPieData(defaultIncomeEntries);
                }else{
                    String selected = spinner.getAdapter().getItem(position) + "";
                    resultIncomeEntries.clear();
                    for(int i = 0; i < mySqlDataList.size(); i++){
                        //時間比對 年
                        if(mySqlDataList.get(i).time.substring(0,4).equals(selected)){
                            resultIncomeEntries.add(mySqlDataList.get(i).entries);
                        }
                    }
                    pieData = setPieData(resultIncomeEntries);
                    //                Toast.makeText(activity, selected +" position", Toast.LENGTH_SHORT).show();
                }
                pieChart.setData(pieData);
                pieChart.invalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    private void showMerchList(List<Merch> merchs) {
        if (merchs == null || merchs.isEmpty()) {
            Toast.makeText(activity,"no merch found", Toast.LENGTH_SHORT).show();
        }
        MerchAdapter merchAdapter = (MerchAdapter) rvMerch.getAdapter();
        if (merchAdapter == null) {
            rvMerch.setAdapter(new MerchAdapter(activity, merchs));
        } else {
            merchAdapter.setMerchs(merchs);
            merchAdapter.notifyDataSetChanged();
        }
    }


    private class MerchAdapter extends RecyclerView.Adapter<MerchAdapter.MyViewHolder> {
        private final LayoutInflater layoutInflater;
        private List<Merch> merchs;

        MerchAdapter(Context context, List<Merch> merchs) {
            layoutInflater = LayoutInflater.from(context);
            this.merchs = merchs;
        }

        void setMerchs(List<Merch> merchs) {
            this.merchs = merchs;
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvPrice;
            View background;

            MyViewHolder(View itemView) {
                super(itemView);
                background = itemView.findViewById(R.id.walletViewBackgroundColor);
                tvName = itemView.findViewById(R.id.nameWallet);
                tvPrice = itemView.findViewById(R.id.priceWallet);
            }
        }

        @Override
        public int getItemCount() {
            return merchs == null ? 0 : merchs.size();
        }

        @NonNull
        @Override
        public MerchAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_member_center_mywallet, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MerchAdapter.MyViewHolder myViewHolder, int position) {
            final Merch merch = merchs.get(position);
//            Log.d(TAG,"tvName: " + merch.getName()+" ooo");
//            Log.d(TAG,"tvPrice: " + merch.getPrice()+" ooo");
            myViewHolder.tvName.setText(merch.getName() + "");
            myViewHolder.tvPrice.setText(merch.getPrice() +"");
        }
    }

    private PieData setPieData(List<PieEntry> entries) {
        //從這裡給資料
        PieDataSet pieDataSet = new PieDataSet(entries, "");
//        pieDataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        pieDataSet.setValueTextColor(Color.BLUE);
        pieDataSet.setValueTextSize(20);
        //間距
        pieDataSet.setSliceSpace(0);
        //自己建立

        /* 官定顏色範本只有5種顏色，不過可以將多個官定範本顏色疊加 */
        //
        pieDataSet.setColors(My_COLORS);
        return new PieData(pieDataSet);
    }

    private static class MySqlData{

        int group_id;
        String time,category;
        PieEntry entries;

        public MySqlData() {
        }

        public MySqlData(int group_id, String time, String category, PieEntry entries) {
            this.group_id = group_id;
            this.time = time;
            this.category = category;
            this.entries = entries;
        }

        public int getGroup_id() {
            return group_id;
        }

        public void setGroup_id(int group_id) {
            this.group_id = group_id;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public PieEntry getEntries() {
            return entries;
        }

        public void setEntries(PieEntry entries) {
            this.entries = entries;
        }
    }

}