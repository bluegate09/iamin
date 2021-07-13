package idv.tfp10101.iamin.member;

import java.sql.Timestamp;
import java.util.List;

public class MyWallet {

	private int group_id;
	private String name;
	private int price;
	private int totoalPrice;
	private int deliverStatus;
	private Timestamp startTime;
	private Timestamp updateTime;
	private String category;

	private List<MyWallet> groupDetail;

	public MyWallet(String name, int price) {
		super();
		this.name = name;
		this.price = price;
	}

	public MyWallet(int group_id, String name, int price, int totoalPrice, int deliverStatus, Timestamp startTime,
			Timestamp updateTime, String category) {
		super();
		this.group_id = group_id;
		this.name = name;
		this.price = price;
		this.totoalPrice = totoalPrice;
		this.deliverStatus = deliverStatus;
		this.startTime = startTime;
		this.updateTime = updateTime;
		this.category = category;
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

	public int getTotoalPrice() {
		return totoalPrice;
	}

	public void setTotoalPrice(int totoalPrice) {
		this.totoalPrice = totoalPrice;
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
	
	
	
}
