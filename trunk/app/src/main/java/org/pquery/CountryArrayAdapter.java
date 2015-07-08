package org.pquery;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import org.pquery.filter.Country;
import org.pquery.filter.CountryList;

/**
 * ArrayAdapter showing a list of Countries
 */
public class CountryArrayAdapter extends ArrayAdapter<Country> {

    private final CountryList countries;
    private final CountryList selected;

    private final Activity context;

    static class ViewHolder {
        CheckedTextView nameTxVw;
    }


    /**
     * @param context
     * @param countries full list of ALL countries
     * @param selected previously selected countries
     */
    public CountryArrayAdapter(Activity context, CountryList countries, CountryList selected) {
        super(context, R.layout.countrylist, countries.getInner());
        this.context = context;
        this.countries = countries;
        this.selected = selected;
    }

    /**
     * @param position line in array we are being told to render
     * @param convertView suggested existing view to use, saves resources; can be null
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.countrylist, null);
        }

        Country country = countries.get(position);      // which country are we on

        // Fill in view line with details on country

        final ViewHolder viewHolder = new ViewHolder();
        viewHolder.nameTxVw = (CheckedTextView) convertView.findViewById(R.id.countrylist_line);
        viewHolder.nameTxVw.setText(country.getName());
        viewHolder.nameTxVw.setCompoundDrawablePadding(20);     // padding between drawables and text

        // Get the flag drawable and set the drawable bounds (how large)
        // Needs to scale depending on screen density
        Drawable flagDraw = parent.getResources().getDrawable(country.getFlag());
        int width = (int) (30 * parent.getResources().getDisplayMetrics().density);      // 30 is effectively 30dp ?
        int height =  (int) ((3f/4f) * width);
        flagDraw.setBounds( 0, 0, width, height);
        viewHolder.nameTxVw.setCompoundDrawables(flagDraw, null, null, null);

        if (selected.contains(country))
            viewHolder.nameTxVw.setChecked(true);
        else
            viewHolder.nameTxVw.setChecked(false);


        convertView.setTag(viewHolder);
        return convertView;
    }
}
