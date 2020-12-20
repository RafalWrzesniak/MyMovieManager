package controllers.movie;

import MoviesAndActors.Movie;
import controllers.MainController;

public interface MovieKind {
    void setMovie(Movie movie);
    void setMainController(MainController mainController);
    MainController getMainController();
}
