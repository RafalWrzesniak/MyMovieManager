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
import java.nio.file.Path;
import java.nio.file.Paths;

import static Configuration.Files.*;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Config {

//    == fields ==
    @Getter private static Path SAVE_PATH;
    @Getter private static final Path DEF_PATH;
    @Getter private static Path SAVE_PATH_MOVIE;
    @Getter private static Path SAVE_PATH_ACTOR;
    @Getter private static Path RECENTLY_WATCHED;
    @Getter private static Path MAIN_MOVIE_FOLDER;

//  == static initializer ==
    static {
        if(Config.class.getResource("Config.class").toString().startsWith("jar")) {
            String startPath = Config.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            int toIndex = startPath.contains("!") ? startPath.indexOf("!") : startPath.length();
            DEF_PATH = Paths.get(startPath.substring(startPath.indexOf("/") + 1, toIndex)).getParent();
        } else {
            DEF_PATH = Paths.get(System.getProperty("user.dir"));
        }
        log.debug("Default path is {}", DEF_PATH);
        initCfg();
        boolean createSaveData = DEFAULT_SAVED_DATA.toFile().mkdirs();
        boolean createMainMovie = DEFAULT_MAIN_MOVIE.toFile().mkdirs();
        boolean createRecently = DEFAULT_RECENTLY_WATCHED.toFile().mkdirs();
        if(createSaveData && createMainMovie && createRecently) {
            log.debug("All three default directories created");
        }

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
                IO.moveFiles(SAVE_PATH, newDirectory.toPath());
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
        if(CFG_FILE.exists() && !CFG_FILE.isDirectory()
                && XMLOperator.createDocToRead(CFG_FILE) != null) {
            Document doc = XMLOperator.createDocToRead(CFG_FILE);
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
                createNodesInCfgFile(doc);
                File cfgPath = CFG_FILE.getParentFile();
                if(!cfgPath.exists() && !cfgPath.mkdirs()) {
                    log.warn("Could not create directory \"{}\"", cfgPath);
                }
                XMLOperator.makeSimpleSave(doc, CFG_FILE);
                updateNodesWithDefValues();
            }
            SAVE_PATH = DEFAULT_SAVED_DATA;
            updateParamInCfg("SAVE_PATH", DEFAULT_SAVED_DATA.toString());
            MAIN_MOVIE_FOLDER = DEFAULT_MAIN_MOVIE;
            updateParamInCfg("MAIN_MOVIE_FOLDER", DEFAULT_MAIN_MOVIE.toString());
            RECENTLY_WATCHED = DEFAULT_RECENTLY_WATCHED;
            updateParamInCfg("RECENTLY_WATCHED", DEFAULT_RECENTLY_WATCHED.toString());

        }
        if(!SAVE_PATH.toFile().exists() && !SAVE_PATH.toFile().mkdirs()) {
            log.warn("Could not create directory \"{}\"", SAVE_PATH);
        }
        updateRelativePaths();
    }

    private static void createNodesInCfgFile(Document doc) {
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
        rootElement.appendChild(doc.createTextNode("\n\t"));
        element = doc.createElement("LINE_WITH_MOVIE_DATA");
        rootElement.appendChild(element);
        rootElement.appendChild(doc.createTextNode("\n\t"));
        element = doc.createElement("LINE_WITH_MOVIE_DATA2");
        rootElement.appendChild(element);
        rootElement.appendChild(doc.createTextNode("\n\t"));
        element = doc.createElement("LINE_WITH_ACTOR_DATA");
        rootElement.appendChild(element);
        rootElement.appendChild(doc.createTextNode("\n\t"));
        element = doc.createElement("LINE_WITH_ACTOR_DATA2");
        rootElement.appendChild(element);
        rootElement.appendChild(doc.createTextNode("\n\t"));
        element = doc.createElement("LINE_WITH_CAST_DATA");
        rootElement.appendChild(element);
        rootElement.appendChild(doc.createTextNode("\n\t"));
        element = doc.createElement("LINE_WITH_ACTOR_FILMOGRAPHY");
        rootElement.appendChild(element);
        rootElement.appendChild(doc.createTextNode("\n\t"));
        element = doc.createElement("ACTOR_NAME");
        rootElement.appendChild(element);
        rootElement.appendChild(doc.createTextNode("\n\t"));
        element = doc.createElement("ACTOR_NATIONALITY");
        rootElement.appendChild(element);
        rootElement.appendChild(doc.createTextNode("\n\t"));
        element = doc.createElement("ACTOR_BIRTHDAY");
        rootElement.appendChild(element);
        rootElement.appendChild(doc.createTextNode("\n\t"));
        element = doc.createElement("ACTOR_IMAGE_URL");
        rootElement.appendChild(element);
        rootElement.appendChild(doc.createTextNode("\n\t"));
        element = doc.createElement("MOVIE_TITLE");
        rootElement.appendChild(element);
        rootElement.appendChild(doc.createTextNode("\n\t"));
        element = doc.createElement("MOVIE_TITLE_ORG");
        rootElement.appendChild(element);
        rootElement.appendChild(doc.createTextNode("\n\t"));
        element = doc.createElement("MOVIE_PREMIERE");
        rootElement.appendChild(element);
        rootElement.appendChild(doc.createTextNode("\n\t"));
        element = doc.createElement("MOVIE_DURATION");
        rootElement.appendChild(element);
        rootElement.appendChild(doc.createTextNode("\n\t"));
        element = doc.createElement("MOVIE_RATE");
        rootElement.appendChild(element);
        rootElement.appendChild(doc.createTextNode("\n\t"));
        element = doc.createElement("MOVIE_RATE_COUNT");
        rootElement.appendChild(element);
        rootElement.appendChild(doc.createTextNode("\n\t"));
        element = doc.createElement("MOVIE_DESCRIPTION");
        rootElement.appendChild(element);
        rootElement.appendChild(doc.createTextNode("\n\t"));
        element = doc.createElement("MOVIE_IMAGE_URL");
        rootElement.appendChild(element);
        rootElement.appendChild(doc.createTextNode("\n\t"));
        element = doc.createElement("MOVIE_GENRES");
        rootElement.appendChild(element);
        rootElement.appendChild(doc.createTextNode("\n\t"));
        element = doc.createElement("MOVIE_PRODUCTION");
        rootElement.appendChild(element);
        rootElement.appendChild(doc.createTextNode("\n\t"));
        element = doc.createElement("MOVIE_CAST");
        rootElement.appendChild(element);
        rootElement.appendChild(doc.createTextNode("\n\t"));
        element = doc.createElement("MOVIE_DIRECTORS");
        rootElement.appendChild(element);
        rootElement.appendChild(doc.createTextNode("\n\t"));
        element = doc.createElement("MOVIE_WRITERS");
        rootElement.appendChild(element);
        rootElement.appendChild(doc.createTextNode("\n"));
    }

    private static void updateNodesWithDefValues() {
        updateParamInCfg("LINE_WITH_MOVIE_DATA", "data-linkable=\"filmMain\"");
        updateParamInCfg("LINE_WITH_MOVIE_DATA2", "data-source=\"linksData\"");
        updateParamInCfg("LINE_WITH_ACTOR_DATA", "personMainHeader");
        updateParamInCfg("LINE_WITH_ACTOR_DATA2", "data-linkable=\"personMain\"");
        updateParamInCfg("LINE_WITH_CAST_DATA", "filmFullCastSection__list");
        updateParamInCfg("LINE_WITH_ACTOR_FILMOGRAPHY", "userFilmographyfalseactors");
        updateParamInCfg("ACTOR_NAME", "itemprop=\"name\"");
        updateParamInCfg("ACTOR_NATIONALITY", "birthPlace");
        updateParamInCfg("ACTOR_BIRTHDAY", "itemprop=\"birthDate\" content");
        updateParamInCfg("ACTOR_IMAGE_URL", "itemprop=\"image\" src");
        updateParamInCfg("MOVIE_TITLE", "\"filmDataBasic\">{\"id\":\\d+,\"title");
        updateParamInCfg("MOVIE_TITLE_ORG", "originalTitle");
        updateParamInCfg("MOVIE_PREMIERE", "releaseWorldPublicString");
        updateParamInCfg("MOVIE_DURATION", "duration");
        updateParamInCfg("MOVIE_RATE", "data-rate");
        updateParamInCfg("MOVIE_RATE_COUNT", "dataRating-count");
        updateParamInCfg("MOVIE_DESCRIPTION", "itemprop=\"description\"");
        updateParamInCfg("MOVIE_IMAGE_URL", "itemprop=\"image\" content");
        updateParamInCfg("MOVIE_GENRES", "gatunek");
        updateParamInCfg("MOVIE_PRODUCTION", "produkcja");
        updateParamInCfg("MOVIE_CAST", "actors");
        updateParamInCfg("MOVIE_DIRECTORS", "director");
        updateParamInCfg("MOVIE_WRITERS", "screenwriter");
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
        Document doc = XMLOperator.createDocToRead(CFG_FILE);
        if(doc == null) return;
        NodeList element = doc.getElementsByTagName(parameter);
        if(!element.item(0).getTextContent().equals(value)) {
            element.item(0).setTextContent(value);
            XMLOperator.makeSimpleSave(doc, CFG_FILE);
            log.info("Parameter \"{}\" changed to \"{}\" in config.cfg", parameter, value);
        }
    }

    public static String getParamValue(String parameter) {
        Document doc = XMLOperator.createDocToRead(CFG_FILE);
        if(doc == null) return "";
        return doc.getElementsByTagName(parameter).item(0).getTextContent();
    }

//    == Exceptions ==

    public static class ArgumentIssue extends Exception {
        public ArgumentIssue(String errorMessage) {
            super(errorMessage);
        }
    }
}
