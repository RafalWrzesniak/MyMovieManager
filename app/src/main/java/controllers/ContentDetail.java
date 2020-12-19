package controllers;

import app.Main;
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

import java.util.ResourceBundle;

public abstract class ContentDetail {

//    == fields ==
    @FXML protected VBox vBox;
    @FXML protected ImageView contentImage;
    @FXML protected ScrollPane scrollPane;
    @Getter protected double PREF_WIDTH = 300.0;
    protected Button returnButton;
    protected MainController mainController;
    protected ContextMenu contextMenu;

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
                contextMenu.hide();
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
        int lastIndex = mainController.rightDetail.getChildren().size() - 1;
        mainController.rightDetail.getChildren().remove(lastIndex);
        mainController.rightDetail.getChildren().get(lastIndex-1).setVisible(true);
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
        vBox.getChildren().add(label);

        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(10);
        flowPane.setVgap(15);
        vBox.getChildren().add(flowPane);
        return flowPane;
    }

    void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}
