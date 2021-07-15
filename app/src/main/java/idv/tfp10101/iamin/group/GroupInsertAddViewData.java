package idv.tfp10101.iamin.group;

import java.util.ArrayList;
import java.util.List;

public class GroupInsertAddViewData {
    // 新增團購時的資料存放
    private static GroupInsertAddViewData groupInsertAddViewData;
    public static GroupInsertAddViewData getGroupInsertAddViewData() {
        if (groupInsertAddViewData == null) {
            groupInsertAddViewData = new GroupInsertAddViewData();
        }
        return groupInsertAddViewData;
    }
    // 清空
    public static void remove() {
        groupInsertAddViewData = null;
    }

    public GroupInsertAddViewData() {
        merchsId = new ArrayList<>();
        merchName = new ArrayList<>();
        merchPrice = new ArrayList<>();
        latLngs = new ArrayList<>();
        locations = new ArrayList<>();
        lats = new ArrayList<>();
        lngs = new ArrayList<>();
    }

    private ArrayList<Integer> merchsId; // 商品ID清單-存入DB用
    private List<String> merchName; // (顯示用)
    private List<Integer> merchPrice; // (顯示用)

    private List<Double[]> latLngs; // 緯經度-存入DB用
    private ArrayList<String> locations; // (顯示用)
    private List<Double> lats; // (顯示用)
    private List<Double> lngs; // (顯示用)

    public ArrayList<Integer> MerchsId() {
        return merchsId;
    }

    public void setMerchsId(ArrayList<Integer> merchsId) {
        this.merchsId = merchsId;
    }

    public List<String> MerchName() {
        return merchName;
    }

    public void setMerchName(List<String> merchName) {
        this.merchName = merchName;
    }

    public List<Integer> MerchPrice() {
        return merchPrice;
    }

    public void setMerchPrice(List<Integer> merchPrice) {
        this.merchPrice = merchPrice;
    }

    public List<Double[]> LatLngs() {
        return latLngs;
    }

//    public void setLatLngs(List<Double[]> latLngs) {
//        this.latLngs = latLngs;
//    }

    public ArrayList<String> Locations() {
        return locations;
    }

    public void setLocations(ArrayList<String> locations) {
        this.locations = locations;
    }

    public List<Double> Lats() {
        return lats;
    }

//    public void setLats(List<Double> lats) {
//        this.lats = lats;
//    }

    public List<Double> Lngs() {
        return lngs;
    }

//    public void setLngs(List<Double> lngs) {
//        this.lngs = lngs;
//    }
}
