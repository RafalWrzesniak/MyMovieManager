package controllers;

import MoviesAndActors.Actor;
import MoviesAndActors.Movie;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

@Slf4j
public class ActorDetail extends ContentDetail implements Initializable {

    @FXML public Label name, bornDate, died, country;
    private Actor actor;
    private ResourceBundle resourceBundle;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
        name.setOnMouseEntered(mouseEvent -> name.setUnderline(true));
        name.setOnMouseExited(mouseEvent -> name.setUnderline(false));
        name.setOnMouseClicked(mouseEvent -> {
            try {
                Desktop.getDesktop().browse(actor.getFilmweb().toURI());
            } catch (IOException | URISyntaxException e) {
                log.warn("Couldn't open in browser link \"{}\"", actor.getFilmweb());
                e.printStackTrace();
            }
        });
    }

    public void setActor(Actor actor) {
        this.actor = actor;
        name.setText(actor.getNameAndSurname());
        if(actor.getDeathDay() == null) {
            bornDate.setText(actor.getBirthday() + ", " + actor.getAge() + resourceBundle.getString("detail.age"));
            vBox.getChildren().remove(died);
        } else {
            bornDate.setText(actor.getBirthday().toString().replaceAll("-", ".") + " - " + actor.getDeathDay().toString().replaceAll("-", "."));
            died.setText(resourceBundle.getString("detail.died") + actor.getAge());
        }
        country.setText(actor.getNationality());

        FlowPane flowPane;
        List<Movie> movieList;
        if(actor.isActor()) {
            flowPane = createFlowPaneOf(resourceBundle.getString("detail.as_actor"));
            movieList = actor.getAllMoviesActorPlayedIn();
            Collections.sort(movieList);
            for(Movie movie : movieList) {
                flowPane.getChildren().add(textLabel(movie.getTitle()));
            }
        }
        if(actor.isDirector()) {
            flowPane = createFlowPaneOf(resourceBundle.getString("detail.as_director"));
            movieList = actor.getAllMoviesDirectedBy();
            Collections.sort(movieList);
            for(Movie movie : movieList) {
                flowPane.getChildren().add(textLabel(movie.getTitle()));
            }
        }
        if(actor.isWriter()) {
            flowPane = createFlowPaneOf(resourceBundle.getString("detail.as_writer"));
            movieList = actor.getAllMoviesWrittenBy();
            Collections.sort(movieList);
            for(Movie movie : movieList) {
                flowPane.getChildren().add(textLabel(movie.getTitle()));
            }
        }

    }

}

