package controllers.movie;

import MoviesAndActors.Actor;
import MoviesAndActors.Movie;
import app.Main;
import controllers.MainController;
import controllers.actor.ActorDetail;
import controllers.actor.ActorPane;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import utils.PaneNames;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public final class MovieInfo implements MovieKind, Initializable {

//    == fields ==
    public static final String ID = "movieInfoScrollPane";
    @FXML public Button returnButton;
    @FXML public ScrollPane movieInfoScrollPane;
    @FXML private ImageView cover;
    @FXML private Label title, titleOrg, premiere, duration, rate, genres, productions, description, writersLabel, directorsLabel;
    @FXML private HBox directors, writers;
    @FXML private FlowPane cast;
    @Setter @Getter private MainController mainController;
    private ResourceBundle resourceBundle;

//    == init ==
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
            movieInfoScrollPane.getContent().setOnScroll(scrollEvent -> {
            double speed = 0.005;
            double deltaY = scrollEvent.getDeltaY() * speed;
            movieInfoScrollPane.setVvalue(movieInfoScrollPane.getVvalue() - deltaY);
        });

        // show / hide scroll bar
        movieInfoScrollPane.setPadding(new Insets(15, 28, 15, 15));
        movieInfoScrollPane.setOnMouseEntered(mouseEvent -> {
            movieInfoScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
            movieInfoScrollPane.setPadding(new Insets(15, 15, 15, 15));
        });
        movieInfoScrollPane.setOnMouseExited(mouseEvent -> {
            movieInfoScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            movieInfoScrollPane.setPadding(new Insets(15, 28, 15, 15));
        });
    }


//    == methods ==
    @Override
    public void setMovie(Movie movie) {
        StringBuilder genreBuild = new StringBuilder();
        movie.getGenres().forEach(genre -> genreBuild.append(genre).append(", "));
        StringBuilder productionsBuild = new StringBuilder();
        movie.getProduction().forEach(prod -> productionsBuild.append(prod).append(", "));

        Platform.runLater(() -> {
            cover.setImage(new Image(movie.getImagePath().toUri().toString()));
            title.setText(movie.getTitle());
            titleOrg.setText(movie.getTitleOrg());
            premiere.setText(movie.getPremiere().format(Main.DTF));
            duration.setText(movie.getDurationFormatted());
            rate.setText(String.format("%s / %d", movie.getRate(), movie.getRateCount()));
            description.setText(movie.getDescription());
            genres.setText(genreBuild.deleteCharAt(genreBuild.lastIndexOf(", ")).toString());
            productions.setText(productionsBuild.deleteCharAt(productionsBuild.lastIndexOf(", ")).toString());
            movie.getDirectors().forEach(actor -> directors.getChildren().add(createActorPane(actor)));
            movie.getWriters().forEach(actor -> writers.getChildren().add(createActorPane(actor)));
            movie.getCast().forEach(actor -> cast.getChildren().add(createActorPane(actor)));
            openDetail(movie);
            if(directors.getChildren().size() == 0) directorsLabel.setVisible(false);
            if(writers.getChildren().size() == 0) writersLabel.setVisible(false);
        });
    }

    private Parent createActorPane(Actor actor) {
        FXMLLoader loader;
        Parent parentPane;
        loader = Main.createLoader(PaneNames.ACTOR_PANE, resourceBundle);
        try {
            parentPane = loader.load();
        } catch (IOException e) {
            log.warn("Couldn't load actor pane for \"{}\"", actor);
            return null;
        }
        ActorPane actorPaneController = loader.getController();
        actorPaneController.setActor(actor);
        actorPaneController.setMainController(mainController);
        return parentPane;
    }

    private void openDetail(Movie movie) {
        Actor actor;
        if(movie.getDirectors().size() != 0) {
            actor = movie.getDirectors().get(0);
        } else {
            actor = movie.getCast().get(0);
        }
        FXMLLoader loader = Main.createLoader(PaneNames.ACTOR_DETAIL, resourceBundle);
        Parent actorDetails;
        try {
            actorDetails = loader.load();
        } catch (IOException e) {
            log.warn("Failed to load fxml view in ActorDetail");
            return;
        }
        ActorDetail actorDetailController = loader.getController();
        actorDetailController.setActor(actor);
        actorDetailController.setMainController(mainController);
        mainController.getRightDetail().getChildren().clear();
        mainController.getRightDetail().getChildren().add(actorDetails);
    }


}
