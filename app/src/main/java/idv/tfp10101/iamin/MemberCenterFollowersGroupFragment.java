package idv.tfp10101.iamin;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.Task;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import idv.tfp10101.iamin.Data.HomeDataControl;
import idv.tfp10101.iamin.group.Group;
import idv.tfp10101.iamin.group.GroupControl;
import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;

public class MemberCenterFollowersGroupFragment extends Fragment {
    private String TAG = "TAG_FollowerGroupPage";
    private Activity activity;
    private ExecutorService executor;
    private List<Group> groups;
    private RecyclerView recyclerViewGroup;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();


        // 需要開啟多個執行緒取得圖片，使用執行緒池功能
        int numProcs = Runtime.getRuntime().availableProcessors();
        Log.d("TAG", "JVM可用的處理器數量: " + numProcs);
        // 建立固定量的執行緒放入執行緒池內並重複利用它們來執行任務
        executor = Executors.newFixedThreadPool(numProcs);

        Bundle bundle = getArguments();
        int followerId = bundle.getInt("followerId");
        String nickname = bundle.getString("name");

        activity.setTitle(nickname +"的賣場");

        GroupControl.getAllGroupByMemberId(activity,followerId);
        groups = GroupControl.getLocalGroup();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_member_center_followers_group, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewGroup = view.findViewById(R.id.rvFollowerGroup);
        recyclerViewGroup.setLayoutManager(new LinearLayoutManager(activity));
        showMyGroup(groups);


    }


    private void showMyGroup(List<Group> groups){
        if(groups == null || groups.isEmpty()){
            Log.d(TAG,"no group found");
        }
        FollowerGroupAdapter groupAdapter = (FollowerGroupAdapter) recyclerViewGroup.getAdapter();
        if(groupAdapter == null){
            recyclerViewGroup.setAdapter(new FollowerGroupAdapter(activity,groups));
        }else{
            groupAdapter.setGroups(groups);
            groupAdapter.notifyDataSetChanged();
        }

    }

    private class FollowerGroupAdapter extends RecyclerView.Adapter<FollowerGroupAdapter.MyViewHolder>{
        private final LayoutInflater layoutInflater;
        private List<Group> groups;
        private final int imageSize;

        public FollowerGroupAdapter(Context context, List<Group> groups) {
            layoutInflater = LayoutInflater.from(context);
            this.groups = groups;
            imageSize = getResources().getDisplayMetrics().widthPixels / 4;
        }

        void setGroups(List<Group> groups){
            this.groups = groups;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_group_buyer, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MemberCenterFollowersGroupFragment.FollowerGroupAdapter.MyViewHolder holder, int position) {
            final Group group = groups.get(position);

            int group_id = group.getGroupId();

            Bitmap groupBitmap = HomeDataControl.getGroupimage(activity,group_id,imageSize,executor);
            if (groupBitmap != null) {
                holder.imv_group.setImageBitmap(groupBitmap);
            } else {
                holder.imv_group.setImageResource(R.drawable.no_image);
            }
            holder.txv_group_name.setText(group.getName());
            Timestamp ts = group.getConditionTime();
            DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            holder.txv_group_conditionTime.setText("結單日期:" + "\n" + sdf.format(ts));

            if (group.getConditionCount() == -1){
                holder.txv_progress.setText("進度:" + group.getProgress() + "份\n" +"目標:" + group.getGoal() + "份");
            }else{
                holder.txv_progress.setText("進度:" + group.getProgress() + "份\n" + "目標:" + group.getGoal() + "份\n" + "購買上限:" + group.getConditionCount() + "份");
            }

            holder.txv_distanceMin.setText("");
            holder.itemView.setOnClickListener(v -> {

                MemberControl.MemberCoordinate coordinate = MemberControl.getCoordinateInstance();

                Bundle bundle = new Bundle();
                bundle.putInt("GroupID", group_id);
                bundle.putDouble("Userlat",coordinate.getLatitude());
                bundle.putDouble("Userlng",coordinate.getLongtitude());

                Navigation.findNavController(v).navigate(R.id.merchbrowseFragment, bundle);
            });

        }

        @Override
        public int getItemCount() {
            return groups == null ? 0: groups.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView txv_group_name, txv_group_conditionTime, txv_progress,txv_distanceMin;
            ImageView imv_group;

            MyViewHolder(View itemView) {
                super(itemView);
                txv_group_name = itemView.findViewById(R.id.txv_group_name);
                txv_group_conditionTime = itemView.findViewById(R.id.txv_group_conditionTime);
                txv_progress = itemView.findViewById(R.id.txv_progress);
                txv_distanceMin = itemView.findViewById(R.id.txv_distanceMin);
                imv_group = itemView.findViewById(R.id.imv_group);

            }
        }
    }


}