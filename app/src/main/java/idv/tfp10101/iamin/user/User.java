package idv.tfp10101.iamin.user;

import java.io.Serializable;

public class User implements Serializable {
    private static User userInstance = null;
    private String id;
    private String name;
    private String email;
    private String password;
    private String phonenumber;
    private String imagePath;
    private String token;

    public static User getInstance(){

        if(userInstance == null){
            userInstance = new User();

        }
        return userInstance;
    }

    public User() {


    }

    public User(String id, String name, String email, String password, String phonenumber, String imagePath, String token) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phonenumber = phonenumber;
        this.imagePath = imagePath;
        this.token = token;
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
