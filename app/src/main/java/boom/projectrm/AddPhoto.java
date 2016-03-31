package boom.projectrm;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddPhoto extends AppCompatActivity implements View.OnClickListener {
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int RESULT_CAMERA_START = 2;
    private Firebase fbdb;
    private GeoFire geofbdb;
    private ImageView imageToUpload;
    private Button bUploadImage;
    private EditText uploadImageName;
    private CheckBox check_useCurrentLocation;
    private File imageFile;
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photo);


        // Firebase initial setup
        Firebase.setAndroidContext(this);
        fbdb = new Firebase("https://boomerango.firebaseio.com/imagesV2");
        geofbdb = new GeoFire(fbdb);

        // set buttons and listeners
        imageToUpload = (ImageView) findViewById(R.id.imageToUpload);
        bUploadImage = (Button) findViewById(R.id.bUploadImage);
        uploadImageName = (EditText) findViewById(R.id.etUploadName);
        check_useCurrentLocation = (CheckBox) findViewById(R.id.useCurrLocation);

        imageToUpload.setOnClickListener(this);
        bUploadImage.setOnClickListener(this);
        if (check_useCurrentLocation.isChecked()) {
            check_useCurrentLocation.setChecked(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageToUpload:
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); // start the gallery picker
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE); // display the image over the gallery button
                break;
            case R.id.bUploadImage:
                Bitmap image = ((BitmapDrawable) imageToUpload.getDrawable()).getBitmap(); // get image from imageToUpload button
                if (image != null) {
                    String uploader = "raj"; // todo retrieve current user here
                    double latitude = 0.0;
                    double longitutde = 0.0;

                    if (check_useCurrentLocation.isChecked()) {
                        Bundle extras = getIntent().getExtras();
                        if (extras != null) {
                            latitude = extras.getDouble("boom.realmaps.EXTRA_CURR_LATITUDE");
                            longitutde = extras.getDouble("boom.realmaps.EXTRA_CURR_LONGITUDE");
                        }
                    }
                    addImage(new Image(uploader, image), latitude, longitutde);
                }
                break;
        }

    }

    /**
     * Gallery selection binds the uri to the imageToUpload
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */


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



    public void dispatchTakePictureIntent(View view){
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


        if(intent.resolveActivity(getPackageManager())!= null){
            File imageFile = null;

            try {
                imageFile = createImageFile();
            }catch(IOException ex){

            }
            if(imageFile != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(imageFile));
                startActivityForResult(intent, RESULT_CAMERA_START);
            }
        }
    }

    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd__HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        mCurrentPhotoPath = "file:" + image.getAbsolutePath();

        return image;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode, data);
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
    }
}
