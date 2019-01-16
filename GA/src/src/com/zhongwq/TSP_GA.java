package com.zhongwq;

import java.io.*;
import java.util.*;

public class TSP_GA {
    private final double variable_percent = 0.01; //变异概率
    private final int inheritance_number = 10000; //遗传次数

    private final int city_number = 198; //城市数量
    private final int entity_number = 200; //个体数量
    private final String data_path = "test.txt";

    private boolean show = false;

    private Solution[] solutions;
    private ArrayList<Solution> sonSolutions; // 子代

    private ArrayList<City> cities;

    private double minLength = Double.MAX_VALUE;
    private Solution bestSolution = null;
    private int bestInheritance = 0;

    private void init(boolean show) {
        cities = new ArrayList<>();
        this.show = show;
        try {
            BufferedReader data = new BufferedReader(new InputStreamReader(
                    new FileInputStream(this.data_path)));
            for (int i = 0; i < this.city_number; i++) {
                String strbuff = data.readLine();
                // 字符分割
                String [] arr = strbuff.split("\\s+");
                int index = 0;
                while (arr[index].equals("")) {
                    index++;
                }
                cities.add(new City(Integer.valueOf(arr[index]), Double.valueOf(arr[index+1]), Double.valueOf(arr[index+2])));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.initSolutions(cities);
    }

    private void initSolutions(ArrayList<City> cities) { // 初始化种群
        this.solutions = new Solution[this.entity_number];
        for (int i = 0; i < (int) (this.entity_number * 0.95); i++) {
            Collections.shuffle(cities);
            while (isExist(cities, solutions)) {
                Collections.shuffle(cities);
            }
            this.solutions[i] = new Solution(cities);
            this.solutions[i].cost = this.solutions[i].getDistance();
        }
        for (int i = (int) (this.entity_number * 0.95); i < this.entity_number; i++) {
            this.solutions[i] = new Solution(cities);
            this.solutions[i].getGreedySolution();
        }
        this.setReproducePercent();
    }

    private boolean isExist(ArrayList<City> random_Cities, Solution[] solutions) {
        for (int i = 0; i < solutions.length; i++) {
            if (solutions[i] == null) {
                return false;
            }
            if (solutions[i].equalTo(random_Cities))
                return true;
            else
                return false;
        }
        return false;
    }

    public void setReproducePercent() { // 进行归一化
        double sumLengthToOne = 0.0;
        for (Solution solution : this.solutions) {
            sumLengthToOne += 1/solution.cost;
        }

        for (Solution solution : this.solutions) {
            solution.reproducePercent = (1 / solution.cost) / sumLengthToOne;
        }
    }

    private void genetic() {
        Convergence convergence = null;
        TourWindow tourWindow = null;

        if (show) {
            convergence = new Convergence("遗传算法收敛过程");
            convergence.setY_Coordinate_Name("所有路径和 ");
            convergence.setVisible(true);

            tourWindow = new TourWindow("路径变化过程", 5);
            tourWindow.setVisible(true);
            tourWindow.setBounds(0, 0, 1000, 700);
            tourWindow.setLocation(800, 0);
        }

        long tp = 0;
        for (int i = 0; i < this.inheritance_number; i++) {
            // System.out.println("第" + (i + 1) + "次遗传 ");
            this.oneGenetic(i + 1); //遗传一次

            long millis = System.currentTimeMillis();
            if (show && millis - tp > 300) {
                tp = millis;
                convergence.addData(millis, bestSolution.cost);
                tourWindow.showTour(bestSolution);
            }
        }
    }

    private void oneGenetic(int geneticNumber) {
        this.sonSolutions = new ArrayList<>();
        this.hybridization(); // 选择 & 杂交 & 变异
        this.updateEntity();  // 淘汰
        this.choose(geneticNumber); //择优
    }

    private void hybridization() {
        for(int i = 0; i < this.entity_number; ++i) { //杂交
            this.oneHybridization();
        }
    }

    private void oneHybridization() {
        Solution father = this.getParentSolution(); //父亲
        Solution mother = this.getParentSolution(); //母亲
        Solution best = this.getBestParent(); //最优个体不参与交叉互换
        while(father == null || father.equals(best)) {
            father = this.getParentSolution();
        }
        int cnt = 0;
        while(mother == null || father.equals(mother) || mother.equals(best)) {
            if(cnt > this.entity_number / 2) {
                break;
            }
            cnt++;
            mother = this.getParentSolution(); //直到父母不等
        }
        Random random = new Random(); //随机长度基因交换
        int crossStartIndex = random.nextInt(this.city_number); //基因交换起始点
        int crossEndIndex = random.nextInt(this.city_number); //基因交换截止点
        while(crossStartIndex == crossEndIndex) {
            crossEndIndex = random.nextInt(this.city_number); //起止不相等
        }

        if(crossStartIndex > crossEndIndex) { //交换开始结束, 使结束>开始
            int temp = crossStartIndex;
            crossStartIndex = crossEndIndex;
            crossEndIndex = temp;
        }

        Solution res = OrderCrossOver(father, mother, crossStartIndex, crossEndIndex);
        if (!isExist(res.cities, this.solutionListToEntityArray(this.sonSolutions)) && !isExist(res.cities, this.solutions)) {
            this.sonSolutions.add(res);
        }


        // no conflict
        /*Solution[] newSolutions = this.swapAndNoConflict(father, mother, crossStartIndex, crossEndIndex); //儿子个体 - 变化更大
        for(Solution solution : newSolutions) {
            if(solution != null && !isExist(solution.cities, this.solutionListToEntityArray(this.sonSolutions)) && !isExist(solution.cities, this.solutions)) { //去除重复个体
                this.sonSolutions.add(solution); //添加到儿子列表
            }
        }*/
        /* Solution[] newSolutions = this.swapAndHandleConflict(father, mother, crossStartIndex, crossEndIndex); //儿子个体 - 变化更大

        for(Solution solution : newSolutions) {
            if (solution != null && !isExist(solution.cities, this.solutionListToEntityArray(this.sonSolutions)) && !isExist(solution.cities, this.solutions)) { //去除重复个体
                this.sonSolutions.add(solution); //添加到儿子列表
            }
        }*/
    }

    private Solution[] swapAndHandleConflict(Solution father, Solution mother, int crossStartIndex, int crossEndIndex) {
        Solution[] newEntities = new Solution[2];
        Solution fatherClone = father.clone();
        Solution motherClone = mother.clone();
        Map<Integer, City> fatherCityRelation = new HashMap<>();
        Map<Integer,City> motherCityRelation = new HashMap<>();
        for(int i = 0; i < this.city_number; ++i) {
            if(i >= crossStartIndex && i <= crossEndIndex) {
                City temp = fatherClone.cities.get(i);
                fatherClone.cities.set(i, motherClone.cities.get(i));
                motherClone.cities.set(i, temp); //交换
                fatherCityRelation.put(fatherClone.cities.get(i).index, motherClone.cities.get(i));
                motherCityRelation.put(motherClone.cities.get(i).index, fatherClone.cities.get(i));
            }
        }
        this.handleConflict(fatherClone, fatherCityRelation, crossStartIndex, crossEndIndex);
        this.handleConflict(motherClone, motherCityRelation, crossStartIndex, crossEndIndex);
        newEntities[0] = fatherClone;
        if(Math.random() < this.variable_percent) { // 根据变异率, 对新子代进行变异
            this.oneVariable2Opt(newEntities[0]);
        }
        newEntities[1] = motherClone;
        if(Math.random() < this.variable_percent) { // 根据变异率, 对新子代进行变异
            this.oneVariable2Opt(newEntities[1]);
        }
        return newEntities;
    }

    /**
     * 解决冲突, 把entity.cities中不在start-end区间内的city换成cityRelation中相同key对应的value
     * @param solution 可能存在冲突的个体
     * @param cityRelation 冲突对应关系
     * @param startIndex 起始位置
     * @param endIndex 截止位置
     */
    private void handleConflict(Solution solution, Map<Integer, City> cityRelation, int startIndex, int endIndex) {
        while(conflictExist(solution, cityRelation, startIndex, endIndex)) {
            for(int i = 0; i < solution.cities.size(); ++i) {
                if(i < startIndex || i > endIndex) {
                    int temp = solution.cities.get(i).index;
                    if(cityRelation.containsKey(temp)) {
                        solution.cities.set(i, cityRelation.get(temp));
                    }
                }
            }
        }
    }

    /**
     * 是否存在冲突, 即entity.cities中是否出现过cityRelation中keySet中的任一元素
     * @param solution 可能存在冲突的实体
     * @param cityRelation 冲突对应关系
     * @param startIndex 起始位置
     * @param endIndex 截止位置
     * @return true-存在 / false-不存在
     */
    private boolean conflictExist(Solution solution, Map<Integer, City> cityRelation, int startIndex, int endIndex) {
        for(int i = 0; i < solution.cities.size(); ++i) {
            if(i < startIndex || i > endIndex) {
                if(cityRelation.containsKey(solution.cities.get(i).index)) {
                    return true;
                }
            }
        }
        return false;
    }


    private Solution getBestParent() {
        if(this.bestSolution == null) {
            Solution bestParent = this.solutions[0].clone();
            for(int i = 1; i < this.solutions.length; ++i) {
                if(this.solutions[i].cost < bestParent.cost) {
                    bestParent = this.solutions[i].clone();
                }
            }
            return bestParent;
        } else {
            return this.bestSolution.clone();
        }
    }

    /***
     * 使用轮盘赌方法选择父母亲
     * @return
     */
    private Solution getParentSolution() {
        Random random = new Random();
        double selectPercent = random.nextDouble();
        double distributionPercent = 0.0;
        for(int i = 0; i < this.solutions.length; ++i) {
            distributionPercent += this.solutions[i].reproducePercent;
            if(distributionPercent > selectPercent) {
                return this.solutions[i];
            }
        }
        return null;
    }

    private Solution OrderCrossOver(Solution father, Solution mother, int crossStartIndex, int crossEndIndex) {
        Solution result = father.clone();
        List<City> resultCities = new ArrayList<>();
        List<City> motherCities = new ArrayList<>();
        List<City> fatherContainCites = new ArrayList<>(); // 父亲保留下来的基因
        List<City> swapCites = new ArrayList<>();
        resultCities.addAll(result.cities);
        motherCities.addAll(mother.cities);
        for (int i = crossStartIndex; i <= crossEndIndex; i++) {
            fatherContainCites.add(resultCities.get(i));
        }
        int i = 0;
        while (i < city_number) {
            if (!fatherContainCites.contains(motherCities.get(i))) {
                swapCites.add(motherCities.get(i));
            }
            ++i;
        }
        i = 0;
        int j = 0;
        while (i < city_number) {
            if (i < crossStartIndex || i > crossEndIndex) {
                result.cities.set(i, swapCites.get(j++));
            }
            ++i;
        }
        if(Math.random() < this.variable_percent) { // 根据变异率, 对新子代进行变异
            this.oneVariable2Opt(result);
        }
        return result;
    }

    private Solution[] swapAndNoConflict(Solution father, Solution mother, int crossStartIndex, int crossEndIndex) {
        Solution[] newSolutions = new Solution[2];
        Solution fatherClone = father.clone();
        Solution motherClone = mother.clone();
        List<City> fatherCities = new ArrayList<>();
        List<City> motherCities = new ArrayList<>();
        fatherCities.addAll(fatherClone.cities);
        motherCities.addAll(motherClone.cities);
        int rightIndex = this.city_number - 1;
        for(int i = 0; i < father.cities.size(); ++i) {
            if(i < crossStartIndex || i > crossEndIndex) {
                while(rightIndex >= crossStartIndex && rightIndex <= crossEndIndex) {
                    rightIndex--;
                }
                fatherClone.cities.set(i, motherCities.get(rightIndex));
                motherClone.cities.set(i, fatherCities.get(rightIndex));
                rightIndex--;
            } else {
                City temp = fatherClone.cities.get(i);
                fatherClone.cities.set(i, motherClone.cities.get(i));
                motherClone.cities.set(i, temp);
            }
        }
        newSolutions[0] = fatherClone;
        if(Math.random() < this.variable_percent) { // 根据变异率, 对新子代进行变异
            this.oneVariable2Opt(newSolutions[0]);
        }
        newSolutions[1] = motherClone;
        if(Math.random() < this.variable_percent) { // 根据变异率, 对新子代进行变异
            this.oneVariable2Opt(newSolutions[1]);
        }
        return newSolutions;
    }


    private Solution[] solutionListToEntityArray(ArrayList<Solution> sonSolutions) {
        Solution[] arr = new Solution[sonSolutions.size()];
        for(int i = 0; i < sonSolutions.size(); ++i) {
            arr[i] = sonSolutions.get(i).clone();
        }
        return arr;
    }

    private void oneVariable2Opt(Solution solution) {
        Random random = new Random();
        int index1 = random.nextInt(this.city_number);
        int index2 = random.nextInt(this.city_number);
        while(index1 == index2) {
            index2 = random.nextInt(this.city_number); //保证突变基因不是同一个
        }
        if (index1 > index2) {
            int tmp = index1;
            index1 = index2;
            index2 = tmp;
        }

        while (index1 < index2) {
            City temp = solution.cities.get(index1);
            solution.cities.set(index1, solution.cities.get(index2));
            solution.cities.set(index2, temp);
            index1++;
            index2--;
        }
    }

    private void oneVariable3Opt(Solution solution) {
        Random random = new Random();
        int[] indexs = new int[3];
        indexs[0] = random.nextInt(this.city_number/2) + 2;
        indexs[1] = random.nextInt(this.city_number - 2) % (this.city_number - 2 - indexs[0]) + indexs[0];
        indexs[2] = random.nextInt(this.city_number) % (this.city_number - indexs[1]) + indexs[1];

        int i = indexs[0];
        int j = indexs[1];
        int k = indexs[2];

        City A = solution.cities.get(i-1);
        City B = solution.cities.get(i);
        City C = solution.cities.get(j-1);
        City D = solution.cities.get(j);
        City E = solution.cities.get(k-1);
        City F = solution.cities.get(k);

        double d0 = A.getDist(B) + C.getDist(D) + E.getDist(F);
        double d1 = A.getDist(C) + B.getDist(D) + E.getDist(F);
        double d2 = A.getDist(B) + C.getDist(E) + D.getDist(F);
        double d3 = A.getDist(D) + E.getDist(B) + C.getDist(F);
        double d4 = F.getDist(B) + C.getDist(D) + E.getDist(A);

        int index1 = 0;
        int index2 = 0;

        if (d0 > d1) {
            index1 = i;
            index2 = j;
        } else if (d0 > d2) {
            index1 = j;
            index2 = k;
        } else if (d0 > d4) {
            index1 = i;
            index2 = k;
        } else if (d0 > d3) {
            List<City> tmp =  solution.cities.subList(0, i);
            tmp.addAll(solution.cities.subList(j, k));
            tmp.addAll(solution.cities.subList(i, j));
            tmp.addAll(solution.cities.subList(k, solution.cities.size()));
            solution.cities = new ArrayList<>();
            solution.cities.addAll(tmp);
        }

        while (index1 < index2) {
            City temp = solution.cities.get(index1);
            solution.cities.set(index1, solution.cities.get(index2));
            solution.cities.set(index2, temp);
            index1++;
            index2--;
        }
    }

    /**
     * 选择优质个体
     *      1. 对所有个体计算路径长短
     *      2. 排序
     *      3. 选择较优个体
     */
    private void updateEntity() {
        for(Solution solution : this.sonSolutions) { //计算所有子代路径长短
            solution.cost = solution.getDistance();
        }
        List<Solution> allSolutions = new ArrayList<>();
        allSolutions.addAll(this.sonSolutions);
        allSolutions.addAll(this.solutionArrayToEntityList(this.solutions));
        Collections.sort(allSolutions); //排序所有个体
        List<Solution> bestEntities = new ArrayList<>();
        for(int i = 0; i < this.entity_number; ++i) {
            bestEntities.add(allSolutions.get(i).clone());
        }
        Collections.shuffle(bestEntities);
        for(int i = 0; i < this.solutions.length; ++i) {
            this.solutions[i] = bestEntities.get(i); //选择
        }
        this.setReproducePercent(); //重新设置选择概率(归一)
    }

    private List<Solution> solutionArrayToEntityList(Solution[] entities) {
        List<Solution> list = new ArrayList<>();
        for(Solution entity : entities) {
            list.add(entity.clone());
        }
        return list;
    }

    private void choose(int geneticNumber) {
        boolean isUpdate = false;
        for(int i = 0; i < this.solutions.length; ++i) {
            if(this.solutions[i].cost < this.minLength) {
                this.bestSolution = this.solutions[i].clone();
                this.bestSolution.reproducePercent = this.solutions[i].reproducePercent;
                this.minLength = this.solutions[i].cost;
                this.bestInheritance = geneticNumber;
                isUpdate = true;
            }
        }
        if(isUpdate) {
            System.out.println("当前最优解为 : "
                    + "\n    出现代数 : " + this.bestInheritance
                    + "\n    路径长度 : " + this.bestSolution.cost
                    + "\n    误差 : " + ((this.bestSolution.cost - 15780) * 1.0 / 15780)*100 + "%"
                    + "\n    适应度 : " + this.bestSolution.reproducePercent); //结果展示
        }
    }



    public static void main(String[] args) {
        double dist = 0;
        for (int i = 0; i < 10; i++) {
            TSP_GA tsp = new TSP_GA();
            tsp.init(false); //初始化数据
            tsp.genetic(); //开始遗传
            dist += (tsp.bestSolution.getDistance() - 15780)*1.0/15780;
            System.out.println("第" + i + "次"
                    + "\n整体最优解为 : "
                    + "\n    出现代数 : " + tsp.bestInheritance
                    + "\n    路径长度 : " + tsp.bestSolution.cost
                    + "\n    误差 : " + ((tsp.bestSolution.cost - 15780) * 1.0 / 15780) * 100 + "%"
                    + "\n    适应度 : " + tsp.bestSolution.reproducePercent
                    + "\n    路径 : " + tsp.bestSolution.toString()); //结果展示
        }
        System.out.println("平均误差: " + (dist*10) + "%");
    }
}
