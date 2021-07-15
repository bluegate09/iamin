package idv.tfp10101.iamin.member;

import java.io.Serializable;
import java.sql.Timestamp;

public class Member implements Serializable {

    private static Member memberInstance = null;

    private int id;
    private int follow_count;
    private double rating;
    private String uUId;
    private String email;
    private String password;
    private String nickname;
    private String phoneNumber;
    private byte[] image;
    private Timestamp startTime;
    private Timestamp updateTime;
    private Timestamp logoutTime;
    private Timestamp loginTime;
    private Timestamp deleteTime;
    private String FCM_token;

    //檢查是否有修改資料

    private boolean update;

    public static Member getInstance(){

        if(memberInstance == null){
            memberInstance = new Member();
        }
        return memberInstance;
    }

    private Member() {
    }

    public Member(int id, String uUid){
        this.id = id;
        this.uUId = uUid;
    }

    public Member(int id,int follow_count, double rating, String nickname) {
        this.id = id;
        this.follow_count = follow_count;
        this.rating = rating;
        this.nickname = nickname;
    }

    public Member(int id, int follow_count, double rating, String uUId, String email, String password, String nickname, String phoneNumber, Timestamp startTime, Timestamp updateTime, Timestamp logoutTime, Timestamp loginTime, Timestamp deleteTime, String FCM_token) {
        this.id = id;
        this.follow_count = follow_count;
        this.rating = rating;
        this.uUId = uUId;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.startTime = startTime;
        this.updateTime = updateTime;
        this.logoutTime = logoutTime;
        this.loginTime = loginTime;
        this.deleteTime = deleteTime;
        this.FCM_token = FCM_token;
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFollow_count() {
        return follow_count;
    }

    public void setFollow_count(int follow_count) {
        this.follow_count = follow_count;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getuUId() {
        return uUId;
    }

    public void setuUId(String uUId) {
        this.uUId = uUId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getUpdateDate() {
        return updateTime;
    }

    public void setUpdateDate(Timestamp updateDate) {
        this.updateTime = updateDate;
    }

    public Timestamp getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Timestamp loginTime) {
        this.loginTime = loginTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public Timestamp getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(Timestamp logoutTime) {
        this.logoutTime = logoutTime;
    }

    public Timestamp getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(Timestamp deleteTime) {
        this.deleteTime = deleteTime;
    }

    public String getFCM_token() {
        return FCM_token;
    }

    public void setFCM_token(String fCM_token) {
        FCM_token = fCM_token;
    }

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

}