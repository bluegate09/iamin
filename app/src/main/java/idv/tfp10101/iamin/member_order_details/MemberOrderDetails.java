package idv.tfp10101.iamin.member_order_details;

public class MemberOrderDetails {
    private int memberOrderDetailsId; //
    private int memberOrderId; // 會員訂單ID
    private int merchId; // 商品ID
    private int quantity; // 數量
    private String format_total; // 樣式總價

    public MemberOrderDetails(int memberOrderDetailsId, int memberOrderId, int merchId, int quantity,
                              String format_total) {
        super();
        this.memberOrderDetailsId = memberOrderDetailsId;
        this.memberOrderId = memberOrderId;
        this.merchId = merchId;
        this.quantity = quantity;
        this.format_total = format_total;
    }

    public int getMemberOrderDetailsId() {
        return memberOrderDetailsId;
    }

    public void setMemberOrderDetailsId(int memberOrderDetailsId) {
        this.memberOrderDetailsId = memberOrderDetailsId;
    }

    public int getMemberOrderId() {
        return memberOrderId;
    }

    public void setMemberOrderId(int memberOrderId) {
        this.memberOrderId = memberOrderId;
    }

    public int getMerchId() {
        return merchId;
    }

    public void setMerchId(int merchId) {
        this.merchId = merchId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getFormat_total() {
        return format_total;
    }

    public void setFormat_total(String format_total) {
        this.format_total = format_total;
    }
}