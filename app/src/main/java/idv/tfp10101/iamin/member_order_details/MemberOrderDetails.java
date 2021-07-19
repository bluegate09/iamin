package idv.tfp10101.iamin.member_order_details;

// 會員訂單明細
public class MemberOrderDetails {
    private int memberOrderDetailsId; //
    private int memberOrderId; // 會員訂單ID
    private int merchId; // 商品ID
    private int quantity; // 數量
    private int format_total; // 樣式總價
    // 關聯資料
    private String name; // 商品名稱

    public MemberOrderDetails(int memberOrderDetailsId, int memberOrderId, int merchId, int quantity,
                              int format_total) {
        this.memberOrderDetailsId = memberOrderDetailsId;
        this.memberOrderId = memberOrderId;
        this.merchId = merchId;
        this.quantity = quantity;
        this.format_total = format_total;
    }

    public MemberOrderDetails(int memberOrderDetailsId, int memberOrderId, int merchId, int quantity, int format_total,
                              String name) {
        this.memberOrderDetailsId = memberOrderDetailsId;
        this.memberOrderId = memberOrderId;
        this.merchId = merchId;
        this.quantity = quantity;
        this.format_total = format_total;
        this.name = name;
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

    public int getFormat_total() {
        return format_total;
    }

    public void setFormat_total(int format_total) {
        this.format_total = format_total;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
