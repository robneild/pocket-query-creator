package com.gisgraphy.gishraphoid;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.gisgraphy.domain.valueobject.GeoNamesAddressDto;
import com.gisgraphy.domain.valueobject.GeoNamesDto;
import com.gisgraphy.domain.valueobject.Street;

import android.location.Address;

public class AndroidAddressBuilder {

    public AndroidAddressBuilder(Locale locale) {
    }

    public AndroidAddressBuilder() {
    }

    public List<Address> transformStreetsToAndroidAddresses(Street[] result) {
        ArrayList<Address> ret = new ArrayList<Address>();
        for (Street street: result) {
            Address address = new Address(Locale.getDefault());
            if (street.name!=null)
                address.setAddressLine(0, street.name);
            if (street.isIn!=null)
                address.setLocality(street.isIn);
            ret.add(address);
        }
        return ret;
    }

    public List<Address> transformStreetsToAndroidAddresses(GeoNamesAddressDto[] result) {
        ArrayList<Address> ret = new ArrayList<Address>();
        for (GeoNamesAddressDto geoNamesAddress: result) {
            Address address = new Address(Locale.getDefault());
            if (geoNamesAddress.toponymName!=null)
                address.setLocality(geoNamesAddress.toponymName);
            ret.add(address);
        }
        return ret;
    }
}
