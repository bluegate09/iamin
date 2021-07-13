package idv.tfp10101.iamin.group;

public class GroupCategory {
    private int groupCategoryId; // ID
    private String category; // 類別

    public GroupCategory(int groupCategoryId, String category) {
        super();
        this.groupCategoryId = groupCategoryId;
        this.category = category;
    }

    public int getGroupCategoryId() {
        return groupCategoryId;
    }

    public void setGroupCategoryId(int groupCategoryId) {
        this.groupCategoryId = groupCategoryId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
