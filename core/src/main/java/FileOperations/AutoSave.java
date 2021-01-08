package FileOperations;

import MoviesAndActors.Actor;
import MoviesAndActors.ContentType;
import MoviesAndActors.Movie;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public final class AutoSave extends Thread {

//    == fields ==
    public static final List<ContentType> NEW_OBJECTS = new ArrayList<>();


//  == methods ==
    @Override
    public void run() {
        setName("AutoSave");
        log.info("AutoSave thread started");
        boolean run = true;
        while (run) {
            synchronized (NEW_OBJECTS) {
                try {
                    NEW_OBJECTS.wait();
                } catch (InterruptedException e) {
                    run = false;
                }
                if (NEW_OBJECTS.size() > 0) {
                    log.info("Saving objects: \"{}\"", NEW_OBJECTS);
                    while(NEW_OBJECTS.size() != 0) {
                        Object object = NEW_OBJECTS.get(0);
                        if(object instanceof Movie) {
                            XMLOperator.saveContentToXML((Movie) object);
                        } else if(object instanceof Actor) {
                            XMLOperator.saveContentToXML((Actor) object);
                        }
                        NEW_OBJECTS.remove(NEW_OBJECTS.get(0));
                    }
                }
            }
        }
        log.info("AutoSave thread interrupted - exiting...");
    }
}