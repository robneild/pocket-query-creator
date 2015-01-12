package org.pquery;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import junit.framework.Assert;

import org.pquery.filter.CacheType;
import org.pquery.filter.CacheTypeList;
import org.pquery.filter.CheckBoxesFilter;
import org.pquery.filter.ContainerType;
import org.pquery.filter.ContainerTypeList;
import org.pquery.filter.OneToFiveFilter;
import org.pquery.service.PQService;
import org.pquery.util.GPS;
import org.pquery.util.Prefs;

import java.util.ArrayList;

/**
 * Handle adding filters
 */
public class CreateFiltersActivity extends SherlockListActivity implements LocationListener {

    private QueryStore queryStore;

    private ArrayList<CreationOption> _filterList;
    private CreationOptionAdapter m_adapter;

    private LocationManager locationManager;
    private Location gpsLocation;

    /**
     * Add icons to the top toolbar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, R.string.create, 0, R.string.create)
                .setIcon(R.drawable.content_new)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, Main.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.string.create:
                Bundle bundle = new Bundle();
                queryStore.saveToBundle(bundle);

                intent = new Intent(this, PQService.class);
                intent.putExtra("queryStore", bundle);
                intent.putExtra("operation", PQService.OPERATION_CREATE);
                getApplicationContext().startService(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        queryStore.saveToBundle(outState);

        outState.putParcelable("gpsLocation", gpsLocation);
    }


    @Override
    protected void onActivityResult(int arg0, int arg1, Intent intent) {
        super.onActivityResult(arg0, arg1, intent);
        if (intent != null) {
            double lat = intent.getDoubleExtra("lat", 0);
            double lon = intent.getDoubleExtra("lon", 0);
            Log.d(getLocalClassName(), "User chose lat=" + lat + ", lon=" + lon);

            queryStore.lat = lat;
            queryStore.lon = lon;

            _filterList.get(1).setCurrentValue(queryStore.lat, queryStore.lon, gpsLocation);

            m_adapter.notifyDataSetChanged();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null) {
            queryStore = new QueryStore(savedInstanceState);
            gpsLocation = savedInstanceState.getParcelable("gpsLocation");
        } else {
            queryStore = new QueryStore();
            gpsLocation = new Location("bob");
        }

        // Setup GPS

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // View

        setContentView(R.layout.create_filters_fragment);

        // Create list of filters, adding each one

        _filterList = new ArrayList<CreationOption>();
        this.m_adapter = new CreationOptionAdapter(this, R.layout.create_filters_fragment_list, _filterList);
        setListAdapter(this.m_adapter);

        _filterList = new ArrayList<CreationOption>();

        CreationOption filterOption = new CreationOption(getResources().getString(R.string.filter_name), R.drawable.abc, getResources());
        filterOption.setCurrentValue(queryStore.name);
        _filterList.add(filterOption);

        filterOption = new CreationOption(getResources().getString(R.string.filter_origin), getResources());
        filterOption.setCurrentValue(queryStore.lat, queryStore.lon, gpsLocation);
        _filterList.add(filterOption);

        filterOption = new CreationOption(getResources().getString(R.string.filter_radius), R.drawable.target, getResources());
        filterOption.setCurrentValue(Prefs.getDefaultRadius(this) + (Prefs.isMetric(this) ? getResources().getString(R.string.filter_radius_km) : getResources().getString(R.string.filter_radius_miles)));

        _filterList.add(filterOption);


        filterOption = new CreationOption(getResources().getString(R.string.filter_checkboxes), R.drawable.tick,
                getResources());
        filterOption.setCurrentValue(Prefs.getCheckBoxesFilter(this), getResources());
        _filterList.add(filterOption);

        filterOption = new CreationOption(getResources().getString(R.string.filter_cache_type), R.drawable.ghost,
                getResources());
        filterOption.setCurrentValue(Prefs.getCacheTypeFilter(this));
        _filterList.add(filterOption);

        filterOption = new CreationOption(getResources().getString(R.string.filter_container),
                R.drawable.container, getResources());
        filterOption.setCurrentValue(Prefs.getContainerTypeFilter(this));
        _filterList.add(filterOption);

        filterOption = new CreationOption(getResources().getString(R.string.filter_terrain), R.drawable.mountain,
                getResources());
        filterOption.setCurrentValue(Prefs.getTerrainFilter(this));
        _filterList.add(filterOption);

        filterOption = new CreationOption(getResources().getString(R.string.filter_difficulty), R.drawable.maze,
                getResources());
        filterOption.setCurrentValue(Prefs.getDifficultyFilter(this));
        _filterList.add(filterOption);


        if (_filterList != null && _filterList.size() > 0) {
            m_adapter.notifyDataSetChanged();
            for (int i = 0; i < _filterList.size(); i++)
                m_adapter.add(_filterList.get(i));
        }
        // m_ProgressDialog.dismiss();
        m_adapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (id == 1) {

            if (queryStore.lat == 0 && queryStore.lon == 0) {

                Intent intent;
                if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS)
                    intent = new Intent(this, MapsActivity.class);
                else
                    intent = new Intent(this, MapsActivityOld.class);

                // Try to open map at current location (if we have it)
                intent.putExtra("lat", gpsLocation.getLatitude());
                intent.putExtra("lon", gpsLocation.getLongitude());

                startActivityForResult(intent, 123);

            } else {
                queryStore.lat = 0;
                queryStore.lon = 0;
                _filterList.get(1).setCurrentValue(queryStore.lat, queryStore.lon, gpsLocation);

                m_adapter.notifyDataSetChanged();
            }


        } else {
            createDialog(position).show();
        }
    }

    private Dialog createDialog(int id) {

        if (id == 0) {

            LayoutInflater vi = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = vi.inflate(R.layout.create_name_fragment, null);


            final EditText nameEditText = (EditText) v.findViewById(R.id.editText_name);
            final CheckBox autoName = (CheckBox) v.findViewById(R.id.checkBox_autoname);

            autoName.setChecked(Prefs.isAutoName(this));
            nameEditText.setText(queryStore.name);
            autoName.setChecked(Prefs.isAutoName(this));

            return new AlertDialog.Builder(this)
                    .setTitle(R.string.pocket_query_name)
                    .setView(v)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if (nameEditText.length() > 0) {
                                queryStore.name = nameEditText.getText().toString();
                            }
                            _filterList.get(0).setCurrentValue(queryStore.name);

                            Prefs.saveAutoName(CreateFiltersActivity.this, autoName.isChecked());

                            m_adapter.notifyDataSetChanged();
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }
                    }).show();
        }


        if (id == 2) {
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
            input.setText(Prefs.getDefaultRadius(this));

            return new AlertDialog.Builder(this)
                    .setTitle(R.string.filter_radius)
                    .setView(input)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            try {
                                int r = Integer.parseInt(input.getText().toString());
                                Prefs.saveDefaultRadius(CreateFiltersActivity.this, r + "");
                                _filterList.get(2).setCurrentValue(r + (Prefs.isMetric(CreateFiltersActivity.this) ? getResources().getString(R.string.filter_radius_km) : getResources().getString(R.string.filter_radius_miles)));
                                m_adapter.notifyDataSetChanged();
                            } catch (NumberFormatException e) {
                            }
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    })

                    .show();
        }

        if (id == 3) {
            // Cache type
            CheckBoxesFilter checkBoxesFilter = Prefs.getCheckBoxesFilter(this);
            String[] options = checkBoxesFilter.getOptions(getResources());
            final boolean[] selections = checkBoxesFilter.getAsBooleanArray();

            return new AlertDialog.Builder(this).setTitle(R.string.filter_checkboxes)
                    .setMultiChoiceItems(options, selections, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        }
                    }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            CheckBoxesFilter checkBoxesFilter = new CheckBoxesFilter(selections);
                            Prefs.saveCheckBoxesFilter(CreateFiltersActivity.this, checkBoxesFilter);

                            _filterList.get(3).setCurrentValue(checkBoxesFilter, getResources());
                            m_adapter.notifyDataSetChanged();
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).create();
        }

        if (id == 4) {
            // Cache type
            CharSequence[] options = new CharSequence[CacheType.values().length];
            for (int i = 0; i < options.length; i++) {
                options[i] = getResources().getString(CacheType.values()[i].getResourceId());
            }
            final boolean[] selections = Prefs.getCacheTypeFilter(this).getAsBooleanArray();

            return new AlertDialog.Builder(this).setTitle(R.string.filter_cache_type)
                    .setMultiChoiceItems(options, selections, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        }
                    }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            CacheTypeList cacheTypeList = new CacheTypeList(selections);
                            Prefs.saveCacheTypeFilter(CreateFiltersActivity.this, cacheTypeList);

                            _filterList.get(4).setCurrentValue(cacheTypeList);
                            m_adapter.notifyDataSetChanged();
                        }
                    }).setNeutralButton(R.string.any, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            CacheTypeList cacheTypeList = new CacheTypeList();
                            cacheTypeList.setAll();
                            Prefs.saveCacheTypeFilter(CreateFiltersActivity.this, cacheTypeList);

                            _filterList.get(4).setCurrentValue(cacheTypeList);
                            m_adapter.notifyDataSetChanged();
                        }

                    }).create();
        }

        if (id == 5) {
            // Container type

            CharSequence[] options = new CharSequence[ContainerType.values().length];
            for (int i = 0; i < options.length; i++) {
                options[i] = getResources()
                        .getString(ContainerType.values()[i].getResourceId());
            }
            final boolean[] selections = Prefs.getContainerTypeFilter(this).getAsBooleanArray();

            return new AlertDialog.Builder(this).setTitle(R.string.filter_container)
                    .setMultiChoiceItems(options, selections, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        }
                    }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ContainerTypeList containerTypeList = new ContainerTypeList(selections);
                            Prefs.saveContainerTypeFilter(CreateFiltersActivity.this, containerTypeList);

                            _filterList.get(5).setCurrentValue(containerTypeList);
                            m_adapter.notifyDataSetChanged();
                        }

                    }).setNeutralButton(R.string.any, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ContainerTypeList containerTypeList = new ContainerTypeList();
                            containerTypeList.setAll();
                            Prefs.saveContainerTypeFilter(CreateFiltersActivity.this, containerTypeList);

                            _filterList.get(5).setCurrentValue(containerTypeList);
                            m_adapter.notifyDataSetChanged();
                        }

                    }).create();
        }

        if (id == 6)
            return CreateDialog(R.string.filter_terrain, Prefs.getTerrainFilter(this), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Prefs.saveTerrainFilter(CreateFiltersActivity.this, newOneToFiveFilter);
                    _filterList.get(6).setCurrentValue(newOneToFiveFilter);
                    m_adapter.notifyDataSetChanged();
                }
            });

        if (id == 7)
            return CreateDialog(R.string.filter_difficulty, Prefs.getDifficultyFilter(this), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Prefs.saveDifficultyFilter(CreateFiltersActivity.this, newOneToFiveFilter);
                    _filterList.get(7).setCurrentValue(newOneToFiveFilter);
                    m_adapter.notifyDataSetChanged();
                }
            });

        Assert.assertTrue(false);
        return null;
    }


    @Override
    public void onResume() {
        super.onResume();
        GPS.requestLocationUpdates(locationManager, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        GPS.stopLocationUpdate(locationManager, this);
    }


    private OneToFiveFilter newOneToFiveFilter;

    private AlertDialog CreateDialog(int titleRes, OneToFiveFilter initialValue, DialogInterface.OnClickListener okHandler) {

        // Create custom view containing max/min drag bar

        newOneToFiveFilter = initialValue;
        DiscreteRangeSeekBar<Integer> seekBar = new DiscreteRangeSeekBar<Integer>(1, 5, 1, this);

        LinearLayout view = (LinearLayout) this.getLayoutInflater().inflate(R.layout.dialog_signin, null);

        // Manually specify a margin round the drag bar else it is a bit difficult to touch
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        layout.setMargins(px, px, px, px);

        view.addView(seekBar, layout);

        // Create dialog

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(initialValue.toString()).
                setTitle(titleRes)
                .setPositiveButton(android.R.string.ok, okHandler)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                }).setView(view);

        final AlertDialog alertDialog = builder.create();

        // Configure drag bar

        seekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer min, Integer max) {
                newOneToFiveFilter = new OneToFiveFilter(min + " - " + max);
                alertDialog.setMessage(newOneToFiveFilter.toString());
            }
        });
        seekBar.setNotifyWhileDragging(true);
        if (initialValue.up)
            seekBar.setSelectedMinValue(initialValue.value);
        else
            seekBar.setSelectedMaxValue(initialValue.value);
        return alertDialog;
    }


    private class CreationOptionAdapter extends ArrayAdapter<CreationOption> {

        private ArrayList<CreationOption> items;

        public CreationOptionAdapter(Context context, int textViewResourceId, ArrayList<CreationOption> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.create_filters_fragment_list, null);
            }
            CreationOption o = items.get(position);
            if (o != null) {

                ImageView icon = (ImageView) v.findViewById(R.id.icon);
                if (o.getIconRes() != null) {
                    icon.setImageResource(o.getIconRes());

                }

                TextView tt = (TextView) v.findViewById(R.id.toptext);
                TextView bt = (TextView) v.findViewById(R.id.bottomtext);
                if (tt != null) {
                    tt.setText(o.getName());
                }
                if (bt != null) {
                    bt.setText(o.getCurrentValue());
                    bt.setTextColor(o.getColor());
                }
            }
            return v;
        }
    }


    private class CreationOption {

        private String name;
        private String currentValue;
        private Integer iconRes;        // null if no icon
        private int color = 0xffaaaaaa;
        private Resources res;

        CreationOption(String name, int iconRes, Resources res) {
            this.name = name;
            this.iconRes = iconRes;
            this.res = res;
        }

        /**
         * Constructor for no icon
         */
        CreationOption(String name, Resources res) {
            this.name = name;
            this.res = res;
        }

        public String getName() {
            return name;
        }

        public Integer getIconRes() {
            return iconRes;
        }

        public String getCurrentValue() {
            return currentValue;
        }

        public void setCurrentValue(String filterValue) {
            this.currentValue = filterValue;
            //this.color = Color.GREEN;
        }

        public void setCurrentValue(CheckBoxesFilter checkBoxesFilter, Resources res) {
            this.currentValue = checkBoxesFilter.toLocalisedString(res);
            this.color = Color.MAGENTA;
        }

        public void setCurrentValue(OneToFiveFilter f) {
            this.currentValue = f.toString();
            if (f.isAll())
                this.color = Color.GREEN;
            else
                this.color = Color.MAGENTA;
        }

        public void setCurrentValue(CacheTypeList cacheTypeList) {
            this.currentValue = cacheTypeList.toLocalisedString(res);
            if (cacheTypeList.isAll()) {
                this.color = Color.GREEN;
                this.currentValue = res.getString(R.string.any);
            } else
                this.color = Color.MAGENTA;
        }

        public void setCurrentValue(ContainerTypeList containerList) {
            this.currentValue = containerList.toLocalisedString(res);
            if (containerList.isAll()) {
                this.color = Color.GREEN;
                this.currentValue = res.getString(R.string.any);
            } else
                this.color = Color.MAGENTA;
        }

        public void setCurrentValue(double mapPositionLat, double mapPositionLon, Location gpsLocation) {
            if (mapPositionLat == 0 && mapPositionLon == 0) {

                this.currentValue = getResources().getString(R.string.gps);

                this.iconRes = R.drawable.ivak_satellite;
                int accuracy = (int) gpsLocation.getAccuracy();

                if (accuracy == 0) {
                    this.color = 0xffFF4500;        // orange. no fix yet at all
                    this.currentValue += getResources().getText(R.string.no_fix_yet);
                } else if (accuracy < 80) {
                    this.color = Color.GREEN;
                    this.currentValue += String.format(getResources().getString(R.string.accuracy), accuracy);
                } else {
                    this.color = Color.YELLOW;
                    this.currentValue += String.format(getResources().getString(R.string.accuracy), accuracy);
                }


            } else {
                this.color = 0xffaaaaaa;
                this.currentValue = getResources().getString(R.string.map_point);
                this.iconRes = R.drawable.treasure_map;
            }
        }

        public void setColor(int color) {
            this.color = color;
        }

        public int getColor() {
            return color;
        }
    }


    // Control GPS


    public void onLocationChanged(Location gpsLocation) {
        if (gpsLocation.getProvider().equals(LocationManager.NETWORK_PROVIDER) &&
                this.gpsLocation != null &&
                this.gpsLocation.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            // don't over write GPS with network provider
        } else {
            this.gpsLocation = gpsLocation;

            // Update displayed accuracy in the "Origin" list item
            _filterList.get(1).setCurrentValue(queryStore.lat, queryStore.lon, gpsLocation);
            m_adapter.notifyDataSetChanged();
        }

    }

    public void onProviderDisabled(String arg0) {
    }

    public void onProviderEnabled(String arg0) {
    }

    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
    }


}