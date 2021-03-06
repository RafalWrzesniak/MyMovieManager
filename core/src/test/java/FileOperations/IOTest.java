package FileOperations;

import MoviesAndActors.Movie;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IOTest {

    static File tmp = new File(System.getProperty("user.dir").concat("\\tmpTest"));
    static String tmpPath = tmp.getPath();

    @BeforeAll
    static void createTmpFolder() throws IOException {
        for(int i = 0 ; i < 6; i++) {
            new File(tmpPath.concat("\\someDir").concat(String.valueOf(i))).mkdirs();
            new File(tmpPath.concat("\\someDir").concat(String.valueOf(i)).concat("\\iAmXml.xml")).createNewFile();
        }
        new File(tmpPath.concat("\\fileName.txt")).createNewFile();
        new File(tmpPath.concat("\\someFile.doc")).createNewFile();
        new File(tmpPath.concat("\\otherFile.asd")).createNewFile();
    }

    @AfterAll
    static void removeTmpFolder() {
        IO.deleteDirectoryRecursively(tmp);
    }

    @Test
    void listDirectory() {
        assertEquals(new ArrayList<>(Arrays.asList(tmp.listFiles())), IO.listDirectory(tmp));
    }


    @Test
    @Order(0)
    void getFileNamesInDirectory() {
        List<String> names = new ArrayList<>(Arrays.asList("fileName", "otherFile", "someDir0", "someDir1", "someDir2", "someDir3", "someDir4",
                "someDir5","someFile"));
        assertEquals(names, IO.getFileNamesInDirectory(tmp));
    }


    @Test
    void removeFileExtension() {
        assertEquals("name", IO.removeFileExtension("name.exe"));
        assertEquals("name", IO.removeFileExtension("name"));
        assertEquals("na.me", IO.removeFileExtension("na.me.txt"));
        assertEquals("..na.me", IO.removeFileExtension("..na.me.txt"));
    }

    @Test
    void getXmlFileFromDir() {
        assertEquals(new File(tmpPath.concat("\\someDir3\\iAmXml.xml")), IO.getXmlFileFromDir(new File((tmpPath.concat("\\someDir3")))));
        assertNull(IO.getXmlFileFromDir(tmp));
    }

    @Test
    void findInFile() throws IOException {
        File fileWithTxt = new File(tmpPath.concat("\\fileName.txt"));
        FileWriter myWriter = new FileWriter(fileWithTxt);
        myWriter.write("Something in first line\n");
        myWriter.write("Second line is here\n");
        myWriter.write("I am third and the last line\n");
        myWriter.close();
        assertEquals("Second line is here", IO.findInFile(fileWithTxt, "here"));
        assertEquals("I am third and the last line", IO.findInFile(fileWithTxt, "last"));
        assertNull(IO.findInFile(fileWithTxt, "tralala"));
    }

    @Test
    void createSummaryImage() {
        Movie movie = new Movie(Map.ofEntries(
                Map.entry(Movie.TITLE, Collections.singletonList("Some Title")),
                Map.entry(Movie.PREMIERE, Collections.singletonList("2020-08-31")),
                Map.entry(Movie.FILMWEB, Collections.singletonList("https://www.filmweb.pl/film/movie"))
        ), true);
        IO.createSummaryImage(movie, tmp);
        assertTrue(IO.listDirectory(tmp).contains(new File(tmpPath.concat("\\tmpTest.png"))));
    }

    @Test
    void deleteDirectory() {
        IO.deleteDirectoryRecursively(new File(tmpPath.concat("\\someDir0")));
        assertFalse(IO.listDirectory(tmp).contains(new File(tmpPath.concat("\\someDir0"))));
    }

}