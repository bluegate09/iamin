package idv.tfp10101.iamin.merch;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class Merch implements Serializable {
    private int merchId;
    private int memberId; // 會員ID
    private String name; // 名稱
    private int price; // 價格
    private String merchDesc; // 商品說明

    public Merch(int merchId, int memberId, String name, int price, String merchDesc) {
        this.merchId = merchId;
        this.memberId = memberId;
        this.name = name;
        this.price = price;
        this.merchDesc = merchDesc;
    }

    public int getMerchId() {
        return merchId;
    }

    public void setMerchId(int merchId) {
        this.merchId = merchId;
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

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getMerchDesc() {
        return merchDesc;
    }

    public void setMerchDesc(String merchDesc) {
        this.merchDesc = merchDesc;
    }

//    @Override
//    public int hashCode() {
//        return memberId + merchId;
//    }
//
//    @Override
//    public boolean equals(@Nullable Object obj) {
//        if (obj instanceof Merch) {
//            return memberId == ((Merch) obj).memberId && merchId == ((Merch) obj).merchId;
//        } else {
//            return super.equals(obj);
//        }
//    }
}
