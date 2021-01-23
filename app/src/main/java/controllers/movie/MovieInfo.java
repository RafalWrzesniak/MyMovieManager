package controllers.movie;

import MoviesAndActors.Actor;
import MoviesAndActors.Movie;
import app.Main;
import controllers.MainController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
        if(genreBuild.toString().contains(", ")) genreBuild.deleteCharAt(genreBuild.lastIndexOf(", "));
        StringBuilder productionsBuild = new StringBuilder();
        movie.getProduction().forEach(prod -> productionsBuild.append(prod).append(", "));
        if(productionsBuild.toString().contains(", ")) productionsBuild.deleteCharAt(productionsBuild.lastIndexOf(", "));

        Platform.runLater(() -> {
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
            openDetail(movie);
            if(directors.getChildren().size() == 0) directorsLabel.setVisible(false);
            if(writers.getChildren().size() == 0) writersLabel.setVisible(false);
        });
    }


    private void openDetail(Movie movie) {
        Actor actor;
        if(movie.getDirectors().size() != 0) {
            actor = movie.getDirectors().get(0);
        } else {
            actor = movie.getCast().get(0);
        }
        mainController.openActorDetail(actor, null, false);
    }


}
