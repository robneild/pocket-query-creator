package org.pquery;

import org.pquery.R;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class TestFragment extends SherlockFragmentActivity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.test_fragment_container);
        
//        if (getSupportFragmentManager().findFragmentById(android.R.id.content) == null) {
//            ArrayListFragment list = new ArrayListFragment();
//            getSupportFragmentManager().beginTransaction().add(android.R.id.content, list).commit();
//        }
    }

   
}