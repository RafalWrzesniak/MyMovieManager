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

//    == fields ==
    @Getter private static Path SAVE_PATH;
    @Getter private static Path SAVE_PATH_MOVIE;
    @Getter private static Path SAVE_PATH_ACTOR;
    @Getter private static Path MAIN_MOVIE_FOLDER;
    @Getter private static Path RECENTLY_WATCHED;


//  == static initializer ==
    static {
        initCfg();

        File tmpFiles = Configuration.Files.TMP_FILES.toFile();
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

    public static void setRECENTLY_WATCHED(Path recentlyWatched) {
        MAIN_MOVIE_FOLDER = recentlyWatched;
        updateParamInCfg("RECENTLY_WATCHED", recentlyWatched.toString());
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
        if(Configuration.Files.CFG_FILE.exists() && !Configuration.Files.CFG_FILE.isDirectory()
                && XMLOperator.createDocToRead(Configuration.Files.CFG_FILE) != null) {
            Document doc = XMLOperator.createDocToRead(Configuration.Files.CFG_FILE);
            assert doc != null;
            Element root = doc.getDocumentElement();
            NodeList element = root.getElementsByTagName("SAVE_PATH");
            SAVE_PATH = Paths.get(element.item(0).getChildNodes().item(0).getTextContent());
            element = root.getElementsByTagName("RECENTLY_WATCHED");
            RECENTLY_WATCHED = Paths.get(element.item(0).getChildNodes().item(0).getTextContent());
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
                rootElement.appendChild(doc.createTextNode("\n\t"));
                element = doc.createElement("RECENTLY_WATCHED");
                rootElement.appendChild(element);
                rootElement.appendChild(doc.createTextNode("\n"));
                XMLOperator.makeSimpleSave(doc, Configuration.Files.CFG_FILE);
            }
            SAVE_PATH = Configuration.Files.DEFAULT_SAVED_DATA;
            updateParamInCfg("SAVE_PATH", Configuration.Files.DEFAULT_SAVED_DATA.toString());
            MAIN_MOVIE_FOLDER = Configuration.Files.DEFAULT_MAIN_MOVIE;
            updateParamInCfg("MAIN_MOVIE_FOLDER", Configuration.Files.DEFAULT_MAIN_MOVIE.toString());
            RECENTLY_WATCHED = Configuration.Files.DEFAULT_RECENTLY_WATCHED;
            updateParamInCfg("RECENTLY_WATCHED", Configuration.Files.DEFAULT_RECENTLY_WATCHED.toString());

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
        Document doc = XMLOperator.createDocToRead(Configuration.Files.CFG_FILE);
        if(doc == null) return;
        NodeList element = doc.getElementsByTagName(parameter);
        if(!element.item(0).getTextContent().equals(value)) {
            element.item(0).setTextContent(value);
            XMLOperator.makeSimpleSave(doc, Configuration.Files.CFG_FILE);
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
