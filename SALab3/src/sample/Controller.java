package sample;

import java.util.Arrays;
import java.util.Collection;
import static java.lang.Math.sqrt;
import static org.apache.commons.math3.special.Erf.erf;

public class Controller {
    public Controller() {
    }

    public long expected_value(long[] longs) {
        var result = 0;
        for (long key : longs) {
            result += key;
        }
        return result / longs.length;
    }

    public long dispersion_value(long[] longs) {
        var result = 0;
        var expected = expected_value(longs);
        for (long key : longs) {
            result += Math.pow(key - expected, 2);
        }
        return result / (longs.length - 1);
    }

    public long dispersionPirson(long[] longs, double[] avar, double math) {
        var result = 0;
        var summ = summAll(longs);
        for (int i = 0; i < longs.length; i++) {
            result += (Math.pow(avar[i] - math, 2) * longs[i]);
        }
        return result / summ;
    }

    public double getMedian(long[] longs) {
        long[] copy = longs.clone();
        Arrays.sort(copy);
        double median;
        if (copy.length % 2 == 0) {
            median = ((double) copy[copy.length / 2] + (double) copy[copy.length / 2 - 1]) / 2;
        } else {
            median = (double) copy[copy.length / 2];
        }
        return median;
    }

    public int nearest(int n, long... args) {
        long nearest = 0;
        long value = (long) 2 * Integer.MAX_VALUE;
        for (long arg : args)
            if (value > Math.abs(n - arg)) {
                value = Math.abs(n - arg);
                nearest = arg;
            }
        var index = 0;
        for (int i = 0; i < args.length; i++) {
            if (nearest == args[i]) {
                index = i;
            }
        }
        return index;
    }

    public long nominatorCorrilation(long[] longs, long[] longs2) {
        long result = 0;
        long avarageX = expected_value(longs2);
        long avarageY = expected_value(longs);
        for (int i = 0; i < longs2.length; i++) {
            result += (longs2[i] - avarageX) * (longs[i] - avarageY);
        }
        return result;
    }

    public Double denominatorCorrilation(long[] longs, long[] longs2) {
        long result = 0;
        long leftPart = 0;
        long rightPart = 0;
        long avarageX = expected_value(longs2);
        long avarageY = expected_value(longs);
        for (int i = 0; i < longs2.length; i++) {
            leftPart += Math.pow((longs2[i] - avarageX), 2);
            rightPart += Math.pow((longs[i] - avarageY), 2);
        }
        result = leftPart * rightPart;
        if (result < 0) {
            result *= -1;
        }
        return Math.sqrt(result);
    }

    public long[] getMode(long[] longs) {
        long maxValue = 0, maxCount = 0;
        for (long aLong : longs) {
            int count = 0;
            for (long aLong1 : longs) {
                if (aLong1 == aLong)
                    ++count;
            }
            if (count > maxCount) {
                maxCount = count;
                maxValue = aLong;
            }
        }
        return new long[]{maxValue, maxCount};
    }

    public long summAll(long[] longs) {
        var result = 0;
        for (long l : longs) {
            result += l;
        }
        return result;
    }

    public double isPirson(long[] longs, long[] minIntravals, double[] avarageIntravals, long[] maxIntravals) {
        Controller controller = new Controller();
        var summAll = controller.summAll(longs);
        var countMath = 0.0;
        for (int i = 0; i < longs.length; i++) {
            countMath += avarageIntravals[i] * longs[i];
        }
        double math = countMath / summAll;
        var s = sqrt(controller.dispersionPirson(longs, avarageIntravals, math));
        double[] m = new double[32];
        var f1 = 0.0;
        var f2 = 0.0;
        for (int i = 0; i < longs.length; i++) {
            /*if (i == 0) {
                f2 = (maxIntravals[i] - math) / s;
                f2 = normRaspred(f2);
                m[i] = (1 - f2) * summAll;
            } else if (i == 31) {
                f2 = (maxIntravals[i] - math) / s;
                f2 = normRaspred(f2);
                m[i] = (1 - f2) * summAll;
            } else {*/
            f1 = (maxIntravals[i] - math) / s;
            f1 = normRaspred(f1);
            f2 = (minIntravals[i] - math) / s;
            f2 = normRaspred(f2);
            m[i] = (f1 - f2) * summAll;
        }
        var XI = 0.0;
        for (int i = 0; i < longs.length; i++) {
            XI += Math.pow(longs[i] - m[i], 2) / m[i];
        }
        return XI;
    }


    public double normRaspred(double value) {
        if (value < 0) {
            return 0.5 - 0.5 * erf(-1 * value / sqrt(2));
        } else {
            return 0.5 + 0.5 * erf(value / sqrt(2));
        }
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


}
