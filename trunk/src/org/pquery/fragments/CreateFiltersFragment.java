package org.pquery.fragments;

import java.util.ArrayList;

import junit.framework.Assert;

import org.pquery.CreateSettingsChangedListener;
import org.pquery.Dialog3;
import org.pquery.DiscreteRangeSeekBar;
import org.pquery.CreateFiltersActivity;
import org.pquery.QueryStore;
import org.pquery.R;
import org.pquery.RangeSeekBar;
import org.pquery.filter.CacheType;
import org.pquery.filter.CacheTypeList;
import org.pquery.filter.CheckBoxesFilter;
import org.pquery.filter.ContainerType;
import org.pquery.filter.ContainerTypeList;
import org.pquery.filter.OneToFiveFilter;
import org.pquery.fragments.PQListFragment.PQClickedListener;
import org.pquery.util.Prefs;

import com.actionbarsherlock.app.SherlockListFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;

public class CreateFiltersFragment extends SherlockListFragment {

    private ArrayList<FilterOption> _filterList;
    private OrderAdapter m_adapter;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle data) {

        View view = inflater.inflate(R.layout.dialog2, null);

        // Create list of filters, adding each one

        _filterList = new ArrayList<FilterOption>();
        this.m_adapter = new OrderAdapter(getActivity(), R.layout.dialog2_1_list, _filterList);
        setListAdapter(this.m_adapter);

        _filterList = new ArrayList<FilterOption>();

        FilterOption filterOption = new FilterOption(getResources().getString(R.string.filter_checkboxes), R.drawable.tick,
                getResources());
        filterOption.setFilterValue(Prefs.getCheckBoxesFilter(getActivity()), getResources());
        _filterList.add(filterOption);

        filterOption = new FilterOption(getResources().getString(R.string.filter_cache_type), R.drawable.ghost,
                getResources());
        filterOption.setFilterValue(Prefs.getCacheTypeFilter(getActivity()));
        _filterList.add(filterOption);

        filterOption = new FilterOption(getResources().getString(R.string.filter_container),
                R.drawable.container, getResources());
        filterOption.setFilterValue(Prefs.getContainerTypeFilter(getActivity()));
        _filterList.add(filterOption);

        filterOption = new FilterOption(getResources().getString(R.string.filter_terrain), R.drawable.mountain,
                getResources());
        filterOption.setFilterValue(Prefs.getTerrainFilter(getActivity()));
        _filterList.add(filterOption);

        filterOption = new FilterOption(getResources().getString(R.string.filter_difficulty), R.drawable.maze,
                getResources());
        filterOption.setFilterValue(Prefs.getDifficultyFilter(getActivity()));
        _filterList.add(filterOption);


        if (_filterList != null && _filterList.size() > 0) {
            m_adapter.notifyDataSetChanged();
            for (int i = 0; i < _filterList.size(); i++)
                m_adapter.add(_filterList.get(i));
        }
        // m_ProgressDialog.dismiss();
        m_adapter.notifyDataSetChanged();

//        ListView lv = (ListView) view.findViewById(android.R.id.list);
//        lv.setTextFilterEnabled(true);
//
//        // List listener
//        lv.setOnItemClickListener(new OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                createDialog(position).show();
//            }
//        });

        // Handle next button
        // Goes onto next stage of wizard

        //            Button nextButton = (Button) view.findViewById(R.id.button_next);
        //
        //            nextButton.setOnClickListener(new View.OnClickListener() {
        //                public void onClick(View view) {
        //
        //                    // Go onto next wizard page; pass current values in QueryStore
        //
        //                    Bundle bundle = new Bundle();
        //                    queryStore.saveToBundle(bundle);
        //
        //                    Intent myIntent = new Intent(view.getContext(), Dialog3.class);
        //                    myIntent.putExtra("QueryStore", bundle);
        //                    startActivity(myIntent);
        //                    finish();
        //                }
        //            });

        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        createDialog(position).show();
    }
    
    private Dialog createDialog(int id) {

        if (id == 0) {
            // Cache type
            CheckBoxesFilter checkBoxesFilter = Prefs.getCheckBoxesFilter(getActivity());
            String[] options = checkBoxesFilter.getOptions(getResources());
            final boolean[] selections = checkBoxesFilter.getAsBooleanArray();

            return new AlertDialog.Builder(getActivity()).setTitle(R.string.filter_checkboxes)
                    .setMultiChoiceItems(options, selections, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        }
                    }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            CheckBoxesFilter checkBoxesFilter = new CheckBoxesFilter(selections);
                            Prefs.saveCheckBoxesFilter(getActivity(), checkBoxesFilter);

                            _filterList.get(0).setFilterValue(checkBoxesFilter, getResources());
                            m_adapter.notifyDataSetChanged();
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).create();
        }

        if (id == 1) {
            // Cache type 
            CharSequence[] options = new CharSequence[CacheType.values().length];
            for (int i = 0; i < options.length; i++) {
                options[i] = getResources().getString(CacheType.values()[i].getResourceId());
            }
            final boolean[] selections = Prefs.getCacheTypeFilter(getActivity()).getAsBooleanArray();

            return new AlertDialog.Builder(getActivity()).setTitle(R.string.filter_cache_type)
                    .setMultiChoiceItems(options, selections, new DialogInterface.OnMultiChoiceClickListener() { 
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        }
                    }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            CacheTypeList cacheTypeList = new CacheTypeList(selections);
                            Prefs.saveCacheTypeFilter(getActivity(), cacheTypeList);

                            _filterList.get(1).setFilterValue(cacheTypeList);
                            m_adapter.notifyDataSetChanged();
                        }
                    }).setNeutralButton(R.string.any, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            CacheTypeList cacheTypeList = new CacheTypeList();
                            cacheTypeList.setAll();
                            Prefs.saveCacheTypeFilter(getActivity(), cacheTypeList);

                            _filterList.get(1).setFilterValue(cacheTypeList);
                            m_adapter.notifyDataSetChanged();
                        }

                    }).create();
        }

        if (id == 2) {
            // Container type

            CharSequence[] options = new CharSequence[ContainerType.values().length];
            for (int i = 0; i < options.length; i++) {
                options[i] = getResources()
                        .getString(ContainerType.values()[i].getResourceId());
            }
            final boolean[] selections = Prefs.getContainerTypeFilter(getActivity()).getAsBooleanArray();

            return new AlertDialog.Builder(getActivity()).setTitle(R.string.filter_container)
                    .setMultiChoiceItems(options, selections, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        }
                    }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ContainerTypeList containerTypeList = new ContainerTypeList(selections);
                            Prefs.saveContainerTypeFilter(getActivity(), containerTypeList);

                            _filterList.get(2).setFilterValue(containerTypeList);
                            m_adapter.notifyDataSetChanged();
                        }

                    }).setNeutralButton(R.string.any, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ContainerTypeList containerTypeList = new ContainerTypeList();
                            containerTypeList.setAll();
                            Prefs.saveContainerTypeFilter(getActivity(), containerTypeList);

                            _filterList.get(2).setFilterValue(containerTypeList);
                            m_adapter.notifyDataSetChanged();
                        }

                    }).create();
        }

        if (id == 3)
            return CreateDialog(R.string.filter_terrain, Prefs.getTerrainFilter(getActivity()), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Prefs.saveTerrainFilter(getActivity(), newOneToFiveFilter);
                    _filterList.get(3).setFilterValue(newOneToFiveFilter);
                    m_adapter.notifyDataSetChanged();
                }
            } ); 

        if (id == 4)
            return CreateDialog(R.string.filter_difficulty, Prefs.getDifficultyFilter(getActivity()), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Prefs.saveDifficultyFilter(getActivity(), newOneToFiveFilter);
                    _filterList.get(4).setFilterValue(newOneToFiveFilter);
                    m_adapter.notifyDataSetChanged();
                }
            } );

        Assert.assertTrue(false);
        return null;
    }

    private class OrderAdapter extends ArrayAdapter<FilterOption> {

        private ArrayList<FilterOption> items;

        public OrderAdapter(Context context, int textViewResourceId, ArrayList<FilterOption> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.dialog2_1_list, null);
            }
            FilterOption o = items.get(position);
            if (o != null) {

                ImageView icon = (ImageView) v.findViewById(R.id.icon);
                icon.setImageResource(o.getIconRes());

                TextView tt = (TextView) v.findViewById(R.id.toptext);
                TextView bt = (TextView) v.findViewById(R.id.bottomtext);
                if (tt != null) {
                    tt.setText(o.getFilterName());
                }
                if (bt != null) {
                    bt.setText(o.getFilterValue());
                    bt.setTextColor(o.getColor());
                }
            }
            return v;
        }
    }

    private OneToFiveFilter newOneToFiveFilter;

    private AlertDialog CreateDialog(int titleRes, OneToFiveFilter initialValue, DialogInterface.OnClickListener okHandler) {

        // Create custom view containing max/min drag bar

        newOneToFiveFilter = initialValue;
        DiscreteRangeSeekBar<Integer> seekBar = new DiscreteRangeSeekBar<Integer>(1, 5, 1, getActivity());

        LinearLayout view = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.dialog_signin, null);

        // Manually specify a margin round the drag bar else it is a bit difficult to touch
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        layout.setMargins(px,px,px,px);

        view.addView(seekBar, layout);

        // Create dialog

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

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
}

class FilterOption {

    private String filterName;
    private String filterValue;
    private int iconRes;
    private int color = Color.BLUE;
    private Resources res;

    FilterOption(String filterName, int iconRes, Resources res) {
        this.filterName = filterName;
        this.iconRes = iconRes;
        this.res = res;
    }

    public String getFilterName() {
        return filterName;
    }

    public int getIconRes() {
        return iconRes;
    }

    public String getFilterValue() {
        return filterValue;
    }

    public void setFilterValue(String filterValue) {
        this.filterValue = filterValue;
    }

    public void setFilterValue(CheckBoxesFilter checkBoxesFilter, Resources res) {
        this.filterValue = checkBoxesFilter.toLocalisedString(res);
        this.color = Color.MAGENTA;
    }

    public void setFilterValue(OneToFiveFilter f) {
        this.filterValue = f.toString();
        if (f.isAll())
            this.color = Color.GREEN;
        else
            this.color = Color.MAGENTA;
    }

    public void setFilterValue(CacheTypeList cacheTypeList) {
        this.filterValue = cacheTypeList.toLocalisedString(res);
        if (cacheTypeList.isAll()) {
            this.color = Color.GREEN;
            this.filterValue = res.getString(R.string.any);
        } else
            this.color = Color.MAGENTA;
    }

    public void setFilterValue(ContainerTypeList containerList) {
        this.filterValue = containerList.toLocalisedString(res);
        if (containerList.isAll()) {
            this.color = Color.GREEN;
            this.filterValue = res.getString(R.string.any);
        } else
            this.color = Color.MAGENTA;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}



