package controllers.actor;

import MoviesAndActors.Actor;
import app.Main;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Getter;


import java.time.LocalDate;

public class ActorEdit {

//    == fields ==
    @FXML public TextField setName, setSurname, setNationality;
    @FXML public DatePicker setBirthday, setDeathDay;
    @FXML public ImageView image;
    @FXML public Button changeImage;
    private Actor actor;
    @Getter private BooleanBinding valid;
    private final SimpleBooleanProperty birthdayValid = new SimpleBooleanProperty();
    private final SimpleBooleanProperty deathDayValid = new SimpleBooleanProperty(true);


//    == init ==
    public void initialize() {
        setBirthday.setShowWeekNumbers(false);
        setDeathDay.setShowWeekNumbers(false);
        setName.textProperty().addListener(maxLen(setName, 50));
        setSurname.textProperty().addListener(maxLen(setSurname, 50));
        setNationality.textProperty().addListener(maxLen(setNationality, 40));
        setBirthday.getEditor().textProperty().addListener(dateListener(setBirthday));
        setDeathDay.getEditor().textProperty().addListener(dateListener(setDeathDay));
        valid = birthdayValid.and(deathDayValid);
    }


//    == methods ==
    public void setActor(Actor actor) {
        this.actor = actor;
        setName.setText(actor.getName());
        setSurname.setText(actor.getSurname());
        setNationality.setText(actor.getNationality());
        image.setImage(new Image(actor.getImagePath().toUri().toString()));
        setBirthday.getEditor().setText(actor.getBirthday().format(Main.DTF));
        if(actor.getDeathDay() != null) {
            setDeathDay.getEditor().setText(actor.getDeathDay().format(Main.DTF));
        }
    }

    public void processResult() {
        if(!setName.getText().isEmpty() && !setName.getText().equals(actor.getName())) {
            actor.setName(setName.getText());
        }
        if(!setSurname.getText().isEmpty() && !setSurname.getText().equals(actor.getSurname())) {
            actor.setSurname(setSurname.getText());
        }
        if(!setNationality.getText().isEmpty() && !setNationality.getText().equals(actor.getNationality())) {
            actor.setNationality(setNationality.getText());
        }

        if(!setBirthday.getEditor().getText().isEmpty() && !setBirthday.getEditor().getText().equals(actor.getBirthday().format(Main.DTF))) {
            actor.setBirthday(LocalDate.parse(setBirthday.getEditor().getText(), Main.DTF));
        }
        if(!setDeathDay.getEditor().getText().isEmpty() && !setDeathDay.getEditor().getText().equals(actor.getDeathDay().format(Main.DTF))) {
            actor.setDeathDay(LocalDate.parse(setDeathDay.getEditor().getText(), Main.DTF));
        }
    }

    private ChangeListener<? super String> dateListener(DatePicker datePicker) {
        return (ChangeListener<String>) (observableValue, old, text) -> {
            if(text.length() > 10 || !text.matches("[\\d.]*")) {
                datePicker.getEditor().setText(old);
                return;
            }

            if(!text.matches("\\d{2}\\.\\d{2}\\.(20|19)\\d{2}")) {
                datePicker.getEditor().setStyle("-fx-text-fill: red");
                if(datePicker.equals(setBirthday)) {
                    birthdayValid.setValue(false);
                } else if(datePicker.equals(setDeathDay)) {
                    deathDayValid.setValue(false);
                }
            } else {
                datePicker.getEditor().setStyle("-fx-text-fill: almost_white");
                if(datePicker.equals(setBirthday)) {
                    birthdayValid.setValue(true);
                } else if(datePicker.equals(setDeathDay)) {
                    deathDayValid.setValue(true);
                }
            }
        };
    }

    private ChangeListener<String> maxLen(TextInputControl textField, int max) {
        return (observableValue, oldText, newText) -> {
            if (newText.length() > max) textField.setText(oldText);
        };
    }


}
