package idv.tfp10101.iamin.merch;

/**
 * 新增團購時，所選擇的商品Data
 */
public class MerchSelect {
    private Merch merch;
    private Boolean isSelect;

    public MerchSelect(Merch merch, Boolean isSelect) {
        this.merch = merch;
        this.isSelect = isSelect;
    }

    public Merch getMerch() {
        return merch;
    }

    public void setMerch(Merch merch) {
        this.merch = merch;
    }

    public Boolean getSelect() {
        return isSelect;
    }

    public void setSelect(Boolean select) {
        isSelect = select;
    }
}
