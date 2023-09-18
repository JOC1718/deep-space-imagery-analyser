package com.example.ca1_v1;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import utils.ObjectImageProcessing;

import java.io.File;
import java.util.*;

public class IndexController {
    public ImageView bwImageView, originalImageView;
    public Slider brightnessSlider;
    public RadioButton labelBtn;
    public Spinner<Integer> minSize, maxSize;
    ObjectImageProcessing imageProcessing;
    public static IndexController indexController;

    public void initialize(){
        brightnessSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> getBwImage());


        bwImageView.setOnMouseClicked(mouseEvent -> bwImageViewClicked((int)mouseEvent.getX(), (int)mouseEvent.getY())); //System.out.println((int)mouseEvent.getX()+" "+(int)mouseEvent.getY()));

        SpinnerValueFactory<Integer> minSpinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 0);
        minSize.setValueFactory(minSpinnerValueFactory);
        minSize.setEditable(true);

        SpinnerValueFactory<Integer> maxSpinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 10000);
        maxSize.setValueFactory(maxSpinnerValueFactory);
        maxSize.setEditable(true);

        indexController = this;
    }

    public void openFileMenu(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(HelloApplication.primaryStage);

        if(file != null){
            initializeScene(file);
            reset(null);
        }
    }

    private void initializeScene(File file){
        imageProcessing = new ObjectImageProcessing(new Image(file.toURI().toString()));

        originalImageView.setFitWidth(imageProcessing.getWidth());
        originalImageView.setFitHeight(imageProcessing.getHeight());
        originalImageView.setImage(imageProcessing.getImage());

        bwImageView.setFitWidth(imageProcessing.getWidth());
        bwImageView.setFitHeight(imageProcessing.getHeight());
        bwImageView.setImage(imageProcessing.convertImageBW(brightnessSlider.getValue(), minSize.getValue(), maxSize.getValue()));
    }

    private void getBwImage(){
        bwImageView.setImage(imageProcessing.convertImageBW(brightnessSlider.getValue(), minSize.getValue(), maxSize.getValue()));
        reset(null);
    }

    public void bwImageViewClicked(double x, double y){
        if(imageProcessing != null){
            bwImageView.setImage(imageProcessing.colourObject((int)x, (int)y));
        }
    }

    public void findObjects(ActionEvent actionEvent) {
        bwImageView.setImage(imageProcessing.findObjects(minSize.getValue(), maxSize.getValue()));
    }

    public void identify(ActionEvent actionEvent) {
        reset(null);

        Object[] objects = imageProcessing.identifyObjects((int)originalImageView.getLayoutX(), (int)originalImageView.getLayoutY());

        if(labelBtn.isSelected()){
            ((Pane) originalImageView.getParent()).getChildren().addAll((ArrayList<Circle>)objects[0]);
            ((Pane) originalImageView.getParent()).getChildren().addAll((ArrayList<Text>)objects[1]);
        } else{
            ((Pane) originalImageView.getParent()).getChildren().addAll((ArrayList<Circle>)objects[0]);
        }

    }

    public void reset(ActionEvent event) {
        ((Pane)originalImageView.getParent()).getChildren().removeIf(c -> c instanceof Circle);
        ((Pane)originalImageView.getParent()).getChildren().removeIf(t -> t instanceof Text);

    }

    public void colourObjects(ActionEvent event) {
        if(imageProcessing != null){
            bwImageView.setImage(imageProcessing.colourObjects());
        }
    }
}
