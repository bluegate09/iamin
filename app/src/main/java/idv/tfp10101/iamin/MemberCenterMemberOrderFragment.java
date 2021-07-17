package idv.tfp10101.iamin;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import idv.tfp10101.iamin.group.Group;
import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;
import idv.tfp10101.iamin.merch.Merch;
import idv.tfp10101.iamin.network.RemoteAccess;

public class MemberCenterMemberOrderFragment extends Fragment {
    private Activity activity;
    private Member member;
    private List<Group> groups;
    private RecyclerView rvMemberOrderList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        member = MemberControl.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_member_center_member_order, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvMemberOrderList = view.findViewById(R.id.rvMyWallet);
        rvMemberOrderList.setLayoutManager(new LinearLayoutManager(activity));


    }

    private List<Group> getGroups(){
        List<Group> groups = new ArrayList<>();
        if (RemoteAccess.networkConnected(activity)) {
            String url = RemoteAccess.URL_SERVER + "memberController";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getFollowMember");
            jsonObject.addProperty("member", new Gson().toJson(member));
            String jsonIn = RemoteAccess.getRemoteData(url, jsonObject.toString());

            //string to jsonArray
            try {
                JSONArray jsonArray = new JSONArray(jsonIn);
                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
//                    Log.d(TAG, jsonObject2.toString() + "<------------- jsonObject2.toString()");

                    int id = jsonObject2.getInt("MEMBER_ID");
                    String temp_nickname = jsonObject2.getString("NICKNAME");
                    double temp_rating = jsonObject2.getDouble("RATING");
                    int temp_followCount = jsonObject2.getInt("FOLLOW_COUNT");

//                    groups.add(i,new Group(id,temp_followCount,temp_rating,temp_nickname));

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(activity,"No network", Toast.LENGTH_SHORT).show();
        }
        return groups;
    }

    private class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.MyViewHolder>{

        private final LayoutInflater layoutInflater;
        private List<Group> groups;

        GroupAdapter(Context context, List<Group> groups) {
            layoutInflater = LayoutInflater.from(context);
            this.groups = groups;
        }

        class MyViewHolder extends RecyclerView.ViewHolder{

            MyViewHolder(View itemView) {
                super(itemView);
            }

        }

        void setGroups(List<Group> groups){
            this.groups = groups;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_member_center_order_list, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MemberCenterMemberOrderFragment.GroupAdapter.MyViewHolder holder, int position) {
            final Group group = groups.get(position);
        }

        @Override
        public int getItemCount() {
            return groups.size();
        }


    }

    private class MySqlGroupData {
        //itemview 需要的資料
        // group table
        private String title;
        // member_order table
        private int price;
        // group table
        private String deadline;
        // location table
        private String location;

        //需要join group member_order location
    }
}

