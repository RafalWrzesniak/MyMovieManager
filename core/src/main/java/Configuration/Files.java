package Configuration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Files {

//    == directories ==
    /**
     * Path for files that are needed only for some time
     */
    public static final Path TMP_FILES = Paths.get(System.getProperty("user.dir"), "tmp");
    /**
     * Default path for saving data by application
     */
    public static final Path DEFAULT_SAVED_DATA = Paths.get(System.getProperty("user.dir"),"savedData");
    /**
     * Default path of location of main movies folder
     */
    public static final Path DEFAULT_MAIN_MOVIE = Paths.get(System.getProperty("user.dir"), "Filmy");
    /**
     * Default path of location of recently watched movies
     */
    public static final Path DEFAULT_RECENTLY_WATCHED = Paths.get(System.getProperty("user.dir"), "Ogladnięte");



//  == files ==
    /**
     * File to be used when there is no available image of actor on the web
     */
    public static final Path NO_ACTOR_IMAGE = Paths.get("app","src" ,"main", "resources", "iHaveNoImage.jpg");
    /**
     * File to be used when there is no available cover of movie on the web
     */
    public static final Path NO_MOVIE_COVER = Paths.get("app","src" ,"main", "resources", "movieHasNoCover.jpg");
    /**
     * File that contains last seen movies in {@link Config#getMAIN_MOVIE_FOLDER()}
     */
    public static final File LAST_RIDE = Paths.get("core","src", "main", "resources", "lastRide.xml").toFile();
    /**
     * File to store some app configuration
     */
    static final File CFG_FILE = Paths.get("core","src", "main", "resources", "config.cfg").toFile();

}
