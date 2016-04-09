package boom.projectrm;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.geofire.GeoFire;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;

public class MainActivity extends FragmentActivity {
    protected static final String FIREBASE_URL_PREFIX = "https://boomerango.firebaseio.com/";
    public static Firebase fbdb;
    public static GeoFire geofbdb;
    private GoogleMap mMap;
    private ViewPager viewpager;



    public static FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();
        Firebase.setAndroidContext(this);
        fbdb = new Firebase(FIREBASE_URL_PREFIX + "imagesV2");
        geofbdb = new GeoFire(new Firebase(FIREBASE_URL_PREFIX + "geoimage"));

        viewpager = (ViewPager)findViewById(R.id.pager);


        PagerAdapter pAdapter = new PagerAdapter(fragmentManager);
        viewpager.setAdapter(pAdapter);

    }

}
