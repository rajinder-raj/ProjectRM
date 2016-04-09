package boom.projectrm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AddPhoto extends AppCompatActivity implements View.OnClickListener,
        OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, LocationListener {
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int RESULT_CAMERA_START = 2;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int CAMERA_REQUEST = 1888;
    private Firebase fbdb;
    private GeoFire geofbdb;
    private GoogleMap mMap;
    private ImageView imageToUpload;
    private Button bUploadImage;
    private Button finalLocation;
    private ImageButton bSearch;
    private EditText uploadImageName;
    private File imageFile;
    private String mCurrentPhotoPath;
    private EditText address;
    private SupportMapFragment fragment;


    protected LocationManager locationManager;

    private Location location;

    private boolean isGPSenabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000*600*1;


    private double currLat = 0;
    private double currLon = 0;

    private Uri currImage = null; // cheap way to pass back full size image path from gallery or camera

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_add_photo);

        // setOnMarkerClickListener, GoogleMap

        // Firebase initial setup
        Firebase.setAndroidContext(this);
        fbdb = MainActivity.fbdb;
        geofbdb = MainActivity.geofbdb;

        // set buttons and listeners
        imageToUpload = (ImageView) findViewById(R.id.imageToUpload);
        bUploadImage = (Button) findViewById(R.id.bUploadImage);
        uploadImageName = (EditText) findViewById(R.id.etUploadName);
        address = (EditText) findViewById(R.id.locationAddress);
        bSearch = (ImageButton) findViewById(R.id.imageButton);
        finalLocation = (Button) findViewById(R.id.regLocation);

        FragmentManager myFM = getSupportFragmentManager();
        fragment = (SupportMapFragment) myFM.findFragmentById(R.id.location_map);
        fragment.getMapAsync(this);

        //tohide
        hideYoWifeMap();
        finalLocation.setVisibility(View.INVISIBLE);
        bUploadImage.setEnabled(false);

        bSearch.setOnClickListener(this);
        imageToUpload.setOnClickListener(this);
        bUploadImage.setOnClickListener(this);
        finalLocation.setOnClickListener(this);
    }

    /**
     *
     * @param
     */
    private void hideYoWifeMap() {
        fragment.getView().setVisibility(View.INVISIBLE);
        bSearch.setVisibility(View.INVISIBLE);
        address.setVisibility(View.INVISIBLE);
    }

    /**
     *
     * @param
     */
    private void ShowYoKidsMap() {
        fragment.getView().setVisibility(View.VISIBLE);
        bSearch.setVisibility(View.VISIBLE);
        address.setVisibility(View.VISIBLE);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.regLocation:
                hideYoWifeMap();
                finalLocation.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "Location Registered!", Toast.LENGTH_LONG).show();
                break;
            case R.id.imageToUpload:
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); // start the gallery picker
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE); // display the image over the gallery button
                //TODO:KIM-CHI
                //toShow
                ShowYoKidsMap();
                finalLocation.setVisibility(View.VISIBLE);
                break;
            case R.id.bUploadImage:
                Bitmap image = null;
                try {
                    image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), currImage);
                    //((BitmapDrawable) imageToUpload.getDrawable()).getBitmap(); // get image from imageToUpload button
                }
                catch (FileNotFoundException e) {}
                catch (IOException e) {}

                    if (image != null) {
                    String uploader = "ryan leford"; // todo retrieve current user here
                    if (currLat != 0 && currLon != 0) {
                        addImage(new Image(uploader, image), currLat, currLon);
                    }
                }
                break;
            case R.id.imageButton:
                String add = address.getText().toString();
                List<Address> addressList = null;

                if(!add.isEmpty()) {
                    Geocoder geo = new Geocoder(this);
                    if (add != null || !add.equals("")) {
                        try {
                            addressList = geo.getFromLocationName(add, 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Address address1 = addressList.get(0);

                        LatLng lat = new LatLng(address1.getLatitude(), address1.getLongitude());
                        currLat = address1.getLatitude();
                        currLon = address1.getLongitude();
                        mMap.addMarker(new MarkerOptions().position(new LatLng(address1.getLatitude(), address1.getLongitude())).icon(
                                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        //mMap.addMarker(new MarkerOptions().position(lat).title("Marker"));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lat, 20.0f));

                        Toast.makeText(this, "Found Location!", Toast.LENGTH_LONG).show();
                        //takes keyboard out
                        View view = getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                        //TODO: implement if city zoom different size, if else closer
                        //takes keyboard out
                        //slider();
                        //mMap.animateCamera(CameraUpdateFactory.newLatLng(lat));

                        bUploadImage.setEnabled(true);
                    }
                } else {
                    //error popup message
                    Toast.makeText(this, "Please enter something", Toast.LENGTH_LONG).show();
                }
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true); //this is the my current location
        mMap.setOnMyLocationButtonClickListener(this);
        Geocoder geo = new Geocoder(this);
        List<Address> addressList = null;
        try {
            addressList = geo.getFromLocationName("Calgary", 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Address address1 = addressList.get(0);




        LatLng myCoordinates = new LatLng(getLocation().getLatitude(), getLocation().getLongitude());
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(myCoordinates)      // Sets the center of the map to LatLng (refer to previous snippet)
                .zoom(12)
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        //mMap.setOnMyLocationButtonClickListener(googleMap.getMyLocation());
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));

    }

    public Location getLocation(){
        try{
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

            isGPSenabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            isNetworkEnabled = locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER);

            if(!isGPSenabled && isNetworkEnabled){

            }
            else
                this.canGetLocation = true;

                if(isNetworkEnabled)
                {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            this);
                }
            if(locationManager != null)
            {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                if(location != null){
                    currLat = location.getLatitude();
                    currLon = location.getLongitude();
                }
            }

            if(isGPSenabled)
            {
                if(location == null) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            this);
                    if(locationManager != null)
                    {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        if(location != null){
                            currLat = location.getLatitude();
                            currLon = location.getLongitude();
                        }
                    }
                }

            }

        }
        catch(SecurityException e)
        {
            e.printStackTrace();
        }

        return location;
    }


    /**
     * Add image to the Firebase database with GeoFire support.
     *
     * @param img
     * @param imgGeo
     */
    public void addImage(Image img, GeoLocation imgGeo) {
        Firebase fbdb_post = fbdb.child("images");
        Firebase fbdb_newpost = fbdb_post.push();

        fbdb_newpost.setValue(img);

        String key = fbdb_newpost.getKey();
        geofbdb.setLocation(key, imgGeo);
        Toast.makeText(getApplicationContext(), "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
    }

    public void addImage(Image img, double latitude, double longitude) {
        GeoLocation imgGeo = new GeoLocation(latitude, longitude);
        addImage(img, imgGeo);
    }

    /**
     * Extract the GeoLocation of an image based on its Exif info
     *
     * @param imagePath
     * @return
     */
    public GeoLocation extractExifGeoLocation(String imagePath) {
        try {
            ExifInterface bitmapExif = new ExifInterface(imagePath);
            double latitude = Double.parseDouble(bitmapExif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
            double longitude = Double.parseDouble(bitmapExif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
            return new GeoLocation(latitude, longitude);
        } catch (IOException e) {
            return null;
        }
    }

    public GeoQuery getImageRadius(double latitude, double longitude, double radius) {
        return geofbdb.queryAtLocation(new GeoLocation(latitude, longitude), radius);
    }

    /**
     * Callback interface for when the My Location button is clicked.
     * Will set a message when the mylocation is clicked
     * @return
     */
    @Override
    public boolean onMyLocationButtonClick() {

        Toast.makeText(this, "Your Current Location Found", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        CameraPosition test = mMap.getCameraPosition();
        currLat = test.target.latitude;
        currLon = test.target.longitude;
        mMap.addMarker(new MarkerOptions().position(new LatLng(test.target.latitude, test.target.longitude)).icon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        bUploadImage.setEnabled(true);
        //hideYoWifeMap();
        //mMap.setLocation(false);
        //mMap.addMarker(new MarkerOptions().position(new LatLng(currLat, currLon)).icon(
                //BitmapDescriptorFactory.//fromResource(R.drawable.hand)));
                //defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));


        return false;
    }

    /**
     * Called when the location has changed.
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        //currLocation = location;
        //mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).icon(
                //BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void dispatchTakePictureIntent(View view){
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, RESULT_CAMERA_START);
        /*
        if(intent.resolveActivity(getPackageManager())!= null){
            File imageFile = null;

            try {
                imageFile = createImageFile();
            } catch (IOException ex) {

            }
            if(imageFile != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
                startActivityForResult(intent, RESULT_CAMERA_START);
            }
        }
        */
    }

    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd__HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        mCurrentPhotoPath = "file:" + image.getAbsolutePath();

        return image;
    }

    /**
     * Gallery selection binds the uri to the imageToUpload
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode, data);


        if(resultCode != RESULT_CANCELED) { // handles issue with user ending camera app
            if (requestCode == RESULT_LOAD_IMAGE && data != null)
            {
                Uri selectedImage = data.getData();
                currImage = selectedImage; // passes back the fullsize image reference back to the main method

                imageToUpload.setImageBitmap(decodeSampledBitmapFromUri(getRealPathFromURI(this, selectedImage), 156, 158));
            }
            else if (requestCode == RESULT_CAMERA_START && resultCode == RESULT_OK) {
                Uri selectedImage = data.getData();
                currImage = selectedImage; // passes back the fullsize image reference back to the main method

                imageToUpload.setImageBitmap(decodeSampledBitmapFromUri(getRealPathFromURI(this, selectedImage), 156, 158));
            }


            /*
            if (requestCode == RESULT_LOAD_IMAGE && data != null) {
                Uri selectedImage = data.getData();
                currImage = selectedImage; // passes back the fullsize image reference back to the main method
                Bitmap thumbBitmap = BitmapFactory.decodeFile(getRealPathFromURI(this, selectedImage), options);

                imageToUpload.setImageBitmap(thumbBitmap);

            } else if (requestCode == RESULT_CAMERA_START && resultCode == RESULT_OK) {
                Uri uploadedImage = data.getData();
                currImage = uploadedImage;
                Bitmap thumbBitmap = BitmapFactory.decodeFile(getRealPathFromURI(this, uploadedImage), options);

                imageToUpload.setImageBitmap(thumbBitmap);

                /*
                if (imageFile.exists()) {
                    Bitmap thumbBitmap = BitmapFactory.decodeFile(imageFile.getPath(), options);
                    imageToUpload.setImageBitmap(thumbBitmap);
                    Toast.makeText(this, "The file was saved at" + imageFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();

                */

            //}

            /*
                switch (resultCode){
                    case RESULT_CAMERA_START:
                        if(imageFile.exists())
                        {
                            Toast.makeText(this,"The file was saved at"+imageFile.getAbsolutePath(),Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Toast.makeText(this,"Error",Toast.LENGTH_LONG).show();
                        }

                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                    case RESULT_LOAD_IMAGE:
                        Uri selectedImage = data.getData();
                        imageToUpload.setImageURI(selectedImage);

                    default:
                        break;



            }
            */
        }
    }

    /**
     * Gets the aboslute path from a relative uri using current context
     * @param context
     * @param contentUri
     * @return
     */
    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Get a sample bitmap from a large bitmap
     * @param path
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampledBitmapFromUri(String path, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * Get InSampleSize for the BitmapFactory.decode option
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
