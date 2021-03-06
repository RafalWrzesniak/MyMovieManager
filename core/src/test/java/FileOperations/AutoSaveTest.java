package FileOperations;

import Configuration.Config;
import Configuration.Files;
import MoviesAndActors.Actor;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AutoSaveTest {

    @BeforeEach
    void clearTmp() {
        IO.deleteDirectoryRecursively(Files.TMP_FILES.toFile());
        System.out.println(Files.TMP_FILES.toFile().mkdirs());
        Config.setSAVE_PATH(Files.TMP_FILES.toFile(), false);
    }

    @BeforeAll
    static void beforeAll() {
        Config.setSAVE_PATH(Files.TMP_FILES.toFile(), false);
    }

    @AfterAll
    static void afterAll() {
        Config.setSAVE_PATH(new File(System.getProperty("user.dir").concat("\\savedData")), false);
    }


    @Test
    void startAutoSaveAndCheckIfItIsSaving() {
        Actor me = new Actor(Map.ofEntries(
                Map.entry(Actor.NAME, "Rafał"),
                Map.entry(Actor.SURNAME, "Wrześniak"),
                Map.entry(Actor.NATIONALITY, "Poland"),
                Map.entry(Actor.BIRTHDAY, "1994-08-11"),
                Map.entry(Actor.FILMWEB, "https://www.filmweb.pl/1"),
                Map.entry(Actor.IMAGE_PATH, "E:\\xInne\\me.jpg")
        ));


        AutoSave autoSave = new AutoSave();
        autoSave.start();

        Actor pazura = new Actor(Map.ofEntries(
                Map.entry(Actor.NAME, "Cezary"),
                Map.entry(Actor.SURNAME, "Pazura"),
                Map.entry(Actor.NATIONALITY, "Poland"),
                Map.entry(Actor.BIRTHDAY, "1962-06-13"),
                Map.entry(Actor.FILMWEB, "https://www.filmweb.pl/2"),
                Map.entry(Actor.IMAGE_PATH, "E:\\xInne\\cp.jpg")
        ));
        try {
            Thread.sleep(3000);
            autoSave.interrupt();
            autoSave.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<File> files = IO.listDirectory(Config.getSAVE_PATH_ACTOR().toFile());

        assertTrue(files.contains(Config.getSAVE_PATH_ACTOR().resolve("actor0").toFile()));
        assertTrue(files.contains(Config.getSAVE_PATH_ACTOR().resolve("actor1").toFile()));

        assertNotNull(IO.getXmlFileFromDir(Config.getSAVE_PATH_ACTOR().resolve("actor0").toFile()));
        assertNotNull(IO.getXmlFileFromDir(Config.getSAVE_PATH_ACTOR().resolve("actor1").toFile()));
    }

    @Test
    void checkIfObjectAddedToSaveListWillBeSaveAfterThreadInter() throws InterruptedException {
        Actor me = new Actor(Map.ofEntries(
                Map.entry(Actor.NAME, "Rafał"),
                Map.entry(Actor.SURNAME, "Wrześniak"),
                Map.entry(Actor.NATIONALITY, "Poland"),
                Map.entry(Actor.BIRTHDAY, "1994-08-11"),
                Map.entry(Actor.FILMWEB, "https://www.filmweb.pl"),
                Map.entry(Actor.IMAGE_PATH, "E:\\xInne\\me.jpg")
        ));

        Actor pazura = new Actor(Map.ofEntries(
                Map.entry(Actor.NAME, "Cezary"),
                Map.entry(Actor.SURNAME, "Pazura"),
                Map.entry(Actor.NATIONALITY, "Poland"),
                Map.entry(Actor.BIRTHDAY, "1962-06-13"),
                Map.entry(Actor.FILMWEB, "https://www.filmweb.pl/person/Cezary+Pazura-81"),
                Map.entry(Actor.IMAGE_PATH, "E:\\xInne\\cp.jpg")

        ));

        AutoSave autoSave = new AutoSave();
        autoSave.start();
        autoSave.interrupt();

        Thread.sleep(3000);
        List<File> files = IO.listDirectory(Config.getSAVE_PATH_ACTOR().toFile());
        assertTrue(files.contains(Config.getSAVE_PATH_ACTOR().resolve("actor2").toFile()));
        assertTrue(files.contains(Config.getSAVE_PATH_ACTOR().resolve("actor3").toFile()));

        assertNotNull(IO.getXmlFileFromDir(Config.getSAVE_PATH_ACTOR().resolve("actor2").toFile()));
        assertNotNull(IO.getXmlFileFromDir(Config.getSAVE_PATH_ACTOR().resolve("actor3").toFile()));




    }



}