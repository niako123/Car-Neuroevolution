/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package edu.vanier.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import neuralNetwork.NeuralNetworkManipulation;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Map.fxml"));
        loader.setController(new FXMLController());
        Pane root = loader.load();
        Scene scene = new Scene(root, 1200, 800, Color.WHITE);
        primaryStage.setScene(scene);
        primaryStage.show();
        Label time = new Label();
        root.getChildren().add(time);

        //components in the map
        ArrayList<Shape> shapeDangers = dangers(root);
        ArrayList<Car> cars = new ArrayList<>();
        HashMap<Car, Double> findBestCar = new HashMap<>();
        // HashMap <Double, Car> findMaxX = new HashMap<>(); 
        ArrayList<Car> eliminatedCars = new ArrayList<>();

        for (int i = 0; i < 5; i++) {

            Car car = new Car(root);
            cars.add(car);
            car.setRotate(180);
        }

        /*
        //Display Sensors length
        VBox sensors = new VBox();
        for (int i = 0; i < cars.get(0).sensors.length; i++) {
            Label label = new Label();
            Sensor sensor = cars.get(0).sensors[i];
            label.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(sensor.projectedLength.get()), cars.get(0).sensors[i].projectedLength));
            sensors.getChildren().add(label);
        }
        root.getChildren().add(sensors);
         */
        //Behaviors at each frame.
        AnimationTimer timer = new AnimationTimer() {
            /*entry.getKey().setTimeElapsed(timer)
             long start = System.nanoTime();
             // some time passes
             long end = System.nanoTime();
             long elapsedTime = end - start; 
             each car must have their own timer to calculate the elapsed time... might be memory consuming 
           
             */


            int timeCounter = 0;

            @Override
            public void handle(long now) {
                timeCounter++;

                time.setText(String.valueOf(timeCounter));

                if (cars.size() == 0 || timeCounter == 10000) {
                    timeCounter = 0;
                    Collections.sort(eliminatedCars, new carComparator());
                    Car mutator = eliminatedCars.get(eliminatedCars.size() - 1);
                    Car secondMutator = eliminatedCars.get(eliminatedCars.size() - 2);
                    cars.addAll(eliminatedCars);

                    for (int i = 0; i < cars.size(); i++) {

                        cars.get(i).setCenterX(65);
                        cars.get(i).setCenterY(130);

                        cars.get(i).setRotate(170 + i * 3);
                        NeuralNetworkManipulation.Mutate(cars.get(i).neuralNetwork, mutator.neuralNetwork, mutator.fitnessScore, secondMutator.neuralNetwork, secondMutator.fitnessScore);

                    }

                    for (int i = 0; i < eliminatedCars.size(); i++) {

                        for (Sensor sensor : eliminatedCars.get(i).sensors) {

                            
                            
                            

                            if(!root.getChildren().contains(sensor))
                            root.getChildren().add(sensor);
                        }

                    }

                    eliminatedCars.clear();

                }

                for (int i = 0; i < cars.size(); i++) {
                    Car car = cars.get(i);

                    car.move();
                    car.fitnessScore ++;

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

                            eliminatedCars.add(car);
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
            if (!Circle.class.isInstance(node) && !Sensor.class.isInstance(node) && Shape.class.isInstance(node)) {
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

    public static void main(String[] args) {
        launch(args);
    }

}
