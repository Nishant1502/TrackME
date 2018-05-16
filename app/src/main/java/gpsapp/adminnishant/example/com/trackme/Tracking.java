package gpsapp.adminnishant.example.com.trackme;

import android.icu.text.DecimalFormat;
import android.location.Location;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Tracking extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String email;
    DatabaseReference locations;
    double lat,lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        locations= FirebaseDatabase.getInstance().getReference("Locations");
        if(getIntent()!=null)
        {
            email=getIntent().getStringExtra("email");
            lat=getIntent().getDoubleExtra("lat",0);
            lng=getIntent().getDoubleExtra("lng",0);

        }
        if(!TextUtils.isEmpty(email))
            loadlocationforthisuser(email);
    }

    private void loadlocationforthisuser(String email) {
        Query user_location=locations.orderByChild("email").equalTo(email);

        user_location.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot postsnapshot:dataSnapshot.getChildren()) {
                    Track tracking = postsnapshot.getValue(Track.class);

                    //add marker to user location
                    LatLng friendlocation=new LatLng(Double.parseDouble(tracking.getLat()),Double.parseDouble(tracking.getLng()));

                    //create location from user coordinate
                    Location currentuser=new Location("");
                    currentuser.setLatitude(lat);
                    currentuser.setLongitude(lng);

                    //create location from friend coordinate

                    Location friend =new Location("");
                    friend.setLatitude(Double.parseDouble(tracking.getLat()));
                    friend.setLongitude(Double.parseDouble(tracking.getLng()));

                    //create function to calculate distance

                   //distance(currentuser,friend);

                    mMap.addMarker(new MarkerOptions()
                            .position(friendlocation)
                            .title(tracking.getEmail())
                            .snippet("Distance"+new DecimalFormat("#.#").format(distance(currentuser,friend)))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

                    );

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng),12.0f));
                }
                // create market for current user

                LatLng current= new LatLng(lat,lng);
                mMap.addMarker(new MarkerOptions().position(current).title(FirebaseAuth.getInstance().getCurrentUser().getEmail()));




            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private double distance(Location currentuser, Location friend) {
        double theta=currentuser.getLongitude() - friend.getLongitude();
        double dist= Math.sin(deg2rad(currentuser.getLatitude()))
                * Math.sin(deg2rad(friend.getLatitude()))
                * Math.cos(deg2rad(currentuser.getLatitude()))
                * Math.cos(deg2rad(friend.getLatitude()))
                * Math.cos(deg2rad(theta));
        dist=Math.acos(dist);
        dist=rad2deg(dist);
        dist=dist*60*1.515;
        return (dist);

    }

    private double rad2deg(double rad) {

        return (rad *180/Math.PI);
    }

    private double deg2rad(double deg) {

        return (deg * Math.PI/180.0);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


    }
}
