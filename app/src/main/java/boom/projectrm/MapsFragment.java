package boom.projectrm;

import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.SupportErrorDialogFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;

/**
 * Created by Kim on 3/20/2016.
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback, GeoQueryEventListener,  GoogleMap.OnCameraChangeListener {
    private static final GeoLocation STARTUP_CENTER = new GeoLocation(51.03, 114.04);
    private static final int STARTUP_ZOOM_LEVEL = 14;
    private static final String FIREBASE_URL_PREFIX = "https://boomerango.firebaseio.com/";

    private SupportMapFragment fragment;
    private Circle searchArea;
    private GoogleMap mMap;
    private GeoFire geofbdb;
    private GeoQuery query;

    private Map<String, Marker> markers;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_one, container, false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //FragmentManager fm = getFragmentManager();
        fragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.location_map);
        fragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));

        setUpMap();
    }

    /**
     * When the map starts up create listener for geofire
     */
    @Override
    public void onStart() {
        super.onStart();

    }

    /**
     * When the map ends clear listener geofire listener and markers
     */
    @Override
    public void onStop() {
        super.onStart();
        query.removeAllListeners();

        for(Marker marker: markers.values()) {
            marker.remove();
        }
        markers.clear();
    }

    /**
     * Add the markers to the map
     */
    private void setUpMap() {
        // convert the startup center from geolocation to latlng
        LatLng startupCenter = new LatLng(STARTUP_CENTER.latitude, STARTUP_CENTER.longitude);

        // search circle that markers will appear in
        searchArea = mMap.addCircle(new CircleOptions().center(startupCenter).radius(1000));
        searchArea.setFillColor(Color.argb(66, 255, 219, 164));
        searchArea.setStrokeColor(Color.argb(66, 255, 162, 47));

        // set the camera for the map
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startupCenter, STARTUP_ZOOM_LEVEL));
        mMap.setOnCameraChangeListener(this);

        Firebase.setAndroidContext(getContext());

        geofbdb = new GeoFire(new Firebase(FIREBASE_URL_PREFIX + "imagesV2"));
        query = geofbdb.queryAtLocation(STARTUP_CENTER, 1);

        markers = new HashMap<String, Marker>();

        query.addGeoQueryEventListener(this);
    }

    public void onMapClick(LatLng latLng) {

    }

    /**
     * Add markers for new values in the search area
     * @param key
     * @param location
     */
    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)));
        markers.put(key, marker);
    }

    /**
     * Remove the markers outside the search area
     * @param key
     */
    @Override
    public void onKeyExited(String key) {
        Marker marker = markers.get(key);
        if (marker != null) {
            marker.remove();
            markers.remove(key);
        }
    }

    /**
     * Optional: move the marker if the gps location changed
     * @param key
     * @param location
     */
    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        Marker marker = this.markers.get(key);
        if (marker !=  null) {
            marker.setPosition(new LatLng(location.latitude, location.longitude));
        }
    }

    @Override
    public void onGeoQueryReady() {
    }

    @Override
    public void onGeoQueryError(FirebaseError error) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Error")
                .setMessage("Error with querying the GeoFire database " + error.getMessage())
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    /**
     * custom method for handling marker motion for older apis
     * @param cameraPosition
     */
    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        LatLng center = cameraPosition.target;
        double radius = zoomLevelToRadius(cameraPosition.zoom);
        searchArea.setCenter(center);
        searchArea.setRadius(radius);
        query.setCenter(new GeoLocation(center.latitude, center.longitude));
        query.setRadius(radius/1000);
    }

    /**
     * Adjust the zoom to fit the circle
     * @param zoomLevel
     * @return
     */
    private double zoomLevelToRadius(double zoomLevel) {
        // Approximation to fit circle into view
        return 16384000/Math.pow(2, zoomLevel);
    }
    //@Override
    //  public void onResume() {
    // super.onResume();
    // if (mMap == null) {
    //    mMap = fragment.getMapAsync(this);
    //      mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)));
    //  }




}
