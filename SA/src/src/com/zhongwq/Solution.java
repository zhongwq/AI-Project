package com.zhongwq;

import java.util.ArrayList;
import java.util.Collections;

public class Solution {
    private ArrayList<City> citiesList;
    private int distance = 0;

    public Solution() {
        citiesList = new ArrayList<City>();
    }


    /**
     * 初始化城市列表
     * */
    public Solution(ArrayList<City> tour){
        citiesList = new ArrayList<City>();
        for (City city : tour) {
            this.citiesList.add(city);
        }

    }

    /***
     * 返回城市列表
     * @return
     */
    public ArrayList<City> getCitiesList(){
        return citiesList;
    }

    /***
     * 创建一个随机初始解
     */
    public Solution getRandomInitial() {
        for (int cityIndex = 0; cityIndex < citiesList.size(); cityIndex++) {
            setCity(cityIndex, this.getCity(cityIndex));
        }
        Collections.shuffle(citiesList);
        return this;
    }

    /***
     * 获取邻居
     * @return
     */
    public Solution generateNeighour() {
        int choice = (int) (2 * Math.random());
        switch (choice) {
            case 0:
                return generateNeighourTourWith2Opt();
            case 1:
                return generateNeighourTourWithExchange();
        }
        return null;
    }

    /***
     * 领域操作: 交换两个城市
     * */
    public Solution generateNeighourTourWithExchange(){
        Solution newSolution = new Solution(this.citiesList);
        int tourPos1 = (int) (newSolution.numberOfCities() * Math.random());
        int tourPos2 = (int) (newSolution.numberOfCities() * Math.random());
        City citySwap1 = newSolution.getCity(tourPos1);
        City citySwap2 = newSolution.getCity(tourPos2);
        newSolution.setCity(tourPos2, citySwap1);
        newSolution.setCity(tourPos1, citySwap2);

        return newSolution;
    }


    /***
     * 领域操作: 2-opt
     * @return
     */
    public Solution generateNeighourTourWith2Opt(){
        Solution newSolution = new Solution(this.citiesList);
        // Get a random positions in the tour
        int tourPos1 = (int) (newSolution.numberOfCities() * Math.random());
        int tourPos2 = (int) (newSolution.numberOfCities() * Math.random());
        while (tourPos1 == tourPos2) {
            tourPos1 = (int) (newSolution.numberOfCities() * Math.random());
            tourPos2 = (int) (newSolution.numberOfCities() * Math.random());
        }
        if (tourPos1 > tourPos2) {
            int tmp = tourPos1;
            tourPos1 = tourPos2;
            tourPos2 = tmp;
        }

        while (tourPos1 < tourPos2) {
            // Get the cities at selected positions in the tour
            City citySwap1 = newSolution.getCity(tourPos1);
            City citySwap2 = newSolution.getCity(tourPos2);

            // Swap them
            newSolution.setCity(tourPos2, citySwap1);
            newSolution.setCity(tourPos1, citySwap2);

            tourPos1++;
            tourPos2--;
        }

        return newSolution;
    }

    public City getCity(int tourPosition) {
        return (City)citiesList.get(tourPosition);
    }

    public void setCity(int tourPosition, City city) {
        citiesList.set(tourPosition, city);
        distance = 0; // 表示距离要计算
    }

    public Solution addCity(City city) {
        citiesList.add(city);
        return this;
    }

    public ArrayList<City> getAllCities() {
        return citiesList;
    }

    public int getDistance(){
        if (distance == 0) {
            int tourDistance = 0;
            for (int cityIndex=0; cityIndex < numberOfCities(); cityIndex++) {
                City fromCity = getCity(cityIndex);
                City destinationCity;
                if(cityIndex+1 < numberOfCities()){
                    destinationCity = getCity(cityIndex+1);
                }
                else{
                    destinationCity = getCity(0);
                }
                tourDistance += fromCity.getDist(destinationCity);
            }
            distance = tourDistance;
        }
        return distance;
    }

    public int numberOfCities() {
        return citiesList.size();
    }

    @Override
    public String toString() {
        String geneString = "|";
        for (int i = 0; i < numberOfCities(); i++) {
            geneString += getCity(i)+"|";
        }
        return geneString;
    }
}
