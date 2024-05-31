package com.example.vectorsense;

public class CurrentAttLocation {
    public CurrentAttLocation(){}

    public CurrentAttLocation(double lati, double longi , String base64) {
        this.lati = lati;
        this.longi = longi;
        this.image = base64;
    }

    public  double lati;
    public  double longi;
    public  String image;
}
