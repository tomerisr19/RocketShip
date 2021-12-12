package com.example.fluppygamenew;

public class Record implements Comparable<Record> {
    private long score = 0;
    private double lat=0.0;
    private double lon=0.0;

    public Record() {
    }

    public Record setScore(long score) {
        this.score = score;
        return this;
    }

    public long getScore(){
        return score;
    }

    public double getLat() {
        return lat;
    }

    public Record setLat(double lat) {
        this.lat = lat;
        return this;
    }

    public double getLon() {
        return lon;
    }

    public Record setLon(double lon) {
        this.lon = lon;
        return this;
    }

    @Override
    public String toString() {
        return "Your Score: " + score;
    }

    @Override
    public int compareTo(Record o) {
        return (int)(o.getScore() - this.getScore());
    }


}