package controllers.movie;

import Configuration.Files;
import MoviesAndActors.Movie;
import controllers.ContentPane;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import lombok.extern.slf4j.Slf4j;
import utils.MovieContextMenu;
import utils.MultiMovieContextMenu;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

@Slf4j
public class MoviePane extends ContentPane implements Initializable, MovieKind {

//    == fields ==
    protected static Set<Movie> selectedMovies = new HashSet<>();
    private ResourceBundle resourceBundle;
    @FXML private Label title, duration;
    @FXML private StackPane moviePane;
    private Movie movie;

//    == init ==
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
        contentPane = moviePane;
//        title.visibleProperty().bind(
//                Bindings.or(
//                        Bindings.not(iHaveImage),
//                        Bindings.or(
//                                blue_background.visibleProperty(),
//                                green_background.visibleProperty()
//                        )
//                )
//        );
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

    @Override
    public void selectItemClicked(MouseEvent mouseEvent) {
        contextMenu.hide();
        if(mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            if(mouseEvent.isControlDown()) {
                if(!selectedMovies.contains(movie)) {
                    selectedMovies.add(movie);
                    blue_background.setVisible(false);
                    green_background.setVisible(true);
                } else {
                    selectedMovies.remove(movie);
                    blue_background.setVisible(true);
                    green_background.setVisible(false);
                }
            } else {
                selectedMovies.clear();
                selectedMovies.add(movie);
                selectItem();
                if(mouseEvent.getClickCount() >= 2) {
                    mainController.openMovieInfo(movie, resourceBundle);
                }
            }
        } else if(mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
            if(selectedMovies.size() == 0) selectedMovies.add(movie);
            if(selectedMovies.size() == 1) {
                selectedMovies.clear();
                selectedMovies.add(movie);
                selectItem();
                contextMenu = new MovieContextMenu(movie, resourceBundle, this).getContextMenu();
                contextMenu.show(contentPane, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            } else if(selectedMovies.size() > 1 && green_background.isVisible()) {
                contextMenu = new MultiMovieContextMenu(new ArrayList<>(selectedMovies), resourceBundle, this).getContextMenu();
                contextMenu.show(contentPane, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            } else {
                selectedMovies.clear();
                selectedMovies.add(movie);
                selectItem();
            }
        }
    }

}
