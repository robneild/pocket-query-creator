package org.pquery;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import junit.framework.Assert;

import org.pquery.filter.CacheType;
import org.pquery.filter.CacheTypeList;
import org.pquery.filter.CheckBoxesFilter;
import org.pquery.filter.ContainerType;
import org.pquery.filter.ContainerTypeList;
import org.pquery.filter.Country;
import org.pquery.filter.CountryList;
import org.pquery.filter.DaysToGenerateFilter;
import org.pquery.filter.OneToFiveFilter;
import org.pquery.service.PQService;
import org.pquery.util.Logger;
import org.pquery.util.Prefs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Show page of options available for creating a pocket query
 * e.g. name, location, type, radius etc.
 */
public class CreateFiltersActivity extends ListActivity implements LocationListener {

    private QueryStore queryStore;

    private ArrayList<CreationOption> _filterList;
    private CreationOptionAdapter m_adapter;

    private LocationManager locationManager;
    private Location gpsLocation;

    /** Remember if asked for file permissions...so don't repeatedly ask e.g. on rotation */
    private boolean filePermissionsAsked;

    /** Remember if asked for gps permssions */
    private boolean gpsPermissionsAsked;
    private int[] gpsPermissionsGiven;

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

            _filterList.get(2).setCurrentValue(queryStore.lat, queryStore.lon, gpsLocation);

            m_adapter.notifyDataSetChanged();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);

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

        // Create the list of of possible filters, adding an option to open a dialog for each one

        _filterList = new ArrayList<CreationOption>();
        this.m_adapter = new CreationOptionAdapter(this, R.layout.create_filters_fragment_list, _filterList);
        setListAdapter(this.m_adapter);

        _filterList = new ArrayList<CreationOption>();

        CreationOption filterOption = new CreationOption(getResources().getString(R.string.filter_name), R.drawable.abc, getResources());
        filterOption.setCurrentValue(queryStore.name);
        _filterList.add(filterOption);

        filterOption = new CreationOption(getResources().getString(R.string.filter_days_to_generate), R.drawable.calendar, getResources());
        filterOption.setCurrentValue(Prefs.getDaysToGenerateFilter(this), getResources());
        _filterList.add(filterOption);

        filterOption = new CreationOption(getResources().getString(R.string.filter_origin), getResources());
        filterOption.setCurrentValue(queryStore.lat, queryStore.lon, gpsLocation);
        _filterList.add(filterOption);

        filterOption = new CreationOption(getResources().getString(R.string.filter_radius), R.drawable.target, getResources());
        filterOption.setCurrentValue(Prefs.getDefaultRadius(this) + (Prefs.isMetric(this) ? getResources().getString(R.string.filter_radius_km) : getResources().getString(R.string.filter_radius_miles)));
        _filterList.add(filterOption);


        filterOption = new CreationOption(getResources().getString(R.string.filter_checkboxes), R.drawable.tick,
                getResources());
        filterOption.setCurrentValue(Prefs.getCheckBoxesFilter(this).toLocalisedString(getResources()));
        _filterList.add(filterOption);

        filterOption = new CreationOption(getResources().getString(R.string.filter_cache_type), R.drawable.ghost,
                getResources());
        filterOption.setCurrentValue(Prefs.getCacheTypeFilter(this).toLocalisedString(getResources()));
        _filterList.add(filterOption);

        filterOption = new CreationOption(getResources().getString(R.string.filter_container),
                R.drawable.container, getResources());
        filterOption.setCurrentValue(Prefs.getContainerTypeFilter(this).toLocalisedString(getResources()));
        _filterList.add(filterOption);

        filterOption = new CreationOption(getResources().getString(R.string.filter_terrain), R.drawable.mountain,
                getResources());
        filterOption.setCurrentValue(Prefs.getTerrainFilter(this).toLocalisedString(this));
        _filterList.add(filterOption);

        filterOption = new CreationOption(getResources().getString(R.string.filter_difficulty), R.drawable.maze,
                getResources());
        filterOption.setCurrentValue(Prefs.getDifficultyFilter(this).toLocalisedString(this));
        _filterList.add(filterOption);

        filterOption = new CreationOption(getResources().getString(R.string.filter_countries), R.drawable.ryanlerch_flagpole,
                getResources());
        filterOption.setCurrentValue(Prefs.getCountriesFilter(this).toLocalisedString(getResources()));
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
        if (id == 2) {

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
                _filterList.get(2).setCurrentValue(queryStore.lat, queryStore.lon, gpsLocation);

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

        if (id == 1) {

            LayoutInflater vi = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = vi.inflate(R.layout.days_to_generate_fragment, null);

            final DaysToGenerateFilter daysToGenerateFilter = Prefs.getDaysToGenerateFilter(this);

            final RadioButton uncheck = (RadioButton) v.findViewById(R.id.radio_uncheckDayOfWeekAfterQuery);
            final RadioButton oncheckeddays = (RadioButton) v.findViewById(R.id.radio_runQueryEveryWeekOnCheckedDays);
            final RadioButton runonce = (RadioButton) v.findViewById(R.id.radio_runqueryonce);

            final CheckBox sun = (CheckBox) v.findViewById(R.id.checkBox_sun);
            final CheckBox mon = (CheckBox) v.findViewById(R.id.checkBox_mon);
            final CheckBox tue = (CheckBox) v.findViewById(R.id.checkBox_tue);
            final CheckBox wed = (CheckBox) v.findViewById(R.id.checkBox_wed);
            final CheckBox thu = (CheckBox) v.findViewById(R.id.checkBox_thu);
            final CheckBox fri = (CheckBox) v.findViewById(R.id.checkBox_fri);
            final CheckBox sat = (CheckBox) v.findViewById(R.id.checkBox_sat);

            if (daysToGenerateFilter.howOftenRun == DaysToGenerateFilter.UNCHECK_DAY_AFTER_QUERY) uncheck.setChecked(true);
            if (daysToGenerateFilter.howOftenRun == DaysToGenerateFilter.RUN_EVERY_WEEK_ON_CHECKED_DAYS) oncheckeddays.setChecked(true);
            if (daysToGenerateFilter.howOftenRun == DaysToGenerateFilter.RUN_ONCE_THEN_DELETE) runonce.setChecked(true);

            if (daysToGenerateFilter.dayOfWeek[0]) sun.setChecked(true);
            if (daysToGenerateFilter.dayOfWeek[1]) mon.setChecked(true);
            if (daysToGenerateFilter.dayOfWeek[2]) tue.setChecked(true);
            if (daysToGenerateFilter.dayOfWeek[3]) wed.setChecked(true);
            if (daysToGenerateFilter.dayOfWeek[4]) thu.setChecked(true);
            if (daysToGenerateFilter.dayOfWeek[5]) fri.setChecked(true);
            if (daysToGenerateFilter.dayOfWeek[6]) sat.setChecked(true);

            return new AlertDialog.Builder(this)
                    .setTitle(R.string.pocket_query_name)
                    .setView(v)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                            if (uncheck.isChecked()) daysToGenerateFilter.howOftenRun = DaysToGenerateFilter.UNCHECK_DAY_AFTER_QUERY;
                            if (oncheckeddays.isChecked()) daysToGenerateFilter.howOftenRun = DaysToGenerateFilter.RUN_EVERY_WEEK_ON_CHECKED_DAYS;
                            if (runonce.isChecked()) daysToGenerateFilter.howOftenRun = DaysToGenerateFilter.RUN_ONCE_THEN_DELETE;

                            daysToGenerateFilter.dayOfWeek[0] = sun.isChecked();
                            daysToGenerateFilter.dayOfWeek[1] = mon.isChecked();
                            daysToGenerateFilter.dayOfWeek[2] = tue.isChecked();
                            daysToGenerateFilter.dayOfWeek[3] = wed.isChecked();
                            daysToGenerateFilter.dayOfWeek[4] = thu.isChecked();
                            daysToGenerateFilter.dayOfWeek[5] = fri.isChecked();
                            daysToGenerateFilter.dayOfWeek[6] = sat.isChecked();

                            Prefs.saveDaysToGenerateFilter(CreateFiltersActivity.this, daysToGenerateFilter);

                            _filterList.get(1).setCurrentValue(Prefs.getDaysToGenerateFilter(CreateFiltersActivity.this), getResources());
                            m_adapter.notifyDataSetChanged();
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }
                    }).show();
        }


        if (id == 3) {
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
                                _filterList.get(3).setCurrentValue(r + (Prefs.isMetric(CreateFiltersActivity.this) ? getResources().getString(R.string.filter_radius_km) : getResources().getString(R.string.filter_radius_miles)));
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

        if (id == 4) {
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

                            _filterList.get(4).setCurrentValue(checkBoxesFilter.toLocalisedString(getResources()));
                            m_adapter.notifyDataSetChanged();
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).create();
        }

        if (id == 5) {
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

                            _filterList.get(5).setCurrentValue(cacheTypeList.toLocalisedString(getResources()));
                            m_adapter.notifyDataSetChanged();
                        }
                    }).setNeutralButton(R.string.any, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            CacheTypeList cacheTypeList = new CacheTypeList();
                            cacheTypeList.setAll();
                            Prefs.saveCacheTypeFilter(CreateFiltersActivity.this, cacheTypeList);

                            _filterList.get(5).setCurrentValue(cacheTypeList.toLocalisedString(getResources()));
                            m_adapter.notifyDataSetChanged();
                        }

                    }).create();
        }

        if (id == 6) {
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

                            _filterList.get(6).setCurrentValue(containerTypeList.toLocalisedString(getResources()));
                            m_adapter.notifyDataSetChanged();
                        }

                    }).setNeutralButton(R.string.any, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ContainerTypeList containerTypeList = new ContainerTypeList();
                            containerTypeList.setAll();
                            Prefs.saveContainerTypeFilter(CreateFiltersActivity.this, containerTypeList);

                            _filterList.get(6).setCurrentValue(containerTypeList.toLocalisedString(getResources()));
                            m_adapter.notifyDataSetChanged();
                        }

                    }).create();
        }

        if (id == 7)
            return CreateDialog(R.string.filter_terrain, Prefs.getTerrainFilter(this), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Prefs.saveTerrainFilter(CreateFiltersActivity.this, newOneToFiveFilter);
                    _filterList.get(7).setCurrentValue(newOneToFiveFilter.toLocalisedString(CreateFiltersActivity.this));
                    m_adapter.notifyDataSetChanged();
                }
            });

        if (id == 8)
            return CreateDialog(R.string.filter_difficulty, Prefs.getDifficultyFilter(this), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Prefs.saveDifficultyFilter(CreateFiltersActivity.this, newOneToFiveFilter);
                    _filterList.get(8).setCurrentValue(newOneToFiveFilter.toLocalisedString(CreateFiltersActivity.this));
                    m_adapter.notifyDataSetChanged();
                }
            });


        // Country list

        if (id == 9) {
            ListView countryView = new ListView(this);

            final CountryList selectedCountries = Prefs.getCountriesFilter(this);
            final CountryList allCountries = new CountryList(this);

            final CountryArrayAdapter countryAdapter = new CountryArrayAdapter(this, allCountries, selectedCountries);
            countryView.setAdapter(countryAdapter);
            countryView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // User clicked on a country. Have to manually add/remove from selected countries list
                    Country clickedCountry = allCountries.get(position);
                    if (selectedCountries.contains(clickedCountry))
                        selectedCountries.remove(clickedCountry);
                    else
                        selectedCountries.add(clickedCountry);

                    countryAdapter.notifyDataSetChanged();  // informs countryView to update. Array has changed
                }
            });

            return new AlertDialog.Builder(this)
                    .setTitle(R.string.filter_countries)
                    .setView(countryView)

                    .setNeutralButton(R.string.none, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            selectedCountries.clear();
                            Prefs.saveCountriesFilter(CreateFiltersActivity.this, selectedCountries);
                            _filterList.get(9).setCurrentValue(selectedCountries.toLocalisedString(getResources()));
                            m_adapter.notifyDataSetChanged();
                        }
                    })

                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Prefs.saveCountriesFilter(CreateFiltersActivity.this, selectedCountries);
                            _filterList.get(9).setCurrentValue(selectedCountries.toLocalisedString(getResources()));
                            m_adapter.notifyDataSetChanged();
                        }
                    })

                    .setNegativeButton(R.string.any, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Prefs.saveCountriesFilter(CreateFiltersActivity.this, allCountries);
                            _filterList.get(9).setCurrentValue(allCountries.toLocalisedString(getResources()));
                            m_adapter.notifyDataSetChanged();
                        }
                    })
                    .show();
        }

        Assert.assertTrue(false);
        return null;
    }


    @Override
    public void onResume() {
        super.onResume();
        startGps();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopGps();
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
                    bt.setText(Html.fromHtml(o.getCurrentValue()));//o.getCurrentValue());
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

        public void setCurrentValue(DaysToGenerateFilter daysToGenerateFilter, Resources resources) {
            this.currentValue = daysToGenerateFilter.toLocalisedString(resources);
        }

        public void setColor(int color) {
            this.color = color;
        }

        public int getColor() {
            return color;
        }


    }




    // Control GPS


    private void startGps() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ) {

            // Before android 6. Just ask for best location fix

            List<String> providers = locationManager.getAllProviders();
            for (String provider: providers) {
                locationManager.requestLocationUpdates(provider, 2000, 5, this);
            }

        } else {

            // Android 6 and above so have to worry about asking for permissions

            // Check if already have all permissions
            if (checkSelfPermission(ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED ||
                    checkSelfPermission(ACCESS_FINE_LOCATION)== PERMISSION_GRANTED) {

                gpsPermissionsAsked = true;
                gpsPermissionsGiven = new int[] {PERMISSION_GRANTED,PERMISSION_GRANTED };
            }

            // Only ask for permissions once or would repeatedly just get permission
            // dialog popping up
            if (gpsPermissionsAsked) {

                // OK we have already asked, have we received yet

                // Check if have received the 'async' permission response
                // (this could take some time to received as pops up dialog)
                if (gpsPermissionsGiven != null && gpsPermissionsGiven.length > 1) {
                    Criteria criteria = new Criteria();

                    if (gpsPermissionsGiven[1] == PERMISSION_GRANTED) {
                        criteria.setAccuracy(Criteria.ACCURACY_FINE);

                        String provider = locationManager.getBestProvider(criteria, true);
                        if  (provider != null) {
                            locationManager.requestLocationUpdates(provider, 2000, 5, this);
                        }
                    }

                    if (gpsPermissionsGiven[0] == PERMISSION_GRANTED) {
                        criteria.setAccuracy(Criteria.ACCURACY_COARSE);

                        String provider = locationManager.getBestProvider(criteria, true);
                        if  (provider != null) {
                            locationManager.requestLocationUpdates(provider, 2000, 5, this);
                        }
                    }

                    // else
                    // No permissions
                    // Not much we can do here. I think 'getBestProvider' will return null below


                } else {

                    // Must be waiting for response to 'ask permission' dialog
                }

            } else {
                // Not asked yet, so ask for permissions

                gpsPermissionsAsked = true;

                if (checkSelfPermission(ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED ||
                        checkSelfPermission(ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
                    requestPermissions(new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION}, 0);
                }

            }
        }

    }

    private void stopGps() {
        Logger.d("stop gps");
        try {
            locationManager.removeUpdates(this);
        } catch (SecurityException e) {
            Logger.d("failed to remove updates; " + e);
        }
    }

    /** Called asynchronously in response to our permissions request */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        // Show warning if don't have all permissions
        // Not the end of the world, as the user can proceed by selecting point on map
        if ((grantResults[0] != PERMISSION_GRANTED) || (grantResults[1]) != PERMISSION_GRANTED) {
            Toast toast = Toast.makeText(this, R.string.location_denied, Toast.LENGTH_LONG);
            toast.show();
        }

        gpsPermissionsGiven = grantResults;
        startGps();
    }











    public void onLocationChanged(Location gpsLocation) {
        if (gpsLocation.getProvider().equals(LocationManager.NETWORK_PROVIDER) &&
                this.gpsLocation != null &&
                this.gpsLocation.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            // don't over write GPS with network provider
        } else {
            this.gpsLocation = gpsLocation;

            // Update displayed accuracy in the "Origin" list item
            _filterList.get(2).setCurrentValue(queryStore.lat, queryStore.lon, gpsLocation);
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