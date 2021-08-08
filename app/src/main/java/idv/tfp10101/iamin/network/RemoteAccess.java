package idv.tfp10101.iamin.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import idv.tfp10101.iamin.Constants;
import idv.tfp10101.iamin.R;


/**
 * Server連線 Action總表
 */
public class RemoteAccess {
    private static final String TAG = "TAG_RemoteAccess";
    // 根網址
//    public static String URL_SERVER = "http://10.0.2.2:8080/iamin_JavaServlet/";
    //實機測試

    public static String URL_SERVER = "http://219.68.160.213:8080/iamin_JavaServlet/";

    /**
     * (Json)抓取server資料
     * @param url
     * @param requst
     * @return
     */
    public static String getRemoteData(String url, String requst) {
        JsonCallable jsonCallable = new JsonCallable(url, requst);
        // callable 轉 Runnable (FutureTask<> -> Runnable的子代)
        FutureTask<String> task = new FutureTask<>(jsonCallable);
        Thread thread = new Thread(task);
        thread.start();
        try {
            return task.get();
        } catch (Exception e) {
            Log.e(Constants.TAG, "getRemoteData(): " + e.toString());
            task.cancel(true);
            return null;
        }
    }

    public static void sendChatTokenToServer(String token, Context context) {
        if (networkConnected(context)) {
            String url = URL_SERVER + "FcmChatServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "register");
            jsonObject.addProperty("registrationToken", token);
            RemoteAccess.getRemoteData(url, jsonObject.toString());
        } else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * (Images)抓取server資料
     * @param url
//     * @param requst
     * @return
     */
    public static String getRemoteChatData(String url, String outStr) {
        JsonCallable callable = new JsonCallable(url, outStr);
        FutureTask<String> task = new FutureTask<>(callable);
        Thread thread = new Thread(task);
        thread.start();
        try {
            return task.get();
        } catch (Exception e) {
            Log.e(TAG, "getRemoteData(): " + e.toString());
            task.cancel(true);
            return "";
        }
    }

    public static String getRemotePayData(String url, String outStr, String apiKey) {
        JCallable callable = new JCallable(url, outStr, apiKey);
        FutureTask<String> task = new FutureTask<>(callable);
        Thread thread = new Thread(task);
        thread.start();
        try {
            return task.get();
        } catch (Exception e) {
            task.cancel(true);
            return "";
        }
    }

    // 適用取得一張圖
    public static Bitmap getRemoteImage(String url, String outStr) {
        ImageCallable callable = new ImageCallable(url, outStr);
        FutureTask<Bitmap> task = new FutureTask<>(callable);
        Thread thread = new Thread(task);
        thread.start();
        try {
            return task.get();
        } catch (Exception e) {
            task.cancel(true);
            return null;
        }
    }
    // 搭配Executor取圖 thread pool
    public static Bitmap getRemoteImage(String url, String outStr, ExecutorService executor) {

        ImageCallable callable = new ImageCallable(url, outStr);
        //每一個提交都會產生一個future
        Future<Bitmap> future = executor.submit(callable);
        Bitmap bitmap = null;
        try {
            bitmap = future.get();
        } catch (ExecutionException | InterruptedException e) {
            future.cancel(true);
        }
        return bitmap;
    }

    /**
     * 檢查是否有網路連線
     * API 23 以上：
     * ConnectivityManager -> Network -> NetworkCapabilities
     *
     * API 23 下：
     * ConnectivityManager -> NetworkInfo
     * @param context
     * @return
     */
    public static boolean networkConnected(Context context) {
        // Connectivity(連接性)
        ConnectivityManager CM = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (CM != null) {
            // VERSION > API 23
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network network = CM.getActiveNetwork(); /** 需要權限 */
                // NetworkCapabilities(網絡能力)
                NetworkCapabilities networkCapabilities = CM.getNetworkCapabilities(network);
                // (Wifi 行動網路 有線網路)
                if (networkCapabilities != null) {
                    boolean wifi = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
                    boolean cellular = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
                    boolean ethernet = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
                    Log.d(Constants.TAG, "TRANSPORT_WIFI: " + String.valueOf(wifi));
                    Log.d(Constants.TAG, "TRANSPORT_CELLULAR: " + String.valueOf(cellular));
                    Log.d(Constants.TAG, "TRANSPORT_ETHERNET: " + String.valueOf(ethernet));
                    return wifi || cellular || ethernet;
                }
            }else {
                NetworkInfo networkInfo = CM.getActiveNetworkInfo();
                return networkInfo != null && networkInfo.isConnected();
            }
        }
        Log.e(Constants.TAG, "ConnectivityManager Is Null");
        return false;
    }
}
