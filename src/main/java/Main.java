// 导入需要的类

import java.sql.Time;
import java.util.*;

// 定义一个点类，表示地图上的一个位置
class Point {
    // 点的坐标
    double x;
    double y;

    // 点的名称
    String name;

    // 构造方法，传入坐标和名称
    public Point(double x, double y, String name) {
        this.x = x;
        this.y = y;
        this.name = name;
    }

    // 计算两点之间的距离，使用欧几里得距离公式
    public double distanceTo(Point other) {
        return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
    }

    // 重写toString方法，方便打印
    public String toString() {
        return name + "(" + x + ", " + y + ")";
    }
}

// 定义一个节约里程法类，用于规划车辆路径
class SavingMethod {
    // 车辆的起点和终点，假设都是同一个点
    Point depot;

    // 车辆的数量和容量
    int vehicleNum;
    int vehicleCapacity;

    int vehicleDistance;

    // 需要服务的客户点列表
    List<Point> customers;

    // 构造方法，传入起点终点，车辆数量和容量，客户点列表
    public SavingMethod(Point depot, int vehicleNum, int vehicleCapacity,int vehicleDistance, List<Point> customers) {
        this.depot = depot;
        this.vehicleNum = vehicleNum;
        this.vehicleCapacity = vehicleCapacity;
        this.vehicleDistance = vehicleDistance;
        this.customers = customers;
    }

    // 计算节约值矩阵，表示将两个客户点合并到同一条路径上所能节省的距离
    public double[][] calculateSavingMatrix() {
        // 获取客户点的数量
        int n = customers.size();

        // 创建一个n*n的二维数组，用于存储节约值矩阵
        double[][] savingMatrix = new double[n][n];

        // 遍历每一对客户点
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                // 获取第i个和第j个客户点
                Point p1 = customers.get(i);
                Point p2 = customers.get(j);

                // 计算节约值，即从起点到p1再到p2再回到终点的距离减去从起点到p1再回到终点加上从起点到p2再回到终点的距离
                double saving = depot.distanceTo(p1) + depot.distanceTo(p2) - p1.distanceTo(p2);

                // 将节约值存入矩阵中，注意矩阵是对称的，所以要存两次
                savingMatrix[i][j] = saving;
                savingMatrix[j][i] = saving;
            }
        }

        // 返回节约值矩阵
        return savingMatrix;
    }

    // 根据节约值矩阵生成初始路径列表，每个路径只包含一个客户点
    public List<List<Point>> generateInitialRoutes(double[][] savingMatrix) {
        // 获取客户点的数量
        int n = customers.size();

        // 创建一个列表，用于存储初始路径列表
        List<List<Point>> initialRoutes = new ArrayList<>();

        // 遍历每个客户点
        for (int i = 0; i < n; i++) {
// 创建一个列表，用于存储一个初始路径，包含起点，客户点，终点
            List<Point> route = new ArrayList<>();
            route.add(depot);
            route.add(customers.get(i));
            route.add(depot);

            // 将初始路径添加到初始路径列表中
            initialRoutes.add(route);
        }

        // 返回初始路径列表
        return initialRoutes;
    }

    // 根据节约值矩阵和初始路径列表优化路径，合并节约值最大的两个路径，直到满足车辆数量和容量的限制
    public List<List<Point>> optimizeRoutes(double[][] savingMatrix, List<List<Point>> initialRoutes, Map<String, Double> demands) {
        // 创建一个列表，用于存储优化后的路径列表，初始为初始路径列表的副本
        List<List<Point>> optimizedRoutes = new ArrayList<>(initialRoutes);

        // 创建一个布尔数组，用于标记哪些客户点已经被合并到同一条路径上
        boolean[] merged = new boolean[customers.size()];

        // 创建一个循环，不断寻找节约值最大的两个路径进行合并，直到无法合并或达到车辆数量的限制
        while (true) {
            // 定义变量，用于记录节约值最大的两个路径的索引和节约值
            int maxSavingRoute1 = -1;
            int maxSavingRoute2 = -1;
            double maxSaving = 0;

            // 遍历每一对路径
            for (int i = 0; i < optimizedRoutes.size(); i++) {
                for (int j = i + 1; j < optimizedRoutes.size(); j++) {
                    // 获取第i个和第j个路径
                    List<Point> route1 = optimizedRoutes.get(i);
                    List<Point> route2 = optimizedRoutes.get(j);

                    // 获取这两个路径的最后一个和第一个客户点的索引
                    int lastCustomer1 = customers.indexOf(route1.get(route1.size() - 2));
                    int firstCustomer2 = customers.indexOf(route2.get(1));

                    // 如果这两个客户点都没有被合并过，并且它们的节约值大于当前的最大节约值
                    if (lastCustomer1==-1 || firstCustomer2==-1){
                        System.out.println();
                    }
                    if (!merged[lastCustomer1] && !merged[firstCustomer2] && savingMatrix[lastCustomer1][firstCustomer2] > maxSaving) {
                        // 更新最大节约值和对应的路径索引
                        maxSaving = savingMatrix[lastCustomer1][firstCustomer2];
                        maxSavingRoute1 = i;
                        maxSavingRoute2 = j;
                    }
                }
            }

            // 如果没有找到可以合并的路径，或者已经达到车辆数量的限制，就退出循环
            if (maxSavingRoute1 == -1 || maxSavingRoute2 == -1 || optimizedRoutes.size() <= vehicleNum) {
                break;
            }

            // 否则，获取节约值最大的两个路径
            List<Point> route1 = optimizedRoutes.get(maxSavingRoute1);
            List<Point> route2 = optimizedRoutes.get(maxSavingRoute2);

            // 计算合并后的路径的总距离和总需求量
            double totalDistance = 0;
            int totalDemand = 0;
            for (int i = 0; i < route1.size() - 1; i++) {
                totalDistance += route1.get(i).distanceTo(route1.get(i + 1));
                totalDemand += Double.parseDouble(String.valueOf(demands.get(route1.get(i).name)));
            }
            for (int i = 1; i < route2.size(); i++) {
                totalDistance += route2.get(i).distanceTo(route2.get(i - 1));
                totalDemand += Double.parseDouble(String.valueOf(demands.get(route2.get(i).name)));
            }
            totalDistance -= maxSaving;

            // 如果合并后的路径的总距离和总需求量都不超过车辆的容量，就进行合并
            if (totalDistance <= vehicleDistance && totalDemand <= vehicleCapacity) {

                // 将合并的两个客户点标记为已合并
                merged[customers.indexOf(route1.get(route1.size() - 2))] = true;
                merged[customers.indexOf(route2.get(1))] = true;

                // 将第二条路径的客户点从第一个到倒数第二个添加到第一条路径的末尾
                route1.remove(route1.size()-1);
                for (int i = 1; i < route2.size() - 1; i++) {
                    route1.add(route2.get(i));
                }

                // 将第二条路径从优化后的路径列表中移除
                optimizedRoutes.remove(maxSavingRoute2);

            }
            System.out.println(Arrays.toString(merged));
        }

        // 返回优化后的路径列表
        return optimizedRoutes;
    }

    // 打印路径列表，显示每条路径的客户点，距离和需求量
    public void printRoutes(List<List<Point>> routes, Map<String, Double> demands) {
        // 遍历每条路径
        for (int i = 0; i < routes.size(); i++) {
            // 获取第i条路径
            List<Point> route = routes.get(i);

            // 计算路径的总距离和总需求量
            double totalDistance = 0;
            int totalDemand = 0;
            for (int j = 0; j < route.size() - 1; j++) {
                totalDistance += route.get(j).distanceTo(route.get(j + 1));
                totalDemand += Double.parseDouble(String.valueOf(demands.get(route.get(j).name)));
            }

            // 打印路径的信息，包括序号，客户点，距离和需求量
            System.out.println("Route " + (i + 1) + ": " + route + ", Distance: " + totalDistance + ", Demand: " + totalDemand);
        }
    }

    // 主方法，用于测试节约里程法
    public static void main(String[] args) {
        // 创建一个起点终点，坐标为(0, 0)，名称为D
        List<Point> customers = new ArrayList<>();
        Map<String, Double> demands = new HashMap<>();
        Point depot = new Point(0, 0, "D");
        demands.put(depot.name, 0.0);

        // 创建一个车辆数量和容量
        int vehicleNum = 3;
        int vehicleCapacity = 100;
        int vehicleDistance=400;

        // 创建一个客户点列表，包含6个客户点，坐标和名称随机生成，名称中的数字表示需求量
        customers.add(new Point(10, 10, "C10"));
        demands.put("C10", 10.0);
        customers.add(new Point(20, 20, "C20"));
        demands.put("C20", 10.0);
        customers.add(new Point(30, 30, "C30"));
        demands.put("C30", 10.0);
        customers.add(new Point(40, 40, "C40"));
        demands.put("C40", 10.0);
        customers.add(new Point(50, 50, "C50"));
        demands.put("C50", 10.0);
        customers.add(new Point(60, 60, "C60"));
        demands.put("C60", 10.0);

        // 创建一个节约里程法对象，传入起点终点，车辆数量和容量，客户点列表
        SavingMethod sm = new SavingMethod(depot, vehicleNum, vehicleCapacity,vehicleDistance, customers);

        // 计算节约值矩阵
        double[][] savingMatrix = sm.calculateSavingMatrix();

        // 打印节约值矩阵
        System.out.println("Saving matrix:");
        for (int i = 0; i < savingMatrix.length; i++) {
            for (int j = 0; j < savingMatrix[i].length; j++) {
                System.out.print(savingMatrix[i][j] + "\t");
            }
            System.out.println();
        }

        // 根据节约值矩阵生成初始路径列表
        List<List<Point>> initialRoutes = sm.generateInitialRoutes(savingMatrix);

        // 打印初始路径列表
        System.out.println("Initial routes:");
        sm.printRoutes(initialRoutes, demands);

        // 根据节约值矩阵和初始路径列表优化路径
        List<List<Point>> optimizedRoutes = sm.optimizeRoutes(savingMatrix, initialRoutes, demands);

        // 打印优化后的路径列表
        System.out.println("Optimized routes:");
        sm.printRoutes(optimizedRoutes, demands);
    }
}