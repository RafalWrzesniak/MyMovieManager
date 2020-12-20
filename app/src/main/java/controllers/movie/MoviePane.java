package controllers.movie;

import Configuration.Files;
import MoviesAndActors.Movie;
import app.Main;
import controllers.ContentPane;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import lombok.extern.slf4j.Slf4j;
import utils.PaneNames;

import java.io.IOException;
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
        createContextMenu();
        title.visibleProperty().bind(Bindings.or(
                Bindings.not(iHaveImage),
                Bindings.or(blue_background.visibleProperty(),
                            green_background.visibleProperty())
        ));
    }

//    == methods ==
    private void createContextMenu() {
        contextMenu = new ContextMenu();
        //        TODO context menu
        final MenuItem item1 = new MenuItem("Otwórz");
        final MenuItem item2 = new MenuItem("Oznacz jako obejrzane");
        final MenuItem item3 = new MenuItem("Zmień okładkę");
        final MenuItem item4 = new MenuItem("Dodaj do listy");
        final MenuItem item5 = new MenuItem("Usuń");
        item1.setOnAction(actionEvent -> System.out.println("item1 pressed"));
        contextMenu.getItems().addAll(item1, item2, item3, item4, item5);
        moviePane.setOnContextMenuRequested(e -> contextMenu.show(moviePane, e.getScreenX(), e.getScreenY()));
    }

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
    }

    @FXML
    @Override
    public void selectItem() {
        super.selectItem();
        FXMLLoader loader = Main.createLoader(PaneNames.MOVIE_DETAIL, resourceBundle);
        Parent movieDetails;
        try {
            movieDetails = loader.load();
        } catch (IOException e) {
            log.warn("Failed to load fxml view in MovieDetail");
            return;
        }
        MovieDetail movieDetailController = loader.getController();
        movieDetailController.setMovie(movie);
        movieDetailController.setMainController(mainController);
        mainController.rightDetail.getChildren().clear();
        mainController.rightDetail.getChildren().add(movieDetails);
    }


}
