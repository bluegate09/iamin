package idv.tfp10101.iamin.Report;

public class Report {

    private int member_id;  //檢舉人id
    private int reported_member_id; //被檢舉人id
    private String report_item;  	//檢舉項目
    private String report_message;	//檢舉說明

    public Report(int member_id, int reported_member_id, String report_item, String report_message) {
        this.member_id = member_id;
        this.reported_member_id = reported_member_id;
        this.report_item = report_item;
        this.report_message = report_message;
    }

    public int getMember_id() {
        return member_id;
    }

    public void setMember_id(int member_id) {
        this.member_id = member_id;
    }

    public int getReported_member_id() {
        return reported_member_id;
    }

    public void setReported_member_id(int reported_member_id) {
        this.reported_member_id = reported_member_id;
    }

    public String getReport_item() {
        return report_item;
    }

    public void setReport_item(String report_item) {
        this.report_item = report_item;
    }

    public String getReport_message() {
        return report_message;
    }

    public void setReport_message(String report_message) {
        this.report_message = report_message;
    }
}
