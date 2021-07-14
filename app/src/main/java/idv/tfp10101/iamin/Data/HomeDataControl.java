package idv.tfp10101.iamin.Data;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import idv.tfp10101.iamin.R;

import idv.tfp10101.iamin.network.RemoteAccess;

public class HomeDataControl {
    // Singleton
    private static List<HomeData> HomeDatas;

    public static List<HomeData> getLocalHomeDatas(){
        if (HomeDatas == null) {
            HomeDatas = new ArrayList<>();
        }
        return HomeDatas;
    }

    /**
     * 存入首頁的團購資料
     * @param homedatas
     */
    public static void setLocalHomeDatas(List<HomeData> homedatas){
        if (HomeDatas == null) {
            HomeDatas = new ArrayList<>();
        }
        HomeDatas = homedatas;
    }


    public static void getAllHomeData(Context context) {
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "Home";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAllHomeData");

            // requst
            String jsonString = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));
            /** 匿名內部類別實作TypeToken，抓取 泛型 在呼叫方法 */
            Type listType = new TypeToken<List<HomeData>>(){}.getType();
            setLocalHomeDatas(new Gson().fromJson(jsonString, listType));

        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
        }
    }

    public static void getAllGroupPrice(Context context,int GroupID) {
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "Home";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAllGroupPrice");
            jsonObject.addProperty("groupID",GroupID);

            // requst
            String jsonString = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));
            /** 匿名內部類別實作TypeToken，抓取 泛型 在呼叫方法 */
            Type listType = new TypeToken<List<HomeData>>(){}.getType();
            setLocalHomeDatas(new Gson().fromJson(jsonString, listType));
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
        }
    }
    //取得團購瀏覽圖片
    public static void getAllGroupimg(Context context,int GroupID) {
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "Home";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAllGroupimg");
            jsonObject.addProperty("groupID",GroupID);

            // requst
            String jsonString = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));
            //明天記得接回傳圖片
            /** 匿名內部類別實作TypeToken，抓取 泛型 在呼叫方法 */
            Type listType = new TypeToken<List<HomeData>>(){}.getType();
            setLocalHomeDatas(new Gson().fromJson(jsonString, listType));
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
        }
    }
}
