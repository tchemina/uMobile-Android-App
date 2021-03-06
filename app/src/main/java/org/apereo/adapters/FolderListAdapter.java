package org.apereo.adapters;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.androidannotations.annotations.ViewById;
import org.apereo.App;
import org.apereo.R;
import org.apereo.models.Folder;

import java.util.List;

/**
 * Created by schneis on 8/20/14.
 */
public class FolderListAdapter extends ArrayAdapter<Folder> {

    int colorBlack = App.getInstance().getResources().getColor(android.R.color.black);
    int colorThemeAccent = App.getInstance().getResources().getColor(R.color.theme_accent);
    int colorThemeLightTint = App.getInstance().getResources().getColor(R.color.theme_light_tint);

    Context context;
    int layoutResourceId;
    List<Folder> data = null;
    int selectedIndex;

    public FolderListAdapter(Context context, int layoutResourceId, List<Folder> data, int selectedIndex) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        this.selectedIndex = selectedIndex;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        FolderHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new FolderHolder();

            row.setTag(holder);
        } else {
            holder = (FolderHolder) row.getTag();
        }

        holder.txtName = (TextView) row.findViewById(R.id.name);

        if (position == selectedIndex) {
            holder.txtName.setTypeface(Typeface.DEFAULT_BOLD);
            holder.txtName.setTextColor(colorThemeLightTint);
            holder.txtName.setBackgroundColor(colorThemeAccent);
        } else {
            holder.txtName.setTypeface(Typeface.DEFAULT);
            holder.txtName.setTextColor(colorBlack);
            holder.txtName.setBackgroundColor(colorThemeLightTint);
        }

        Folder folder = data.get(position);
        holder.txtName.setText(folder.getName());

        return row;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    static class FolderHolder {
        TextView txtName;
    }
}
