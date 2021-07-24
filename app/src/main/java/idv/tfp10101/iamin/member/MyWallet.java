package idv.tfp10101.iamin.member;

import android.os.Parcel;
import android.os.Parcelable;

import java.sql.Timestamp;
import java.util.List;

public class MyWallet {

	private int group_id;
	private String name;
	private String groupName;
	private int price;
	private int totalPrice;
	private int deliverStatus;
	private Timestamp startTime;
	private Timestamp updateTime;
	private String category;

	private List<MyWallet> groupDetail;

	public MyWallet(String name, int price) {
		this.name = name;
		this.price = price;
	}

	public MyWallet(int group_id, String groupName,int totoalPrice, int deliverStatus, Timestamp startTime,
					Timestamp updateTime, String category, List<MyWallet> groupDetail) {
		this.group_id = group_id;
		this.groupName = groupName;
		this.totalPrice = totalPrice;
		this.deliverStatus = deliverStatus;
		this.startTime = startTime;
		this.updateTime = updateTime;
		this.category = category;
		this.groupDetail = groupDetail;
	}

	public int getGroup_id() {
		return group_id;
	}

	public void setGroup_id(int group_id) {
		this.group_id = group_id;
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

	public int getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(int totoalPrice) {
		this.totalPrice = totalPrice;
	}

	public int getDeliverStatus() {
		return deliverStatus;
	}

	public void setDeliverStatus(int deliverStatus) {
		this.deliverStatus = deliverStatus;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public List<MyWallet> getGroupDetail() {
		return groupDetail;
	}

	public void setGroupDetail(List<MyWallet> groupDetail) {
		this.groupDetail = groupDetail;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
}
