package controllers.movie;

import MoviesAndActors.Actor;
import MoviesAndActors.Movie;
import app.Main;
import controllers.MainController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
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

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;

@Slf4j
public final class MovieInfo implements MovieKind {

//    == fields ==
    @Setter @Getter private MainController mainController;
    public static final String ID = "movieInfoScrollPane";
    private Movie movie;

    @FXML private HBox directors;
    @FXML private ImageView cover;
    @FXML private FlowPane cast, writers;
    @FXML @Getter private Button returnButton;
    @FXML private ScrollPane movieInfoScrollPane;
    @FXML private Label title, titleOrg, premiere, duration, rate, genres, productions, description, writersLabel, directorsLabel;

    //    == init ==
    public void initialize() {
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
        this.movie = movie;
        StringBuilder genreBuild = new StringBuilder();
        movie.getGenres().forEach(genre -> genreBuild.append(genre).append(", "));
        if(genreBuild.toString().contains(", ")) genreBuild.deleteCharAt(genreBuild.lastIndexOf(", "));
        StringBuilder productionsBuild = new StringBuilder();
        movie.getProduction().forEach(prod -> productionsBuild.append(prod).append(", "));
        if(productionsBuild.toString().contains(", ")) productionsBuild.deleteCharAt(productionsBuild.lastIndexOf(", "));

        Platform.runLater(() -> {
            openDetail(movie);
            cover.setImage(new Image(movie.getImagePath().toUri().toString()));
            title.setText(movie.getTitle());
            titleOrg.setText(movie.getTitleOrg());
            premiere.setText(movie.getPremiere().format(Main.DTF));
            duration.setText(movie.getDurationFormatted());
            rate.setText(String.format("%s / %d", movie.getRate(), movie.getRateCount()));
            description.setText(movie.getDescription());
            genres.setText(genreBuild.toString());
            productions.setText(productionsBuild.toString());
            movie.getDirectors().forEach(actor -> directors.getChildren().add(mainController.openActorPane(actor)));
            movie.getWriters().forEach(actor -> writers.getChildren().add(mainController.openActorPane(actor)));
            movie.getCast().forEach(actor -> cast.getChildren().add(mainController.openActorPane(actor)));
            if(directors.getChildren().size() == 0) directorsLabel.setVisible(false);
            if(writers.getChildren().size() == 0) writersLabel.setVisible(false);
        });
    }


    private void openDetail(Movie movie) {
        Actor actor;
        if(movie.getDirectors().size() != 0) {
            actor = movie.getDirectors().get(0);
            mainController.openActorDetail(actor, null, false);
        } else if(movie.getCast().size() > 0){
            actor = movie.getCast().get(0);
            mainController.openActorDetail(actor, null, false);
        }
    }


}
