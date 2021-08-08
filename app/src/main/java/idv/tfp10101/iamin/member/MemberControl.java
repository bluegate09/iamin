package idv.tfp10101.iamin.member;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import idv.tfp10101.iamin.R;
import idv.tfp10101.iamin.Rating.Rating;
import idv.tfp10101.iamin.group.Group;
import idv.tfp10101.iamin.member_order.MemberOrder;
import idv.tfp10101.iamin.network.RemoteAccess;

public class MemberControl {


    private static Member memberInstance;
    public static Member getInstance(){
        if(memberInstance == null){
            memberInstance = new Member();
        }
        return memberInstance;
    }

    public static void setMember(Member member){
        if(memberInstance == null){
            memberInstance = new Member();
        }
        memberInstance = member;
    }

    /**
     *
     * @param context context
     * @param member member bean
     * @param value 欲執行的action
     * @return json String
     */
    public static String memberRemoteAccess(Context context , Member member, String value) {
        if (RemoteAccess.networkConnected(context)) {

            String url = RemoteAccess.URL_SERVER + "memberController";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", value);
            jsonObject.addProperty("member", new Gson().toJson(member));

            return RemoteAccess.getRemoteData(url, jsonObject.toString());
        } else {
            Toast.makeText(context, "沒有網路", Toast.LENGTH_SHORT).show();
            return "";
        }
    }

    /**
     * 使用賣家ID抓取賣家資料
     * @param context
     * @param seller
     */
    public static Member getsellerByMemberId(Context context, Member seller) {
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "memberController";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "findById");
            jsonObject.addProperty("member", new Gson().toJson(seller));

            // requst
            String jsonString = RemoteAccess.getRemoteData(url, jsonObject.toString());
           return new Gson().fromJson(jsonString, Member.class);
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    /**
     * 使用賣家ID抓取賣家資料
     * @param context
     * @param seller
     */
    public static Bitmap getsellerimageByMemberId(Context context, Member seller) {
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "memberController";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getImage");
            jsonObject.addProperty("member", new Gson().toJson(seller));

            return RemoteAccess.getRemoteImage(url, jsonObject.toString());
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public static Bitmap getMemberImageByMemberId(Context context, Member member, ExecutorService executor){
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "memberController";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getImage");
            jsonObject.addProperty("member", new Gson().toJson(member));

            return RemoteAccess.getRemoteImage(url, jsonObject.toString(), executor);
        }else{
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    /**
     * 使用賣家ID抓取賣家資料
     * @param context
     * @param seller
     */
    public static void followed(Context context, int buyer, int seller) {
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "memberController";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "follow");
            jsonObject.addProperty("member", new Gson().toJson(new Member()));
            jsonObject.addProperty("buyer_id", buyer);
            jsonObject.addProperty("follwer_id", seller);
            RemoteAccess.getRemoteData(url, jsonObject.toString());
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 使用賣家ID抓取賣家資料
     * @param context
     * @param seller
     */
    public static int chackfollowed(Context context, int buyer, int seller) {
        // 如果有網路，就進行 request
        if (RemoteAccess.networkConnected(context)) {
            // 網址 ＆ Action
            String url = RemoteAccess.URL_SERVER + "memberController";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "chackfollow");
            jsonObject.addProperty("member", new Gson().toJson(new Member()));
            jsonObject.addProperty("buyer_id", buyer);
            jsonObject.addProperty("follwer_id", seller);
            String result = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));
            return Integer.parseInt(result);
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
        }
        return -1;
    }

    /**
     * 給rating物件去更新db資料
     * @param context
     * @param rating
     */
    public static void submitRating(Context context, Rating rating){
        if (RemoteAccess.networkConnected(context)) {
            String url = RemoteAccess.URL_SERVER + "Rating";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action","insertRatingAndUpdateMember");
            jsonObject.addProperty("rating",new Gson().toJson(rating));
            RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));
        }else {
        Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     *
     * 檢查是否評價過
     * @param context
     * @param memberOrderID
     */
    public static Rating checkIsRate(Context context, int memberOrderID){
        if (RemoteAccess.networkConnected(context)) {
            String url = RemoteAccess.URL_SERVER + "Rating";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action","checkIsRated");
            jsonObject.addProperty("memberOrderID",new Gson().toJson(memberOrderID));
            String jsonRating =  RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));
            return new Gson().fromJson(jsonRating , Rating.class);
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
            return null;
        }
    }


        /**
         * 輸入member_id取得rating
         * @param context
         * @param member_id
         * @return
         */

    public static List<Rating> getRating(Context context, int member_id){
        List<Rating> ratings = new ArrayList<>();
        Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy h:mm:ss a").create();
        if (RemoteAccess.networkConnected(context)) {
            String url = RemoteAccess.URL_SERVER + "Rating";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action","getAllRatingByMemberId");
            jsonObject.addProperty("member_id",new Gson().toJson(member_id));

            String jsonIn = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));
            Type listType = new TypeToken<List<Rating>>() {}.getType();
            ratings = gson.fromJson(jsonIn,listType);

        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
        }
        return ratings;
    }

    /**
     *
     * 電話號碼驗證成功更新
     * @param context
     * @param member
     */
    public static void updatePhoneNumber(Context context,Member member){
        Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy h:mm:ss a").create();
        if (RemoteAccess.networkConnected(context)) {
            String url = RemoteAccess.URL_SERVER + "memberController";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action","phoneAuthSuccess");
            jsonObject.addProperty("member",new Gson().toJson(member));
            RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));
        }else {
            Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 取得所有團購
     */
     public static List<MemberOrder> getMyMemberOrder(Context context,Member member){
         List<MemberOrder> memberOrders = new ArrayList<>();
         Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy h:mm:ss a").create();
         if (RemoteAccess.networkConnected(context)) {
             // 網址 ＆ Action
             String url = RemoteAccess.URL_SERVER + "memberController";
             JsonObject jsonObject = new JsonObject();
             jsonObject.addProperty("action", "getMyMemberOrder");
             jsonObject.addProperty("member", new Gson().toJson(member));

             String jsonIn = RemoteAccess.getRemoteData(url, new Gson().toJson(jsonObject));
             Type listType = new TypeToken<List<MemberOrder>>() {}.getType();
             memberOrders = gson.fromJson(jsonIn,listType);

         }else{
             Toast.makeText(context, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
         }
         return memberOrders;
     }

    /**
     *當前使用者座標
     */
    private static MemberCoordinate coordinateInstance;
    public static MemberCoordinate getCoordinateInstance(){
        if(coordinateInstance == null){
            coordinateInstance = new MemberCoordinate(0.0,0.0);
        }
        return coordinateInstance;
    }

    public static void setMemberCoordinate(MemberCoordinate memberCoordinate){
        if(coordinateInstance == null){
            coordinateInstance = new MemberCoordinate(0.0,0.0);
        }
        coordinateInstance = memberCoordinate;
    }

    /**
     * 存座標的class
     */
    public static class MemberCoordinate{
        private double latitude; // 緯度
        private double longtitude; // 經度

        public MemberCoordinate(double latitude, double longtitude) {
            this.latitude = latitude;
            this.longtitude = longtitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongtitude() {
            return longtitude;
        }

        public void setLongtitude(double longtitude) {
            this.longtitude = longtitude;
        }
    }
}
