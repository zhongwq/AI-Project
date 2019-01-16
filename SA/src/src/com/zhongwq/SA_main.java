package com.zhongwq;

import java.io.*;

public class SA_main {
    private double currentTemperature = 100; // 初始温度
    private double minTemperature = 0.01; // 结束温度
    private double internalLoop = 1000; // 单次迭代次数
    private double coolingRate = 0.99; // 降温系数
    private Solution currentSolution ;
    private boolean show = false;
    private boolean SAsearch = false;

    /**
     * 初始化一个解
     **/
    public void initSolution(String filename, int cityNum, boolean show, boolean SAsearch) {
        Solution solution = new Solution();
        this.show = show;
        this.SAsearch = SAsearch;
        try {
            BufferedReader data = new BufferedReader(new InputStreamReader(
                    new FileInputStream(filename)));
            for (int i = 0; i < cityNum; i++) {
                String strbuff = data.readLine();
                // 字符分割
                String [] arr = strbuff.split("\\s+");
                int index = 0;
                while (arr[index].equals("")) {
                    index++;
                }
                solution.addCity(new City(Integer.valueOf(arr[index]), Double.valueOf(arr[index+1]), Double.valueOf(arr[index+2])));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentSolution = solution.getRandomInitial();
        System.out.println("Initial solution distance: " + currentSolution.getDistance());
    }

    /**
     * 计算接收概率
     **/
    private double acceptanceProbability(int energy, int newEnergy, double temperature) {
        // 如果新解更优, 100%接收
        if (newEnergy < energy) {
            return 1.0;
        }
        // 如果新解的cost更高, 返回接收概率
        if (SAsearch)
            return Math.exp((energy - newEnergy) / temperature);  // 模拟退火时
        return 0; // 爬山算法时, 不接受差解
    }

    /**
     * 使用模拟退火算法得到解
     * @return best solution
     * */
    public Solution anneal() {
        Convergence convergence = null;
        TourWindow tourWindow = null;
        if (show) {
            convergence = new Convergence(SAsearch?"模拟退火算法收敛过程":"局部搜索算法收敛过程");
            convergence.setY_Coordinate_Name("所有路径和 ");
            convergence.setVisible(true);

            tourWindow = new TourWindow("路径变化过程", 5);
            tourWindow.setVisible(true);
            tourWindow.setBounds(0, 0, 900, 600);
            tourWindow.setLocation(800, 0);
        }

        long tp = 0;
        int count = 0;

        Solution bestSolution = new Solution(currentSolution.getCitiesList());
        Solution newSolution = null;
        // Loop until system has cooled
        while (currentTemperature > minTemperature) {
            for (int i = 0; i < internalLoop; i++) {
                // 通过多邻域操作获取新解
                newSolution = currentSolution.generateNeighour();
                // 获得新解的cost
                int currentEnergy = currentSolution.getDistance();
                int neighbourEnergy = newSolution.getDistance();

                // 根据概率查看是否接收新解作为startPoint
                if (acceptanceProbability(currentEnergy, neighbourEnergy,
                        currentTemperature) > Math.random()) {
                    currentSolution = new Solution(newSolution.getCitiesList());
                }

                if (currentSolution.getDistance() < bestSolution.getDistance()) {
                    bestSolution = new Solution(currentSolution.getCitiesList());
                }
                ++count;
            }
            currentTemperature *= coolingRate; // 降温

            // 显示数据
            long millis = System.currentTimeMillis();
            if (show && millis - tp > 10) {
                tp = millis;
                convergence.addData(millis, bestSolution.getDistance(), currentSolution.getDistance());
                tourWindow.showTour(bestSolution);
            }
        }
        return bestSolution;
    }

    public static void main(String[] args) {
        SA_main sa = new SA_main();
        sa.initSolution("test.txt", 198, true, true);
        Solution bestSolution = sa.anneal();
        System.out.println("Final solution distance: " + bestSolution.getDistance() + ", 误差: " + ((bestSolution.getDistance() - 15780) * 1.0 / 15780) * 100 + "%");
        System.out.println("Tour: " + bestSolution);

    }
}
