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
 
public class CustomList extends ArrayAdapter<String>{
 
    private final Activity context;
    ArrayList<String> its;

    public CustomList(Activity context, ArrayList<String> items) {
        super(context, R.layout.row, items);
        this.context = context;
        its=items;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.row, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        txtTitle.setText(Html.fromHtml(its.get(position)));
        // imageView.setImageResource(imageId[position]);
        return rowView;
    }
}
