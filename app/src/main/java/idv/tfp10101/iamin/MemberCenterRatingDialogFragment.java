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
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import idv.tfp10101.iamin.Rating.Rating;
import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;

public class MemberCenterRatingDialogFragment extends Fragment {
    private Activity activity;
    private Member member,buyer;
    private RecyclerView rvMemberRating;
    private ExecutorService executor;
    private List<Rating> ratingList = new ArrayList<>();
    private Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy h:mm:ss a").create();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        member = MemberControl.getInstance();
        buyer = new Member();
        ratingList = MemberControl.getRating(activity,member.getId());

        // 需要開啟多個執行緒取得圖片，使用執行緒池功能
        int numProcs = Runtime.getRuntime().availableProcessors();
//        Log.d("TAG", "JVM可用的處理器數量: " + numProcs);
        // 建立固定量的執行緒放入執行緒池內並重複利用它們來執行任務
        executor = Executors.newFixedThreadPool(numProcs);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_member_center_rating_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvMemberRating = view.findViewById(R.id.rvMemberRating);
        rvMemberRating.setLayoutManager(new LinearLayoutManager(activity));
        showRatingList(ratingList);


    }

    private void showRatingList(List<Rating> ratingList) {
        if(ratingList == null || ratingList.isEmpty()){
            Toast.makeText(activity, "目前沒有評價喔", Toast.LENGTH_SHORT).show();
            return;
        }
        MyAdapter myAdapter = (MyAdapter) rvMemberRating.getAdapter();
        if(myAdapter == null){
            rvMemberRating.setAdapter(new MyAdapter(activity,ratingList));
        }else{
            myAdapter.setRatingList(ratingList);
            myAdapter.notifyDataSetChanged();
        }
    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>{
        private final LayoutInflater layoutInflater;
        private List<Rating> ratingList;

        public MyAdapter(Context context, List<Rating> ratingList) {
            this.layoutInflater = LayoutInflater.from(context);
            this.ratingList = ratingList;
        }

        void setRatingList(List<Rating> ratingList){
            this.ratingList = ratingList;
        }

        @Override
        public int getItemCount() {
            return ratingList == null ? 0 : ratingList.size();
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_rating,parent,false);
            return new MyViewHolder(itemView);
        }

        class MyViewHolder extends RecyclerView.ViewHolder{
            TextView tvRatingNickname,tvRatingEmail,tvRatingPhone,tvMemberCenterRatingMessage,tvRatingTime;
            ImageView ivRatingPortrait;
            RatingBar ratingBar;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                tvRatingNickname = itemView.findViewById(R.id.tvRatingNickname);
                tvRatingEmail = itemView.findViewById(R.id.tvRatingEmail);
                tvRatingPhone = itemView.findViewById(R.id.tvRatingPhone);
                tvMemberCenterRatingMessage = itemView.findViewById(R.id.tvMemberCenterRatingMessage);
                ivRatingPortrait = itemView.findViewById(R.id.ivRatingPortrait);
                ratingBar = itemView.findViewById(R.id.ratingRatingBar);
                tvRatingTime = itemView.findViewById(R.id.tvRatingTime);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull MemberCenterRatingDialogFragment.MyAdapter.MyViewHolder holder, int position) {
            final Rating rating = ratingList.get(position);

            //rating bean 相關
            holder.tvMemberCenterRatingMessage.setText(rating.getRating_message());
            holder.ratingBar.setRating(rating.getOrder_rating());
            int length = rating.getStart_time().toString().length();
            holder.tvRatingTime.setText(rating.getStart_time().toString().substring(0,length-5));

            //member bean 相關
            buyer.setId(rating.getBuyer_Id());
            String memberJson = MemberControl.memberRemoteAccess(activity, buyer, "findById");
            buyer = gson.fromJson(memberJson, Member.class);

            holder.tvRatingNickname.setText(buyer.getNickname());
            holder.tvRatingEmail.setText(buyer.getEmail() != null ? buyer.getEmail() : "");
            holder.tvRatingPhone.setText(buyer.getPhoneNumber() != null ? buyer.getPhoneNumber() : "");

            //member image取得
            Bitmap bitmap = MemberControl.getMemberImageByMemberId(activity, buyer, executor);
            if (bitmap != null) {
                holder.ivRatingPortrait.setImageBitmap(bitmap);
            } else {
                holder.ivRatingPortrait.setImageResource(R.drawable.avatar);
            }
        }
    }
}