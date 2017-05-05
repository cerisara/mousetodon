package fr.xtof54.mousetodon;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import android.text.Html;
import android.graphics.Bitmap;
 
public class CustomList extends ArrayAdapter<String> {
 
    private final Activity context;
    public ArrayList<Bitmap> imgsinrow = null;

    public CustomList(Activity context, ArrayList<String> tts) {
        super(context, R.layout.row, tts);
        this.context = context;
    }

    public void initempty() {
        this.clear();
        imgsinrow = new ArrayList<Bitmap>();
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.row, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        txtTitle.setText(Html.fromHtml(getItem(position)));
        if (imgsinrow!=null && imgsinrow.size()>position) {
            Bitmap bMap = imgsinrow.get(position);
            if (bMap!=null) imageView.setImageBitmap(bMap);
        }
        return rowView;
    }
}
