package idv.tfp10101.iamin;

import androidx.annotation.FractionRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;
import idv.tfp10101.iamin.message.Message;
import java.util.Objects;

import static idv.tfp10101.iamin.Constants.FCM_Token;
import static idv.tfp10101.iamin.Constants.TAG;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TAG_MainActivity";
    /**
     * FCMService 本地廣播
     */
    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** FCMService 本地廣播 */
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                /** 建立AlertDialog */
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(intent.getStringExtra("title"));
                builder.setMessage(intent.getStringExtra("body"));
                builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                // 顯示
                builder.show();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("FCMService"));

        /** FCM_Serller 相關設定 */
        // 設定app在背景時收到FCM，會自動顯示notification（前景時則不會自動顯示）
        // (API 26 才有支援 Channel Id 功能)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "seller_notification_channel_id";
            String channelName = "seller_notification_channel_name";
            String channelChatId = "chat_notification_channel_id";
            String channelChatName = "chat_notification_channel_name";
            /**
                NotificationManager -> 需設定2大類
                1. Notification本身訊息 (icon, 圖示, title, body, 自訂資料)
                2. NotificationChannel 設定 重要程度 提示燈 ...等
             */
            // 取得通知服務
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            // Channel
            // 本身訊息 (icon 圖示 已在manifest設定， 訊息部分由Server發送)
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_DEFAULT));

            notificationManager.createNotificationChannel(new NotificationChannel(channelChatId,
                    channelChatName, NotificationManager.IMPORTANCE_DEFAULT));
        }

        // 當Notification被點擊時會開啟App來到MainActivity，需取得自訂資料後，在跳轉Fragment頁面
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String data = bundle.getString("data");

            // 可以依據data來決定要去哪一Fragment頁面
            Log.d(TAG, "" + data);
//            Toast.makeText(this, "data: " + data, Toast.LENGTH_SHORT).show();
            // 先取得NavController
            NavController navController = Navigation.findNavController(
                    this,
                    R.id.nav_host_fragment
            );
            // 再利用Fragment ID切換到指定Fragment
            if (data != null) {
                switch (data) {
                    case "Reach_Notification":
                        navController.navigate(R.id.memberCenterMemberOrderFragment);
                        break;
                    case "Message_Fragment":
                        navController.navigate(R.id.messageFragment, bundle);
                        break;
                    case "Chat_Fragment":
                        navController.navigate(R.id.chatFragment, bundle);
                        break;
                    case "Seller_Fragment":
                        navController.navigate(R.id.sellerFragment);
//                        navController.navigate(R.id.homeFragment);
                        break;
                    default:
                        break;
                }
            }
        }

        /** 測試用 */
        FirebaseApp.initializeApp(this);
        // 取得registration token
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    String token = task.getResult();
                    Log.d(Constants.TAG, "MainActivityToken: " + token);

                    SharedPreferences pref = this.getSharedPreferences(FCM_Token, MODE_PRIVATE);
                    pref.edit()
                            .putString(FCM_Token, token)
                            .apply();

                }
            }
        });

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {

                Log.d(TAG, "Token: " + task.getResult());
            }
        });
        /**提取會員資料**/
        fetchMember();

    }

    private void fetchMember() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Member member = MemberControl.getInstance();
        if (currentUser != null && member.getuUId() == null) {
            SharedPreferences sharedPreferences = MainActivity.this.getSharedPreferences(FCM_Token, MODE_PRIVATE);
            String token = sharedPreferences.getString(FCM_Token, "");
            member.setFCM_token(token);
            member.setuUId(currentUser.getUid());
            MemberControl.memberRemoteAccess(MainActivity.this, member, "updateTokenbyUid");
            String jsonMember = MemberControl.memberRemoteAccess(MainActivity.this, member, "findbyUuid");
            Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy h:mm:ss a").create();
            member = gson.fromJson(jsonMember, Member.class);
            MemberControl.setMember(member);
            Log.d(TAG, "Fetch Member Date Complete");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        handleBottomNavigationView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleBottomNavigationView();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        handleBottomNavigationView();
        /** FCMService 本地廣播 */
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        handleBottomNavigationView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void handleBottomNavigationView() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        NavController navController =
                Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if(destination.getId() == R.id.memberCenterMyWalletFragment||
               destination.getId() == R.id.signUpFragment||
               destination.getId() == R.id.phoneAuthFragment||
               destination.getId() == R.id.memeberCenterProfileFragment||
               destination.getId() == R.id.memberCenterFragment||
               destination.getId() == R.id.memberCenterMyIncome||
               destination.getId() == R.id.merchbrowseFragment ||
               destination.getId() == R.id.messageFragment||
               destination.getId() == R.id.memberCenterIncomeDeatilsFragment||
               destination.getId() == R.id.memberCenterMyWalletDetailsFragment

            ){
                bottomNavigationView.setVisibility(View.GONE);
            } else {
                bottomNavigationView.setVisibility(View.VISIBLE);
            }
        });
    }
}