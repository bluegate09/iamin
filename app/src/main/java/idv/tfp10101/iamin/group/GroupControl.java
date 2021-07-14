package idv.tfp10101.iamin.group;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import idv.tfp10101.iamin.R;
import idv.tfp10101.iamin.network.RemoteAccess;

/**
 * 賣家團購單控制
 */
public class GroupControl {

    // Singleton
    private static List<Group> Groups;
    public static List<Group> getLocalGroup() {
        if (Groups == null) {
            Groups = new ArrayList<>();
        }
        return Groups;
    }
    public static void setLocalGroup(List<Group> groups) {
        if (Groups == null) {
            Groups = new ArrayList<>();
        }
        Groups = groups;
    }

    public static void getAllGroupByMemberId(Context context, int memberId) {
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "Group";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAllByMemberId");
            jsonObject.addProperty("memberId", memberId);

            // requst
            String jsonString = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));
            /** 匿名內部類別實作TypeToken，抓取 泛型 在呼叫方法 */
            Type listType = new TypeToken<List<Group>>(){}.getType();
            setLocalGroup(new Gson().fromJson(jsonString, listType));
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
        }
    }

    public static void getAllGroup(Context context) {
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "Group";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAll");

            // requst
            String jsonString = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));
            /** 匿名內部類別實作TypeToken，抓取 泛型 在呼叫方法 */
            Type listType = new TypeToken<List<Group>>(){}.getType();
            setLocalGroup(new Gson().fromJson(jsonString, listType));
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
        }
    }
}
