package controllers.movie;

import Configuration.Files;
import MoviesAndActors.Movie;
import controllers.ContentPane;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import lombok.extern.slf4j.Slf4j;
import utils.MovieContextMenu;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class MoviePane extends ContentPane implements Initializable, MovieKind {

//    == fields ==
    @FXML private StackPane moviePane;
    @FXML private Label title, duration;
    private Movie movie;
    private ResourceBundle resourceBundle;

//    == init ==
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
        contentPane = moviePane;
        title.visibleProperty().bind(
                Bindings.or(
                        Bindings.not(iHaveImage),
                        Bindings.or(
                                blue_background.visibleProperty(),
                                green_background.visibleProperty()
                        )
                )
        );
    }

//    == methods ==

    @Override
    public void setMovie(Movie movie) {
        this.movie = movie;
        if(movie.getImagePath() == null || !movie.getImagePath().toFile().exists()) {
            movie.setImagePath(Files.NO_MOVIE_COVER);
        } else if(!movie.getImagePath().equals(Files.NO_MOVIE_COVER)) {
            super.iHaveImage.setValue(true);
        }
        cover.setImage(new Image(movie.getImagePath().toUri().toString()));
        duration.setText(movie.getDurationShortFormatted());
        title.setText(movie.getTitle());
        this.contextMenu = new MovieContextMenu(movie, resourceBundle, this).getContextMenu();
    }


    @Override
    public void selectItem() {
        super.selectItem();
        mainController.openMovieDetail(movie, this, false);
    }


}
