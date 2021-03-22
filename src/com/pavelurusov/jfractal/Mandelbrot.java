package com.pavelurusov.jfractal;

import com.pavelurusov.complex.Complex;

/** @author Pavel Urusov, me@pavelurusov.com
 * This class extends the abstract Fractal class and implelents the Mandelbrot set
 */

public class Mandelbrot extends Fractal {
    private final int maxIterations;
    private final double MAX_X = 0.75, MAX_Y = 1.5, MIN_X = -2.25, MIN_Y = -1.5;


    public Mandelbrot(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public double calculate(double x, double y) {
        Complex z = new Complex(0,0);
        Complex c = new Complex(x, y);
        int n = 0;
        while(z.abs() <= 2 && n < maxIterations) {
            z = z.multiply(z).add(c);
            n++;
        }
        return n;
    }

    public double getMaxX() {
        return MAX_X;
    }

    public double getMaxY() {
        return MAX_Y;
    }

    public double getMinX() {
        return MIN_X;
    }

    public double getMinY() {
        return MIN_Y;
    }

}
