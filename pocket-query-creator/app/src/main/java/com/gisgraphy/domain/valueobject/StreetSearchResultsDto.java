package com.gisgraphy.domain.valueobject;

public class StreetSearchResultsDto {

    /**
     * {"numFound":50,"QTime":252,"result":[{"name":"Newton Street","distance":36.3294330748716,"gid":106603218,"openstreetmapId":16978709,"streetType":"RESIDENTIAL","oneWay":false,"countryCode":"US","length":601.901637459321,"lat":39.83543539626998,"lng":-105.03742540092145,"isIn":"Westminster"},
     */

    public int numFound;
    public Street[] result;


    public Street[] getResult() {
        return result;
    }
}
