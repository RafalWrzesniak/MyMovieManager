package utils;

import MoviesAndActors.Actor;
import MoviesAndActors.ContentList;
import controllers.MainController;
import controllers.actor.ActorKind;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

import java.util.List;
import java.util.ResourceBundle;

public class MultiActorContextMenu extends ActorContextMenu {

    public MultiActorContextMenu(List<Actor> actorList, ResourceBundle resourceBundle, ActorKind owner) {
        super(resourceBundle, owner);

        final MenuItem reDownload = new MenuItem(resourceBundle.getString("context_menu.re_download"));
        reDownload.setOnAction(event -> actorList.forEach(this::reDownload));

        final Menu addToContentList = new Menu(resourceBundle.getString("context_menu.add_to_list"));

        final MenuItem removeFromList = new MenuItem(resourceBundle.getString("context_menu.remove_from_list"));
        removeFromList.setOnAction(event -> actorList.forEach(actor -> {
            owner.getMainController().getActorListView().getSelectionModel().getSelectedItem().remove(actor);
            owner.getMainController().displayPanesByPopulating(true);
            owner.getMainController().getActorListView().refresh();
        }));

        contextMenu.setOnShown(event -> {
            ContentList<Actor> selectedList = owner.getMainController().getActorListView().getSelectionModel().getSelectedItem();
            if(selectedList == null || selectedList.getListName().equals(ContentList.ALL_ACTORS_DEFAULT)) {
                removeFromList.setDisable(true);
            }
            addToContentList.getItems().clear();
            for(ContentList<Actor> list : MainController.observableContentActors) {
                MenuItem listMenu = new MenuItem(list.getDisplayName());
                if(list.getListName().equals(ContentList.ALL_ACTORS_DEFAULT) || list.equals(selectedList)) {
                    listMenu.setDisable(true);
                }
                listMenu.setOnAction(actionEvent -> actorList.forEach(actor -> {
                    list.add(actor);
                    owner.getMainController().getActorListView().refresh();
                }));
                addToContentList.getItems().add(listMenu);
            }
        });

        contextMenu.getItems().addAll(reDownload, addToContentList, removeFromList);



    }
}
