package boom.projectrm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddPhoto extends AppCompatActivity implements View.OnClickListener {
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int RESULT_CAMERA_START = 2;
    private static final int CAMERA_REQUEST = 1888;
    private Firebase fbdb;
    private GeoFire geofbdb;
    private ImageView imageToUpload;
    private Button bUploadImage;
    private EditText uploadImageName;
    private CheckBox check_useCurrentLocation;
    private File imageFile;
    private String mCurrentPhotoPath;

    private Uri currImage = null; // cheap way to pass back full size image path from gallery or camera

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photo);

        // setOnMarkerClickListener, GoogleMap

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
                Bitmap image = null;
                try {
                    image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), currImage);
                    //((BitmapDrawable) imageToUpload.getDrawable()).getBitmap(); // get image from imageToUpload button
                }
                catch (FileNotFoundException e) {}
                catch (IOException e) {}

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
        startActivityForResult(intent, CAMERA_REQUEST);
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


        if(resultCode != RESULT_CANCELED && data != null) { // handles issue with user ending camera app
            if (requestCode == RESULT_LOAD_IMAGE || requestCode == RESULT_CAMERA_START && resultCode == RESULT_OK)
            {
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
