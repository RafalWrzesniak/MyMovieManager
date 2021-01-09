package controllers.movie;

import MoviesAndActors.Movie;
import app.Main;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Getter;

import java.time.LocalDate;

public class MovieEdit {

//    == fields ==
    @FXML public Button changeCover;
    @FXML public ImageView cover;
    @FXML public TextField setTitle, setTitleOrg, setLength, setRate, setRateCount, setGenre, setProduction;
    @FXML public TextArea setDescription;
    @FXML public DatePicker setPremiere;
    @Getter private BooleanBinding valid;
    private Movie movie;
    private final SimpleBooleanProperty observableBooleanLength = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty observableBooleanRate = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty observableBooleanPremiere = new SimpleBooleanProperty(true);


//    == init ==
    public void initialize() {
        valid = observableBooleanLength.and(observableBooleanRate).and(observableBooleanPremiere);
        setTitle.textProperty().addListener(maxLen(setTitle, 50));
        setTitleOrg.textProperty().addListener(maxLen(setTitleOrg, 50));
        setDescription.textProperty().addListener(maxLen(setDescription, 300));
        setRate.textProperty().addListener(durationOrRateListener(setRate, observableBooleanRate, '.'));
        setLength.textProperty().addListener(durationOrRateListener(setLength, observableBooleanLength, ':'));
        setRateCount.textProperty().addListener((observableValue, s, t1) -> { if(!t1.matches("\\d*") || t1.length() > 7) setRateCount.setText(s); });
        setPremiere.getEditor().textProperty().addListener(dateListener());
        setGenre.textProperty().addListener(maxLen(setGenre, 50));
        setProduction.textProperty().addListener(maxLen(setProduction, 50));
    }



//    == methods ==
    public void setMovie(Movie movie) {
        this.movie = movie;
        setTitle.setText(movie.getTitle());
        setTitleOrg.setText(movie.getTitleOrg());
        setLength.setText(movie.getDurationShortFormatted());
        setRate.setText(movie.getRate().toString());
        setRateCount.setText(movie.getRateCount().toString());
        StringBuilder genres = new StringBuilder();
        movie.getGenres().forEach(genre -> genres.append(genre).append("; "));
        setGenre.setText(genres.deleteCharAt(genres.lastIndexOf("; ")).toString());
        StringBuilder productions = new StringBuilder();
        movie.getProduction().forEach(prod -> productions.append(prod).append("; "));
        setProduction.setText(productions.deleteCharAt(productions.lastIndexOf("; ")).toString());
        setDescription.setText(movie.getDescription());
        setPremiere.getEditor().setText(movie.getPremiere().format(Main.DTF));
        cover.setImage(new Image(movie.getImagePath().toUri().toString()));
    }

    public void processResult() {
        if(!setTitle.getText().isEmpty() && !setTitle.getText().equals(movie.getTitle())) {
            movie.setTitle(setTitle.getText());
        }
        if(!setTitleOrg.getText().isEmpty() && !setTitleOrg.getText().equals(movie.getTitleOrg())) {
            movie.setTitleOrg(setTitleOrg.getText());
        }
        if(!setLength.getText().isEmpty() && !setLength.getText().equals(movie.getDurationShortFormatted())) {
            String len = setLength.getText();
            int duration = Integer.parseInt(len.substring(0, len.indexOf(":")))*60 + Integer.parseInt(len.substring(len.indexOf(":")+1));
            movie.setDuration(duration);
        }
        if(!setRate.getText().isEmpty() && !setRate.getText().equals(movie.getRate().toString())) {
            movie.setRate(Double.parseDouble(setRate.getText()));
        }
        if(!setRateCount.getText().isEmpty() && !setRateCount.getText().equals(movie.getRateCount().toString())) {
            movie.setRateCount(Integer.parseInt(setRateCount.getText()));
        }
        if(!setDescription.getText().isEmpty() && !setDescription.getText().equals(movie.getDescription())) {
            movie.setDescription(setDescription.getText());
        }
        if(!setPremiere.getEditor().getText().isEmpty() && !setPremiere.getEditor().getText().equals(movie.getPremiere().format(Main.DTF))) {
            movie.setPremiere(LocalDate.parse(setPremiere.getEditor().getText(), Main.DTF));
        }

        if(!setGenre.getText().isEmpty()) {
            String[] genres = setGenre.getText().split("; ");
            if(genres.length != 0) {
                movie.clearGenres();
                for (String genre : genres) {
                    if(!movie.getGenres().contains(genre)) {
                        movie.addGenre(genre.replaceAll(" ", ""));
                    }
                }
            }
        }

        if(!setProduction.getText().isEmpty()) {
            String[] prods = setProduction.getText().split("; ");
            if(prods.length != 0) {
                movie.clearProduction();
                for (String prod : prods) {
                    if(!movie.getProduction().contains(prod)) {
                        movie.addProduction(prod.replaceAll(" ", ""));
                    }
                }
            }
        }

    }


    private ChangeListener<? super String> dateListener() {
        return (ChangeListener<String>) (observableValue, old, text) -> {
            if(text.length() > 10 || !text.matches("[\\d.]*")) {
                setPremiere.getEditor().setText(old);
                return;
            }

            if(!text.matches("\\d{2}\\.\\d{2}\\.(20|19)\\d{2}")) {
                setPremiere.getEditor().setStyle("-fx-text-fill: red");
                observableBooleanPremiere.setValue(false);
            } else {
                setPremiere.getEditor().setStyle("-fx-text-fill: almost_white");
                observableBooleanPremiere.setValue(true);
            }
        };
    }

    private ChangeListener<String> maxLen(TextInputControl textField, int max) {
        return (observableValue, oldText, newText) -> {
            if (newText.length() > max) textField.setText(oldText);
        };
    }


    private ChangeListener<String> durationOrRateListener(TextField textField, SimpleBooleanProperty propertyToSet, char charInside) {
        if(charInside != ':' && charInside != '.') throw new IllegalArgumentException("charInside must be : or .");
        return (observableValue, oldText, newText) -> {
            if(newText.length() == 5) {
                textField.setText(oldText);
                return;
            }
            if(!newText.matches("[\\d" + charInside + "]*")) {
                textField.setText(oldText);
            }
            if(newText.matches("^\\d$") && oldText.isEmpty()) {
                textField.setText(newText.concat(String.valueOf(charInside)));
            }
            if(!newText.matches("^\\d" + charInside + "\\d{1,2}?$")) {
                textField.setStyle("-fx-text-fill: red");
                propertyToSet.setValue(false);
            } else {
                textField.setStyle("-fx-text-fill: almost_white");
                propertyToSet.setValue(true);
            }
        };
    }
}
