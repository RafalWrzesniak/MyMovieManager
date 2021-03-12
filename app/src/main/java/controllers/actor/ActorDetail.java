package controllers.actor;

import MoviesAndActors.Actor;
import MoviesAndActors.Movie;
import app.Main;
import controllers.ContentDetail;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import utils.ActorContextMenu;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

@Slf4j
public class ActorDetail extends ContentDetail implements Initializable, ActorKind {

//    == fields ==
    @FXML private Label name, bornDate, died, country;
    @Getter @Setter private ActorPane owner;
    private ResourceBundle resourceBundle;
    private Actor actor;

//    == init ==
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.init(resourceBundle);
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


//    == methods ==
    @Override
    public void setActor(Actor actor) {
        this.actor = actor;
        if(owner != null) owner.setActor(actor);
        this.contextMenu = new ActorContextMenu(actor, resourceBundle, this).getContextMenu();
        vBox.setOnContextMenuRequested(e -> {
            if(contextMenu.isShowing()) {
                contextMenu.hide();
            } else {
                contextMenu.show(vBox, e.getScreenX(), e.getScreenY());
            }
        });
        Platform.runLater(() -> {
            contentImage.setImage(new Image(actor.getImagePath().toUri().toString()));
            name.setText(actor.getNameAndSurname());
            if(actor.getDeathDay() == null) {
                bornDate.setText(actor.getBirthday().format(Main.DTF) + ", " + actor.getAge() + resourceBundle.getString("detail.age"));
                vBox.getChildren().remove(died);
            } else {
                bornDate.setText(actor.getBirthday().format(Main.DTF) + " - " + actor.getDeathDay().format(Main.DTF));
                died.setText(resourceBundle.getString("detail.died") + actor.getAge());
            }
            country.setText(actor.getNationality());
        });


        FlowPane flowPane;
        List<Movie> movieList;
        if(actor.isActor()) {
            flowPane = createFlowPaneOf(resourceBundle.getString("detail.as_actor"));
            movieList = actor.getAllMoviesActorPlayedIn();
            Collections.sort(movieList);
            for(Movie movie : movieList) {
                Label movieLabel = textLabel(movie.getTitle());
                movieLabel.setOnMouseClicked(event -> mainController.openMovieDetail(movie, null, true));
                flowPane.getChildren().add(movieLabel);
            }
        }
        if(actor.isDirector()) {
            flowPane = createFlowPaneOf(resourceBundle.getString("detail.as_director"));
            movieList = actor.getAllMoviesDirectedBy();
            Collections.sort(movieList);
            for(Movie movie : movieList) {
                Label movieLabel = textLabel(movie.getTitle());
                movieLabel.setOnMouseClicked(event -> mainController.openMovieDetail(movie, null, true));
                flowPane.getChildren().add(movieLabel);
            }
        }
        if(actor.isWriter()) {
            flowPane = createFlowPaneOf(resourceBundle.getString("detail.as_writer"));
            movieList = actor.getAllMoviesWrittenBy();
            Collections.sort(movieList);
            for(Movie movie : movieList) {
                Label movieLabel = textLabel(movie.getTitle());
                movieLabel.setOnMouseClicked(event -> mainController.openMovieDetail(movie, null, true));
                flowPane.getChildren().add(movieLabel);
            }
        }

        // update movies if files are not complete
        actor.getAllMoviesActorPlayedIn().forEach(movie -> {
            if(!movie.getCast().contains(actor)) {
                movie.addActor(actor);
            }
        });
        actor.getAllMoviesDirectedBy().forEach(movie -> {
            if(!movie.getDirectors().contains(actor)) {
                movie.addDirector(actor);
            }
        });
        actor.getAllMoviesWrittenBy().forEach(movie -> {
            if(!movie.getWriters().contains(actor)) {
                movie.addWriter(actor);
            }
        });
    }

}

