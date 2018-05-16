package gpsapp.adminnishant.example.com.trackme;

/**
 * Created by Admin on 15-May-18.
 */

public class Track {
    private String email,uid,lat,lng;



    public Track(String email, String uid, String lat, String lng) {
        this.email = email;
        this.uid = uid;
        this.lat = lat;
        this.lng = lng;
    }



    public Track()
    {

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }
}
