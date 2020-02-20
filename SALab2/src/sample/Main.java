package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main extends Application {

	private int i = 0;
	private int points = 0;
	final static private Double T_190 = 1.6529;
	final static private Double T_380 = 1.6488;

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

	public Double expected_value(Collection<Double> collection) {
		var result = 0.0;
		for (Double key : collection) {
			result += key;
		}
		return result / collection.size();
	}

	public Double dispersion_value(Collection<Double> collection) {
		var result = 0.0;
		var expected = expected_value(collection);
		for (Double key : collection) {
			result += Math.pow(key - expected, 2);
		}
		return result / (collection.size() - 1);
	}

	public Double criterion_t_value(Map<Double, Double> map) {
		var expected_one = expected_value(map.keySet());
		var expected_two = expected_value(map.values());
		var dispersion_one = dispersion_value(map.keySet());
		var dispersion_two = dispersion_value(map.values());
		var result = (expected_one - expected_two) /
				Math.sqrt((dispersion_one / map.size()) + (dispersion_two / map.size()));
		return result < 0 ? result * -1 : result;
	}

	public Double criterion_z_value(Map<Double, Double> map) {
		var expected_one = expected_value(map.keySet());
		var expected_two = expected_value(map.values());
		var dispersion_one = dispersion_value(map.keySet());
		var dispersion_two = dispersion_value(map.values());
		var result = (expected_one - expected_two) /
				(dispersion_one + dispersion_two) *
				Math.sqrt(map.size() * map.size() / (map.size() + map.size()));
		return result < 0 ? result * -1 : result;
	}

	public Double getMinElement(Collection<Double> list) {
		Double min = getMaxElement(list);
		for (Double d : list) {
			if (d < min)
				min = d;
		}
		return min;
	}

	public Double getMaxElement(Collection<Double> list) {
		Double max = 0.0;
		for (Double d : list) {
			if (d > max)
				max = d;
		}
		return max;
	}

	public String getValue(Double criterion) {
		if (criterion < T_380)
			return "Accepted";
		else
			return "Not accepted";
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Map<Double, Double> map = parseFile();

		//map.entrySet().removeIf(e -> e.getKey() < 0.5);
		//map.entrySet().removeIf(e -> e.getKey() > 15.0);

		System.out.println("MAX X = " + getMaxElement(map.keySet()));
		System.out.println("MIN X = " + getMinElement(map.keySet()));

		System.out.println("Математическое ожидание: " + expected_value(map.keySet()));
		System.out.println("Дисперсия: " + dispersion_value(map.keySet()));
		System.out.println("Доверительный интервал для математического ожидания: " + (T_190 * Math.sqrt(dispersion_value(map.keySet()) / map.keySet().size())));
		System.out.println("Доверительный интервал для дисперсии:(" + ((dispersion_value(map.keySet()) * (map.keySet().size() - 1)) / 159.11251) +
				';' + ((dispersion_value(map.keySet()) * (map.keySet().size() - 1)) / 223.1602) + ')');

		var criterionT = criterion_t_value(map);
		var criterionZ = criterion_z_value(map);

		System.out.println("T критерий: " + criterionT);
		System.out.println("Z критерий " + criterionZ);

		System.out.println("Гипотеза о равенстве математических ожиданий(дисперсии известны):" + getValue(criterionT));
		System.out.println("Гипотеза о равенстве математических ожиданий(дисперсии неизвестны):" + getValue(criterionZ));
	}


	public static void main(String[] args) {
		launch(args);
	}
}
