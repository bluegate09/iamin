package idv.tfp10101.iamin.member;

import java.sql.Timestamp;

public class MyIncome {

    int groupId;
    String category;
    int memberOrderId;
    int totalPrice;
    boolean deliverStatus;
    boolean ReceivePaymentStatus;
    Timestamp updateTime;
    String groupTitle;

    public MyIncome(int groupId, String category, int memberOrderId, int totalPrice, boolean deliverStatus,
                    boolean receivePaymentStatus, Timestamp updateTime,String groupTitle) {
        super();
        this.groupId = groupId;
        this.category = category;
        this.memberOrderId = memberOrderId;
        this.totalPrice = totalPrice;
        this.deliverStatus = deliverStatus;
        ReceivePaymentStatus = receivePaymentStatus;
        this.updateTime = updateTime;
        this.groupTitle = groupTitle;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getMemberOrderId() {
        return memberOrderId;
    }

    public void setMemberOrderId(int memberOrderId) {
        this.memberOrderId = memberOrderId;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public boolean isDeliverStatus() {
        return deliverStatus;
    }

    public void setDeliverStatus(boolean deliverStatus) {
        this.deliverStatus = deliverStatus;
    }

    public boolean isReceivePaymentStatus() {
        return ReceivePaymentStatus;
    }

    public void setReceivePaymentStatus(boolean receivePaymentStatus) {
        ReceivePaymentStatus = receivePaymentStatus;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public String getGroupTitle() {
        return groupTitle;
    }

    public void setGroupTitle(String groupTitle) {
        this.groupTitle = groupTitle;
    }

}
