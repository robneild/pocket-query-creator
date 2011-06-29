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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.os.Bundle;

public class QueryStore {

    public String name;
    public int radius;
    public double lat;
    public double lon;

    public Map <String,String> cookies;

    public QueryStore() {
    }

    public QueryStore(Bundle bundle) {
        name = bundle.getString("QueryStore_name");
        radius = bundle.getInt("QueryStore_radius");
        lat = bundle.getDouble("QueryStore_lat");
        lon = bundle.getDouble("QueryStore_lon");

        cookies = new HashMap<String,String>();

        String[] keys = bundle.getStringArray("QueryStore_cookie_keys");
        String[] values = bundle.getStringArray("QueryStore_cookie_values");

        for (int i=0; i<keys.length; i++) {
            cookies.put(keys[i], values[i]);
        }
    }


    public void saveToBundle(Bundle bundle) {
        bundle.putString("QueryStore_name", name);
        bundle.putInt("QueryStore_radius", radius);
        bundle.putDouble("QueryStore_lat", lat);
        bundle.putDouble("QueryStore_lon", lon);

        bundle.putStringArray("QueryStore_cookie_keys", cookies.keySet().toArray(new String[0]));
        bundle.putStringArray("QueryStore_cookie_values", cookies.values().toArray(new String[0]));

    }

}
