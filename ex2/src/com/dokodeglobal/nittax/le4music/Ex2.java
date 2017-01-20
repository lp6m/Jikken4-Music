package com.dokodeglobal.nittax.le4music;

import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import jp.ac.kyoto_u.kuis.le4music.Plot;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.jfree.chart.JFreeChart;

public class Ex2 extends Application{
	
    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("le4music-ex2: Karaoke System");
        AnchorPane root = FXMLLoader.load(getClass().getResource("GUI.fxml"));
        Scene scene = new Scene(root);      
        stage.setScene(scene);
        stage.show();
		
	}
	
    public static void main(String... args){
	    launch(args);
    }
    
}
