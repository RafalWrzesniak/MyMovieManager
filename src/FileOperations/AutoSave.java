package FileOperations;

import MoviesAndActors.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public final class AutoSave extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(AutoSave.class.getName());
    public static final List<ContentType> NEW_OBJECTS = new ArrayList<>();

    {
        setName("AutoSave");
    }

    @Override
    public void run() {
        boolean run = true;
        while (run) {
            synchronized (NEW_OBJECTS) {
                try {
                    NEW_OBJECTS.wait();
                } catch (InterruptedException e) {
                    run = false;
                }
                if (NEW_OBJECTS.size() > 0) {
                    logger.info("Saving objects: \"{}\"", NEW_OBJECTS);
                    while(NEW_OBJECTS.size() != 0) {
                        XMLOperator.saveContentToXML(NEW_OBJECTS.get(0));
                        NEW_OBJECTS.remove(NEW_OBJECTS.get(0));
                    }
                }
            }
        }
        logger.info("AutoSave thread interrupted - exiting...");
    }
}