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

import android.location.Location;
import android.os.Bundle;

import java.text.DateFormat;
import java.util.Date;

public class QueryStore {

    public String name;
    public double lat;
    public double lon;
    public boolean debug;

    // Filters
    // If empty assume match all

    //public CacheTypeList cacheTypeList = new CacheTypeList();
    //public ContainerTypeList containerTypeList  = new ContainerTypeList();
    //public OneToFiveFilter terrainFilter = new OneToFiveFilter();
    //public OneToFiveFilter difficultyFilter = new OneToFiveFilter();

    public QueryStore() {
        //radius = Prefs.getRadius(cxt);
        //cacheTypeList = Prefs.getCacheTypeFilter(cxt);
        //containerTypeList = Prefs.getContainerTypeFilter(cxt);
        //terrainFilter = Prefs.getTerrainFilter(cxt);
        //difficultyFilter = Prefs.getDifficultyFilter(cxt);

        name = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date());
        name = name.replaceAll(":", ".");
        name = name.replaceAll("/", "-");
    }


    public QueryStore(Bundle bundle) {
        name = bundle.getString("QueryStore_name");
        //radius = bundle.getInt("QueryStore_radius");
        lat = bundle.getDouble("QueryStore_lat");
        lon = bundle.getDouble("QueryStore_lon");

        debug = bundle.getBoolean("QueryStore_debug");

        // Restore cookie list from 2 arrays

        //viewStateMap = new HashMap<String,String>();

        //ArrayList<String> names = bundle.getStringArrayList("QueryStore_viewStateMap_names");
        //ArrayList<String> values = bundle.getStringArrayList("QueryStore_viewStateMap_values");

        //for (int i=0; i<names.size(); i++) {
        //    viewStateMap.put(names.get(i), values.get(i));
        //}


//        // Retrieve cache type filter
//        
//        cacheTypeList = new CacheTypeList(bundle.getString("QueryStore_cacheType"));
//        
//        // Retrieve container type filter
//        
//        containerTypeList = new ContainerTypeList(bundle.getString("QueryStore_containerType"));
//
//        difficultyFilter = new OneToFiveFilter(bundle.getString("QueryStore_difficulty"));
//        terrainFilter = new OneToFiveFilter(bundle.getString("QueryStore_terrain"));
    }


    public void saveToBundle(Bundle bundle) {
        bundle.putString("QueryStore_name", name);
        //bundle.putInt("QueryStore_radius", radius);
        bundle.putDouble("QueryStore_lat", lat);
        bundle.putDouble("QueryStore_lon", lon);

        // Store the cookie list into 2 arrays

        //ArrayList<String> names = new ArrayList<String>();
        //ArrayList<String> values = new ArrayList<String>();

        //for (String c: viewStateMap.keySet()) {
        //    names.add(c);
        //    values.add(viewStateMap.get(c));
        //}

        //bundle.putStringArrayList("QueryStore_viewStateMap_names", names);
        //bundle.putStringArrayList("QueryStore_viewStateMap_values", values);


        bundle.putBoolean("QueryStore_debug", debug);

        // Store cache type filter

//        bundle.putString("QueryStore_cacheType", cacheTypeList.toString());
//        
//        // Store container type filter
//        
//        bundle.putString("QueryStore_containerType", containerTypeList.toString());
//        
//        bundle.putString("QueryStore_difficulty", difficultyFilter.toString());
//        bundle.putString("QueryStore_terrain", terrainFilter.toString());
    }

    public boolean haveLocation() {
        if (lat == 0 || lat == 0)
            return false;
        return true;
    }

    public Location getLocation() {
        Location ret = new Location("rob");
        ret.setLatitude(lat);
        ret.setLongitude(lon);
        return ret;
    }
}
