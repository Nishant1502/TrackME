package gpsapp.adminnishant.example.com.trackme;

/**
 * Created by Admin on 15-May-18.
 */

public class User {
    private String email,status;
    public  User()
    {

    }

    public User(String email, String status) {
        this.email = email;
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
