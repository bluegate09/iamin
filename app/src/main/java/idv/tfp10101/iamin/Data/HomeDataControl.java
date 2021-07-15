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
import java.util.concurrent.ExecutorService;

import idv.tfp10101.iamin.R;

import idv.tfp10101.iamin.group.Group;
import idv.tfp10101.iamin.network.RemoteAccess;

public class HomeDataControl {
    // Singleton
    private static List<Group> Groups;

    public static List<Group> getLocalGroups(){
        if (Groups == null) {
            Groups = new ArrayList<>();
        }
        return Groups;
    }

    /**
     * 存入首頁的團購資料
     * @param groups
     */
    public static void setLocalGroup(List<Group> groups){
        if (Groups == null) {
            Groups = new ArrayList<>();
        }
        Groups = groups;
    }


    public static void getAllGroup(Context context) {
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "Home";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAllGroup");

            // requst
            String jsonString = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));
            /** 匿名內部類別實作TypeToken，抓取 泛型 在呼叫方法 */
            Type listType = new TypeToken<List<Group>>(){}.getType();
            setLocalGroup(new Gson().fromJson(jsonString, listType));

        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
        }
    }

//    public static void getAllGroupPrice(Context context,int GroupID) {
//        // 如果有網路，就進行 request
//        if (RemoteAccess.networkConnected(context)) {
//            // 網址 ＆ Action
//            String url = RemoteAccess.URL_SERVER + "Home";
//            JsonObject jsonObject = new JsonObject();
//            jsonObject.addProperty("action", "getAllGroupPrice");
//            jsonObject.addProperty("groupID",GroupID);
//
//            // requst
//            String jsonString = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));
//            /** 匿名內部類別實作TypeToken，抓取 泛型 在呼叫方法 */
//            Type listType = new TypeToken<List<HomeData>>(){}.getType();
//            setLocalHomeDatas(new Gson().fromJson(jsonString, listType));
//        }else {
//            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
//        }
//    }
//取得團購瀏覽圖片
public static Bitmap getGroupimage(Context context, int GroupID, int imageSize, ExecutorService executor) {
    // 如果有網路，就進行 request
    if (RemoteAccess.networkConnected(context)) {
        // 網址 ＆ Action
        String url = RemoteAccess.URL_SERVER + "Home";
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", "getGroupimage");
        jsonObject.addProperty("groupID",GroupID);
        jsonObject.addProperty("imageSize", imageSize);

        // requst
        return RemoteAccess.getRemoteImage(url,jsonObject.toString(),executor);
    }else {
        Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
        return null;
    }
}
}
