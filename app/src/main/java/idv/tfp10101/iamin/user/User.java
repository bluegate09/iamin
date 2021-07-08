package idv.tfp10101.iamin.user;

import java.io.Serializable;

public class User implements Serializable {
    String id, profilepic, userName, email, password, uuid, lastMessage, token;

    public User() {
    }

    public User(String id, String profilepic, String userName, String email, String password, String uuid, String lastMessage) {
        this.id = id;
        this.profilepic = profilepic;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.uuid = uuid;
        this.lastMessage = lastMessage;
    }

    // token
    public User(String token) {
        this.token = token;
    }

    // register
    public User(String id, String userName, String email, String password, String uuid) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.uuid = uuid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProfilepic() {
        return profilepic;
    }

    public void setProfilepic(String profilepic) {
        this.profilepic = profilepic;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
