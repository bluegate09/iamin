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
import idv.tfp10101.iamin.merch.Merch;
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

    /**
     * 使用會員ID抓取目前開團購的清單
     * @param context
     * @param memberId
     */
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
            if (jsonString == null) {
                return;
            }
            /** 匿名內部類別實作TypeToken，抓取 泛型 在呼叫方法 */
            Type listType = new TypeToken<List<Group>>(){}.getType();
            setLocalGroup(new Gson().fromJson(jsonString, listType));
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 抓取目前server上有哪些團購種類
     * @param context
     * @return
     */
    public static List<GroupCategory> getAllCategory(Context context) {
        List<GroupCategory> groupCategories = new ArrayList<>();
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "Group";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAllCategory");

            // requst
            String jsonString = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));
            /** 匿名內部類別實作TypeToken，抓取 泛型 在呼叫方法 */
            Type listType = new TypeToken<List<GroupCategory>>(){}.getType();
            groupCategories = new Gson().fromJson(jsonString, listType);
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
        }

        return groupCategories;
    }

    /**
     * 新增團購
     */
    public static int insertGroup(Context context, Group group, List<Double[]> LatLngs) {
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "Group";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "insert");
            jsonObject.addProperty("group", new Gson().toJson(group));
            jsonObject.addProperty("LatLngs", new Gson().toJson(LatLngs));

            // requst
            String result = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));
            return Integer.parseInt(result);
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
            return -1;
        }
    }

    /**
     * 刪除團購
     */
    public static int deleteGroup(Context context, int id, List<Merch> merches) {
        String result = "0";
        List<Integer> merchsId = new ArrayList<>();
        for (Merch merch : merches) {
            merchsId.add(merch.getMerchId());
        }
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "Group";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "deleteById");
            jsonObject.addProperty("id", id);
            jsonObject.addProperty("merchsId", new Gson().toJson(merchsId));

            // requst
            result = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));

            return Integer.parseInt(result);
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
            return -1;
        }
    }

    /**
     * 抓取目前有哪些團購已經達標
     */
    public static List<Group> getReachGroup(Context context, int memberId) {
        // 刷新團購清單
        getAllGroupByMemberId(context, memberId);
        // 取得目前已達標的團購
        if (Groups == null || Groups.isEmpty()) {
            return null;
        }
        List<Group> reachGroups = new ArrayList<>();
        for (Group group : Groups) {
            if (group.getProgress() >= group.getGoal()) {
                reachGroups.add(group);
            }
        }
        if (reachGroups.isEmpty()) {
            return null;
        }else {
            return reachGroups;
        }
    }
}
