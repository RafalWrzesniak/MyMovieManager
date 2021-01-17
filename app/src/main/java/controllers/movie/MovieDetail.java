package controllers.movie;

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
import utils.MovieContextMenu;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;


@Slf4j
public class MovieDetail extends ContentDetail implements Initializable, MovieKind {

//    == fields ==
    @FXML private Label title, year, duration, description;
    private ResourceBundle resourceBundle;
    private Movie movie;
    @Getter @Setter private MoviePane owner;

//    == init ==
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.init(resourceBundle);
        this.resourceBundle = resourceBundle;
        title.setOnMouseEntered(mouseEvent -> title.setUnderline(true));
        title.setOnMouseExited(mouseEvent -> title.setUnderline(false));
        title.setOnMouseClicked(mouseEvent -> {
            try {
                Desktop.getDesktop().browse(movie.getFilmweb().toURI());
            } catch (IOException | URISyntaxException e) {
                log.warn("Couldn't open in browser link \"{}\"", movie.getFilmweb());
                e.printStackTrace();
            }
        });
    }


//    == methods ==
    @Override
    public void setMovie(Movie movie) {
        this.movie = movie;
        if(owner != null) owner.setMovie(movie);
        this.contextMenu = new MovieContextMenu(movie, resourceBundle, this).getContextMenu();
        vBox.setOnContextMenuRequested(e -> {
            if(contextMenu.isShowing()) {
                contextMenu.hide();
            } else {
                contextMenu.show(vBox, e.getScreenX(), e.getScreenY());
            }
        });
        Platform.runLater(() -> {
            title.setText(movie.getTitle());
            year.setText(movie.getPremiere().format(Main.DTF));
            duration.setText(movie.getDurationFormatted());
            description.setText(movie.getDescription());
            contentImage.setImage(new Image(movie.getImagePath().toUri().toString()));
        });

        FlowPane flowPane;
        List<Actor> actorList;
        if(movie.getDirectors().size() > 0) {
            flowPane = createFlowPaneOf(resourceBundle.getString("detail.as_director"));
            actorList = movie.getDirectors();
            Collections.sort(actorList);
            for(Actor actor : actorList) {
                Label actorLabel = textLabel(actor.getNameAndSurname());
                actorLabel.setOnMouseClicked(event -> mainController.openActorDetail(actor, null, true));
                flowPane.getChildren().add(actorLabel);
            }
        }
        flowPane = createFlowPaneOf(resourceBundle.getString("detail.as_actor"));
        actorList = movie.getCast().subList(0, Math.min(4, movie.getCast().size()));
        for(Actor actor : actorList) {
            Label actorLabel = textLabel(actor.getNameAndSurname());
            actorLabel.setOnMouseClicked(event -> mainController.openActorDetail(actor, null, true));
            flowPane.getChildren().add(actorLabel);
        }
    }

}
