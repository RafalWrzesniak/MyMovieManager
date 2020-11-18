package FileOperations;

import Configuration.Config;
import Internet.Connection;
import MoviesAndActors.Actor;
import MoviesAndActors.ContentList;
import MoviesAndActors.ContentType;
import MoviesAndActors.Movie;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExportImport {

    public static class ExportAll extends Thread {

        private File outPutFile;

        public ExportAll(File outPutFile) {
            if(outPutFile != null) {
                if(!outPutFile.getPath().endsWith(".zip")) {
                    outPutFile = new File(outPutFile.getPath().concat(".zip"));
                }
            }
            this.outPutFile = outPutFile;
        }

        public ExportAll() {
        }

        @Override
        public void run() {
            setName("ExportAll");
            if(outPutFile == null) {
                outPutFile = new File(
                        System.getProperty("user.dir")
                        .concat("\\MyMovieManager_exported_data_")
                        .concat(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                        .replaceAll("\\..*$", "")
                        .replaceAll(":", "_")
                        .concat(".zip")));
            }
            try {
                zipFile(Config.getSAVE_PATH().toFile(), outPutFile);
                log.info("All data was successfully zipped end exported to \"{}\"", outPutFile);
            } catch (IOException e) {
                log.warn("Failed to ExportAll and ZIP file");
            }

        }

        private void zipFile(File fileToZip, File zipOut) throws IOException {
            ZipOutputStream zipOutStream = new ZipOutputStream(new FileOutputStream(zipOut.toString()));
            zipFile(fileToZip, "exported", zipOutStream);
            zipOutStream.close();
        }

        private void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
            if(fileToZip == null) return;
            List<File> files = IO.listDirectory(fileToZip);
            if(files.size() == 0) files.add(fileToZip);
            for(File file : files) {
                if(file.isDirectory()) {
                    zipFile(file, fileName.concat("\\").concat(file.getName()), zipOut);
                } else {
                    FileInputStream fis = new FileInputStream(file);
                    ZipEntry zipEntry = new ZipEntry(fileName.concat("\\").concat(file.getName()));
                    zipOut.putNextEntry(zipEntry);
                    byte[] bytes = new byte[1024];
                    int length;
                    while((length = fis.read(bytes)) >= 0) {
                        zipOut.write(bytes, 0, length);
                    }
                    zipOut.closeEntry();
                    fis.close();
                }
            }
        }



    }

    public static class ImportAll extends Thread {

        private final File inputFile;

        public ImportAll(File inputFile) {
            this.inputFile = inputFile;
        }

        @Override
        public void run() {
            setName("ImportAll");
            try {
                IO.deleteDirectoryRecursively(Config.getSAVE_PATH().toFile());
                unZipFile();
                log.info("Successfully imported data from \"{}\"", inputFile);
            } catch (IOException e) {
                log.warn("Failed to import file \"{}\" because of \"{}\"", inputFile, e.getMessage());
            }
        }

        private void unZipFile() throws IOException{
            File destDir = Config.getSAVE_PATH().toFile();
            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(inputFile));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                String zipName;
                if(zipEntry.getName().startsWith("exported")) {
                    zipName = zipEntry.getName().substring(zipEntry.getName().indexOf('\\'));
                } else throw new IOException("Wrong ZIP file, can't import data");
                String relPath = zipName.substring(0, zipName.lastIndexOf('\\'));
                new File(destDir, relPath).mkdirs();
                File newFile = new File(destDir, zipName);
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        }

    }


    public static class ExportAllToXml extends Thread {

        private File exportFile;

        public ExportAllToXml(File exportFile) {
            this.exportFile = exportFile;
        }

        public ExportAllToXml() {
        }

        @Override
        public void run() {
            setName("Export");
            if(exportFile != null) {
                exportAll(exportFile);
            } else {
                exportAll(new File(
                        System.getProperty("user.dir")
                        .concat("\\MyMovieManager_exported_data_")
                        .concat(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                        .replaceAll("\\..*$", "")
                        .replaceAll(":", "_")
                        .concat(".xml"))));
            }
        }


        private static void exportAll(File exportFile) {
            if(!exportFile.getPath().endsWith(".xml")) {
                exportFile = new File(exportFile.getPath().concat(".xml"));
            }
            Document doc = XMLOperator.createDoc();
            assert doc != null;
            Element rootElement = doc.createElement("exported");
            doc.appendChild(rootElement);

            Function<Path, Boolean> copyNodesFromPath = path -> {
                File xml;
                for(File dir : Objects.requireNonNull(IO.listDirectory(path.toFile()))) {
                    if(dir.isDirectory()) {
                        xml = IO.getXmlFileFromDir(dir);
                    } else if(dir.toString().endsWith(".xml")) {
                        xml = dir;
                    } else return false;
                    Element localRoot = XMLOperator.createRootElementFromXml(xml);
                    assert localRoot != null;
                    rootElement.appendChild(doc.createTextNode("\n"));
                    rootElement.appendChild(doc.adoptNode(localRoot.cloneNode(true)));
                }
                return true;
            };
            copyNodesFromPath.apply(Config.getSAVE_PATH_ACTOR());
            copyNodesFromPath.apply(Config.getSAVE_PATH_MOVIE());
            rootElement.appendChild(doc.createTextNode("\n"));
            XMLOperator.makeSimpleSave(doc, exportFile);
            log.info("All data successfully exported to file \"{}\"", exportFile);
        }

    }

    public static class ImportDataFromXml extends XMLOperator.ReadAllDataFromFiles {

        private final File importFile;

        public ImportDataFromXml(File importFile) {
            this.importFile = importFile;
        }

        @Override
        public void run() {
            setName("ImportData");
            log.info("Import data started from file \"{}\"", importFile.toString());
            convertImportFileToDirs(importFile);
            super.run();
            setName("ImportData");
            Thread downloadMovieImages = new Thread(() -> {
                for(Movie movie : getAllMovies().getList()) {
                    try {
                        Connection connection = new Connection(movie.getFilmweb());
                        Connection.downloadImage(connection.getImageUrl(true), movie.getImagePath());
                    } catch (IOException ignored) { }
                }
            });

            Thread downloadActorImages = new Thread(() -> {
                for(Actor actor : getAllActors().getList()) {
                    try {
                        Connection connection = new Connection(actor.getFilmweb());
                        Connection.downloadImage(connection.getImageUrl(false), actor.getImagePath());
                    } catch (IOException ignored) { }
                }
            });

            downloadActorImages.start();
            downloadMovieImages.start();

            try {
                downloadActorImages.join();
                downloadMovieImages.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            log.info("Import data finished from file \"{}\"", importFile.toString());
        }

        public static void convertImportFileToDirs(File importFile) {
            Element rootElement = XMLOperator.createRootElementFromXml(importFile);
            if(rootElement == null || !rootElement.getTagName().equals("exported")) {
                log.warn("Failed to import file \"{}\" - wrong XML", importFile);
                return;
            }
            Function<String, Boolean> saveAllContentsToFiles = content -> {
                NodeList singleContent = rootElement.getElementsByTagName(content);
                for(int i = 0; i < singleContent.getLength(); i++) {
                    Document doc = XMLOperator.createDoc();
                    assert doc != null;
                    doc.appendChild(doc.adoptNode(singleContent.item(i).cloneNode(true)));
                    Element localRoot = doc.getDocumentElement();

                    String reprName = "crashed";
                    File outDir = new File(Config.getSAVE_PATH().toUri());
                    if(content.equals(Actor.class.getSimpleName())) {
                        String contentId = localRoot.getElementsByTagName(ContentType.ID).item(0).getTextContent();
                        reprName = localRoot.getElementsByTagName(Actor.NAME).item(0).getTextContent().concat("_")
                                .concat(localRoot.getElementsByTagName(Actor.SURNAME).item(0).getTextContent());
                        outDir = new File(Config.getSAVE_PATH_ACTOR() + "\\actor" + contentId);

                    } else if(content.equals(Movie.class.getSimpleName())) {
                        String contentId = localRoot.getElementsByTagName(ContentType.ID).item(0).getTextContent();
                        reprName = localRoot.getElementsByTagName(Movie.TITLE).item(0).getTextContent();
                        outDir = new File(Config.getSAVE_PATH_MOVIE() + "\\movie" + contentId);

                    } else if(content.equals(ContentList.class.getSimpleName())) {
                        reprName = localRoot.getElementsByTagName("listName").item(0).getTextContent();
                        String type = localRoot.getElementsByTagName("type").item(0).getTextContent();
                        if(type.equals(Actor.class.getSimpleName())) outDir = Config.getSAVE_PATH_ACTOR().toFile();
                        else if(type.equals(Movie.class.getSimpleName())) outDir = Config.getSAVE_PATH_MOVIE().toFile();
                    }
                    if(!outDir.mkdirs()) {
                        log.warn("Could not create directory \"{}\"", outDir);
                    }
                    File targetFile = new File(outDir.toString().concat("\\").concat(reprName).concat(".xml"));
                    XMLOperator.makeSimpleSave(doc, targetFile);
                }
                return true;
            };

            if(IO.deleteDirectoryRecursively(Config.getSAVE_PATH().toFile())) {
                if(Config.getSAVE_PATH().toFile().mkdir()) Config.updateRelativePaths();
                if(saveAllContentsToFiles.apply(Movie.class.getSimpleName()) &&
                        saveAllContentsToFiles.apply(Actor.class.getSimpleName()) &&
                        saveAllContentsToFiles.apply(ContentList.class.getSimpleName())
                ) {
                    log.info("Successfully imported data from file \"{}\"", importFile);
                } else {
                    log.warn("Some data could not be read while import from \"{}\"", importFile);
                }
            }
        }

    }

}
