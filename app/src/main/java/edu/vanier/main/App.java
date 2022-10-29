/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package edu.vanier.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Map.fxml"));
        loader.setController(new FXMLController());
        Pane root = loader.load();
        Scene scene = new Scene(root, 1200, 800, Color.WHITE);
        primaryStage.setScene(scene);
        primaryStage.show();

        //components in the map
        ArrayList<Shape> shapeDangers = dangers(root);
        ArrayList<Car> cars = new ArrayList<>();
        HashMap<Car, Double> findBestCar = new HashMap<>();
        // HashMap <Double, Car> findMaxX = new HashMap<>(); 

        for (int i = 0; i < 5; i++) {

            Car car = new Car(root);
            cars.add(car);
            car.setRotate(60 * i);
        }

        //Display Sensors length
        VBox sensors = new VBox();
        for (int i = 0; i < cars.get(0).sensors.length; i++) {
            Label label = new Label();
            Sensor sensor = cars.get(0).sensors[i];
            label.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(sensor.projectedLength.get()), cars.get(0).sensors[i].projectedLength));
            sensors.getChildren().add(label);
        }
        root.getChildren().add(sensors);

        //Behaviors at each frame.
        AnimationTimer timer = new AnimationTimer() {
            /*entry.getKey().setTimeElapsed(timer)
             long start = System.nanoTime();
             // some time passes
             long end = System.nanoTime();
             long elapsedTime = end - start; 
             each car must have their own timer to calculate the elapsed time... might be memory consuming 
           
             */

            @Override
            public void handle(long now) {

                for (int i = 0; i < cars.size(); i++) {
                    Car car = cars.get(i);
                    car.move();
                    randomMove(car);

                    //detect collision
                    for (int j = 0; j < shapeDangers.size(); j++) {
                        if (Shape.intersect(car, shapeDangers.get(j)).getBoundsInParent().getWidth() != -1) {
                            cars.remove(car);

                            findBestCar.put(car, shapeDangers.get(j).getLayoutX());
                            double maxValueInMap = (Collections.max(findBestCar.values()));
                            for (HashMap.Entry<Car, Double> entry : findBestCar.entrySet()) {
                                entry.getKey().setFitnessScore((double) entry.getKey().getVelocity() * entry.getKey().getTimeElapsed());
                            }
                            for (HashMap.Entry<Car, Double> currentEntry : findBestCar.entrySet()) {

                                if (currentEntry.getValue() == maxValueInMap) {
                                    currentEntry.getKey().setFitnessScore(maxValueInMap);
                                    // return currentEntry.getKey(); --> should be added to another function 
                                }

                            }

                            for (int k = 0; k < car.sensors.length; k++) {
                                root.getChildren().remove(car.sensors[k]);
                            }
                        }
                    }
                    detectSensorsIntersection(car, shapeDangers);
                }
            }
        };
        timer.start();

    }

    //detect all shapes that represent dangers to the car.
    private static ArrayList<Shape> dangers(Pane root) {

        ArrayList<Shape> dangers = new ArrayList<>();
        for (int i = 0; i < root.getChildren().size(); i++) {
            Node node = root.getChildren().get(i);
            if (!Circle.class.isInstance(node) && !Sensor.class.isInstance(node)) {
                dangers.add((Shape) root.getChildren().get(i));
            }
        }

        return dangers;
    }

    private static void detectSensorsIntersection(Car car1, ArrayList<Shape> dangers) {

        for (int i = 0; i < car1.sensors.length; i++) {
            Sensor cSensor = car1.sensors[i];
            boolean touched = false;
            ArrayList<Double> intersections = new ArrayList<>();
            for (int j = 0; j < dangers.size(); j++) {
                Shape shape = Shape.intersect(cSensor, dangers.get(j));
                if (shape.getBoundsInParent().getWidth() != -1) {
                    double projected = Math.sqrt(Math.pow((shape.getBoundsInParent().getCenterX() - car1.getCenterX()), 2) + Math.pow((shape.getBoundsInParent().getCenterY() - car1.getCenterY()), 2)) - car1.getRadius();
                    if (projected >= 0) {
                        cSensor.setStroke(Color.RED);
                        touched = true;
                        intersections.add(projected);
                    }
                }
            }
            if (touched) {
                Collections.sort(intersections);
                cSensor.projectedLength.setValue(intersections.get(0));
            }
            if (!touched) {
                cSensor.setStroke(Color.GREEN);
                cSensor.projectedLength.setValue(cSensor.length - car1.getRadius());

            }
        }
    }

    public void randomMove(Car car) {
        double rand = Math.random();
        if (rand < 0.5) {
            car.rotateLeft();
        } else {
            car.rotateRight();
        }

    }

    public static void main(String[] args) {
        launch(args);
    }

}
