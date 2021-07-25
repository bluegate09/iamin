package idv.tfp10101.iamin;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import idv.tfp10101.iamin.group.Group;
import idv.tfp10101.iamin.group.GroupControl;
import idv.tfp10101.iamin.member.Member;

public class MemberCenterFollowersGroupFragment extends Fragment {
    private String TAG = "TAG_FollowerGroupPage";
    private Activity activity;
    private Member member;
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




    }

    private class FollowerGroupAdapter extends RecyclerView.Adapter<FollowerGroupAdapter.MyViewHolder>{
        private final LayoutInflater layoutInflater;
        private List<Group> groups;

        public FollowerGroupAdapter(LayoutInflater layoutInflater, List<Group> groups) {
            this.layoutInflater = layoutInflater;
            this.groups = groups;
        }

        void setGroups(List<Group> groups){
            this.groups = groups;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull MemberCenterFollowersGroupFragment.FollowerGroupAdapter.MyViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return groups == null ? 0: groups.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            MyViewHolder(View itemView) {
                super(itemView);

            }
        }
    }


}