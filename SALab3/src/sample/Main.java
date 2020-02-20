package sample;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.math3.special.Erf;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static java.lang.Math.sqrt;
import static org.apache.commons.math3.special.Erf.erf;

public class Main extends Application {

    private double LAMBDA_05 = 1.36;
    private double CRITICAL_VALUE = 42.557;
    private long[] long256 = new long[256];
    private long[] long32 = new long[32];
    private long[] minIntravals = new long[32];
    private double[] avarageIntravals = new double[32];
    private long[] maxIntravals = new long[32];
    private long intecivity = 8;
    private long minIntecivity = 0;
    private double avarageIntecivity = 3.5;
    private long maxIntecivity = 7;

    private File convertToGreyScale(File file) throws IOException {
        BufferedImage image = ImageIO.read(file);
        int width = image.getWidth();
        int height = image.getHeight();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Color c = new Color(image.getRGB(j, i));
                int red = (int) (c.getRed() * 0.299);
                int green = (int) (c.getGreen() * 0.587);
                int blue = (int) (c.getBlue() * 0.114);
                Color newColor = new Color(red + green + blue,
                        red + green + blue, red + green + blue);
                image.setRGB(j, i, newColor.getRGB());
            }
        }
        File output = new File(file.getName() + "-converted.jpg");
        ImageIO.write(image, "jpg", output);
        return output;
    }

    private void setConstans() {
        for (int i = 0; i < minIntravals.length; i++) {
            minIntravals[i] += minIntecivity;
            minIntecivity += 8;
        }
        for (int i = 0; i < avarageIntravals.length; i++) {
            avarageIntravals[i] += avarageIntecivity;
            avarageIntecivity += 8;
        }
        for (int i = 0; i < maxIntravals.length; i++) {
            maxIntravals[i] += maxIntecivity;
            maxIntecivity += 8;
        }
        for (int i = 0; i < long256.length; i++)
            long256[i] = i;

        for (int i = 0; i < long32.length; i++) {
            long32[i] += intecivity;
            intecivity += 8;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        Controller controller = new Controller();
        setConstans();
        ImageView imageView = new ImageView();
        ImageView imageView2 = new ImageView();

        final CategoryAxis xAxis_brightness = new CategoryAxis();
        final NumberAxis yAxis_brightness = new NumberAxis();
        final BarChart<String, Number> brightnessHistogram
                = new BarChart<>(xAxis_brightness, yAxis_brightness);
        brightnessHistogram.setAnimated(true);
        brightnessHistogram.setTitle("Image 1");
        brightnessHistogram.setCategoryGap(0);
        brightnessHistogram.setBarGap(0);

        final CategoryAxis xAxis_brightness2 = new CategoryAxis();
        final NumberAxis yAxis_brightness2 = new NumberAxis();
        final BarChart<String, Number> brightnessHistogram2
                = new BarChart<>(xAxis_brightness2, yAxis_brightness2);
        brightnessHistogram2.setAnimated(true);
        brightnessHistogram2.setTitle("Image 2");
        brightnessHistogram2.setCategoryGap(0);
        brightnessHistogram2.setBarGap(0);

        try {
            File file = new File("123.jpg");
            FileInputStream inputstream = new FileInputStream(convertToGreyScale(file));
            Image image = new Image(inputstream);
            imageView.setImage(image);
            brightnessHistogram.getData().clear();
            ImageHistogram imageHistogram = new ImageHistogram(image);
            if (imageHistogram.isSuccess()) {
                brightnessHistogram.getData().add(
                        imageHistogram.getSeriesBrightness());
            }
            File file2 = new File("image4.jpg");
            FileInputStream inputstream2 = new FileInputStream(convertToGreyScale(file2));
            Image image2 = new Image(inputstream2);
            brightnessHistogram2.getData().clear();
            imageView2.setImage(image2);
            ImageHistogram imageHistogram2 = new ImageHistogram(image2);
            if (imageHistogram2.isSuccess()) {
                brightnessHistogram2.getData().add(
                        imageHistogram2.getSeriesBrightness());
            }

            System.out.println("_________________IMAGE 1_________________");
            getAllParams(imageHistogram);
            System.out.println("_________________IMAGE 2_________________");
            getAllParams(imageHistogram2);
            System.out.println("Коэффицент корреляций Гистограмм = " + controller.nominatorCorrilation(imageHistogram.getBrightness(), imageHistogram2.getBrightness()) /
                    controller.denominatorCorrilation(imageHistogram.getBrightness(), imageHistogram2.getBrightness()));
            System.out.println("коэффициент корреляции Изображений = " + controller.nominatorCorrilation(imageHistogram.getValuePixel(), imageHistogram2.getValuePixel()) /
                    controller.denominatorCorrilation(imageHistogram.getValuePixel(), imageHistogram2.getValuePixel()));

        } catch (IOException e) {
            e.printStackTrace();
        }


        VBox hBox = new VBox();
        hBox.getChildren().addAll(brightnessHistogram, brightnessHistogram2);
        StackPane root = new StackPane();
        root.getChildren().addAll(hBox);

        Scene scene = new Scene(root, 1000, 800);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void getAllParams(ImageHistogram imageHistogram) {
        Controller controller = new Controller();
        //System.out.println(Arrays.toString(imageHistogram.getBrightness()));

        double median = controller.getMedian(imageHistogram.getBrightness());
        System.out.println("Выборочное среднее = " + controller.expected_value(imageHistogram.getBrightness()));
        System.out.println("СКО = " + sqrt(controller.dispersion_value(imageHistogram.getBrightness())));
        System.out.println("Массив[" + controller.nearest((int) median, imageHistogram.getBrightness()) + "] = " +
                imageHistogram.getBrightness()[controller.nearest((int) median, imageHistogram.getBrightness())] + " -> Медиана (" + median + ")");
        System.out.println("Мода = " + controller.getMode(imageHistogram.getBrightness())[0] + "; Количество повторений = " + controller.getMode(imageHistogram.getBrightness())[1]);
        System.out.println("Гипотеза о нормальном распределений - " +
                ((controller.isPirson(imageHistogram.getL(),minIntravals,avarageIntravals,maxIntravals) < CRITICAL_VALUE) ? "Принята" : "Отвергнута"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
