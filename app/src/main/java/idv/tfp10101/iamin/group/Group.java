package idv.tfp10101.iamin.group;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import idv.tfp10101.iamin.merch.Merch;

public class Group implements Serializable {
    private int groupId;
    private int memberId;
    private String name; // 標題
    private int progress; // 目標進度
    private int goal; // 目標
    private int categoryId; // 類別ID
    private String groupItem; // 團購項目
    private String contactNumber; // 聯絡電話
    private int paymentMethod; // 收款方式 (1.面交 2.信用卡 3.兩者皆可)
    private int groupStatus; // 團購狀態 (1.揪團中 2.達標 3.失敗or放棄)
    private String caution; // 注意事項
    private Boolean privacyFlag; // 隱私設定
    private int totalAmount; // 總金額
    private int amount; // 目前收款金額
    private int conditionCount; // 停單條件(份數)
    private Timestamp conditionTime; // 停單條件(時間)
    // 關聯資料
    private String category; // 類別名稱
    private List<Merch> merchs; // 商品列表

    public Group(int groupId, int memberId, String name, int progress, int goal, int categoryId, String groupItem,
                 String contactNumber, int paymentMethod, int groupStatus, String caution,
                 Boolean privacyFlag, int totalAmount, int amount, int conditionCount,
                 Timestamp conditionTime) {

        this(groupId, memberId, name, progress, goal, categoryId, groupItem,
                contactNumber, paymentMethod, groupStatus,
                caution, privacyFlag, totalAmount, amount,
                conditionCount, conditionTime, null, null);

    }

    public Group(int groupId, int memberId, String name, int progress, int goal, int categoryId, String groupItem,
                 String contactNumber, int paymentMethod, int groupStatus, String caution,
                 Boolean privacyFlag, int totalAmount, int amount, int conditionCount,
                 Timestamp conditionTime, String category, List<Merch> merchs) {
        this.groupId = groupId;
        this.memberId = memberId;
        this.name = name;
        this.progress = progress;
        this.goal = goal;
        this.categoryId = categoryId;
        this.groupItem = groupItem;
        this.contactNumber = contactNumber;
        this.paymentMethod = paymentMethod;
        this.groupStatus = groupStatus;
        this.caution = caution;
        this.privacyFlag = privacyFlag;
        this.totalAmount = totalAmount;
        this.amount = amount;
        this.conditionCount = conditionCount;
        this.conditionTime = conditionTime;
        this.category = category;
        this.merchs = merchs;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getGoal() {
        return goal;
    }

    public void setGoal(int goal) {
        this.goal = goal;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getGroupItem() {
        return groupItem;
    }

    public void setGroupItem(String groupItem) {
        this.groupItem = groupItem;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public int getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(int paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public int getGroupStatus() {
        return groupStatus;
    }

    public void setGroupStatus(int groupStatus) {
        this.groupStatus = groupStatus;
    }

    public String getCaution() {
        return caution;
    }

    public void setCaution(String caution) {
        this.caution = caution;
    }

    public Boolean getPrivacyFlag() {
        return privacyFlag;
    }

    public void setPrivacyFlag(Boolean privacyFlag) {
        this.privacyFlag = privacyFlag;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getConditionCount() {
        return conditionCount;
    }

    public void setConditionCount(int conditionCount) {
        this.conditionCount = conditionCount;
    }

    public Timestamp getConditionTime() {
        return conditionTime;
    }

    public void setConditionTime(Timestamp conditionTime) {
        this.conditionTime = conditionTime;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<Merch> getMerchs() {
        return merchs;
    }

    public void setMerchs(List<Merch> merchs) {
        this.merchs = merchs;
    }
}
