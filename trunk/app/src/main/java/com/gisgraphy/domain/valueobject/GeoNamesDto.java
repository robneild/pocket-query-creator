package com.gisgraphy.domain.valueobject;

public class GeoNamesDto {

    public GeoNamesAddressDto[] geonames;

    public GeoNamesAddressDto[] getResult() {
        return geonames;
    }
}
