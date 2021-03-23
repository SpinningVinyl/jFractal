package com.pavelurusov.jfractal;

import com.pavelurusov.complex.Complex;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.geometry.Rectangle2D;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class Controller {
    @FXML
    public ImageView fractalView;
    @FXML
    public Slider zoomSlider;
    @FXML
    public Label zoomLabel;
    @FXML
    public BorderPane root;
    @FXML
    public RadioButton juliaRB, mandelbrotRB;
    @FXML
    public RadioButton c1RB, c2RB, c3RB, c4RB, cCustomRB;
    @FXML
    public Slider aSlider;
    @FXML
    public Label aLabel;
    @FXML
    public Slider colorSlider;
    @FXML
    public Label colorLabel;
    @FXML
    public Label progressLabel;

    private ThreadManager fractalGenerator;

    Settings settings = Settings.getInstance();
    private double zoom;

    @FXML
    public void initialize() {
        // create a new service
        fractalGenerator = new ThreadManager();
        // progressLabel should be visible only while the service is running
        progressLabel.visibleProperty().bind(fractalGenerator.runningProperty());
        // colorSlider should be disabled while the service is running
        colorSlider.disableProperty().bind(fractalGenerator.runningProperty());
        // if the service succeeded, display the generated image
        fractalGenerator.setOnSucceeded(e -> fractalView.setImage(fractalGenerator.getImage()));

        // UI for setting Julia set parameters should be disabled when Mandelbrot is selected
        c1RB.disableProperty().bind(mandelbrotRB.selectedProperty());
        c2RB.disableProperty().bind(mandelbrotRB.selectedProperty());
        c3RB.disableProperty().bind(mandelbrotRB.selectedProperty());
        c4RB.disableProperty().bind(mandelbrotRB.selectedProperty());
        cCustomRB.disableProperty().bind(mandelbrotRB.selectedProperty());
        aSlider.disableProperty().bind(mandelbrotRB.selectedProperty());

        // event handlers for scrolling the image by clicking + dragging
        ObjectProperty<Point2D> mouseDown = new SimpleObjectProperty<>();
        fractalView.setViewport(new Rectangle2D(0, 0, settings.IMG_WIDTH, settings.IMG_HEIGHT));
        fractalView.setOnMousePressed(e -> {
            Point2D click = translateCoords(fractalView, new Point2D(e.getX(), e.getY()));
            mouseDown.set(click);
        });
        fractalView.setOnMouseDragged(e -> {
            Point2D currentPoint = translateCoords(fractalView, new Point2D(e.getX(), e.getY()));
            move(fractalView, currentPoint.subtract(mouseDown.get()));
            mouseDown.set(translateCoords(fractalView, new Point2D(e.getX(), e.getY())));
        });

        // event handlers for zooming the image
        zoomSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            zoom = newValue.doubleValue();
            zoomLabel.setText(String.format("%.2f",zoom));
            Point2D center = translateCoords(fractalView, new Point2D(fractalView.getX()/2, fractalView.getY()/2));
            Rectangle2D viewport = fractalView.getViewport();
            double viewportWidth = settings.IMG_WIDTH /zoom;
            double viewportHeight = settings.IMG_HEIGHT /zoom;
            double viewportX = sanityCheck(center.getX() - (center.getX() - viewport.getMinX())/zoom, 0, settings.IMG_WIDTH - viewportWidth);
            double viewportY = sanityCheck(center.getY() - (center.getY() - viewport.getMinY())/zoom, 0, settings.IMG_HEIGHT - viewportHeight);
            fractalView.setViewport(new Rectangle2D(viewportX, viewportY, viewportWidth, viewportHeight));
        });
        fractalView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                reset(fractalView, settings.IMG_WIDTH, settings.IMG_HEIGHT);
                zoomSlider.setValue(1);
            }
        });

        // setting stuff when the values of the sliders are changed
        aSlider.valueProperty().addListener((obs, oldValue, newValue) -> aLabel.setText(String.format("%.4f",newValue.doubleValue()) + "π"));
        colorSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            settings.colorOffset = newValue.intValue();
            colorLabel.setText(String.valueOf(settings.colorOffset));
        });

    } // end of initialize()

    // event handler for the "Generate" button
    @FXML
    private void onGenButtonClicked () {
        generate();
    } // end of onGenButtonClicked()

    // event handler for the "Save PNG" button
    @FXML
    private void onSaveButtonClicked() {
        // if there is an image
        if (fractalView.getImage() != null) {
            // create a new buffered image
            BufferedImage bImage = SwingFXUtils.fromFXImage(fractalView.getImage(), null);
            // create and show file chooser to get the file path
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG image", "*.png"));
            File outputFile = fileChooser.showSaveDialog(root.getScene().getWindow());
            // if the user pressed the OK button
            if (outputFile != null) {
                try {
                    // save the image
                    ImageIO.write(bImage, "png", outputFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    } // end of onSaveButtonClicked()

    private void generate() {
        // create a new Writable Image
        WritableImage wi = new WritableImage(settings.IMG_WIDTH, settings.IMG_HEIGHT);

        // declare a new Fractal object
        Fractal fractal;

        // this variable is passed to the Julia object
        Complex c;

        // if the user selected the Mandelbrot set, create a new Mandelbrot object
        if (mandelbrotRB.isSelected()) {
            fractal = new Mandelbrot(settings.MAX_ITERATIONS);
        // else create a new Julia object
        } else {
            if (c1RB.isSelected()) {
                c = new Complex(0.285, 0.01);
            } else if(c2RB.isSelected()) {
                c = new Complex(-0.7269, 0.1889);
            } else if(c3RB.isSelected()) {
                c = new Complex(-0.8, 0.156);
            } else if(c4RB.isSelected()) {
                c = new Complex(-0.4, 0.6);
            } else {
                c = new Complex(0, aSlider.getValue()*Math.PI).exp();
            }
            fractal = new Julia(settings.MAX_ITERATIONS, c);
        }

        // if the service is not ready, reset it
        if (fractalGenerator.getState() != Worker.State.READY) {
            fractalGenerator.reset();
        }

        // pass the fractal and the image to the service
        fractalGenerator.setFractal(fractal);
        fractalGenerator.setImage(wi);

        // start the service
        fractalGenerator.start();
    } // end of generate()

    // helper methods for scrolling and zooming the image view
    private double sanityCheck(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    } // end of sanityCheck()

    private Point2D translateCoords(ImageView imageView, Point2D imageViewCoordinates) {
        double xProportion = imageViewCoordinates.getX() / imageView.getBoundsInLocal().getWidth();
        double yProportion = imageViewCoordinates.getY() / imageView.getBoundsInLocal().getHeight();

        Rectangle2D viewport = imageView.getViewport();
        return new Point2D(
                viewport.getMinX() + xProportion * viewport.getWidth(),
                viewport.getMinY() + yProportion * viewport.getHeight());
    } // end of translateCoords()

    private void reset(ImageView imageView, double width, double height) {
        imageView.setViewport(new Rectangle2D(0, 0, width, height));
    } // end of reset()

    private void move(ImageView imageView, Point2D delta) {
        Rectangle2D viewport = imageView.getViewport();

        double width = imageView.getImage().getWidth() ;
        double height = imageView.getImage().getHeight() ;

        double maxX = width - viewport.getWidth();
        double maxY = height - viewport.getHeight();

        double minX = sanityCheck(viewport.getMinX() - delta.getX(), 0, maxX);
        double minY = sanityCheck(viewport.getMinY() - delta.getY(), 0, maxY);

        imageView.setViewport(new Rectangle2D(minX, minY, viewport.getWidth(), viewport.getHeight()));
    } // end of move()
}