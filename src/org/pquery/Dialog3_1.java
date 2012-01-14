package org.pquery;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.pquery.Dialog2.LoginAsync;
import org.pquery.util.CacheTypeList;
import org.pquery.util.ContainerTypeList;
import org.pquery.util.Prefs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Handle adding filters
 *
 */
public class Dialog3_1 extends ListActivity {


    private ArrayList<FilterOption> _filterList;
    private OrderAdapter m_adapter;

    // Wizard state

    private QueryStore queryStore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog3_1);

        Context cxt = getApplicationContext();

        // Get parameters passed from previous wizard stage

        Bundle bundle = getIntent().getBundleExtra("QueryStore");
        Assert.assertNotNull(bundle);
        queryStore = new QueryStore(bundle);

        queryStore.cacheTypeList = Prefs.getCacheTypeFilter(cxt);
        queryStore.containerTypeList = Prefs.getContainerTypeFilter(cxt);

        _filterList = new ArrayList<FilterOption>();
        this.m_adapter = new OrderAdapter(this, R.layout.dialog3_1_list, _filterList);
        setListAdapter(this.m_adapter);


        _filterList = new ArrayList<FilterOption>();

        FilterOption o1 = new FilterOption(cxt.getResources().getString(R.string.filter_cache_type), R.drawable.ghost, cxt.getResources());
        o1.setFilterValue(Prefs.getCacheTypeFilter(cxt));

        FilterOption o2 = new FilterOption(cxt.getResources().getString(R.string.filter_container), R.drawable.container, cxt.getResources());
        o2.setFilterValue(Prefs.getContainerTypeFilter(cxt));

        _filterList.add(o1);
        _filterList.add(o2);

        if(_filterList != null && _filterList.size() > 0){
            m_adapter.notifyDataSetChanged();
            for(int i=0;i<_filterList.size();i++)
                m_adapter.add(_filterList.get(i));
        }
        //m_ProgressDialog.dismiss();
        m_adapter.notifyDataSetChanged();

        ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        // List listener

        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                createDialog(position).show();
            }});


        // Handle next button
        // Goes onto next stage of wizard

        Button nextButton = (Button) findViewById(R.id.button_next);

        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                // Go onto next wizard page; pass current values in QueryStore

                Bundle bundle = new Bundle();
                queryStore.saveToBundle(bundle);

                Intent myIntent = new Intent(view.getContext(), Dialog4.class);
                myIntent.putExtra("QueryStore", bundle);
                startActivity(myIntent);
                finish();
            }
        });

        // Handle cancel button
        // Just closes the activity

        Button cancelButton = (Button) findViewById(R.id.button_cancel);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });
    }
    
    private Dialog createDialog(int id) {

        if (id==0) {
            // Cache type

            CharSequence[] options = new CharSequence[ CacheType.values().length ];
            for (int i=0; i<options.length; i++) {
                options[i] = getApplicationContext().getResources().getString(CacheType.values()[i].getResourceId());
            }
            final boolean[] selections = queryStore.cacheTypeList.getAsBooleanArray();

            return 
                    new AlertDialog.Builder( this )
            .setTitle(R.string.filter_cache_type)
            .setMultiChoiceItems( options, selections, new DialogInterface.OnMultiChoiceClickListener() {
                @Override public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                }
            })
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {
                    queryStore.cacheTypeList = new CacheTypeList(selections);
                    Prefs.saveCacheTypeFilter(getApplicationContext(), queryStore.cacheTypeList);

                    _filterList.get(0).setFilterValue(queryStore.cacheTypeList);
                    m_adapter.notifyDataSetChanged();
                }
            })
            .setNeutralButton(R.string.any, new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {
                    queryStore.cacheTypeList.setAll();
                    Prefs.saveCacheTypeFilter(getApplicationContext(), queryStore.cacheTypeList);

                    _filterList.get(0).setFilterValue(queryStore.cacheTypeList);
                    m_adapter.notifyDataSetChanged();
                }

            })
            .create();
        }

        if (id==1) {
            // Container type

            CharSequence[] options = new CharSequence[ContainerType.values().length];
            for (int i=0; i<options.length; i++) {
                options[i] = getApplicationContext().getResources().getString(ContainerType.values()[i].getResourceId());
            }
            final boolean[] selections = queryStore.containerTypeList.getAsBooleanArray();

            return 
                    new AlertDialog.Builder( this )
            .setTitle(R.string.filter_container)
            .setMultiChoiceItems( options, selections, new DialogInterface.OnMultiChoiceClickListener() {
                @Override public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                }
            })
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {
                    queryStore.containerTypeList = new ContainerTypeList(selections);
                    Prefs.saveContainerTypeFilter(getApplicationContext(), queryStore.containerTypeList);

                    _filterList.get(1).setFilterValue(queryStore.containerTypeList);
                    m_adapter.notifyDataSetChanged();
                }

            })
            .setNeutralButton(R.string.any, new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {
                    queryStore.containerTypeList.setAll();
                    Prefs.saveContainerTypeFilter(getApplicationContext(), queryStore.containerTypeList);

                    _filterList.get(1).setFilterValue(queryStore.containerTypeList);
                    m_adapter.notifyDataSetChanged();
                }

            })
            .create();
        }

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
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.dialog3_1_list, null);
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
                if(bt != null){
                    bt.setText(o.getFilterValue());
                    bt.setTextColor(o.getColor());
                }
            }
            return v;
        }
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
