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
    public static final Path TMP_FILES = Config.getDEF_PATH().resolve("tmp");
    /**
     * Default path for saving data by application
     */
    public static final Path DEFAULT_SAVED_DATA = Config.getDEF_PATH().resolve("savedData");
    /**
     * Default path of location of main movies folder
     */
    public static final Path DEFAULT_MAIN_MOVIE = Config.getDEF_PATH().resolve("Filmy");
    /**
     * Default path of location of recently watched movies
     */
    public static final Path DEFAULT_RECENTLY_WATCHED = Config.getDEF_PATH().resolve("Oglądnięte");



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
    public static final File LAST_RIDE = Config.getDEF_PATH().resolve("cfg").resolve("lastRide.xml").toFile();
    /**
     * File to store some app configuration
     */
    static File CFG_FILE = Config.getDEF_PATH().resolve("cfg").resolve("config.cfg").toFile();

}
