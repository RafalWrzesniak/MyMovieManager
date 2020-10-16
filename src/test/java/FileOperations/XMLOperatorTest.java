package FileOperations;

import Configuration.Config;
import MoviesAndActors.Actor;
import MoviesAndActors.ContentList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class XMLOperatorTest {

    private final Actor actor = new Actor(Map.ofEntries(
            Map.entry(Actor.ID, "0"),
            Map.entry(Actor.NAME, "Jack"),
            Map.entry(Actor.SURNAME, "Sparrow"),
            Map.entry(Actor.NATIONALITY, "Karaibian"),
            Map.entry(Actor.BIRTHDAY, "1957-06-02"),
            Map.entry(Actor.FILMWEB, "https://www.filmweb.pl/person/someone999"),
            Map.entry(Actor.IMAGE_PATH, "E:\\xInne\\dk.jpg")));

    private final Actor actor2 = new Actor(Map.ofEntries(
            Map.entry(Actor.ID, "1"),
            Map.entry(Actor.NAME, "Cezary"),
            Map.entry(Actor.SURNAME, "Pazura"),
            Map.entry(Actor.NATIONALITY, "Poland"),
            Map.entry(Actor.BIRTHDAY, "1962-06-13"),
            Map.entry(Actor.FILMWEB, "https://www.filmweb.pl/person/someone2"),
            Map.entry(Actor.IMAGE_PATH, "E:\\xInne\\cp.jpg")));

    @BeforeAll
    static void beforeAll() {
        Config.setSAVE_PATH(Config.TMP_FILES.toFile(), false);
    }

    @AfterAll
    static void afterAll() {
        Config.setSAVE_PATH(new File(System.getProperty("user.dir").concat("\\savedData")), false);
    }

    @BeforeEach
    void clearTmp() {
        IO.deleteDirectoryRecursively(Config.TMP_FILES.toFile());
        if(Config.TMP_FILES.toFile().mkdirs()) {
            Config.setSAVE_PATH(Config.TMP_FILES.toFile(), false);
        }

    }

    @Test
    void saveContentToXML() throws IOException {
        AutoSave.NEW_OBJECTS.add(actor);
        XMLOperator.saveContentToXML(actor);
        List<File> files = IO.listDirectory(Config.getSAVE_PATH_ACTOR().toFile());
        assertTrue(files.contains(Config.getSAVE_PATH_ACTOR().resolve("actor0").toFile()));
        File file = Config.getSAVE_PATH_ACTOR().resolve("actor0").resolve(actor.getReprName().concat(".xml")).toFile();
        assertEquals("<id>0</id>", Objects.requireNonNull(IO.findInFile(file, "<id>")).replaceAll("\\s", ""));
        assertEquals("<name>Jack</name>", Objects.requireNonNull(IO.findInFile(file, "<name>")).replaceAll("\\s", ""));
        assertEquals("<surname>Sparrow</surname>", Objects.requireNonNull(IO.findInFile(file, "<surname>")).replaceAll("\\s", ""));
        assertEquals("<nationality>Karaibian</nationality>", Objects.requireNonNull(IO.findInFile(file, "<nationality>")).replaceAll("\\s", ""));
        assertEquals("<birthday>1957-06-02</birthday>", Objects.requireNonNull(IO.findInFile(file, "<birthday>")).replaceAll("\\s", ""));
        assertEquals("<imagePath>E:\\xInne\\dk.jpg</imagePath>", Objects.requireNonNull(IO.findInFile(file, "<imagePath>")).replaceAll("\\s", ""));
        assertEquals("<filmweb>https://www.filmweb.pl/person/someone999</filmweb>", Objects.requireNonNull(IO.findInFile(file, "<filmweb>")).replaceAll("\\s", ""));
    }

    @Test
    void createAndDeleteListFile() throws IOException {
        ContentList<Actor> testList = new ContentList<>("actorTestList");
        testList.add(actor);
        List<File> files = IO.listDirectory(Config.getSAVE_PATH_ACTOR().toFile());
        File file = Config.getSAVE_PATH_ACTOR().resolve("actorTestList.xml").toFile();
        assertTrue(files.contains(file));

        assertEquals("<listName>actorTestList</listName>", Objects.requireNonNull(IO.findInFile(file, "<listName>")).replaceAll("\\s", ""));
        assertEquals("<type>Actor</type>", Objects.requireNonNull(IO.findInFile(file, "<type>")).replaceAll("\\s", ""));
        assertEquals("<actor>0</actor>", Objects.requireNonNull(IO.findInFile(file, "<actor>")).replaceAll("\\s", ""));

        XMLOperator.removeContentList(testList);
        files = IO.listDirectory(Config.getSAVE_PATH_ACTOR().toFile());
        assertFalse(files.contains(file));

    }

    @Test
    void updateListFile() throws IOException {
        ContentList<Actor> testList = new ContentList<>("actorTestList2");
        testList.add(actor);
        List<File> files = IO.listDirectory(Config.getSAVE_PATH_ACTOR().toFile());
        File file = Config.getSAVE_PATH_ACTOR().resolve("actorTestList2.xml").toFile();
        assertTrue(files.contains(file));
        testList.add(actor2);
        assertEquals("<actor>1</actor>", Objects.requireNonNull(IO.findInFile(file, "<actor>1</actor>")).replaceAll("\\s", ""));

        testList.remove(actor2);
        assertNull(IO.findInFile(file, "<actor>1</actor>"));
    }

    @Test
    void readAllDataFromFiles() throws InterruptedException {
        for(File file : IO.listDirectory(Config.getSAVE_PATH().toFile())) {
            IO.deleteDirectoryRecursively(file);
        }

        Path files = Paths.get("src","test", "resources", "DataForReadAllTest");
        try {
            FileSystemUtils.copyRecursively(files, Config.getSAVE_PATH());
        } catch (IOException e) {
            e.printStackTrace();
        }
        XMLOperator.ReadAllDataFromFiles readAllDataFromFiles = new XMLOperator.ReadAllDataFromFiles();
        readAllDataFromFiles.start();
        readAllDataFromFiles.join();

        assertEquals(1, readAllDataFromFiles.getAllMoviesLists().size());
        assertEquals(1, readAllDataFromFiles.getAllActorsLists().size());
        assertEquals(ContentList.ALL_ACTORS_DEFAULT, readAllDataFromFiles.getAllActorsLists().get(0).getListName());
        assertEquals(ContentList.ALL_MOVIES_DEFAULT, readAllDataFromFiles.getAllMoviesLists().get(0).getListName());

        assertEquals(1, readAllDataFromFiles.getAllMovies().size());
        assertEquals(2, readAllDataFromFiles.getAllActors().size());
        assertEquals("Most szpiegów", readAllDataFromFiles.getAllMovies().get(0).getTitle());

    }
}