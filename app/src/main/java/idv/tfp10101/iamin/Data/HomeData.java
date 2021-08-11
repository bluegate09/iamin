package idv.tfp10101.iamin.Data;

import com.google.android.gms.maps.model.LatLng;

import java.sql.Timestamp;
import java.util.List;

import idv.tfp10101.iamin.group.Group;
import idv.tfp10101.iamin.merch.Merch;

public class HomeData {

    private Group group;
    private Float distance;
    private LatLng latLng;

    public HomeData(Group group, Float distance) {
        this.group = group;
        this.distance = distance;
    }

    public HomeData(Group group, Float distance, LatLng latLng) {
        this.group = group;
        this.distance = distance;
        this.latLng = latLng;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Float getDistance() {
        return distance;
    }

    public void setDistance(Float distance) {
        this.distance = distance;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
}
