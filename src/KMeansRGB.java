import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class KMeansRGB {
    /**
     * 聚类的个数
     */
    int clusterCount;

    /**
     * 迭代次数
     */
    int iterationNum = 10;

    /**
     * 可接受的误差范围
     */
    double errorValue = 0.0;
    /**
     * rgb数据集
     */
    List<List<Integer>> rgbLists = new ArrayList<>();
    /**
     * 像素点数据集
     */
    List<List<Integer>> pointLists = new ArrayList<>();
    /**
     * 簇的中心点
     */
    List<List<Integer>> centerRgbs = new ArrayList<>();
    /**
     * 聚类rgb结果的集合簇，key为聚类中心点在centerRgbs中的下标，value为该类簇下的数据点
     */
    public HashMap<Integer, List<List<Integer>>> clustersRgbMap = new HashMap<>();
    /**
     * 聚类point结果的集合簇，key为聚类中心点在centerRgbs中的下标，value为该类簇下的数据点
     */
    public HashMap<Integer, List<List<Integer>>> clustersPointMap = new HashMap<>();

    public KMeansRGB(int clusterCount, int iterationNum, double errorValue, List<List<Integer>> rgbLists, List<List<Integer>> pointLists) {
        this.clusterCount = clusterCount;
        this.iterationNum = iterationNum;
        this.errorValue = errorValue;
        this.rgbLists = rgbLists;
        this.pointLists = pointLists;
        this.loadData();
    }

    public void loadData() {
        if (rgbLists.size() != pointLists.size()) {
            System.out.println("rgblists size and pointslist size consistent");
            return;
        }
        Random random = new Random();
        for (int i = 0; i < clusterCount; i++) {
            int r = random.nextInt(255) + 1;
            int g = random.nextInt(255) + 1;
            int b = random.nextInt(255) + 1;
            centerRgbs.add(Arrays.asList(r, g, b));
            clustersRgbMap.put(i, new ArrayList<>());
            clustersPointMap.put(i, new ArrayList<>());
        }
    }

    public void doKMeansRGB() {
        System.out.println("start kmeans");
        long l = System.currentTimeMillis();
        double err = Integer.MAX_VALUE;
        while (iterationNum > 0) {
            // 每次聚类前清空原聚类结果的list
            for (int key : clustersRgbMap.keySet()) {
                List<List<Integer>> list = clustersRgbMap.get(key);
                List<List<Integer>> clusterList = clustersPointMap.get(key);
                list.clear();
                clusterList.clear();

                clustersRgbMap.put(key, list);
                clustersPointMap.put(key, clusterList);
            }
            // 计算每个点所属类簇
            for (int i = 0; i < rgbLists.size(); i++) {
                dispatchPointToCluster(i, rgbLists.get(i), centerRgbs);
            }
            // 计算每个簇的中心点，并得到中心点偏移误差
            err = getClusterCenterPoint(centerRgbs, clustersRgbMap);
            // 误差为0 就结束迭代
            if (Double.doubleToLongBits(err) <= Double.doubleToLongBits(errorValue)) {
                break;
            }
            iterationNum--;
        }
        System.out.println("And the center error value： " + err + "----" + (System.currentTimeMillis() - l));
    }

    /**生成最终图片查看效果*/
    public static void testClustersPointDraw(BufferedImage fileImage, HashMap<Integer, List<List<Integer>>> clustersPoints) {
        Random random = new Random();
        BufferedImage testImage = new BufferedImage(fileImage.getWidth(), fileImage.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D testgraph = (Graphics2D) testImage.getGraphics();
        testgraph.setBackground(new Color(0, 0, 0, 0));
        testgraph.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Integer key : clustersPoints.keySet()) {
            int r = random.nextInt(255) + 1;
            int g = random.nextInt(255) + 1;
            int b = random.nextInt(255) + 1;
            Color color = new Color(r, g, b);
            List<List<Integer>> lists = clustersPoints.get(key);
            for (int i = 0; i < lists.size(); i++) {
                List<Integer> point = lists.get(i);
                Integer x = point.get(0);
                Integer y = point.get(1);
                testImage.setRGB(x, y, color.getRGB());
            }
        }
        testgraph.dispose();
        try {
            ImageIO.write(testImage, "PNG", new File("./kmeansTest.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 计算每个类簇的中心点，并返回中心点偏移误差
     *
     * @return
     */
    public double getClusterCenterPoint(List<List<Integer>> centerPoints, HashMap<Integer, List<List<Integer>>> clusters) {
        double error = 0;
        for (int i = 0; i < centerPoints.size(); i++) {
            List<Integer> tmpCenterPoint = centerPoints.get(i);
            int centerR = 0, centerG = 0, centerB = 0;

            List<List<Integer>> lists = clusters.get(i);
            if (lists.size() == 0) {
                continue;
            }
            for (int j = 0; j < lists.size(); j++) {
                List<Integer> rgb = lists.get(j);
                centerR += rgb.get(0);
                centerG += rgb.get(1);
                centerB += rgb.get(2);
            }
            centerR /= lists.size();
            centerG /= lists.size();
            centerB /= lists.size();

            error += Math.abs(centerR - tmpCenterPoint.get(0));
            error += Math.abs(centerG - tmpCenterPoint.get(1));
            error += Math.abs(centerB - tmpCenterPoint.get(2));
            centerPoints.set(i, Arrays.asList(centerR, centerG, centerB));
        }
        return error;
    }

    /**
     * 计算rgb，并将该rgb划分到距离最近的中心点的簇中
     *
     * @return
     */
    public void dispatchPointToCluster(int rgbIndex, List<Integer> rgbLists, List<List<Integer>> centerPoints) {
        int index = 0;
        double tmpMinDistance = Double.MAX_VALUE;
        for (int i = 0; i < centerPoints.size(); i++) {
            double distance = calDistance(rgbLists, centerPoints.get(i));
            if (distance < tmpMinDistance) {
                tmpMinDistance = distance;
                index = i;
            }
        }
        List<List<Integer>> list = clustersRgbMap.get(index);
        list.add(rgbLists);

        List<List<Integer>> pointsTo = clustersPointMap.get(index);
        pointsTo.add(pointLists.get(rgbIndex));

        clustersRgbMap.put(index, list);
        clustersPointMap.put(index, pointsTo);
    }

    /*
     * Computes the euclidean distance between two points
     * [O(length of the vectors) : length of the vectors represent the depth of the data (in this case: 3 *RGB*)] time complexity
     */
    public double calDistance(List<Integer> rgbList1, List<Integer> rgbList2) {
        return Math.pow((rgbList1.get(0) - rgbList2.get(0)), 2) + Math.pow((rgbList1.get(1) - rgbList2.get(1)), 2) + Math.pow((rgbList1.get(2) - rgbList2.get(2)), 2);
    }

}
