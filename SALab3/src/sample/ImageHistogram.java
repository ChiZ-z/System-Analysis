package sample;

import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

class ImageHistogram {

	private Image image;

	private long[] alpha = new long[256];
	private long[] red = new long[256];
	private long[] green = new long[256];
	private long[] blue = new long[256];

	private long[] brightness = new long[256];
	private long[] L = new long[32];
	private long[] valuePixel;

	XYChart.Series seriesAlpha;
	XYChart.Series seriesRed;
	XYChart.Series seriesGreen;
	XYChart.Series seriesBlue;

	XYChart.Series seriesBrightness;

	private boolean success;

	ImageHistogram(Image src) {
		image = src;
		success = false;

		//init
		for (int i = 0; i < 256; i++) {
			alpha[i] = red[i] = green[i] = blue[i] = 0;
			brightness[i] = 0;
		}

		PixelReader pixelReader = image.getPixelReader();
		if (pixelReader == null) {
			return;
		}

		valuePixel = new long[(int) (image.getHeight() * image.getWidth())];

		var index = 0;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int argb = pixelReader.getArgb(x, y);
				int a = (0xff & (argb >> 24));
				int r = (0xff & (argb >> 16));
				int g = (0xff & (argb >> 8));
				int b = (0xff & argb);
				alpha[a]++;
				red[r]++;
				green[g]++;
				blue[b]++;
				//Convert RGB to HSB (or HSV)
				float[] hsb = new float[3];
				valuePixel[index] =((r + g + b) / 3);
				index++;
				Color.RGBtoHSB(r, g, b, hsb);
				brightness[(int) (hsb[2] * 255)]++;
			}
		}

		seriesAlpha = new XYChart.Series();
		seriesRed = new XYChart.Series();
		seriesGreen = new XYChart.Series();
		seriesBlue = new XYChart.Series();
		seriesBrightness = new XYChart.Series();
		seriesAlpha.setName("alpha");
		seriesRed.setName("red");
		seriesGreen.setName("green");
		seriesBlue.setName("blue");
		seriesBrightness.setName("Brightness");


		var count = 0;
		var indexL = 0;
		for (long i : brightness) {
			L[indexL] += i;
			if (count == 7) {
				count = 0;
				indexL++;
			}
			count++;
			if (indexL == 32)
				break;
		}
		var pixel = 8;
		for (long l : L) {
			seriesBrightness.getData().add(new XYChart.Data(String.valueOf(pixel), l));
			pixel += 8;
		}

		//copy alpha[], red[], green[], blue[], brightness
		//to seriesAlpha, seriesRed, seriesGreen, seriesBlue, seriesBrightness
		for (int i = 0; i < 256; i++) {
			seriesAlpha.getData().add(new XYChart.Data(String.valueOf(i), alpha[i]));
			seriesRed.getData().add(new XYChart.Data(String.valueOf(i), red[i]));
			seriesGreen.getData().add(new XYChart.Data(String.valueOf(i), green[i]));
			seriesBlue.getData().add(new XYChart.Data(String.valueOf(i), blue[i]));
		}

		success = true;
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public long[] getValuePixel() {
		return valuePixel;
	}

	public void setValuePixel(long[] valuePixel) {
		this.valuePixel = valuePixel;
	}

	public long[] getAlpha() {
		return alpha;
	}

	public void setAlpha(long[] alpha) {
		this.alpha = alpha;
	}

	public long[] getRed() {
		return red;
	}

	public void setRed(long[] red) {
		this.red = red;
	}

	public long[] getGreen() {
		return green;
	}

	public void setGreen(long[] green) {
		this.green = green;
	}

	public long[] getBlue() {
		return blue;
	}

	public void setBlue(long[] blue) {
		this.blue = blue;
	}

	public long[] getBrightness() {
		return brightness;
	}

	public void setBrightness(long[] brightness) {
		this.brightness = brightness;
	}

	public long[] getL() {
		return L;
	}

	public void setL(long[] l) {
		L = l;
	}

	public boolean isSuccess() {
		return success;
	}

	public XYChart.Series getSeriesAlpha() {
		return seriesAlpha;
	}

	public XYChart.Series getSeriesRed() {
		return seriesRed;
	}

	public XYChart.Series getSeriesGreen() {
		return seriesGreen;
	}

	public XYChart.Series getSeriesBlue() {
		return seriesBlue;
	}

	public XYChart.Series getSeriesBrightness() {
		return seriesBrightness;
	}
}
