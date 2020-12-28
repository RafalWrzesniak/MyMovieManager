package controllers;

import app.Main;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import lombok.Getter;
import lombok.Setter;

import java.util.ResourceBundle;

public abstract class ContentDetail {

//    == fields ==
    @FXML public VBox vBox;
    @FXML protected ImageView contentImage;
    @FXML protected ScrollPane scrollPane;
    @Getter protected double PREF_WIDTH = 300.0;
    @Getter @Setter protected MainController mainController;
    protected ContextMenu contextMenu;
    @Getter protected Button returnButton;


//    == init ==
    protected void init(ResourceBundle resourceBundle) {
        scrollPane.getContent().setOnScroll(scrollEvent -> {
            double speed = 0.008;
            double deltaY = scrollEvent.getDeltaY() * speed;
            scrollPane.setVvalue(scrollPane.getVvalue() - deltaY);
        });
        scrollPane.setPadding(new Insets(10, 18, 15, 5));
        scrollPane.setOnMouseClicked(e -> {
            if(e.getButton().equals(MouseButton.PRIMARY)) {
                if(contextMenu != null) contextMenu.hide();
            }
        });

        // show / hide scroll bar
        scrollPane.vbarPolicyProperty().addListener((observableValue, scrollBarPolicy, t1) -> {
            if(t1.equals(ScrollPane.ScrollBarPolicy.ALWAYS)) {
                scrollPane.setPadding(new Insets(10, 5, 15, 5));
            } else if(t1.equals(ScrollPane.ScrollBarPolicy.NEVER)) {
                scrollPane.setPadding(new Insets(10, 18, 15, 5));
            }
        });
        scrollPane.setOnMouseEntered(mouseEvent -> {
            ScrollBar sb = Main.getScrollBar(scrollPane);
            if(sb.getBlockIncrement() < 0.87) {
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
            }
        });
        scrollPane.setOnMouseExited(mouseEvent -> scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER));

        // prepare button to return
        ImageView imageView = new ImageView();
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        imageView.setImage(new Image("icons/return.png"));

        returnButton = new Button(resourceBundle.getString("detail.return"));
        returnButton.getStyleClass().add("returnButton");
        returnButton.setOnAction(event -> returnToEarlierElement());
        returnButton.setGraphicTextGap(10);
        returnButton.setGraphic(imageView);
    }


//    == methods ==
    protected void returnToEarlierElement() {
        int lastIndex = mainController.getRightDetail().getChildren().size() - 1;
        mainController.getRightDetail().getChildren().remove(lastIndex);
        mainController.getRightDetail().getChildren().get(lastIndex-1).setVisible(true);
    }

    protected Label textLabel(String text) {
        if(text == null || text.isEmpty()) return null;
        Label label = new Label(text);
        label.setMaxWidth(160);
        label.setWrapText(true);
        label.setTextAlignment(TextAlignment.CENTER);
        label.getStyleClass().add("contentButton");
        return label;
    }

    protected FlowPane createFlowPaneOf(String textLabel) {
        Label label = new Label(textLabel);
        label.setTextAlignment(TextAlignment.LEFT);
        label.getStyleClass().add("white_text");

        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(10);
        flowPane.setVgap(15);

        Platform.runLater(() -> {
            vBox.getChildren().add(label);
            vBox.getChildren().add(flowPane);
        });
        return flowPane;
    }

}
