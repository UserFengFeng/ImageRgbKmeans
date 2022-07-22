import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Test {
    public static void main(String[] args) throws IOException {
        BufferedImage fileImage = ImageIO.read(new File("D:\\download\\panorama.webp"));

        BufferedImage bufferedImage = new BufferedImage(fileImage.getWidth(), fileImage.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();
        graphics.setBackground(new Color(0, 0, 0, 0));
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(Color.RED);
        // 真实图片中要分簇轮廓的每个像素点的rgb
        List<List<Integer>> rgbPointList = new ArrayList<>();
        // 真实图片中要分簇的每个像素点 要跟rgb一一对应
        List<List<Integer>> PointList = new ArrayList<>();

        Random random = new Random();
        for (int i = 0; i < 1000; i++) {
            int r = random.nextInt(255) + 1;
            int g = random.nextInt(255) + 1;
            int b = random.nextInt(255) + 1;
            rgbPointList.add(Arrays.asList(r, g, b));
            PointList.add(Arrays.asList(i, i+1));
        }

        // 分簇
        KMeansRGB kMeansRGB = new KMeansRGB(2, 100, 0.0, rgbPointList, PointList);
        kMeansRGB.doKMeansRGB();

        // 分好簇类的rgb结果
        HashMap<Integer, List<List<Integer>>> clustersRgbMap = kMeansRGB.clustersRgbMap;
        // 分好簇类的point结果 跟rgb的是一一对应
        HashMap<Integer, List<List<Integer>>> clustersPointMap = kMeansRGB.clustersPointMap;
    }
}
