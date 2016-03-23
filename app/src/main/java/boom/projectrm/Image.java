package boom.projectrm;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.util.Date;


/**
 * Created by jparsee(john) on 17/02/16.
 */
public class Image {
    private String uploader = null;
    private String image_serialized = null;

    private Date upload_date;
    // private ArrayList<LikeDislike> likesDislikes = new ArrayList<LikeDislike>();
    // unused, to add later!

    /**
     * Default constructor, auto set date that this is created
     * @param username
     * @param bitmap
     */
    public Image(String username, Bitmap bitmap) {
        this.uploader = username;
        image_serialized = serializeBitmap(bitmap);
        upload_date = new Date(); // gets today's date
    }

    /**
     * Custom upload date constructor
     * @param username
     * @param bitmap
     * @param customDate
     */
    public Image(String username, Bitmap bitmap, Date customDate) {
        this.uploader = username;
        image_serialized = serializeBitmap(bitmap);
        upload_date = customDate;
    }

    // For Firebase database; do not remove!
    public Image() {    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }

    public Date getUploadDate() {
        return upload_date;
    }

    public void setUploadDate(Date upload_date) {
        this.upload_date = upload_date;
    }

    public String getImage_serialized() {
        return image_serialized;
    }

    /**
     * Take a bitmap image, compress and serializes it
     * @param bitmap - bitmap image
     * @return - serialized bitmap, uncompressed
     */
    public String serializeBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); // TODO - comment here
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream); // reduce the size of the image to a jpeg
       return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT); // encodes it in a string
    }

    /**
     * Get the image back as a bitmap
     * TODO - test if encoding is correct
     * @return - normal bitmap, compressed
     */
    public Bitmap convertBitmap() {
        byte[] bitmapByteArray = Base64.decode(image_serialized, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapByteArray, 0, bitmapByteArray.length);
        return bitmap;
    }
}
