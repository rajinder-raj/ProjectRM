package boom.projectrm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;

/**
 * Created by Kim on 3/20/2016.
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback, GeoQueryEventListener,
        GoogleMap.OnCameraChangeListener, View.OnClickListener, GoogleMap.OnMarkerClickListener {
    private static final GeoLocation STARTUP_CENTER = new GeoLocation(51.04861497826971, -114.07084610313177);
    private static final int STARTUP_ZOOM_LEVEL = 14;

    private SliderLayout sliderShow;
    private View view;
    private ImageButton bSearch;

    private SupportMapFragment fragment;
    private Circle searchArea;
    private GoogleMap mMap;
    private GeoQuery query;

    private Map<String, Marker> markers;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_one, container, false);
        bSearch = (ImageButton) view.findViewById(R.id.button);
        bSearch.setOnClickListener(this);

        return view;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //FragmentManager fm = getFragmentManager();
        fragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.location_map);
        fragment.getMapAsync(this);

        sliderShow = (SliderLayout) getActivity().findViewById(R.id.slider);
        sliderIntro();
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

    public void sliderIntro() {
        TextSliderView textSliderView = new TextSliderView(getActivity());
        textSliderView
                .description("Real Views")
                //.image("http://www.exclusivegrouptravel.com/Careers/Beachchairs.jpg");
                .image("https://76714110-a-e4b983e5-s-sites.googlegroups.com/a/mtroyal.ca/rajinder_eportfolio/home/Logo%20%281%29.png?attachauth=ANoY7cp3W9NYSpiUKAOp-74mJHQsRQxMLfxlnrH95M3i_H3dPp8Xb2Q82A2G-hVtoZjxxoIIFkb6lXj4p-nYo0rG1o5o4eFaElhl565OqUryGWm92j5gS45e8vRbqSFzLQhhte6Qx0wT1qkJP-2M9RslxzCXYfiPEUsdvzro7lTftEm98_VrB1m_2wREkUn2Ni_yIQzaI1VZLLLmT35mR-o-9eGcmS094p_YnUnkYrp1F-8vLDOgw8k%3D&attredirects=0");

        sliderShow.addSlider(textSliderView);
    }

    /**
     * Raj - load sliders when the marker on the map is clicked
     * @date April 12, 2016
     */
    public void loadPictureToSlide() {
        TextSliderView textSliderView = new TextSliderView(getActivity());
        textSliderView
                .description("Real Views")
                        //.image("http://www.exclusivegrouptravel.com/Careers/Beachchairs.jpg");
                .image("https://d2q79iu7y748jz.cloudfront.net/s/_logo/94511cfc58af497597d0b27153dfc32d.png");
        sliderShow.removeAllSliders(); //clear slider
        sliderShow.addSlider(textSliderView);
    }

    public void onClick(View v) {
        EditText address = (EditText) getActivity().findViewById(R.id.locationAddress);
        String add = address.getText().toString();
        List<Address> addressList = null;

        if(!add.isEmpty()) {
            Geocoder geo = new Geocoder(getActivity());
            if (add != null || !add.equals("")) {
                try {
                    addressList = geo.getFromLocationName(add, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Address address1 = addressList.get(0);

                LatLng lat = new LatLng(address1.getLatitude(), address1.getLongitude());
                mMap.addMarker(new MarkerOptions().position(lat).title("Marker"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lat, 20.0f));
                Toast.makeText(getActivity().getBaseContext(), "Found Location!", Toast.LENGTH_LONG).show();
                //takes keyboard out
                View view = getActivity().getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                //TODO: implement if city zoom different size, if else closer
                //takes keyboard out
                //slider();
                //mMap.animateCamera(CameraUpdateFactory.newLatLng(lat));
            }
        } else {
            //error popup message
            Toast.makeText(getActivity().getBaseContext(), "Please enter something", Toast.LENGTH_LONG).show();
        }
    }

    public Image getImageByKey(String key) {
        Query result = MainActivity.fbdb.equalTo(key);

        return null;
    }


    /**
     * When the map starts up create listener for geofire
     */
    @Override
    public void onStart() {
        super.onStart();

        //query.addGeoQueryEventListener(this);
    }

    public void onPause()
    {
        super.onPause();
        mMap.clear();
    }

    public void onResume()
    {
        super.onResume();
        fragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.location_map);
        fragment.getMapAsync(this);
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
        //searchArea.setFillColor(Color.argb(66, 255, 219, 164));
        //searchArea.setStrokeColor(Color.argb(66, 255, 162, 47));

        // set the camera for the map
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startupCenter, STARTUP_ZOOM_LEVEL));
        mMap.setOnCameraChangeListener(this);

        //set the on marker listener
        mMap.setOnMarkerClickListener(this);

        query = MainActivity.geofbdb.queryAtLocation(STARTUP_CENTER, 1);

        markers = new HashMap<String, Marker>();

        query.addGeoQueryEventListener(this);

        // test statement
        Marker testMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(STARTUP_CENTER.latitude, STARTUP_CENTER.longitude)));
        markers.put("laks;jdf;ljawf", testMarker);
    }

    public void onMapClick(LatLng latLng) {

    }

    public void setUpMarkers()
    {
        for(Marker marker: markers.values()) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude)));
        }
    }


    /**
     * Add markers for new values in the search area
     * @param key
     * @param location
     */
    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)));
        this.markers.put(key, marker);
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
        double radius = zoomLevelToRadius(STARTUP_ZOOM_LEVEL);
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

    @Override
    public boolean onMarkerClick(Marker marker) {
        //TODO: Implement the get photo to slideshow here
        Toast.makeText(getActivity().getBaseContext(), "Marker Location: " + marker.getPosition().latitude +
                marker.getPosition().longitude, Toast.LENGTH_LONG).show();

        loadPictureToSlide();
        return false;
    }
    //@Override
    //  public void onResume() {
    // super.onResume();
    // if (mMap == null) {
    //    mMap = fragment.getMapAsync(this);
    //      mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)));
    //  }




}
