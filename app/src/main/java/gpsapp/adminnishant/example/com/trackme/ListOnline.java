package gpsapp.adminnishant.example.com.trackme;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ListOnline extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,LocationListener{


    //Firebase
    DatabaseReference onlineref,currentref,userinforef,counterref,currentuserref,locations;
    FirebaseRecyclerAdapter<User,ListOnlineViewHolder> adapter;

    //view
    RecyclerView listonline;
    RecyclerView.LayoutManager layoutmanager;

    //location
    private static final int MY_PERMISSION_REQUEST_CODE=7171;
    private static final int PLAY_SERVICES_RES_REQUEST=7172;
    private LocationRequest mlocationrequest;
    private GoogleApiClient mgoogleapiclient;
    private Location mlastlocation;

    private static int UPDATE_INTERVAL=5000;
    private static int FASTEST_INTERVAL=3000;
    private static int DISTANCE=10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_online);

        listonline=(RecyclerView)findViewById(R.id.listonline);
        listonline.setHasFixedSize(true);
        layoutmanager=new LinearLayoutManager(this);
        listonline.setLayoutManager(layoutmanager);

        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Presence system");
        setSupportActionBar(toolbar);


        locations=FirebaseDatabase.getInstance().getReference("Locations");
        onlineref= FirebaseDatabase.getInstance().getReference().child(".info/connected");
        counterref=FirebaseDatabase.getInstance().getReference("lastonline");
        currentuserref=FirebaseDatabase.getInstance().getReference("lastonline")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        

        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION )!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            },MY_PERMISSION_REQUEST_CODE );


        }
        else {

            if (checkPlayService())
            {
                buildGoogleApiClient();
                createlocationrequest();
                displaylocation();
            }

        }
            setupsystem();
        updatelist();

    }

    private void displaylocation() {

        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION )!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            return;

        }

        mlastlocation=LocationServices.FusedLocationApi.getLastLocation(mgoogleapiclient);
        if(mlastlocation!=null)
        {

            locations.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(new Track(FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                            FirebaseAuth.getInstance().getCurrentUser().getUid(),
                            String.valueOf(mlastlocation.getLatitude()),
                            String.valueOf(mlastlocation.getLongitude())));
            //update to firebase
        }
        else {

            // Toast.makeText(this, "Couldn't get the location", Toast.LENGTH_SHORT).show();
            Log.d("TEST","Couldnt load location");

        }
    }

    private void createlocationrequest() {
        mlocationrequest=new LocationRequest();
        mlocationrequest.setInterval(UPDATE_INTERVAL);
        mlocationrequest.setFastestInterval(FASTEST_INTERVAL);
        mlocationrequest.setSmallestDisplacement(DISTANCE);
        mlocationrequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildGoogleApiClient() {

        mgoogleapiclient=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mgoogleapiclient.connect();

    }

    private boolean checkPlayService() {
        int resultcode= GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultcode!= ConnectionResult.SUCCESS)
        {
            if(GooglePlayServicesUtil.isUserRecoverableError(resultcode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultcode,this,PLAY_SERVICES_RES_REQUEST).show();
            }
            else
            {
                Toast.makeText(this, "Device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;

    }

    private void updatelist() {
        adapter=new FirebaseRecyclerAdapter<User, ListOnlineViewHolder>(
                User.class,
                R.layout.user_layout,
                ListOnlineViewHolder.class,
                counterref
        ) {

            @Override
            protected void populateViewHolder(ListOnlineViewHolder viewHolder, final User model, int position) {
                viewHolder.txtemail.setText(model.getEmail());

                //implementing click of recyclerview
                viewHolder.itemclicklistener=new itemClickListener(){
                    @Override
                    public void onClick(View view, int position){
                        if(!model.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail()))
                        {
                            Intent map=new Intent(ListOnline.this,Tracking.class);
                            map.putExtra("email",model.getEmail());
                            map.putExtra("lat",mlastlocation.getLatitude());
                            map.putExtra("lng",mlastlocation.getLongitude());
                            startActivity(map);
                        }
                    }


                };

            }
        };
        adapter.notifyDataSetChanged();
        listonline.setAdapter(adapter);
    }

    private void setupsystem() {
        onlineref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue(Boolean.class))
                {
                    currentuserref.onDisconnect().removeValue();
                    counterref.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(new User(FirebaseAuth.getInstance().getCurrentUser().getEmail(),"online"));

                    adapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        onlineref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot postsnapshot:dataSnapshot.getChildren())
                {
                    User user=postsnapshot.getValue(User.class);
                    Log.d("Log",""+user.getEmail()+"is"+user.getStatus());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.action_join:
                counterref.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .setValue(new User(FirebaseAuth.getInstance().getCurrentUser().getEmail(),"online"));

                break;

            case R.id.action_logout:
                currentuserref.removeValue();
                break;



        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode)
        {
            case MY_PERMISSION_REQUEST_CODE:
            {
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    if(checkPlayService())
                    {
                        buildGoogleApiClient();
                        createlocationrequest();
                        displaylocation();
                    }
                }
            }
            break;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displaylocation();
        startLocationUpdates();

    }

    private void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION )!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            return;

        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mgoogleapiclient,mlocationrequest,this);

    }

    @Override
    public void onConnectionSuspended(int i) {

        mgoogleapiclient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mgoogleapiclient!=null)
            mgoogleapiclient.connect();
    }


    @Override
    protected void onStop() {
        if(mgoogleapiclient!=null)
            mgoogleapiclient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        checkPlayService();
    }

    @Override
    public void onLocationChanged(Location location) {

        mlastlocation=location;
        displaylocation();

    }
}
