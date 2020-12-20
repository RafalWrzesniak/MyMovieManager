package controllers.actor;

import MoviesAndActors.Actor;
import controllers.MainController;

public interface ActorKind {
    void setActor(Actor actor);
    void setMainController(MainController mainController);
    MainController getMainController();
}
