package idv.tfp10101.iamin.service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import idv.tfp10101.iamin.Constants;

public class FCMServiceSeller extends FirebaseMessagingService {
    // 當Android裝置在前景收到FCM時呼叫
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // 取得notification資料，主要為title與body這2個保留字
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        String title = "付款通知";
        String body = "ABC";
        if (notification != null) {
            title = notification.getTitle();
            body = notification.getBody();
        }
        // 取得自訂資料
        Map<String, String> map = remoteMessage.getData();
        String data = map.get("data");
        Log.d(Constants.TAG, "onMessageReceived():\ntitle: " + title + ", body: " + body + ", data: " + data);
    }

    // 當registration token更新時呼叫，應該將新的token傳送至server
    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(Constants.TAG, "onNewToken: " + token);
        //RemoteAccess.sendTokenToServer(token, this);
    }
}
