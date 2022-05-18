package MoviesAndActors.builder;

import MoviesAndActors.Actor;

public class ActorBuilder extends Actor.ActorBuilder {

    @Override
    public Actor build() {
        Actor actor = super.build()
                .withDownloadedLocalImage()
                .withCalculatedAge();
        if(actor.getBirthday() == null || actor.getNationality() == null) return null;
        actor.saveMe();
        return actor;
    }

    public Actor.ActorBuilder createId() {
        return super.createId();
    }

    public Actor.ActorBuilder fullName(String fullName) {
        return super.fullName(fullName);
    }

    public static ActorBuilder builder() {
        return new ActorBuilder();
    }

}
