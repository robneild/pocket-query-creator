package org.pquery;

import java.util.ArrayList;
import junit.framework.Assert;

import org.pquery.R;
import org.pquery.filter.CacheType;
import org.pquery.filter.CacheTypeList;
import org.pquery.filter.CheckBoxesFilter;
import org.pquery.filter.ContainerType;
import org.pquery.filter.ContainerTypeList;
import org.pquery.filter.OneToFiveFilter;
import org.pquery.fragments.CreateCategoriesFragment;
import org.pquery.fragments.CreateFiltersFragment;
import org.pquery.util.Prefs;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Handle adding filters
 * 
 */
public class CreateFiltersActivity extends SherlockFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        if (savedInstanceState == null) {
            // During initial setup, plug in the details fragment.
            CreateFiltersFragment details = new CreateFiltersFragment();
            details.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, details).commit();
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            Intent intent = new Intent(this, Main.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}