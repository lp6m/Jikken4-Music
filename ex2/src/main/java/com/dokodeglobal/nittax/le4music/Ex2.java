package com.dokodeglobal.nittax.le4music;
import com.dokodeglobal.nittax.le4music.analyzer.*;
import com.dokodeglobal.nittax.le4music.myutils.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import javafx.scene.layout.*;
public class Ex2 extends Application{
	
    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("le4music-ex1: GUI");
        AnchorPane root = FXMLLoader.load(getClass().getResource("GUI.fxml"));
        Scene scene = new Scene(root);      
        stage.setScene(scene);
        stage.show();
	}
	
    public static void main(String... args){
        launch(args);
    }
    
}
