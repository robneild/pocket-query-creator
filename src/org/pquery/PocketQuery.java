/* 
 * Copyright (C) 2011 Robert Neild
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.pquery;


import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class PocketQuery extends ListActivity {

    static final String[] OPTIONS = new String[] {"Create New Pocket Query", "Settings", "About"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Show list
        setListAdapter(new MyListAdapter(this)); // new ArrayAdapter<String>(this, R.layout.list_item, OPTIONS));

        ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        // List listener

        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0) {

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String username = prefs.getString("username_preference", "");
                    String password = prefs.getString("password_preference","");

                    if (username!=null && username.length()>0 &&
                            password!=null && password.length()>0) {


                        Intent myIntent = new Intent(view.getContext(), Dialog1.class);
                        startActivity(myIntent);
                    } else {
                        Toast.makeText(getApplicationContext(), "Enter your credentials on the settings page", Toast.LENGTH_LONG).show();
                    }
                }

                if (position == 1) {
                    Intent myIntent = new Intent(getApplicationContext(), PreferencesFromXml.class);
                    startActivity(myIntent);
                }

                if (position == 2) {
                    Intent myIntent = new Intent(getApplicationContext(), About.class);
                    startActivity(myIntent);
                }




            }
        });
    }





    class MyListAdapter extends BaseAdapter {
        public MyListAdapter(Context context) {
            mContext = context;
        }


        public int getCount() {
            return OPTIONS.length;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv;
            if (convertView == null) {
                tv = (TextView) LayoutInflater.from(mContext).inflate(android.R.layout.simple_expandable_list_item_1, parent, false);
            } else {
                tv = (TextView) convertView;
            }
            tv.setText(OPTIONS[position]);
            return tv;
        }

        private Context mContext;
    }



}