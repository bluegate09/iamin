package idv.tfp10101.iamin;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;
import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;
import idv.tfp10101.iamin.network.RemoteAccess;

public class ChatFragment extends Fragment {
    private static final String TAG = "TAG_ChatFragment";
    private ExecutorService executor;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AppCompatActivity activity;
    private RecyclerView rvSeller;
    private List<Member> members;
    private Member member;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTokenSendServer();
        activity = (AppCompatActivity) getActivity();
        int numProcs = Runtime.getRuntime().availableProcessors();
        Log.d(TAG, "JVM可用的處理器數量: " + numProcs);
        executor = Executors.newFixedThreadPool(numProcs);
        member = MemberControl.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().hide();
        }
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getTokenSendServer();
        SearchView searchView = view.findViewById(R.id.searchView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        rvSeller = view.findViewById(R.id.rvSellers);

        rvSeller.setLayoutManager(new LinearLayoutManager(activity));
        members = showAllSellers();
        showSellers(members);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            showSellers(members);
            swipeRefreshLayout.setRefreshing(false);
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                // 如果搜尋條件為空字串，就顯示原始資料；否則就顯示搜尋後結果
                if (newText.isEmpty()) {
                    showSellers(members);
                } else {
                    List<Member> searchSellers = new ArrayList<>();
                    // 搜尋原始資料內有無包含關鍵字(不區別大小寫)
                    for (Member member : members) {
                        if (member.getNickname().toUpperCase().contains(newText.toUpperCase())) {
                            searchSellers.add(member);
                        }
                    }
                    showSellers(searchSellers);
                }
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        getTokenSendServer();
        showAllSellers();
    }

    private List<Member> showAllSellers() {
        List<Member> members = null;
        if (RemoteAccess.networkConnected(activity)) {
//            Log.d(TAG, "showAllSellers");
            String url = RemoteAccess.URL_SERVER + "Chat";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAllSeller");

            String jsonIn = RemoteAccess.getRemoteData(url, jsonObject.toString());

            Type listType = new TypeToken<List<Member>>() {
            }.getType();
//            Log.d(TAG, "listType : " + listType);
            members = new Gson().fromJson(jsonIn, listType);
        } else {
            Toast.makeText(activity, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
        }
        return members;
    }

    private void showSellers(List<Member> members) {
//        Log.d(TAG, "showSellers() called");
        if (members == null || members.isEmpty()) {
            Toast.makeText(activity, R.string.textNoSellerFound, Toast.LENGTH_SHORT).show();
        }
        SellerAdapter sellerAdapter = (SellerAdapter) rvSeller.getAdapter();
        if (sellerAdapter == null) {
            rvSeller.setAdapter(new SellerAdapter(activity, members));
        } else {
            sellerAdapter.setMembers(members);
            sellerAdapter.notifyDataSetChanged();
        }
    }

    private class SellerAdapter extends RecyclerView.Adapter<SellerAdapter.SellerViewHolder> {
        private final LayoutInflater layoutInflater;
        private List<Member> members;
        private final int imageSize;

        SellerAdapter(Context context, List<Member> memberList) {
            layoutInflater = LayoutInflater.from(context);
            this.members = memberList;
            imageSize = getResources().getDisplayMetrics().widthPixels / 4;
        }

        public void setMembers(List<Member> members) {
            this.members = members;
        }

        public class SellerViewHolder extends RecyclerView.ViewHolder{
            CircleImageView ivSeller;
            TextView tvName;

            public SellerViewHolder(View itemView) {
                super(itemView);
                ivSeller = itemView.findViewById(R.id.ivSeller);
                tvName = itemView.findViewById(R.id.tvName);
            }
        }


        @Override
        public int getItemCount() {
            return members == null ? 0 : members.size();
        }

        @NonNull
        @Override
        public SellerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_chat_seller, parent, false);
            return new SellerViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull SellerViewHolder holder, int position) {
            member = members.get(position);
            holder.tvName.setText(member.getNickname());

            holder.itemView.setOnClickListener(v -> {
                getTokenSendServer();
                Bundle bundle = new Bundle();
                bundle.putSerializable("member", member);
                Log.d(TAG, "進入聊天");
                Navigation.findNavController(v).navigate(R.id.action_chatFragment_to_messageFragment, bundle);
            });

            String url = RemoteAccess.URL_SERVER + "Chat";
            int id = member.getId();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getImage");
            jsonObject.addProperty("id", id);
            jsonObject.addProperty("imageSize", imageSize);
            Bitmap bitmap = RemoteAccess.getRemoteImage(url, jsonObject.toString(), executor);
            if (bitmap != null) {
                holder.ivSeller.setImageBitmap(bitmap);
            } else {
                holder.ivSeller.setImageResource(R.drawable.avatar);
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

    // send Chat token
    private void getTokenSendServer() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    String token = task.getResult();
                    Log.d(TAG, "Chat 取token : " + token);
                    RemoteAccess.sendChatTokenToServer(token, activity);
                }
            }
        });
    }

}