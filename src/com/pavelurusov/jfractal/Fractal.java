package com.pavelurusov.jfractal;

/** @author Pavel Urusov, me@pavelurusov.com
 *  base abstract class for the Mandelbrot and Julia classes
 *  */

public abstract class Fractal {
    public abstract double calculate(double x, double y);
    public abstract double getMinX();
    public abstract double getMinY();
    public abstract double getMaxX();
    public abstract double getMaxY();
}
