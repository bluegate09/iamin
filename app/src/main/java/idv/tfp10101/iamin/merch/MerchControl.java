package idv.tfp10101.iamin.merch;

import android.content.Context;
import android.util.Base64;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import idv.tfp10101.iamin.R;
import idv.tfp10101.iamin.network.RemoteAccess;

/**
 * 商品控制
 */
public class MerchControl {
    // Singleton
    private static List<Merch> Merchs;
    public static List<Merch> getLocalMerchs(){
        if (Merchs == null) {
            Merchs = new ArrayList<>();
        }
        return Merchs;
    }

    /**
     * 存入賣家個人的商品資料
     * @param merchs
     */
    public static void setLocalMerchs(List<Merch> merchs){
        if (Merchs == null) {
            Merchs = new ArrayList<>();
        }
        Merchs = merchs;
    }

    /**
     * 從server抓取所有商品
     * (需要 Gson)
     * @return
     */
    public static void getAllMerchByMemberId(Context context, int memberId) {
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "Merch";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAllByMemberId");
            jsonObject.addProperty("memberId", memberId);

            // requst
            String jsonString = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));
            /** 匿名內部類別實作TypeToken，抓取 泛型 在呼叫方法 */
            Type listType = new TypeToken<List<Merch>>(){}.getType();
            setLocalMerchs(new Gson().fromJson(jsonString, listType));
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 刪除選擇的 商品
     * @param context
     * @param id
     * @return
     */
    public static int deleteMerchById(Context context, int id) {
        String result = "0";
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "Merch";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "deleteById");
            jsonObject.addProperty("id", id);

            // requst
            result = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));

            return Integer.parseInt(result);
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
            return -1;
        }
    }

    /**
     * 新增商品 (包含圖片)
     * @param context
     * @param merch
     * @param images
     * @return
     */
    public static int insertMerch(Context context, Merch merch, List<byte[]> images) {
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "Merch";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "insert");
            jsonObject.addProperty("merch", new Gson().toJson(merch));
            // 有圖才上傳
            if (images != null) {


                JsonArray jsonArray = new JsonArray();
                int count = 0;
                for (byte[] image : images) {
                    JsonObject jsonObjectImg = new JsonObject();
                    jsonObjectImg.addProperty("image", Base64.encodeToString(image, Base64.DEFAULT));
                    jsonArray.add(jsonObjectImg);
                }
                jsonObject.add("arrImgBase64", jsonArray);
            }
            // requst
            String result = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));

            return Integer.parseInt(result);
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
            return -1;
        }
    }

    /**
     * 抓取商品的圖片
     * @param context
     * @param id
     * @return
     */
    public static List<byte[]> getMerchImgsById(Context context, int id) {
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "Merch";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getImages");
            jsonObject.addProperty("id", id);
            // requst (網址, 傳送資料)
            String jsonString = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));
            /** 匿名內部類別實作TypeToken，抓取 泛型 在呼叫方法 */
            Type listType = new TypeToken<List<byte[]>>(){}.getType();
            return new Gson().fromJson(jsonString, listType);
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    /**
     * 抓取商品的圖片 (限一張)
     * @param context
     * @param id
     * @return
     */
    public static byte[] getMerchImgById(Context context, int id) {
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "Merch";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getImage");
            jsonObject.addProperty("id", id);
            // requst (網址, 傳送資料)
            String jsonString = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));
            /** 匿名內部類別實作TypeToken，抓取 泛型 在呼叫方法 */
            Type listType = new TypeToken<byte[]>(){}.getType();
            return new Gson().fromJson(jsonString, listType);
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    /**
     * 更新商品 (包含圖片)
     * @param context
     * @param merch
     * @param images
     * @return
     */
    public static int updateMerch(Context context, Merch merch, List<byte[]> images) {
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "Merch";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "update");
            jsonObject.addProperty("merch", new Gson().toJson(merch));
            // 有圖才上傳
            if (images != null) {
                JsonArray jsonArray = new JsonArray();
                int count = 0;
                for (byte[] image : images) {
                    JsonObject jsonObjectImg = new JsonObject();
                    jsonObjectImg.addProperty("image", Base64.encodeToString(image, Base64.DEFAULT));
                    jsonArray.add(jsonObjectImg);
                }
                jsonObject.add("arrImgBase64", jsonArray);
            }
            // requst
            String result = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));

            return Integer.parseInt(result);
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
            return -1;
        }
    }

    /**
     * (以樂功能)
     */
    public static void getAllMerchByGroupId(Context context, int groupId) {
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "Merch";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAllByGroupIdId");
            jsonObject.addProperty("groupId", groupId);

            // requst
            String jsonString = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));
            /** 匿名內部類別實作TypeToken，抓取 泛型 在呼叫方法 */
            Type listType = new TypeToken<List<Merch>>(){}.getType();
            setLocalMerchs(new Gson().fromJson(jsonString, listType));
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
        }
    }
}
