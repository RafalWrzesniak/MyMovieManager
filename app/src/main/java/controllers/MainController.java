package controllers;

import Configuration.Config;
import FileOperations.AutoSave;
import FileOperations.ExportImport;
import FileOperations.IO;
import FileOperations.XMLOperator;
import MoviesAndActors.Actor;
import MoviesAndActors.ContentList;
import MoviesAndActors.ContentType;
import MoviesAndActors.Movie;
import MyMovieManager.DownloadAndProcessMovies;
import MyMovieManager.MovieMainFolder;
import app.Main;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;


@Slf4j
public class MainController implements Initializable {

//  == constants ==
    public static final String ACTOR_PANE = "actor-pane", ACTOR_DETAIL = "actor-detail", MOVIE_PANE = "movie-pane",
        MOVIE_DETAIL = "movie-detail", SETTINGS = "settings", SINGLE_DIALOG = "single-dialog";

//    == fields ==
    private static final ObservableList<ContentList<Movie>> observableContentMovies = FXCollections.observableList(new ArrayList<>());
    private static final ObservableList<ContentList<Actor>> observableContentActors = FXCollections.observableList(new ArrayList<>());
//    private ContentList<? extends ContentType<?>> currentlyDisplayedList;
    private static ContentList<Actor> allActors;
    private static ContentList<Movie> allMovies;
    private static ContentList<Movie> moviesToWatch;


    public ContentList<? extends ContentType<?>> currentlyDisplayedList;
    public Label clearSearch;
    private int lastTakenIndex;
    public boolean reOpenSettings;
    private ResourceBundle resourceBundle;
    @FXML public TextField searchField;
    @FXML private BorderPane main_view;
    @FXML private ScrollPane scrollPane;
    @FXML private FlowPane flowPaneContentList;
    @FXML StackPane rightDetail;
    @FXML private Button addContentMovies, addContentActors, addFolderWithMovies, addSingleMovie;
    @FXML public Button settings;
    @FXML public ContextMenu movieListViewContextMenu, actorListViewContextMenu;
    @FXML private MenuItem importZip, importXml, exportZip, exportXml;

    @FXML private ListView<ContentList<Movie>> movieListView;
    @FXML private ListView<ContentList<Actor>> actorListView;


//    == init ==
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resourceBundle = resources;

        AutoSave.NEW_OBJECTS.clear();
        Main.autoSave = new AutoSave();
        Main.autoSave.start();
        if(observableContentMovies.isEmpty()) {
            // load all data
            XMLOperator.ReadAllDataFromFiles readData = initReadData();
            initReadMainFolder();
            // add read data to observable lists
            addReadDataToObservableList(readData);
        }

        System.out.println(allMovies);
        System.out.println(allActors);
        System.out.println(moviesToWatch);

        // control speed of scroll pane
        scrollPane.getContent().setOnScroll(scrollEvent -> {
            final double SPEED = 0.005;
            double deltaY = scrollEvent.getDeltaY() * SPEED;
            scrollPane.setVvalue(scrollPane.getVvalue() - deltaY);
        });

        // flow pane click listener
        flowPaneContentList.addEventFilter(MouseEvent.MOUSE_CLICKED, this::handleClickingOnMovies);

        // Left menu list view
        // set items
        movieListView.setItems(observableContentMovies);
        actorListView.setItems(observableContentActors);
        // set cell factory
        movieListView.setCellFactory(displayTextInViewListMovie());
        actorListView.setCellFactory(displayTextInViewListActor());
        // handling selection items in list view
        movieListView.setOnMouseClicked(event -> selectItemListener(actorListView));
        actorListView.setOnMouseClicked(event -> selectItemListener(movieListView));
        movieListView.getSelectionModel().select(moviesToWatch);
        populateFlowPaneContentList(moviesToWatch);
        // set list view context menu user data
        movieListViewContextMenu.setUserData(movieListView);
        actorListViewContextMenu.setUserData(actorListView);

//        currentlyDisplayedList.sort(Movie.COMP_DURATION);
//        populateFlowPaneContentList(moviesToWatch);
    }



//    == fxml handling ==

    @FXML
    public void addContentList(ActionEvent actionEvent) {
        log.info("Create new ContentList called from \"{}\"", actionEvent.getSource());
        FXMLLoader loader = createLoader(SINGLE_DIALOG, resourceBundle);
        Dialog<ButtonType> dialog;
        try {
            dialog = createDialog(actionEvent, loader);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        DialogController controller = loader.getController();
        dialog.setOnShown(dialogEvent -> controller.setTextFieldListener(dialog.getDialogPane().lookupButton(ButtonType.OK)));
        String labelText = resourceBundle.getString("dialog.label.create_new");
        String promptText = resourceBundle.getString("dialog.prompt.new_list");
        String warningInfo = resourceBundle.getString("dialog.warning.nameTooLong");
        controller.setTexts(labelText, promptText, warningInfo);
        Optional<ButtonType> result = dialog.showAndWait();

        if(result.isPresent() && result.get() == ButtonType.OK) {
            String newName = controller.textField.getText();
            if(actionEvent.getSource().equals(addContentMovies)) {
                observableContentMovies.add(new ContentList<>(newName));
            } else if(actionEvent.getSource().equals(addContentActors)) {
                observableContentActors.add(new ContentList<>(newName));
            }
        }
    }

    @FXML
    public void setMainFolder(ActionEvent actionEvent) {
        ((Button) actionEvent.getSource()).getStyleClass().add("buttonPressed");
        File chosenDir = chooseDirectoryDialog(resourceBundle.getString("dialog.chooser.set_main"));
        if(chosenDir != null) {
            Config.setMAIN_MOVIE_FOLDER(chosenDir.toPath());
        }
        ((Button) actionEvent.getSource()).getStyleClass().remove("buttonPressed");

    }

    @FXML
    public void refreshMainFolder() {
        log.info("Refresh called");
        MovieMainFolder movieMainFolder = new MovieMainFolder(moviesToWatch, allMovies, allActors);
        movieMainFolder.start();
        new Thread(() -> {
            try {
                movieMainFolder.join();
            } catch (InterruptedException e) {
                log.warn("Unexpected interrupted exception while downloading new movies \"{}\"", e.getMessage());
            }
            Platform.runLater(() -> populateFlowPaneContentList(moviesToWatch));
        }).start();
    }

    @FXML
    public void addMoviesToAppFromFile(ActionEvent actionEvent) {
        log.info("Adding movies from " + actionEvent.getSource());
        ((Button) actionEvent.getSource()).getStyleClass().add("buttonPressed");

        if(actionEvent.getSource().equals(addSingleMovie)) {
            File chosenFile = chooseFileDialog(resourceBundle.getString("dialog.chooser.find_movie_file"), "movie");
            ((Button) actionEvent.getSource()).getStyleClass().remove("buttonPressed");
            if(chosenFile != null) {
                DownloadAndProcessMovies.handleMovieFromFile(chosenFile, allMovies, allActors);
            }
        }
        else if(actionEvent.getSource().equals(addFolderWithMovies)) {
            File chosenDir = chooseDirectoryDialog(resourceBundle.getString("dialog.chooser.add_movies_from_dir"));
            ((Button) actionEvent.getSource()).getStyleClass().remove("buttonPressed");
            if(chosenDir != null) {
                List<File> allFoundDirectories = IO.listDirectoryRecursively(chosenDir);
                DownloadAndProcessMovies dap = new DownloadAndProcessMovies(allFoundDirectories, allMovies, allActors);
                dap.start();
            }

        }
        makeCustomRefresh();
    }

    @FXML
    public void addMovieToAppFromWeb(ActionEvent actionEvent) {
        log.info("Adding movies from " + actionEvent.getSource());
        FXMLLoader loader = createLoader(SINGLE_DIALOG, resourceBundle);
        Dialog<ButtonType> dialog;
        try {
            dialog = createDialog(actionEvent, loader);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        DialogController controller = loader.getController();
        dialog.setOnShown(dialogEvent -> controller.setUrlTextListener(dialog.getDialogPane().lookupButton(ButtonType.OK)));
        String labelText = resourceBundle.getString("dialog.label.create_from_web");
        String promptText = resourceBundle.getString("dialog.prompt.filmweb");
        String warningInfo = resourceBundle.getString("dialog.warning.wrong_url");
        controller.setTexts(labelText, promptText, warningInfo);
        controller.textField.setPrefWidth(400);

        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {
            URL providedUrl;
            try {
                providedUrl = new URL(controller.textField.getText());
            } catch (MalformedURLException e) {
                log.warn("Failed to create URL object from provided text");
                return;
            }
            DownloadAndProcessMovies.handleMovieFromUrl(providedUrl, allMovies, allActors);
            makeCustomRefresh();
        }
    }


    @SneakyThrows
    @FXML
    public void importData(ActionEvent actionEvent) {
        MenuItem callingItem = (MenuItem) actionEvent.getSource();
        log.info("Import data called from " + callingItem);
        String saveFormat = callingItem.getId().toLowerCase().substring(6); // importXml || importZip
        File chosenFile = chooseFileDialog(resourceBundle.getString("toolbar.import." + saveFormat), saveFormat);
        if(chosenFile == null) return;
        String warningInfo = MessageFormat.format(resourceBundle.getString("dialog.warning.import_warning"), chosenFile.getName());
        Dialog<ButtonType> dialog = createConfirmDialog(warningInfo);
        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {
            if(callingItem.equals(importZip)) {
                ExportImport.ImportAll importAllZip = new ExportImport.ImportAll(chosenFile);
                importAllZip.start();
                importAllZip.join();
                XMLOperator.ReadAllDataFromFiles readData = initReadData();
                initReadMainFolder();
                addReadDataToObservableList(readData);
                movieListView.getSelectionModel().select(moviesToWatch);
                populateFlowPaneContentList(moviesToWatch);
            } else if(callingItem.equals(importXml)) {
                ExportImport.ImportDataFromXml importDataFromXml = new ExportImport.ImportDataFromXml(chosenFile);
                importDataFromXml.start();
                importDataFromXml.join();
                XMLOperator.ReadAllDataFromFiles readData = initReadData();
                initReadMainFolder();
                addReadDataToObservableList(readData);
                movieListView.getSelectionModel().select(moviesToWatch);
            }
        }
    }

    @FXML
    public void exportData(ActionEvent actionEvent) {
        MenuItem callingItem = (MenuItem) actionEvent.getSource();
        log.info("Export data called from " + callingItem);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(resourceBundle.getString("dialog.import_export_title"));
        fileChooser.setInitialFileName("MyMovieManager_exported_data_"
                .concat(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                .replaceAll("\\..*$", "")
                .replaceAll(":", "_")));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        if(callingItem.equals(exportZip)) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP archive files", "*.zip"));
            File destFile = fileChooser.showSaveDialog(main_view.getScene().getWindow());
            ExportImport.ExportAll exportAll = new ExportImport.ExportAll(destFile);
            exportAll.start();
        } else if(callingItem.equals(exportXml)) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML file", "*.xml"));
            File destFile = fileChooser.showSaveDialog(main_view.getScene().getWindow());
            ExportImport.ExportAllToXml exportAllToXml = new ExportImport.ExportAllToXml(destFile);
            exportAllToXml.start();
        }
    }

    @FXML
    public void openSettings() {
        log.info("Opening settings dialog");
        settings.getStyleClass().add("buttonPressed");
        FXMLLoader loader = createLoader(SETTINGS, resourceBundle);
        Bounds boundsInScreen = settings.localToScreen(settings.getBoundsInLocal());
        Dialog<ButtonType> dialog;
        try {
            dialog = createDialog(loader);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        dialog.setX(boundsInScreen.getMaxX() - 3);
        dialog.setY(boundsInScreen.getMaxY() - 3);
        Settings controller = loader.getController();
        controller.setMainController(this);
        controller.setDialog(dialog);

        Optional<ButtonType> result = dialog.showAndWait();

        if(result.isPresent() && result.get() == ButtonType.OK) {
            if (!controller.mainDir.getText().isEmpty()) {
                Config.setMAIN_MOVIE_FOLDER(Paths.get(controller.mainDir.getText()));
            }
            if (!controller.watched.getText().isEmpty()) {
                Config.setRECENTLY_WATCHED(Paths.get(controller.watched.getText()));
            }
            if (!controller.save.getText().isEmpty()) {
                Config.setSAVE_PATH(new File(controller.save.getText()), true);
            }
        }
        settings.getStyleClass().remove("buttonPressed");
    }



//    == context menu fxml ==
    @FXML
    public void renameContentList(ActionEvent actionEvent) {
        log.info("Rename ContentList called from \"{}\"", actionEvent.getSource());
        FXMLLoader loader = createLoader(SINGLE_DIALOG, resourceBundle);
        Dialog<ButtonType> dialog;
        try {
            dialog = createDialog(actionEvent, loader);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        DialogController controller = loader.getController();
        dialog.setOnShown(dialogEvent -> controller.setTextFieldListener(dialog.getDialogPane().lookupButton(ButtonType.OK)));
        String labelText = resourceBundle.getString("dialog.label.rename_list");
        String promptText = resourceBundle.getString("dialog.prompt.new_list");
        String warningInfo = resourceBundle.getString("dialog.warning.nameTooLong");
        controller.setTexts(labelText, promptText, warningInfo);
        Optional<ButtonType> result = dialog.showAndWait();

        if(result.isPresent() && result.get() == ButtonType.OK) {
            String newName = controller.textField.getText();
            try {
                getSelectedList().setDisplayName(newName);
            } catch (Config.ArgumentIssue argumentIssue) {
                log.warn("Fatal error - could not change ContentList name");
            } finally {
                makeCustomRefresh();
            }
        }
    }

    @FXML
    public void copyContentList(ActionEvent actionEvent) {
        log.info("Copy new ContentList called from \"{}\"", actionEvent.getSource());
        FXMLLoader loader = createLoader(SINGLE_DIALOG, resourceBundle);
        Dialog<ButtonType> dialog;
        try {
            dialog = createDialog(actionEvent, loader);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        DialogController controller = loader.getController();
        dialog.setOnShown(dialogEvent -> controller.setTextFieldListener(dialog.getDialogPane().lookupButton(ButtonType.OK)));
        String labelText = resourceBundle.getString("dialog.label.copy_list");
        String promptText = resourceBundle.getString("dialog.prompt.new_list");
        String warningInfo = resourceBundle.getString("dialog.warning.nameTooLong");
        controller.setTexts(labelText, promptText, warningInfo);
        Optional<ButtonType> result = dialog.showAndWait();

        if(result.isPresent() && result.get() == ButtonType.OK) {
            String newName = controller.textField.getText();
            try {
                if ((((MenuItem) actionEvent.getSource()).getParentPopup()).getUserData().equals(movieListView)) {
                    ContentList<Movie> contentList = new ContentList<>(newName);
                    observableContentMovies.add(contentList);
                    contentList.addAll(movieListView.getSelectionModel().getSelectedItem().getList());
                }
            } catch (NullPointerException ignored) {
                try {
                    if ((((MenuItem) actionEvent.getSource()).getParentPopup()).getUserData().equals(actorListView)) {
                        ContentList<Actor> contentList = new ContentList<>(newName);
                        observableContentActors.add(contentList);
                        contentList.addAll(actorListView.getSelectionModel().getSelectedItem().getList());
                    }
                } catch (NullPointerException ignored2) {
                }
            }
        }
    }

    @FXML
    public void removeContentList(ActionEvent actionEvent) {
        log.info("Remove ContentList called from \"{}\"", actionEvent.getSource());
        String pattern;
        boolean removeList;
        if(getSelectedList().equals(moviesToWatch) || getSelectedList().equals(allMovies) || getSelectedList().equals(allActors)) {
            pattern = resourceBundle.getString("dialog.label.cannot_remove");
            removeList = false;
        } else {
            pattern = resourceBundle.getString("dialog.label.remove_list");
            removeList = true;
        }
        String labelText = MessageFormat.format(pattern, getSelectedList().getDisplayName());
        Dialog<ButtonType> alert;
        try {
            alert = createConfirmDialog(labelText);
        } catch (IOException e) {
            return;
        }

        alert.showAndWait();
        if (alert.getResult() == ButtonType.OK && removeList) {
            log.warn("Removing ContentList \"{}\"", getSelectedList().getDisplayName());
            XMLOperator.removeContentList(getSelectedList());
            observableContentMovies.remove(getSelectedList());
            observableContentActors.remove(getSelectedList());
        }
    }

//    == methods ==
    @SneakyThrows
    private XMLOperator.ReadAllDataFromFiles initReadData() {
        ContentList.clearNames();
        XMLOperator.ReadAllDataFromFiles readData = new XMLOperator.ReadAllDataFromFiles();
        readData.start();
        readData.join();
        allActors = readData.getAllActors();
        allMovies = readData.getAllMovies();
        return readData;
    }

    @SneakyThrows
    private void initReadMainFolder() {
        MovieMainFolder movieMainFolder = new MovieMainFolder(allMovies, allActors);
        movieMainFolder.start();
        movieMainFolder.join();
        moviesToWatch = movieMainFolder.getMoviesToWatch();
        moviesToWatch.sort();
    }

    private void addReadDataToObservableList(XMLOperator.ReadAllDataFromFiles readData) {
        observableContentMovies.clear();
        observableContentActors.clear();
        observableContentMovies.add(allMovies);
        observableContentMovies.add(moviesToWatch);
        List<ContentList<Movie>> noAllMoviesList = readData.getAllMoviesLists();
        noAllMoviesList.remove(allMovies);
        observableContentMovies.addAll(FXCollections.observableArrayList(noAllMoviesList));
        observableContentActors.addAll(FXCollections.observableArrayList(readData.getAllActorsLists()));
    }

    private <T extends ContentType<T>> void populateFlowPaneContentList(ContentList<T> contentList) {
        if(contentList == null) return;

        flowPaneContentList.getChildren().clear();
        for(int i =0; i < contentList.size(); i++) {
            FXMLLoader loader;
            Parent parentPane;

            if(contentList.get(0) instanceof Movie) {
                loader = createLoader(MOVIE_PANE, resourceBundle);
            } else if(contentList.get(0) instanceof Actor) {
                loader = createLoader(ACTOR_PANE, resourceBundle);
            } else {
                return;
            }

            try {
                parentPane = loader.load();
            } catch (IOException e) {
                log.warn("Failed to load fxml view in ParentPane while populating");
                e.printStackTrace();
                continue;
            }

            if(contentList.get(0) instanceof Movie) {
                MoviePane moviePaneController = loader.getController();
                moviePaneController.setMovie((Movie) contentList.get(i));
                moviePaneController.setMainController(this);
            } else if(contentList.get(0) instanceof Actor) {
                ActorPane actorPaneController = loader.getController();
                actorPaneController.setActor((Actor) contentList.get(i));
                actorPaneController.setMainController(this);
            }
            Platform.runLater(() -> flowPaneContentList.getChildren().add(parentPane));
        }
//        currentlyDisplayedList = contentList;
    }


    private void handleClickingOnMovies(MouseEvent event) {
        Stage stage = (Stage) rightDetail.getParent().getScene().getWindow();

        if(rightDetail.getChildren().size() == 0 && main_view.getWidth() <= main_view.getPrefWidth()) {
            stage.setWidth(stage.getWidth() + MovieDetail.getPREF_WIDTH());
        }

        if(event.getPickResult().getIntersectedNode().toString().equals(flowPaneContentList.toString())) {
            rightDetail.getChildren().clear();
            stage.setWidth(stage.getWidth() - MovieDetail.getPREF_WIDTH());
            ContentPane.clearBackSthWasClicked.setValue(true);
            ContentPane.clearBackSthWasClicked.setValue(false);
        }
    }

    public FXMLLoader createLoader(String viewName, ResourceBundle resourceBundle) {
        final String PREFIX = "/fxml-views/";
        final String SUFFIX = ".fxml";
        return new FXMLLoader(getClass().getResource(PREFIX + viewName + SUFFIX), resourceBundle);
    }

    private Dialog<ButtonType> createDialog(FXMLLoader loader) throws IOException {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(main_view.getScene().getWindow());
        dialog.getDialogPane().setContent(loader.load());
        dialog.getDialogPane().getStylesheets().add(String.valueOf(getClass().getResource("/css-styles/dialog.css")));
        dialog.getDialogPane().getStyleClass().add("dialogBorder");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.initStyle(StageStyle.TRANSPARENT);
        return dialog;
    }


    private Dialog<ButtonType> createDialog(ActionEvent actionEvent, FXMLLoader loader) throws IOException {
        Dialog<ButtonType> dialog = createDialog(loader);
        double showingX, showingY;
        try {
            Button callingButton = (Button) actionEvent.getSource();
            Bounds boundsInScreen = callingButton.localToScreen(callingButton.getBoundsInLocal());
            showingX = boundsInScreen.getMaxX() - 3;
            showingY = boundsInScreen.getMaxY() - 3;
            callingButton.getStyleClass().add("buttonPressed");
            dialog.setOnHiding(dialogEvent -> callingButton.getStyleClass().remove("buttonPressed"));
        } catch(ClassCastException ignored) {
            try {
                @SuppressWarnings("unchecked")
                ListView<ContentList<?>> listView = (ListView<ContentList<?>>) (((MenuItem) actionEvent.getSource()).getParentPopup()).getUserData();
                int numberOnList = listView.getSelectionModel().getSelectedIndex();
                Object[] cells = listView.lookupAll(".cell").toArray();
                @SuppressWarnings("unchecked")
                Cell<ContentList<?>> cell = (Cell<ContentList<?>>) cells[numberOnList];

                Bounds boundsInScreen = cell.localToScreen(cell.getBoundsInLocal());
                showingX = boundsInScreen.getMaxX() - 5;
                showingY = boundsInScreen.getMinY() + cell.getHeight() - 7;
            } catch (ClassCastException ignored2) {
                log.warn("Unknown calling item \"{}\" = dialog created on the middle", actionEvent.getSource());
                return dialog;
            }
        }
        dialog.setX(showingX);
        dialog.setY(showingY);
        return dialog;
    }

    private Dialog<ButtonType> createConfirmDialog(String dialogInfo) throws IOException {
        FXMLLoader loader = createLoader(SINGLE_DIALOG, resourceBundle);
        Dialog<ButtonType> dialog = createDialog(loader);
        dialog.getDialogPane().setMaxWidth(600);
        dialog.setX(main_view.getScene().getWindow().getX() + main_view.getScene().getWidth()/2 - dialog.getDialogPane().getMaxWidth()/2);
        dialog.setY(main_view.getScene().getWindow().getY() + main_view.getScene().getHeight()/2);
        dialog.setOnShown(alertEvent -> dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(false));

        DialogController controller = loader.load();
        controller.contentVBox.getChildren().remove(controller.textField);
        ((Label) controller.contentVBox.getChildren().get(0)).setMaxHeight(60);
        ((Label) controller.contentVBox.getChildren().get(0)).setMinHeight(60);
        controller.setTexts(dialogInfo, "");
        controller.showWarning(dialogInfo);
        return dialog;
    }

    private File chooseFileDialog(String title, String fileType) {
        log.debug("Choose file dialog is now open");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        switch (fileType) {
            case "movie":
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Movie files", "*.avi", "*.mkv", "*.mp4", "*.m4v"));
                break;
            case "zip":
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP archive files", "*.zip"));
                break;
            case "xml":
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML file", "*.xml"));
                break;
        }
        fileChooser.setInitialDirectory(Config.getMAIN_MOVIE_FOLDER().toFile());
        File chosenFile =  fileChooser.showOpenDialog(main_view.getScene().getWindow());
        log.debug("Chosen file is \"{}\"", chosenFile);
        return chosenFile;
    }

    public File chooseDirectoryDialog(String title) {
        log.debug("Choose directory dialog is now open");
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(title);
        directoryChooser.setInitialDirectory(Config.getMAIN_MOVIE_FOLDER().toFile());

        File chosenDir = directoryChooser.showDialog(main_view.getScene().getWindow());
        log.debug("Chosen directory is \"{}\"", chosenDir);
        return chosenDir;
    }

    private ContentList<?> getSelectedList() {
        ContentList<?> contentList = movieListView.getSelectionModel().getSelectedItem();
        if(contentList != null) {
            return contentList;
        }
        return actorListView.getSelectionModel().getSelectedItem();
    }


    private void selectItemListener(ListView<?> listViewToDeselect) {
        if (getSelectedList() != null) {
            listViewToDeselect.getSelectionModel().clearSelection();
            populateFlowPaneContentList(getSelectedList());
        }
    }

    public void makeCustomRefresh() {
        int i = movieListView.getSelectionModel().getSelectedIndex();
        int j = actorListView.getSelectionModel().getSelectedIndex();
        if(i > 0) {
            movieListView.getSelectionModel().selectPrevious();
            movieListView.getSelectionModel().selectNext();
        } else if(i == 0) {
            movieListView.getSelectionModel().selectNext();
            movieListView.getSelectionModel().selectPrevious();
        }
        if(j > 0) {
            actorListView.getSelectionModel().selectPrevious();
            actorListView.getSelectionModel().selectNext();
        } else if(j == 0){
            actorListView.getSelectionModel().selectNext();
            actorListView.getSelectionModel().selectPrevious();
        }
    }

    private Callback<ListView<ContentList<Movie>>, ListCell<ContentList<Movie>>> displayTextInViewListMovie() {
        return new Callback<>() {
            @Override
            public ListCell<ContentList<Movie>> call(ListView<ContentList<Movie>> object) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(ContentList<Movie> contentList, boolean empty) {
                        super.updateItem(contentList, empty);
                        if (empty || contentList == null) {
                            setText(null);
                        } else {
                            setMaxWidth(object.getWidth()-10);
                            setPrefWidth(object.getWidth()-10);
                            setWrapText(true);
                            setText(String.format("%s [%d]", contentList.getDisplayName(), contentList.size()));
                        }
                    }
                };
            }
        };
    }
    private Callback<ListView<ContentList<Actor>>, ListCell<ContentList<Actor>>> displayTextInViewListActor() {
        return new Callback<>() {
            @Override
            public ListCell<ContentList<Actor>> call(ListView<ContentList<Actor>> object) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(ContentList<Actor> contentList, boolean empty) {
                        super.updateItem(contentList, empty);
                        if (empty || contentList == null) {
                            setText(null);
                        } else {
                            setMaxWidth(object.getWidth()-10);
                            setPrefWidth(object.getWidth()-10);
                            setWrapText(true);
                            setText(String.format("%s [%d]", contentList.getDisplayName(), contentList.size()));
                        }
                    }
                };
            }
        };
    }


}

