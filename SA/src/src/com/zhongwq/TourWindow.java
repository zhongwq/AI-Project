package com.zhongwq;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

class MyPanel extends JPanel
{
    private ArrayList<City> cities;
    private double scale;

    public MyPanel(double scale) {
        this.scale = scale;
    }

    public void display(ArrayList<City> cities) {
        this.cities = cities;
        this.repaint();
    }

    /**
     * repaint方法会调用paint方法，并自动获得Graphics对像
     * 然后可以用该对像进行2D画图
     * 注：该方法是重写了JPanel的paint方法
     */
    public void paint(Graphics g) {
        //调用的super.paint(g),让父类做一些事前的工作，如刷新屏幕
        super.paint(g);
        if (cities == null)
            return;
        int len = cities.size();
        for (int i = 0; i < len; i++) {
            g.drawRect(50 + (int)(cities.get(i).x / scale),50 + (int)(cities.get(i).y / scale),5,5); //画一个5*5的点
            g.setColor(Color.blue);
            g.fillRect(50 + (int)(cities.get(i).x / scale),50 + (int)(cities.get(i).y / scale), 5, 5); //填充为蓝色
            g.setColor(Color.red);
            if (i != len - 1) {
                g.drawLine(50 + (int)(cities.get(i).x / scale), 50 + (int)(cities.get(i).y / scale), 50 + (int)(cities.get(i+1).x / scale), 50 + (int)(cities.get(i+1).y / scale));//画一条连线。
            } else {
                g.drawLine(50 + (int)(cities.get(i).x / scale), 50 + (int)(cities.get(i).y / scale), 50 + (int)(cities.get(0).x / scale), 50 + (int)(cities.get(0).y / scale));//画一条连线。
            }
        }
    }
}

public class TourWindow extends JFrame {
    private MyPanel myPanel;

    public TourWindow(String ieee, double scale) {
        initComponents(scale);
        setTitle(ieee);
    }

    public void showTour(Solution solution) {
        myPanel.display(solution.getCitiesList());
    }

    private void initComponents(double scale) {
        JFrame jf = new JFrame();
        myPanel = new MyPanel(scale);
        getContentPane().add(myPanel);
    }

}
