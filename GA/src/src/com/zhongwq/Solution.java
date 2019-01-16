package com.zhongwq;

import org.omg.CORBA.MARSHAL;

import java.util.ArrayList;

public class Solution implements Comparable<Solution> {
    public ArrayList<City> cities;
    public double cost;
    public double reproducePercent;

    public Solution(ArrayList<City> cities) {
        this.cities = new ArrayList<>();
        this.cities.addAll(cities);
    }

    public void getGreedySolution() {
        int index = (int) (Math.random() * cities.size());
        boolean visited[] = new boolean[cities.size()];
        ArrayList<City> newCities = new ArrayList<>();
        visited[index] = true;
        double min;
        int minPos = -1;
        newCities.add(cities.get(index));
        while (newCities.size() < cities.size()) {
            min = Double.MAX_VALUE;
            minPos = -1;
            for (int i = 0; i < cities.size(); i++) {
                double dist = cities.get(index).getDist(cities.get(i));
                if (!visited[i] && dist < min) {
                    min = dist;
                    minPos = i;
                }
            }
            newCities.add(cities.get(minPos));
            visited[minPos] = true;
            index = minPos;
        }
        this.cities = newCities;
        this.cost = getDistance();
        System.out.println("Init: " + this.cost);
    }

    public boolean equalTo(ArrayList<City> cities) {
        for (int i = 0; i < this.cities.size(); i++) {
            if (this.cities.get(i).index != cities.get(i).index) {
                return false;
            }
        }
        return true;
    }

    public Solution clone() {
        ArrayList<City> citiesClone = new ArrayList<>();
        citiesClone.addAll(this.cities);
        Solution clone = new Solution(citiesClone);
        clone.cost = this.cost;
        clone.reproducePercent = this.reproducePercent;
        return clone;
    }

    @Override
    public int compareTo(Solution o) {
        return this.sign(this.cost - o.cost);
    }

    private int sign(double x) {
        return (x > 0) ? 1 : ((x == 0) ? 0 : -1);
    }

    public double getDistance(){
        int tourDistance = 0;
        for (int cityIndex=0; cityIndex < cities.size(); cityIndex++) {
            City fromCity = cities.get(cityIndex);
            City destinationCity;
            if(cityIndex+1 < cities.size()){
                destinationCity =  cities.get(cityIndex+1);
            }
            else{
                destinationCity =  cities.get(0);
            }
            tourDistance += fromCity.getDist(destinationCity);
        }
        return tourDistance;
    }


    @Override
    public String toString() {
        String geneString = "|";
        for (int i = 0; i < cities.size(); i++) {
            geneString += cities.get(i)+"|";
        }
        return geneString;
    }
}
