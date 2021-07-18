package idv.tfp10101.iamin.member_order;

public class MemberOrder {
    private int memberOrderId;
    private int memberId; // 會員ID
    private int groupId; // 團購ID
    private int payentMethod; // 收款方式 (1.面交 2.信用卡)
    private int total; // 訂單金額
    private boolean receivePaymentStatus; // 收款狀態
    private boolean deliverStatus; // 發貨狀態

    public MemberOrder(int memberOrderId, int memberId, int groupId, int payentMethod, int total,
                       boolean receivePaymentStatus, boolean deliverStatus) {
        super();
        this.memberOrderId = memberOrderId;
        this.memberId = memberId;
        this.groupId = groupId;
        this.payentMethod = payentMethod;
        this.total = total;
        this.receivePaymentStatus = receivePaymentStatus;
        this.deliverStatus = deliverStatus;
    }

    public int getMemberOrderId() {
        return memberOrderId;
    }

    public void setMemberOrderId(int memberOrderId) {
        this.memberOrderId = memberOrderId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getPayentMethod() {
        return payentMethod;
    }

    public void setPayentMethod(int payentMethod) {
        this.payentMethod = payentMethod;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public boolean isReceivePaymentStatus() {
        return receivePaymentStatus;
    }

    public void setReceivePaymentStatus(boolean receivePaymentStatus) {
        this.receivePaymentStatus = receivePaymentStatus;
    }

    public boolean isDeliverStatus() {
        return deliverStatus;
    }

    public void setDeliverStatus(boolean deliverStatus) {
        this.deliverStatus = deliverStatus;
    }
}
