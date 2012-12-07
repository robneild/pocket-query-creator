package org.pquery;


import junit.framework.Assert;

import org.pquery.dao.QueryName;
import org.pquery.fragments.CreateCategoriesFragment;
import org.pquery.service.PQService;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class CreateActivity extends SherlockFragmentActivity implements CreateSettingsChangedListener {
    
    private QueryStore queryStore;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.create_pq);
        
        if (savedInstanceState!=null)
            queryStore = new QueryStore(savedInstanceState);
        else
            queryStore = new QueryStore();
        
        
        CreateCategoriesFragment fragment = (CreateCategoriesFragment) getSupportFragmentManager().findFragmentById(R.id.titles);
        Assert.assertNotNull(fragment);
        fragment.setInitialName(queryStore.name);
        fragment.setInitialLocation(queryStore.getLocation());
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        queryStore.saveToBundle(outState);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
            menu.add(0, R.string.create, 0, R.string.create)
            .setIcon(R.drawable.content_new)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
            
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
    public void onSettingsChange(Bundle bundle) {
        processBundle(bundle);
    }
    
    @Override
    protected void onActivityResult(int arg0, int arg1, Intent intent) {
        super.onActivityResult(arg0, arg1, intent);
        if (intent!=null) {
            Bundle bundle = intent.getExtras();
            processBundle(bundle);
        }
    }

    private void processBundle(Bundle bundle) {
        Location location = bundle.getParcelable("location");
        QueryName queryName = bundle.getParcelable("queryName");
        
        if (queryName!=null) {
            queryStore.name = queryName.name;
        }
        if (location!=null) {
            queryStore.lat = location.getLatitude();
            queryStore.lon = location.getLongitude();
        }
        
        CreateCategoriesFragment fragment = (CreateCategoriesFragment) getSupportFragmentManager().findFragmentById(R.id.titles);
        Assert.assertNotNull(fragment);
        fragment.setInitialName(queryStore.name);
        fragment.setInitialLocation(queryStore.getLocation());
    }
}
