package utils;

import MoviesAndActors.ContentList;
import MoviesAndActors.Movie;
import controllers.MainController;
import controllers.movie.MovieKind;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

import java.util.List;
import java.util.ResourceBundle;

public class MultiMovieContextMenu extends MovieContextMenu {

    //    == constructor ==
    public MultiMovieContextMenu(List<Movie> movieList, ResourceBundle resourceBundle, MovieKind owner) {
        super(resourceBundle, owner);

        final MenuItem markAsWatchedItem = new MenuItem(resourceBundle.getString("context_menu.mark_as_watched"));
        markAsWatchedItem.setOnAction(event -> movieList.forEach(this::markAsWatched));

        final MenuItem reDownload = new MenuItem(resourceBundle.getString("context_menu.re_download"));
        reDownload.setOnAction(event -> movieList.forEach(this::downloadAgain));

        final Menu addToListItem = new Menu(resourceBundle.getString("context_menu.add_to_list"));

        final MenuItem removeItem = new MenuItem(resourceBundle.getString("context_menu.remove_from_list"));
        removeItem.setOnAction(event -> movieList.forEach(this::handleRemovingFromList));

        contextMenu.getItems().addAll(reDownload, new SeparatorMenuItem(), addToListItem, removeItem);
        contextMenu.setOnShowing(event -> {
            ContentList<Movie> selectedList = owner.getMainController().getMovieListView().getSelectionModel().getSelectedItem();
            if(selectedList.equals(MainController.moviesToWatch)) {
                contextMenu.getItems().add(0, markAsWatchedItem);
            }
            if(selectedList.equals(MainController.allMovies)) {
                removeItem.setDisable(true);
            }
            for(ContentList<Movie> list : MainController.observableContentMovies) {
                MenuItem listMenu = new MenuItem(list.getDisplayName());
                if(list.getListName().equals(ContentList.ALL_MOVIES_DEFAULT) || list.equals(selectedList)) {
                    listMenu.setDisable(true);
                }
                listMenu.setOnAction(actionEvent -> movieList.forEach(movie -> handleAddingToList(movie, list)));
                addToListItem.getItems().add(listMenu);
            }
        });
    }
}
