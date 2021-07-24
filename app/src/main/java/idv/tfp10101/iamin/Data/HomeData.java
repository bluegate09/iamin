package idv.tfp10101.iamin.Data;

import java.sql.Timestamp;
import java.util.List;

import idv.tfp10101.iamin.group.Group;
import idv.tfp10101.iamin.merch.Merch;

public class HomeData {

    private Group group;
    private Float distance;

    public HomeData(Group group, Float distance) {
        this.group = group;
        this.distance = distance;
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
}
