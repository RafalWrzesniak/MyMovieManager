package app;

import Configuration.Config;
import FileOperations.AutoSave;
import FileOperations.IO;
import Internet.WebOperations;
import MoviesAndActors.Actor;
import MoviesAndActors.ContentType;
import MoviesAndActors.Movie;
import controllers.DialogController;
import controllers.MainController;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Window;
import javafx.stage.*;
import lombok.extern.slf4j.Slf4j;
import utils.PaneNames;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

@Slf4j
public class Main extends Application {

    public static AutoSave autoSave;
    public static Stage primaryStage;
    private static JFrame loadingWindow;
    public static final Locale localePl = new Locale("pl", "PL");
    public static final Locale localeEn = new Locale("en", "EN");
    public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("MyMovieManager");
        loadingWindow = new LoadingWindow().getInitWindow();
        Thread.currentThread().setName("JavaFX");
        Main.primaryStage = primaryStage;
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/favicon.png")));
        loadView(localePl, primaryStage);
    }

    @Override
    public void init() {
        log.info("Application MyMovieManager has started");
    }

    @Override
    public void stop() throws Exception {
        autoSave.interrupt();
        autoSave.join();
        log.info("Application is being terminated now...");
    }

//  == static methods ==
    public static void loadView(Locale locale, Stage primaryStage) {
        FXMLLoader loader = createLoader(PaneNames.MAIN_VIEW, ResourceBundle.getBundle("bundles.messages", locale));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        Scene mainScene = new Scene(root);
        primaryStage.setScene(mainScene);
        primaryStage.setOnShown(event -> loadingWindow.dispose());
        primaryStage.show();
    }

    public static FXMLLoader createLoader(String viewName, ResourceBundle resourceBundle) {
        final String PREFIX = "/fxml-views/";
        final String SUFFIX = ".fxml";
        return new FXMLLoader(MainController.class.getResource(PREFIX + viewName + SUFFIX), resourceBundle);
    }

    public static Dialog<ButtonType> createDialog(FXMLLoader loader, Window owner) throws IOException {
        if(loader == null || owner == null) throw new IOException("Args cannot be null!");
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.getDialogPane().setContent(loader.load());
        dialog.getDialogPane().getStylesheets().add(String.valueOf(MainController.class.getResource("/css-styles/dialog.css")));
        dialog.getDialogPane().getStyleClass().add("dialogBorder");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.initStyle(StageStyle.TRANSPARENT);
        MainController.recForDialogs.visibleProperty().unbind();
        MainController.recForDialogs.visibleProperty().bind(dialog.showingProperty());
        return dialog;
    }

    public static <T extends ContentType> Dialog<ButtonType> createChangePhotoDialog(FXMLLoader loader, Window window, ResourceBundle resourceBundle, T content) {
        Dialog<ButtonType> dialog;
        try {
            dialog = Main.createDialog(loader, window);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        String changeFor;
        if(content instanceof Actor) {
            changeFor = ((Actor) content).getNameAndSurname();
        } else if(content instanceof Movie) {
            changeFor = ((Movie) content).getTitle();
        } else {
            return null;
        }
        ImageView currentImage = new ImageView(new Image(content.getImagePath().toUri().toString()));
        currentImage.setFitHeight(166);
        currentImage.setPreserveRatio(true);
        dialog.getDialogPane().setGraphic(currentImage);
        Label infoLabel = new Label(MessageFormat.format(resourceBundle.getString("context_menu.change_photo_for"), changeFor));
        infoLabel.getStyleClass().add("white_text");
        TextField addFromUrlField = new TextField();

        DialogController dialogController = loader.getController();
        dialogController.setTexts(resourceBundle.getString("context_menu.or"), resourceBundle.getString("context_menu.add_image_from_file"));
        addFromUrlField.setPromptText(resourceBundle.getString("context_menu.add_image_from_web"));
        dialogController.contentVBox.getChildren().add(0, infoLabel);
        dialogController.contentVBox.getChildren().add(1, addFromUrlField);

        dialog.getDialogPane().lookupButton(ButtonType.OK).disableProperty().bind(
                Bindings.and(addFromUrlField.textProperty().isEmpty(),
                        dialogController.textField.textProperty().isEmpty()));

        addFromUrlField.textProperty().addListener((observableValue, s, t1) -> {
            if(t1 != null && !t1.isEmpty()) {
                dialogController.textField.setText("");
                if(t1.length() > 300) {
                    addFromUrlField.setText(s);
                }
            }
        });
        dialogController.textField.textProperty().addListener((observableValue, s, t1) -> {
            if(t1 != null && !t1.isEmpty()) {
                addFromUrlField.setText("");
            }
        });

        dialogController.textField.setOnMouseClicked(event -> {
            File chosenFile = Main.chooseFileDialog(resourceBundle.getString("dialog.chooser.choose_image"), "image");
            if(chosenFile != null) {
                dialogController.textField.setText(chosenFile.toString());
            }
        });
        return dialog;
    }

    public static <T extends ContentType> void processChangePhotoResult(DialogController dialogController, T content) {
        Path downloadedImagePath;
        if(content instanceof Actor) {
            Actor actor = (Actor) content;
            downloadedImagePath = Paths.get(IO.createContentDirectory(actor).toString(), actor.getReprName().concat(".jpg"));
        } else if(content instanceof Movie) {
            Movie movie = (Movie) content;
            downloadedImagePath = Paths.get(IO.createContentDirectory(movie).toString(), movie.getReprName().concat(".jpg"));
        } else return;

        TextField addFromUrlField = (TextField) dialogController.contentVBox.getChildren().get(1);
        if(addFromUrlField.getText() != null && !addFromUrlField.getText().isEmpty()) {
            URL imageUrl;
            try {
                imageUrl = new URL(addFromUrlField.getText());
            } catch (MalformedURLException e) {
                log.warn("\"{}\" is not a correct image url!", addFromUrlField.getText());
                return;
            }
            if (WebOperations.downloadImage(imageUrl, downloadedImagePath)) {
                content.setImagePath(downloadedImagePath);
            }
        } else if(dialogController.textField.getText() != null  && !dialogController.textField.getText().isEmpty()) {
            File providedFile = new File(dialogController.textField.getText());
            if(!providedFile.exists()) {
                log.warn("No such file \"{}\"", dialogController.textField.getText());
                return;
            }
            try {
                java.nio.file.Files.copy(providedFile.toPath(), downloadedImagePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                log.warn("Failed to copy image \"{}\"", providedFile);
                return;
            }
            content.setImagePath(downloadedImagePath);
        }
    }

    public static ScrollBar getScrollBar(ScrollPane scrollPane) {
        if(scrollPane == null) return null;
        Set<Node> nodes = scrollPane.lookupAll(".scroll-bar");
        for (final Node node : nodes) {
            if (node instanceof ScrollBar) {
                ScrollBar sb = (ScrollBar) node;
                if (sb.getOrientation() == Orientation.VERTICAL) {
                    return sb;
                }
            }
        }
        return null;
    }

    public static File chooseFileDialog(String title, String fileType) {
        log.debug("Choose file dialog is now open");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        switch (fileType) {
            case "movie":
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Movie file", "*.avi", "*.mkv", "*.mp4", "*.m4v"));
                break;
            case "zip":
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP archive file", "*.zip"));
                break;
            case "xml":
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML file", "*.xml"));
                break;
            case "image":
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image file", "*.jpg", "*.png"));
                break;
        }
        fileChooser.setInitialDirectory(Config.getMAIN_MOVIE_FOLDER().toFile());
        File chosenFile =  fileChooser.showOpenDialog(primaryStage);
        log.debug("Chosen file is \"{}\"", chosenFile);
        return chosenFile;
    }

    public static class LoadingWindow {

        public JFrame getInitWindow() {

            JFrame loadingFrame = new JFrame("MyMovieManager");
            URL url = this.getClass().getResource("/icons/MMM.png");
            Icon icon = new ImageIcon(url);
            JLabel label = new JLabel(icon);
            loadingFrame.setIconImage(new ImageIcon(getClass().getResource("/icons/favicon.png").getPath()).getImage());
            loadingFrame.setUndecorated(true);
            loadingFrame.setBackground(new Color(0,0,0,0));
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            loadingFrame.setLocation((int) ((screenBounds.getWidth() - 600) / 2), (int) ((screenBounds.getHeight() - 0) / 2));
            loadingFrame.getContentPane().add(label);
            loadingFrame.pack();
            loadingFrame.setLocationRelativeTo(null);
            loadingFrame.setVisible(true);
            return loadingFrame;
        }
    }

}
