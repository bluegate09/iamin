package idv.tfp10101.iamin.location;

import java.sql.Timestamp;

public class Location {
    private int locationId; // 地址 ID
    private int groupId; // 團購ID
    private String address; // 地址
    private double latitude; // 緯度
    private double longtitude; // 經度
    private Timestamp pickup_time; // 取貨時間

    public Location(int groupId, double latitude, double longtitude) {
        this(0, groupId, null, latitude, longtitude, null);
    }
    public Location(int locationId, int groupId, String address, double latitude, double longtitude,
                    Timestamp pickup_time) {
        super();
        this.locationId = locationId;
        this.groupId = groupId;
        this.address = address;
        this.latitude = latitude;
        this.longtitude = longtitude;
        this.pickup_time = pickup_time;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
    }

    public Timestamp getPickup_time() {
        return pickup_time;
    }

    public void setPickup_time(Timestamp pickup_time) {
        this.pickup_time = pickup_time;
    }
}
