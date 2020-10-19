package FileOperations;

import Configuration.Config;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class ExportImportTest {

    @BeforeAll
    static void beforeAll() {
        Config.setSAVE_PATH(Config.TMP_FILES.toFile(), false);
    }

    @AfterAll
    static void afterAll() {
        Config.setSAVE_PATH(Config.DEFAULT_SAVED_DATA.toFile(), false);
    }

    void clearTmp() {
        IO.deleteDirectoryRecursively(Config.TMP_FILES.toFile());
        if(Config.TMP_FILES.toFile().mkdirs()) {
            Config.setSAVE_PATH(Config.TMP_FILES.toFile(), false);
        }
    }

    void copyTestFiles() {
        Path files = Paths.get("src","test", "resources", "SomeTestData");
        try {
            FileSystemUtils.copyRecursively(files, Config.getSAVE_PATH());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void exportCurrentDataTest() throws InterruptedException {
        clearTmp();
        copyTestFiles();

        ExportImport.ExportAll exportAll = new ExportImport.ExportAll(Config.getSAVE_PATH().resolve("exportedData.zip").toFile());
        exportAll.start();
        exportAll.join();

        assertTrue(Config.getSAVE_PATH().resolve("exportedData.zip").toFile().exists());
    }

    @Test
    void importExportedDataTest() throws InterruptedException {
        clearTmp();
        ExportImport.ImportAll importAll = new ExportImport.ImportAll(Paths.get("src","test", "resources", "SomeExportedData.zip").toFile());
        importAll.start();
        importAll.join();

        assertEquals(49, IO.listDirectory(Config.getSAVE_PATH_MOVIE().toFile()).size());
        assertEquals(466, IO.listDirectory(Config.getSAVE_PATH_ACTOR().toFile()).size());
    }

    @Test
    void exportDataToXmlTest() throws InterruptedException, IOException {
        clearTmp();
        copyTestFiles();
        File file = Config.getSAVE_PATH().resolve("exportedData.xml").toFile();

        ExportImport.ExportAllToXml exportAllToXml = new ExportImport.ExportAllToXml(file);
        exportAllToXml.start();
        exportAllToXml.join();
        assertTrue(file.exists());

        FileReader fileIn = new FileReader(file);
        BufferedReader reader = new BufferedReader(fileIn);
        int lineCounter = 0;
        while(reader.readLine() != null) {
            lineCounter++;
        }
        reader.close();
        assertEquals(43, lineCounter);
    }

    @Test
    void importExportedXmlTest() throws InterruptedException {
        clearTmp();
        File file = Paths.get("src","test", "resources", "exportedData.xml").toFile();

        ExportImport.ImportDataFromXml importDataFromXml = new ExportImport.ImportDataFromXml(file);
        importDataFromXml.start();
        importDataFromXml.join();

        assertEquals(2, IO.listDirectory(Config.getSAVE_PATH_MOVIE().toFile()).size());
        assertEquals(3, IO.listDirectory(Config.getSAVE_PATH_ACTOR().toFile()).size());
    }
}