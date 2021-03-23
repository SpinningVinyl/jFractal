package com.pavelurusov.jfractal;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadManager extends Service<Void> {

    WritableImage image;
    Fractal fractal;
    Settings settings = Settings.getInstance();

    public ThreadManager(WritableImage image, Fractal fractal) {
        this.image = image;
        this.fractal = fractal;
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int maxCol = settings.IMG_WIDTH - 1;
                int maxRow = settings.IMG_HEIGHT - 1;
                ExecutorService pool = Executors.newFixedThreadPool(4);
                pool.execute(createRunnable(0, 0, maxRow/2, maxCol/2));
                pool.execute(createRunnable(0, maxCol/2 + 1, maxRow/2, maxCol));
                pool.execute(createRunnable(maxRow/2 + 1, 0, maxRow, maxCol/2));
                pool.execute(createRunnable(maxRow/2 + 1, maxCol/2 + 1, maxRow, maxCol));
                pool.shutdown();
                while(!pool.isTerminated()) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        };
    }

    private Runnable createRunnable(int minRow, int minCol, int maxRow, int maxCol) {
        return () -> {
            double deltaX = (fractal.getMaxX() - fractal.getMinX()) / ((int) image.getWidth());
            double deltaY = (fractal.getMaxY() - fractal.getMinY()) / ((int) image.getHeight());
            for(int col = minCol; col <= maxCol; col++) {
                for (int row = minRow; row <= maxRow; row++) {
                    double i = fractal.calculate(fractal.getMinX() + col * deltaX, fractal.getMaxY() - row * deltaY);
                    double hue = settings.colorOffset + 360*(i/(double) settings.MAX_ITERATIONS);
                    double brightness = ((int) i == settings.MAX_ITERATIONS) ? 0 : 1;
                    image.getPixelWriter().setColor(col, row, Color.hsb(hue, 1.0, brightness));
                }
            }
        };
    }

}
