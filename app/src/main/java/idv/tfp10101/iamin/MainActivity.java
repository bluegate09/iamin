package idv.tfp10101.iamin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import static idv.tfp10101.iamin.Constants.FCM_Token;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handleBottomNavigationView();

        /** FCM_Serller 相關設定 */
        // 設定app在背景時收到FCM，會自動顯示notification（前景時則不會自動顯示）
        // (API 26 才有支援 Channel Id 功能)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "seller_notification_channel_id";
            String channelName = "seller_notification_channel_name";
            /*
                NotificationManager -> 需設定2大類
                1. Notification本身訊息 (icon, 圖示, title, body, 自訂資料)
                2. NotificationChannel 設定 重要程度 提示燈 ...等
             */
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            // Channel
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_DEFAULT));
            // 本身訊息 (icon 圖示 已在manifest設定， 訊息部分由Server發送)
        }

        // 當Notification被點擊時會開啟App來到MainActivity，需取得自訂資料後，在跳轉Fragment頁面
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String data = bundle.getString("data");
            // 可以依據data來決定要去哪一Fragment頁面
            Log.d(Constants.TAG, "data: " + data);
            Toast.makeText(this, "data: " + data, Toast.LENGTH_SHORT).show();
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
                            .putString(FCM_Token,token)
                            .apply();

                }
            }
        });

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {

                Log.d("TAG_MAIN","Token: " + task.getResult());
            }
        });
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
        handleBottomNavigationView();
    }

    @Override
    protected void onStop() {
        super.onStop();
        handleBottomNavigationView();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        handleBottomNavigationView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handleBottomNavigationView();
    }

    private void handleBottomNavigationView() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        NavController navController =
                Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if(destination.getId() == R.id.memberCenterMyWalletFragment||
               destination.getId() == R.id.logInFragment||
               destination.getId() == R.id.signUpFragment||
               destination.getId() == R.id.phoneAuthFragment||
               destination.getId() == R.id.memeberCenterProfileFragment||
               destination.getId() == R.id.memberCenterFragment
            ){
                bottomNavigationView.setVisibility(View.GONE);
            }else{
                bottomNavigationView.setVisibility(View.VISIBLE);
            }

//            if (
//                    navController.getCurrentDestination().getId() == R.id.homeFragment ||
//                    navController.getCurrentDestination().getId() == R.id.chatFragment
//                            || navController.getCurrentDestination().getId() == R.id.logInFragment
//            ) {
//                bottomNavigationView.setVisibility(View.VISIBLE);
//            } else {
//                bottomNavigationView.setVisibility(View.GONE);
//            }
        });
    }


}