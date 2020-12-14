package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import lombok.Getter;

public abstract class ContentDetail {


    @FXML protected VBox vBox;
    @FXML protected ImageView contentImage;
    @Getter protected double PREF_WIDTH;


    public void setCover(Image image) {
        contentImage.setImage(image);
    }


    protected Label textLabel(String text) {
        if(text == null || text.isEmpty()) return null;
        Label label = new Label(text);
        label.setMaxWidth(160);
        label.setWrapText(true);
        label.setTextAlignment(TextAlignment.CENTER);
        String basic = "-fx-border-radius: 15; -fx-background-radius: 15; -fx-border-width: 2; -fx-background-color: dark_blue; -fx-padding: 7 10 7 10; " +
                "-fx-font-family: Verdana; -fx-font-size: 14; -fx-text-fill: almost_white;";
        String normalStyle = basic + "-fx-border-color: my_blue;";
        String hoverStyle = basic + "-fx-border-color: my_green;";
        label.setStyle(normalStyle);
        label.setOnMouseEntered(mouseEvent -> label.setStyle(hoverStyle));
        label.setOnMouseExited(mouseEvent -> label.setStyle(normalStyle));
        return label;
    }

    protected FlowPane createFlowPaneOf(String textLabel) {
        Label label = new Label(textLabel);
        label.setPrefWidth(PREF_WIDTH-20);
        label.setTextAlignment(TextAlignment.LEFT);
        label.getStyleClass().add("white_text");
        vBox.getChildren().add(label);

        FlowPane flowPane = new FlowPane();
        flowPane.setPrefWrapLength(PREF_WIDTH-20);
        flowPane.setMinWidth(PREF_WIDTH-20);
        flowPane.setPrefWidth(PREF_WIDTH-20);
        flowPane.setMaxWidth(PREF_WIDTH-20);
        flowPane.setHgap(10);
        flowPane.setVgap(15);
        vBox.getChildren().add(flowPane);
        return flowPane;
    }
}
