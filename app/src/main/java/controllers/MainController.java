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
import MyMovieManager.TaskManager;
import app.Main;
import controllers.actor.ActorDetail;
import controllers.actor.ActorPane;
import controllers.movie.MovieDetail;
import controllers.movie.MovieInfo;
import controllers.movie.MoviePane;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import utils.PaneNames;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
public class MainController implements Initializable {

//    == static fields ==
    public static final ObservableList<ContentList<Movie>> observableContentMovies = FXCollections.observableList(new ArrayList<>());
    public static final ObservableList<ContentList<Actor>> observableContentActors = FXCollections.observableList(new ArrayList<>());
    public static ContentList<Movie> recentlyWatched;
    public static ContentList<Movie> moviesToWatch;
    public static ContentList<Actor> allActors;
    public static ContentList<Movie> allMovies;
    public static List<String> actorStringList;
    public static Rectangle recForDialogs;

//    == fields ==
    private ResourceBundle resourceBundle;
    private SortFilter<?> sortAndFilter;
    private int lastTakenIndex;

    @FXML private HBox progressHBox;
    @FXML private StackPane filterPane;
    @FXML private BorderPane main_view;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField searchField;
    @FXML private FlowPane flowPaneContentList;
    @FXML private Label clearSearch, progressLabel;
    @FXML private MenuItem importZip, importXml, exportZip, exportXml;
    @FXML private ContextMenu movieListViewContextMenu, actorListViewContextMenu;
    @FXML private Button addContentMovies, addContentActors, addFolderWithMovies, addSingleMovie, settings;
    @FXML private MenuItem movieZheta, movieShortest, movieLongest, movieNewest, movieOldest, movieByRate, moviePopular,
            actorZheta ,actorYoungest, actorOldest;

    @FXML @Getter private MenuButton sort;
    @FXML @Getter private StackPane allCenter;
    @FXML @Getter private StackPane rightDetail;
    @FXML @Getter private MenuItem movieAlpha, actorAlpha;
    @FXML @Getter private ListView<ContentList<Movie>> movieListView;
    @FXML @Getter private ListView<ContentList<Actor>> actorListView;

    @Getter private Map<String, Comparator<Movie>> sortMovieMap;
    @Getter private Map<String, Comparator<Actor>> sortActorMap;



//    == init ==
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resourceBundle = resources;
        sortActorMap = Map.of(
                actorAlpha.getId(), Actor.COMP_ALPHA,
                actorZheta.getId(), Actor.COMP_ALPHA.reversed(),
                actorYoungest.getId(), Actor.COMP_AGE,
                actorOldest.getId(), Actor.COMP_AGE.reversed()
        );
        sortMovieMap = Map.of(
                movieOldest.getId(), Movie.COMP_PREMIERE,
                movieShortest.getId(), Movie.COMP_DURATION,
                movieAlpha.getId(), Movie.COMP_ALPHABETICAL,
                movieZheta.getId(), Movie.COMP_ALPHABETICAL.reversed(),
                movieByRate.getId(), Movie.COMP_RATE.reversed(),
                movieNewest.getId(), Movie.COMP_PREMIERE.reversed(),
                movieLongest.getId(), Movie.COMP_DURATION.reversed(),
                moviePopular.getId(), Movie.COMP_POPULARITY.reversed());


        AutoSave.NEW_OBJECTS.clear();
        Main.autoSave = new AutoSave();
        Main.autoSave.start();
        if (observableContentMovies.isEmpty()) {
            // load all data
            XMLOperator.ReadAllDataFromFiles readData = initReadData();
            initReadMainFolder();
            // add read data to observable lists
            addReadDataToObservableList(readData);
        }
        System.out.println(allMovies);
        System.out.println(allActors);
        System.out.println(moviesToWatch);


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

        // sorting
        sort.showingProperty().addListener(SortFilter.borderButtonListener(sort));
    }


//    == fxml handling ==

    @FXML
    public void addContentList(ActionEvent actionEvent) {
        log.info("Create new ContentList called from \"{}\"", actionEvent.getSource());
        FXMLLoader loader = Main.createLoader(PaneNames.SINGLE_DIALOG, resourceBundle);
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
            Platform.runLater(() -> selectItemListener(actorListView));
        }).start();
    }

    @FXML
    public void addMoviesToAppFromFile(ActionEvent actionEvent) {
        log.info("Adding movies from " + actionEvent.getSource());
        ((Button) actionEvent.getSource()).getStyleClass().add("buttonPressed");

        if(actionEvent.getSource().equals(addSingleMovie)) {
            File chosenFile = Main.chooseFileDialog(resourceBundle.getString("dialog.chooser.find_movie_file"), "movie");
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
        FXMLLoader loader = Main.createLoader(PaneNames.SINGLE_DIALOG, resourceBundle);
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
        File chosenFile = Main.chooseFileDialog(resourceBundle.getString("toolbar.import." + saveFormat), saveFormat);
        if(chosenFile == null) return;
        String warningInfo = MessageFormat.format(resourceBundle.getString("dialog.warning.import_warning"), chosenFile.getName());
        Dialog<ButtonType> dialog = createConfirmDialog(warningInfo);
        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {
            if(callingItem.equals(importZip)) {
                ExportImport.ImportAll importAllZip = new ExportImport.ImportAll(chosenFile);
                importAllZip.start();
                importAllZip.join();
                XMLOperator.ReadInitDataFromFiles readData = initReadData();
                addReadDataToObservableList(readData);
                movieListView.getSelectionModel().select(moviesToWatch);
                selectItemListener(actorListView);
            } else if(callingItem.equals(importXml)) {
                ExportImport.ImportDataFromXml importDataFromXml = new ExportImport.ImportDataFromXml(chosenFile);
                importDataFromXml.start();
                importDataFromXml.join();
                XMLOperator.ReadInitDataFromFiles readData = initReadData();
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
        FXMLLoader loader = Main.createLoader(PaneNames.SETTINGS, resourceBundle);
        Bounds boundsInScreen = settings.localToScreen(settings.getBoundsInLocal());
        Dialog<ButtonType> dialog;
        try {
            dialog = Main.createDialog(loader, main_view.getScene().getWindow());
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
            controller.processResult();
        }
        settings.getStyleClass().remove("buttonPressed");
    }



//    == context menu fxml ==
    @FXML
    public void renameContentList(ActionEvent actionEvent) {
        log.info("Rename ContentList called from \"{}\"", actionEvent.getSource());
        FXMLLoader loader = Main.createLoader(PaneNames.SINGLE_DIALOG, resourceBundle);
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
        FXMLLoader loader = Main.createLoader(PaneNames.SINGLE_DIALOG, resourceBundle);
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
            selectItemListener(actorListView);
            displayPanesByPopulating(true);
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
        readData.getAllMoviesLists().forEach(list -> {
            if(list.getListName().equals(ContentList.RECENTLY_WATCHED)) {
                recentlyWatched = list;
            }
        });
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


    @SneakyThrows
    private <T extends ContentType> boolean populateFlowPaneContentList(List<T> list, boolean clear) {
        if(list == null ) return false;
        List<T> targetList;
        int lastIndex;
        int numberOfPanes = 30;
        if(clear) {
            removeMovieInfo();
            flowPaneContentList.getChildren().clear();
            lastIndex = Math.min(list.size(), numberOfPanes);
            lastTakenIndex = 0;
            scrollPane.setVvalue(0);
            if(list.size() == 0) return false;

        } else {
            if(lastTakenIndex == list.size()) return false;
            lastIndex = Math.min(lastTakenIndex + numberOfPanes, list.size());
        }
        targetList = list.subList(lastTakenIndex, lastIndex);
        lastTakenIndex = lastIndex;

        for(int i = 0; i < targetList.size(); i++) {
            try {
                FXMLLoader loader = addContentToFlowPane(targetList.get(i));
                if(flowPaneContentList.getChildren().size() == 1 && i == 0) {
                    ((ContentPane) loader.getController()).selectItem();
                }
            } catch (IOException e) {
                log.warn("Failed to load pane for \"{}\"", targetList.get(i));
                e.printStackTrace();
            }
        }
        return true;
    }

    private <T extends ContentType> FXMLLoader addContentToFlowPane(T content) throws IOException {
        FXMLLoader loader;
        Parent parentPane;
        if(content instanceof Movie) {
            loader = Main.createLoader(PaneNames.MOVIE_PANE, resourceBundle);
            parentPane = loader.load();
            MoviePane moviePaneController = loader.getController();
            moviePaneController.setMovie((Movie) content);
            moviePaneController.setMainController(this);
        } else if(content instanceof Actor) {
            loader = Main.createLoader(PaneNames.ACTOR_PANE, resourceBundle);
            parentPane = openActorPane((Actor) content, loader);
        } else {
            throw new IOException("Not compatible object: " + content);
        }
        flowPaneContentList.getChildren().add(parentPane);
        return loader;
    }


    private Dialog<ButtonType> createDialog(ActionEvent actionEvent, FXMLLoader loader) throws IOException {
        Dialog<ButtonType> dialog = Main.createDialog(loader, main_view.getScene().getWindow());
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
        FXMLLoader loader = Main.createLoader(PaneNames.SINGLE_DIALOG, resourceBundle);
        Dialog<ButtonType> dialog = Main.createDialog(loader, main_view.getScene().getWindow());
        dialog.getDialogPane().setMaxWidth(600);
        dialog.setX(main_view.getScene().getWindow().getX() + main_view.getScene().getWidth()/2 - dialog.getDialogPane().getMaxWidth()/2);
        dialog.setY(main_view.getScene().getWindow().getY() + main_view.getScene().getHeight()/2);
        dialog.setOnShown(alertEvent -> dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(false));

        DialogController controller = loader.getController();
        controller.contentVBox.getChildren().remove(controller.textField);
        ((Label) controller.contentVBox.getChildren().get(0)).setMaxHeight(60);
        ((Label) controller.contentVBox.getChildren().get(0)).setMinHeight(60);
        controller.setTexts(dialogInfo, "");
        controller.showWarning(dialogInfo);
        return dialog;
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

    public ContentList<?> getSelectedList() {
        ContentList<?> contentList = movieListView.getSelectionModel().getSelectedItem();
        if(contentList != null) {
            return contentList;
        }
        return actorListView.getSelectionModel().getSelectedItem();
    }


    private void selectItemListener(ListView<?> listViewToDeselect) {
        if (getSelectedList() != null) {
            listViewToDeselect.getSelectionModel().clearSelection();
            searchField.setText("");
            createSortAndFilter(getSelectedList());
            movieListView.refresh();
            actorListView.refresh();
        }
    }

    public void makeCustomRefresh() {
        movieListView.refresh();
        actorListView.refresh();
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
                ListCell<ContentList<Movie>> cell = new ListCell<>() {
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
                cell.setOnMouseClicked(mouseEvent -> {
                    if(cell.isEmpty()) {
                        cell.setContextMenu(new ContextMenu());
                    } else {
                        cell.setContextMenu(movieListViewContextMenu);
                        selectItemListener(actorListView);
                    }
                });
                return cell;
            }
        };
    }
    private Callback<ListView<ContentList<Actor>>, ListCell<ContentList<Actor>>> displayTextInViewListActor() {
        return new Callback<>() {
            @Override
            public ListCell<ContentList<Actor>> call(ListView<ContentList<Actor>> object) {
                ListCell<ContentList<Actor>> cell = new ListCell<>() {
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
                cell.setOnMouseClicked(mouseEvent -> {
                    if(cell.isEmpty()) {
                        cell.setContextMenu(new ContextMenu());
                    } else {
                        cell.setContextMenu(actorListViewContextMenu);
                        selectItemListener(movieListView);
                    }
                });
                return cell;
            }
        };
    }


    private <T extends ContentType> void createSortAndFilter(ContentList<T> contentList) {
        filterPane.getChildren().clear();
        if(contentList == null || contentList.size() == 0) {
            flowPaneContentList.getChildren().clear();
            return;
        }
        SortFilter<T> filter;
        FXMLLoader filterLoader;
        if(contentList.get(0) instanceof Actor) {
            filterLoader = Main.createLoader(PaneNames.ACTOR_FILTER, resourceBundle);
        } else if(contentList.get(0) instanceof Movie) {
            filterLoader = Main.createLoader(PaneNames.MOVIE_FILTER, resourceBundle);
        } else return;

        try {
            filterLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            log.warn(Arrays.toString(e.getStackTrace()));
            log.warn(e.toString());
        }
        filter = filterLoader.getController();
        filter.setMainController(this);
        sortAndFilter = filter;
        filter.setContentList(contentList);
        filterPane.getChildren().add(filter.filterHBox);
    }

    public void openMovieInfo(Movie movie, ResourceBundle resourceBundle) {
        FXMLLoader loader;
        Parent parentPane;
        loader = Main.createLoader(PaneNames.MOVIE_INFO, resourceBundle);
        try {
            parentPane = loader.load();
        } catch (IOException e) {
            log.warn("Couldn't load MovieInfo pane for \"{}\"", movie);
            return;
        }
        MovieInfo movieInfo = loader.getController();
        movieInfo.setMovie(movie);
        movieInfo.setMainController(this);

        allCenter.getChildren().forEach(child -> child.setVisible(false));
        Platform.runLater(() -> allCenter.getChildren().add(parentPane));
        StackPane tempStackPane = new StackPane();
        tempStackPane.getChildren().addAll(rightDetail.getChildren());
        rightDetail.getChildren().clear();

        movieInfo.getReturnButton().setOnAction(actionEvent -> {
            removeMovieInfo();
            rightDetail.getChildren().clear();
            rightDetail.getChildren().addAll(tempStackPane.getChildren());
        });
    }

    public Parent openActorPane(Actor actor) {
        return openActorPane(actor, Main.createLoader(PaneNames.ACTOR_PANE, resourceBundle));
    }

    public Parent openActorPane(Actor actor, FXMLLoader loader) {
        Parent parentPane;
        try {
            parentPane = loader.load();
        } catch (IOException e) {
            log.warn("Couldn't load actor pane for \"{}\"", actor);
            return null;
        }
        ActorPane actorPaneController = loader.getController();
        actorPaneController.setActor(actor);
        actorPaneController.setMainController(this);
        return parentPane;
    }

    public void openActorDetail(Actor actor, ActorPane owner, boolean withReturnButton) {
        FXMLLoader loader = Main.createLoader(PaneNames.ACTOR_DETAIL, resourceBundle);
        Parent actorDetails;
        try {
            actorDetails = loader.load();
        } catch (IOException e) {
            log.warn("Failed to load fxml view in ActorDetail");
            return;
        }
        ActorDetail actorDetailController = loader.getController();
        actorDetailController.setOwner(owner);
        actorDetailController.setActor(actor);
        actorDetailController.setMainController(this);
        if(withReturnButton) {
            actorDetailController.vBox.getChildren().add(0, actorDetailController.getReturnButton());
            int lastIndex = rightDetail.getChildren().size() - 1;
            rightDetail.getChildren().get(lastIndex).setVisible(false);
        } else {
            rightDetail.getChildren().clear();
        }
        rightDetail.getChildren().add(actorDetails);
    }

    @SneakyThrows
    public void openMovieDetail(Movie movie, MoviePane owner, boolean withReturnButton) {
        FXMLLoader loader = Main.createLoader(PaneNames.MOVIE_DETAIL, resourceBundle);
        Parent movieDetails;
        try {
            movieDetails = loader.load();
        } catch (IOException e) {
            log.warn("Failed to load fxml view in MovieDetail");
            return;
        }
        XMLOperator.detailNeedToBeRead = true;
        XMLOperator.addActorsToMovieIfTheyDoNotExist(movie, allActors, allMovies);
        XMLOperator.detailNeedToBeRead = false;

        MovieDetail movieDetailController = loader.getController();
        movieDetailController.setOwner(owner);
        movieDetailController.setMovie(movie);
        movieDetailController.setMainController(this);
        if(withReturnButton) {
            movieDetailController.vBox.getChildren().add(0, movieDetailController.getReturnButton());
            int lastIndex = rightDetail.getChildren().size() - 1;
            rightDetail.getChildren().get(lastIndex).setVisible(false);
        } else {
            rightDetail.getChildren().clear();
        }
        rightDetail.getChildren().add(movieDetails);
    }



    private void removeMovieInfo() {
        ObservableList<Node> children = allCenter.getChildren();
        int i = 0;
        while (i < children.size()) {
            Node child = children.get(i);
            child.setVisible(true);
            if (child.getId() != null && child.getId().equals(MovieInfo.ID)) {
                allCenter.getChildren().remove(child);
            }
            i++;
        }
    }


    public void runTaskProgressBarChecking() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        TaskManager taskManager = TaskManager.getInstance();

        Platform.runLater(() -> executor.scheduleAtFixedRate(() -> {
            int initTaskNumber = taskManager.getTasksNumber();
            if(initTaskNumber > 0) {
                Platform.runLater(() -> {
                    progressHBox.setVisible(true);
                    progressLabel.setText(MessageFormat.format(resourceBundle.getString("main_view.task"), initTaskNumber, taskManager.getCurrentTask()));
                });
            } else if(progressHBox.isVisible()) {
                Platform.runLater(() -> progressHBox.setVisible(false));
            }
        }, 0, 500, TimeUnit.MILLISECONDS));

        Platform.runLater(() -> main_view.getScene().getWindow().setOnHiding(windowEvent -> executor.shutdown()));

    }




}

