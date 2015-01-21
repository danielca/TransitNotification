package ca.tansitnotificaion;

/**
 * Created by Casey on 14/12/2014.
 * Stop class to easly handle stop data
 */
public class Stop {
    private String stop;
    private String name;
    private String Lat;
    private String Lon;

    public String getStopNumber() {
        return this.stop;
    }
    public void setStopNumber(String newStop) {
        this.stop = newStop;
    }

    public String getStopName() {
        return this.name;
    }
    public void setName(String newName) {
        this.name = newName;
    }

    public void setLat(String newLat) {
        this.Lat = newLat;
    }
    public String getLat() {
        return this.Lat;
    }

    public void setLon(String newLon) {
        this.Lon = newLon;
    }
    public String getLon(){
        return this.Lon;
    }

    @Override
    public String toString() {
        return "Number: " + this.getStopNumber() + " Name: " + this.getStopName() + " Lat: " + this.getLat() + " Lon: " + getLon();
    }

}
