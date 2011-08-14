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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;

import android.os.Bundle;

public class QueryStore {

    public String name;
    public int radius;
    public double lat;
    public double lon;
    public boolean debug;
    
    public List <Cookie> cookies;

    public QueryStore() {
    }

    public QueryStore(Bundle bundle) {
        name = bundle.getString("QueryStore_name");
        radius = bundle.getInt("QueryStore_radius");
        lat = bundle.getDouble("QueryStore_lat");
        lon = bundle.getDouble("QueryStore_lon");

        debug = bundle.getBoolean("QueryStore_debug");
        
        // Restore cookie list from 2 arrays
        
        cookies = new ArrayList<Cookie>();

        ArrayList<String> names = bundle.getStringArrayList("QueryStore_cookie_names");
        ArrayList<String> values = bundle.getStringArrayList("QueryStore_cookie_values");

        for (int i=0; i<names.size(); i++) {
            BasicClientCookie c = new BasicClientCookie(names.get(i),values.get(i));
            c.setDomain("www.geocaching.com");
            c.setPath("/");
            cookies.add(c);
        }
    }


    public void saveToBundle(Bundle bundle) {
        bundle.putString("QueryStore_name", name);
        bundle.putInt("QueryStore_radius", radius);
        bundle.putDouble("QueryStore_lat", lat);
        bundle.putDouble("QueryStore_lon", lon);

        // Store the cookie list into 2 arrays
        
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<String> values = new ArrayList<String>();
        
        for (Cookie c: cookies) {
            names.add(c.getName());
            values.add(c.getValue());
        }
        
        bundle.putStringArrayList("QueryStore_cookie_names", names);
        bundle.putStringArrayList("QueryStore_cookie_values", values);
        
        bundle.putBoolean("QueryStore_debug", debug);
    }


}