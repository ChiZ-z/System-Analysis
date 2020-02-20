package sample;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main extends Application {

	private int i = 0;
	private int points = 0;
	private boolean negativeCorrelation = false;

	public Map<Double, Double> parseFile() throws FileNotFoundException {
		File file = new File("file.txt");
		Scanner input = new Scanner(file);
		Map<Double, Double> MapXY = new HashMap<Double, Double>();
		String tempX = "", tempY = "";
		int count = 0;
		while (input.hasNext()) {
			String nextToken = input.next();
			for (i = 0; i < nextToken.length(); i++) {
				if (nextToken.charAt(i) == ',') {
					if (count == 5) {
						i++;
						tempX = getString(nextToken);
						if (nextToken.charAt(i) == '.') {
							while (nextToken.charAt(i) != ',')
								i++;
						}
						points = 0;
						i++;
						tempY = getString(nextToken);
						points = 0;
					}
					count++;
				}
			}
			if (!tempX.contains("X") || !tempY.contains("X"))
				MapXY.put(Double.parseDouble(tempX), Double.parseDouble(tempY));
			tempX = "";
			tempY = "";
			count = 0;
			input.nextLine();
		}
		input.close();
		return MapXY;
	}

	public String getString(String string) {
		String tempX = "";
		while (string.charAt(i) != ',') {
			if (string.charAt(i) == '.') points++;
			if (points == 2) break;
			tempX += string.charAt(i);
			i++;
		}
		return tempX;
	}

	public Double getMaxElement(Collection<Double> list) {
		Double max = 0.0;
		for (Double d : list) {
			if (d > max)
				max = d;
		}
		return max;
	}

	public Double avarageValue(Collection<Double> list) {
		Double max = 0.0;
		for (Double d : list) {
			max += d;
		}
		return max / list.size();
	}

	public Double nominatorCorrilation(Map<Double, Double> map) {
		Double result = 0.0;
		Double avarageX = avarageValue(map.keySet());
		Double avarageY = avarageValue(map.values());
		for (Double d : map.keySet()) {
			result += (d - avarageX) * (map.get(d) - avarageY);
		}
		return result;
	}

	public Double denominatorCorrilation(Map<Double, Double> map) {
		Double result = 0.0;
		Double leftPart = 0.0;
		Double rightPart = 0.0;
		Double avarageX = avarageValue(map.keySet());
		Double avarageY = avarageValue(map.values());
		for (Double d : map.keySet()) {
			leftPart += Math.pow((d - avarageX), 2);
			rightPart += Math.pow((map.get(d) - avarageY), 2);
		}
		result = leftPart * rightPart;
		if (result < 0) {
			negativeCorrelation = true;
			result *= -1;
		}
		return Math.sqrt(result);
	}

	public Double nominatorA(Map<Double, Double> map) {
		Double leftPart = 0.0;
		Double rightPartX = 0.0;
		Double rightPartY = 0.0;
		for (Double d : map.keySet()) {
			leftPart += d * map.get(d);
			rightPartX += d;
			rightPartY += map.get(d);
		}
		leftPart *= map.size();
		return leftPart - rightPartX * rightPartY;
	}

	public Double nominatorB(Map<Double, Double> map) {
		Double leftPartY = 0.0;
		Double leftPartX = 0.0;
		Double rightPartX = 0.0;
		Double rightPartXY = 0.0;
		for (Double d : map.keySet()) {
			leftPartX += Math.pow(d, 2);
			leftPartY += map.get(d);
			rightPartX += d;
			rightPartXY += d + map.get(d);
		}
		return leftPartY * leftPartX - rightPartX * rightPartXY;
	}

	public Double denominator(Map<Double, Double> map) {
		Double leftPart = 0.0;
		Double rightPart = 0.0;
		for (Double d : map.keySet()) {
			leftPart += Math.pow(d, 2);
			rightPart += d;
		}
		leftPart *= map.size();
		rightPart = Math.pow(rightPart, 2);
		return leftPart - rightPart;
	}

	@Override
	public void start(Stage stage) throws Exception {
		Map<Double, Double> MapXY = parseFile();
		if (MapXY == null) return;
		Double a = nominatorA(MapXY) / denominator(MapXY);
		Double b = nominatorB(MapXY) / denominator(MapXY);
		System.out.print("Коэфиценты уравнения линейной регрессий:\nA = " + nominatorA(MapXY) / denominator(MapXY) +
				"\nB = " + nominatorB(MapXY) / denominator(MapXY) + "\nЛинейный коэффициент корреляции:" +
				"\nRxy = ");
		if (negativeCorrelation) {
			System.out.print("-");
		}
		System.out.print(nominatorCorrilation(MapXY) / denominatorCorrilation(MapXY) + "\n");
		System.out.println("Max X = " + getMaxElement(MapXY.keySet()));
		System.out.println("Max Y = " + getMaxElement(MapXY.values()));

		stage.setTitle("Line Chart SA 1");
		final NumberAxis xAxis = new NumberAxis(0, 12, 1);
		final NumberAxis yAxis = new NumberAxis(0, 1000, 100);
		final LineChart<Number, Number> lineChart =
				new LineChart<Number, Number>(xAxis, yAxis);
		XYChart.Series series = new XYChart.Series();
		series.setName("Выборка");
		for (Double x : MapXY.keySet()) {
			series.getData().add(new XYChart.Data(x, MapXY.get(x)));
		}

		XYChart.Series seriesModel = new XYChart.Series();
		seriesModel.setName("Модель");
		for (Double i = 0.0; i < 12; i += 0.1) {
			//seriesModel.getData().add(new XYChart.Data(i, Math.pow(2.71828182845904523536, i)));
			seriesModel.getData().add(new XYChart.Data(i, a * i + b));
		}

		Double t = (nominatorCorrilation(MapXY) / denominatorCorrilation(MapXY)) * Math.sqrt(MapXY.size() - 2);
		Double l = Math.sqrt(1 - (nominatorCorrilation(MapXY) / denominatorCorrilation(MapXY)));
		t = t / l;
		System.out.println(t);
		/*DecimalFormat numberFormat = new DecimalFormat("#.00");
		for (Double d : MapXY.keySet()) {
			System.out.println(numberFormat.format(d));
		}
		System.out.println("-------------YYYYY-----------");
		for (Double d : MapXY.keySet()) {
			System.out.println(numberFormat.format(MapXY.get(d)));
		}*/

		lineChart.getData().addAll(series, seriesModel);
		lineChart.getStylesheets().add(Main.class.getResource("style.css")
				.toExternalForm());

		Scene scene = new Scene(lineChart, 1000, 1000);
		stage.setScene(scene);
		stage.show();
	}


	public static void main(String[] args) {
		launch(args);
	}
}
