package idv.tfp10101.iamin.group;

public class GroupBlockade {
    private int groupBlockadeId;
    private int groupId;
    private int memberId;
    private String groupName;
    private String reason;
    private boolean notify;

    public GroupBlockade(int groupBlockadeId, int groupId, int memberId, String groupName, String reason,
                         boolean notify) {
        super();
        this.groupBlockadeId = groupBlockadeId;
        this.groupId = groupId;
        this.memberId = memberId;
        this.groupName = groupName;
        this.reason = reason;
        this.notify = notify;
    }

    public int getGroupBlockadeId() {
        return groupBlockadeId;
    }

    public void setGroupBlockadeId(int groupBlockadeId) {
        this.groupBlockadeId = groupBlockadeId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }
}
