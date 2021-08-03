package idv.tfp10101.iamin.member;

import android.os.Parcel;
import android.os.Parcelable;

import java.sql.Timestamp;
import java.util.List;

public class MyWallet {

	int memberOrderDetailsId;
	int memberOrderId;
	String groupTitle;
	String category;
	String merchTitle;
	int merchPrice;
	int quantity;
	int totalPrice;
	String merchDesc;
	Timestamp updateTime;

	public MyWallet(int memberOrderDetailsId, int memberOrderId, String groupTitle, String category, String merchTitle,
					int merchPrice, int quantity, int totalPrice, String merchDesc, Timestamp updateTime) {
		super();
		this.memberOrderDetailsId = memberOrderDetailsId;
		this.memberOrderId = memberOrderId;
		this.groupTitle = groupTitle;
		this.category = category;
		this.merchTitle = merchTitle;
		this.merchPrice = merchPrice;
		this.quantity = quantity;
		this.totalPrice = totalPrice;
		this.merchDesc = merchDesc;
		this.updateTime = updateTime;
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
	public String getGroupTitle() {
		return groupTitle;
	}
	public void setGroupTitle(String groupTitle) {
		this.groupTitle = groupTitle;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getMerchTitle() {
		return merchTitle;
	}
	public void setMerchTitle(String merchTitle) {
		this.merchTitle = merchTitle;
	}
	public int getMerchPrice() {
		return merchPrice;
	}
	public void setMerchPrice(int merchPrice) {
		this.merchPrice = merchPrice;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public int getTotalPrice() {
		return totalPrice;
	}
	public void setTotalPrice(int totalPrice) {
		this.totalPrice = totalPrice;
	}
	public String getMerchDesc() {
		return merchDesc;
	}
	public void setMerchDesc(String merchDesc) {
		this.merchDesc = merchDesc;
	}
	public Timestamp getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

}


