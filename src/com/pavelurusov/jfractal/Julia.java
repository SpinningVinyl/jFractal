package com.pavelurusov.jfractal;

import com.pavelurusov.complex.Complex;

/** @author Pavel Urusov, me@pavelurusov.com
 * This class extends the abstract Fractal class and implelents the Julia set
 */

public class Julia extends Fractal {

    private Complex c;

    private final int maxIterations;
    private final double MAX_X = 1.6, MAX_Y = 1.6, MIN_X = -1.6, MIN_Y = -1.6;

    public Julia(int maxIterations, Complex c) {
        this.maxIterations = maxIterations;
        this.c = c;
    }

    public Julia (int maxIterations) {
        this(maxIterations, new Complex(0.285, 0.01));
    }

    public void setC(Complex c) {
        this.c = c;
    }

    public double calculate(double x, double y) {
        Complex z = new Complex(x, y);
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
