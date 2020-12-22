package controllers.actor;

import MoviesAndActors.Actor;
import app.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.time.LocalDate;

public class ActorEdit {

//    == fields ==
    @FXML public TextField setName, setSurname, setNationality;
    @FXML public DatePicker setBirthday, setDeathDay;
    @FXML public ImageView image;
    @FXML public Button changeImage;
    private Actor actor;


//    == init ==
    public void initialize() {
        setBirthday.setShowWeekNumbers(false);
        setDeathDay.setShowWeekNumbers(false);
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


}
