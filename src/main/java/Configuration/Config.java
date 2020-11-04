package Configuration;

import FileOperations.IO;
import FileOperations.XMLOperator;
import MoviesAndActors.Actor;
import MoviesAndActors.Movie;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Config {

//    == constants ==
    /**
     * Path for files that are needed only for some time
     */
    public static final Path TMP_FILES = Paths.get(System.getProperty("user.dir"), "tmp");
    /**
     * File to be used when there is no available image of actor on the web
     */
    public static final Path NO_ACTOR_IMAGE = Paths.get("src","main", "resources", "iHaveNoImage.jpg");
    /**
     * File to be used when there is no available cover of movie on the web
     */
    public static final Path NO_MOVIE_COVER = Paths.get("src","main", "resources", "movieHasNoCover.jpg");
    /**
     * File that contains last saw files in {@link Config#getMAIN_MOVIE_FOLDER()}
     */
    public static final File LAST_RIDE = Paths.get("src","main", "resources", "lastRide.xml").toFile();
    /**
     * Default path for saving data by application
     */
    public static final Path DEFAULT_SAVED_DATA = Paths.get(System.getProperty("user.dir"),"savedData");
    /**
     * Default path of location of main movies folder
     */
    private static final Path DEFAULT_MAIN_MOVIE = Paths.get("E:", "Rafał", "Filmy");

//    == fields ==
    @Getter private static Path SAVE_PATH;
    @Getter private static Path SAVE_PATH_MOVIE;
    @Getter private static Path SAVE_PATH_ACTOR;
    @Getter private static Path MAIN_MOVIE_FOLDER;


//  == static initializer ==

    static {
        initCfg();

        File tmpFiles = TMP_FILES.toFile();
        if(!tmpFiles.mkdir()) {
            IO.deleteDirectoryRecursively(tmpFiles);
            if(!tmpFiles.exists() && !tmpFiles.mkdir()) {
                log.warn("Could not create TMP directory \"{}\"", tmpFiles);
            }
        }
    }


//    == setters ==

    public static void setMAIN_MOVIE_FOLDER(Path mainMovieFolder) {
        MAIN_MOVIE_FOLDER = mainMovieFolder;
        updateParamInCfg("MAIN_MOVIE_FOLDER", mainMovieFolder.toString());
    }


    public static void setSAVE_PATH(File newDirectory, boolean moveFiles) {
        if(newDirectory != null) {
            if(!newDirectory.exists() && !newDirectory.mkdir()) {
                log.warn("Could not create directory \"{}\"", newDirectory);
            }
            if(moveFiles) {
                try {
                    Files.move(SAVE_PATH, newDirectory.toPath(), REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            updateParamInCfg("SAVE_PATH", newDirectory.toString());
            SAVE_PATH = newDirectory.toPath();
            log.debug("SAVE_PATH changed to \"{}\"", newDirectory.toString());
            updateRelativePaths();
        } else {
            log.warn("Couldn't change SAVE_PATH to null. SAVE_PATH is still \"{}\"", SAVE_PATH);
        }
    }

//    == private methods ==

    private static void initCfg() {
        File cfg = Paths.get("src","main", "resources", "config.cfg").toFile();
        if(cfg.exists() && !cfg.isDirectory() && XMLOperator.createDocToRead(cfg) != null) {
            Document doc = XMLOperator.createDocToRead(cfg);
            assert doc != null;
            Element root = doc.getDocumentElement();
            NodeList element = root.getElementsByTagName("SAVE_PATH");
            SAVE_PATH = Paths.get(element.item(0).getChildNodes().item(0).getTextContent());
            element = root.getElementsByTagName("MAIN_MOVIE_FOLDER");
            setMAIN_MOVIE_FOLDER(Paths.get(element.item(0).getChildNodes().item(0).getTextContent()));
        } else {
            Document doc = XMLOperator.createDoc();
            if(doc != null) {
                Element rootElement = doc.createElement("config");
                rootElement.appendChild(doc.createTextNode("\n\t"));
                doc.appendChild(rootElement);
                Element element = doc.createElement("SAVE_PATH");
                rootElement.appendChild(element);
                rootElement.appendChild(doc.createTextNode("\n\t"));
                element = doc.createElement("MAIN_MOVIE_FOLDER");
                rootElement.appendChild(element);
                rootElement.appendChild(doc.createTextNode("\n"));
                XMLOperator.makeSimpleSave(doc, cfg);
            }
            SAVE_PATH = DEFAULT_SAVED_DATA;
            updateParamInCfg("SAVE_PATH", DEFAULT_SAVED_DATA.toString());
            updateParamInCfg("MAIN_MOVIE_FOLDER", DEFAULT_MAIN_MOVIE.toString());

        }
        if(!SAVE_PATH.toFile().exists() && !SAVE_PATH.toFile().mkdirs()) {
            log.warn("Could not create directory \"{}\"", SAVE_PATH);
        }
        updateRelativePaths();
    }


//    == public methods ==

    public static void updateRelativePaths() {
        SAVE_PATH_MOVIE = Paths.get(SAVE_PATH.toString(), Movie.class.getSimpleName());
        SAVE_PATH_ACTOR = Paths.get(SAVE_PATH.toString(), Actor.class.getSimpleName());
        File moviePath = SAVE_PATH_MOVIE.toFile();
        File actorPath = SAVE_PATH_ACTOR.toFile();
        if(!moviePath.exists() && !moviePath.mkdir()) {
            log.warn("Could not create directory \"{}\"", moviePath);
        }
        if(!actorPath.exists() && !actorPath.mkdir()) {
            log.warn("Could not create directory \"{}\"", actorPath);
        }
    }

    public static void updateParamInCfg(String parameter, String value) {
        File cfg = Paths.get("src","main", "resources", "config.cfg").toFile();
        Document doc = XMLOperator.createDocToRead(cfg);
        if(doc == null) return;
        NodeList element = doc.getElementsByTagName(parameter);
        if(!element.item(0).getTextContent().equals(value)) {
            element.item(0).setTextContent(value);
            XMLOperator.makeSimpleSave(doc, cfg);
            log.info("Parameter \"{}\" changed to \"{}\" in config.cfg", parameter, value);
        }
    }


//    == Exceptions ==

    public static class ArgumentIssue extends Exception {
        public ArgumentIssue(String errorMessage) {
            super(errorMessage);
        }
    }
}
