package fr.xtof54.mousetodon;

import org.json.JSONObject;
import java.io.InputStream;
import java.net.HttpURLConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.util.HashMap;

public class DetIcons {
    public static HashMap<String,Bitmap> avatars = new HashMap<String,Bitmap>();

    public static Bitmap downloadImg(JSONObject  account) {
        try {
            String avatar = account.getString("avatar");
            if (avatar!=null && avatar.startsWith("http")) {
                Bitmap img = avatars.get(avatar);
                if (img==null) img=downloadImg(avatar);
                if (img!=null) {
                    avatars.put(avatar,img);
                    return img;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Bitmap downloadImg(String src) {
       try {
           java.net.URL url = new java.net.URL(src);
           HttpURLConnection connection = (HttpURLConnection) url.openConnection();
           connection.setDoInput(true);
           connection.connect();
           InputStream input = connection.getInputStream();
           Bitmap myBitmap = BitmapFactory.decodeStream(input);
           return myBitmap;
       } catch (Exception e) {
           e.printStackTrace();
           return null;
       }
    }
}
