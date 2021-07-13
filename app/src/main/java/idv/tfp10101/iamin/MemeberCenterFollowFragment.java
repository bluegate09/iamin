package idv.tfp10101.iamin;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
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
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.network.RemoteAccess;

public class MemeberCenterFollowFragment extends Fragment {
    private final static String TAG = "TAG_MC_Follow";
    private ExecutorService executor;
    private Activity activity;
    private Member member_ID;
    private RecyclerView rvMember;
    private List<Member> members;
    private Gson gson2 = new GsonBuilder().setDateFormat("MMM d, yyyy h:mm:ss a").create();
//    private Gson gson=  new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        member_ID = Member.getInstance();
        int numProcs = Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool(numProcs);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_memeber_center_follow, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SearchView searchView = view.findViewById(R.id.svFollwoSearch);

        rvMember = view.findViewById(R.id.rvFollowRecyclerView);
        rvMember.setLayoutManager(new LinearLayoutManager(activity));
        members = getMembers();
        showFollowMember(members);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.isEmpty()) {
                    showFollowMember(members);
                }else{
                    List<Member> searchMembers = new ArrayList<>();
                    for(Member member : members){
                        if (member.getNickname().toUpperCase().contains(query.toUpperCase())) {
                            searchMembers.add(member);
                        }
                    }showFollowMember(searchMembers);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.isEmpty())
                    showFollowMember(members);
                return false;
            }
        });
    }

    private void showFollowMember(List<Member> members) {
        if (members == null || members.isEmpty()) {
            Toast.makeText(activity,"no member found", Toast.LENGTH_SHORT).show();
        }
        MemberAdapter memberAdapter = (MemberAdapter) rvMember.getAdapter();
        if (memberAdapter == null) {
            rvMember.setAdapter(new MemberAdapter(activity, members));
        } else {
            memberAdapter.setMembers(members);
            memberAdapter.notifyDataSetChanged();
        }
    }

    private List<Member> getMembers() {
        List<Member> members = new ArrayList<>();

        if (RemoteAccess.networkConnected(activity)) {
            String url = RemoteAccess.URL_SERVER + "memberServelt";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getFollowMember");
            jsonObject.addProperty("member", new Gson().toJson(member_ID));
            String jsonIn = RemoteAccess.getRemoteData(url, jsonObject.toString());

//            Log.d(TAG,"jsonIn: " + jsonIn);
            Type listType = new TypeToken<List<Member>>() {}.getType();

            members = gson2.fromJson(jsonIn, listType);
//            Log.d(TAG,"members: " + members);

        } else {
            Toast.makeText(activity,"No network", Toast.LENGTH_SHORT).show();
        }
        return members;
    }

    private class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MyViewHolder> {
        private final LayoutInflater layoutInflater;
        private List<Member> members;

        MemberAdapter(Context context, List<Member> members) {
            layoutInflater = LayoutInflater.from(context);
            this.members = members;
        }

        void setMembers(List<Member> members) {
            this.members = members;
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView tvNickname, tvRating, tvFollowCount, tvFollowBt, tvStatus, tvfollowBt;

            MyViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.ivFollowPortrait);
                tvNickname = itemView.findViewById(R.id.tvFollowNickname);
                tvRating = itemView.findViewById(R.id.tvFollowRating);
                tvFollowCount = itemView.findViewById(R.id.tvFollowCount);
                tvFollowBt = itemView.findViewById(R.id.tvFollowbt);
                tvStatus = itemView.findViewById(R.id.tvFollowStatus);
            }
        }

        @Override
        public int getItemCount() {
            return members == null ? 0 : members.size();
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_member_center_follow, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override //幾筆跑幾次
        public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int position) {
            final Member member = members.get(position);

            String ratingText = "RATING: " + member.getRating();
            String followCountText = "Follower: "+member.getFollow_count();

            myViewHolder.tvNickname.setText(member.getNickname());
            myViewHolder.tvRating.setText(ratingText);
            myViewHolder.tvFollowCount.setText(followCountText);
            myViewHolder.tvStatus.setText("TODO");
            myViewHolder.tvFollowBt.setOnClickListener(v -> {

                String url = RemoteAccess.URL_SERVER + "memberServelt";
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "follow");
                //追蹤的member_id
                jsonObject.addProperty("follwer_id",member.getId());
                //登入的id
                jsonObject.addProperty("member", new Gson().toJson(member_ID));
                RemoteAccess.getRemoteData(url, jsonObject.toString());
                members = getMembers();
                showFollowMember(members);

            });
            myViewHolder.itemView.setOnClickListener(view -> {
                //連到賣家頁面
            });

            String url = RemoteAccess.URL_SERVER + "memberServelt";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getImage");
            jsonObject.addProperty("member", new Gson().toJson(member));
            Bitmap bitmap = RemoteAccess.getRemoteImage(url, jsonObject.toString(), executor);
            if (bitmap != null) {
                myViewHolder.imageView.setImageBitmap(bitmap);
            } else {
                myViewHolder.imageView.setImageResource(R.drawable.silhouettes);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}