package org.pquery;

import junit.framework.Assert;

import org.pquery.fragments.CreateFiltersFragment;
import org.pquery.fragments.CreateNameFragment;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class CreateNameActivity extends SherlockFragmentActivity implements CreateSettingsChangedListener {

    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        String initialName = getIntent().getExtras().getString("initialName");
        Assert.assertNotNull(initialName);
        
        if (savedInstanceState == null) {
            // During initial setup, plug in the details fragment.
            CreateNameFragment details = new CreateNameFragment(initialName);
            //details.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, details).commit();
        }

    }
    
    @Override
    public void onSettingsChange(Bundle bundle) {
        setResult(RESULT_OK, new Intent().putExtras(bundle));
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
