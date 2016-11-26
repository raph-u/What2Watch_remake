/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package what2watch;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import sun.awt.RepaintArea;

/**
 *
 * @author Raphael.BAZZARI
 */
public class FXMLDocumentController implements Initializable {
    
    @FXML
    private Button settingsButton;
    @FXML
    private Button scanFolder;
    @FXML
    private TextArea synopsisTextArea;
    @FXML
    private ListView<Movie> movieListView;
    @FXML
    private TextField searchTextField;
    @FXML
    private Button searchButton;
    @FXML
    private ComboBox<String> searchCriteriasComboBox;
    @FXML
    private ImageView movieImageView;
    @FXML
    private TextField startingYearTextField;
    @FXML
    private TextField endingYearTextField;
    @FXML
    private Label titleLabel;
    @FXML
    private Label titleValueLabel;
    @FXML
    private Label yearLabel;
    @FXML
    private Label yearValueLabel;
    @FXML
    private Label genreLabel;
    @FXML
    private Label genreValueLabel;
    @FXML
    private Label ActorsLabel;
    @FXML
    private Label actorsValueLabel;
    @FXML
    private Label synopsisLabel;
    @FXML
    private Label startingYearLabel;
    @FXML
    private Label endingYearLabel;
    @FXML
    private Label directorsLabel;
    @FXML
    private Label directorsValueLabel;
    @FXML
    private ProgressIndicator searchProgressIndicator;
    
    private UserPreferences prefs = new UserPreferences();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Displaying the settings window before the main one if no path has been saved in the app
        if(this.prefs.getPath().equals("")) {
            try {
                showSettings(null);
            } catch (IOException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        // Combobox search criterias configuration
        this.searchCriteriasComboBox.getItems().addAll(
            "Titre",
            "Acteur",
            "Année"
        );
        
        // We hide the progressIndicator
        searchProgressIndicator.setVisible(false);
    }    

    @FXML
    private void showSettings(ActionEvent event) throws IOException {
        // Settings window creation
        Parent root = FXMLLoader.load(getClass().getResource("FXMLSettings.fxml"));
        Stage settingStage = new Stage();
        
        // Window customization
        settingStage.setResizable(false);
        settingStage.initModality(Modality.APPLICATION_MODAL);
        settingStage.setTitle("Sélection du répertoire de films");
        
        Scene scene = new Scene(root);
        settingStage.setScene(scene);
        
        settingStage.showAndWait();
    }

    @FXML
    private void browseFiles(ActionEvent event) throws IOException {
        CacheDb cacheDb = new CacheDb();
        // Update the cache db (add or remove movies on DB, it depend on the
        // browser.getMovieFileNames) and get real titles of all the movies on 
        // the DB.
        ArrayList<String> fileNames = ParsingFiles.parse(FileBrowser.getMovieFileNames());
        ArrayList<String> rawFileNames = FileBrowser.getMovieFileNames();
        DbHandler dbHandler = new DbHandler(cacheDb,fileNames,rawFileNames);
        
        // Init progress indicator to 0 and display it
        searchProgressIndicator.setProgress(0);
        searchProgressIndicator.setVisible(true);
        
        // Update DB and display result on the list
        dbHandler.update(movieListView, searchProgressIndicator);
    }

    @FXML
    private void updateSearchMode(ActionEvent event) {
        String searchCriteria = this.searchCriteriasComboBox.getValue();

        switch (searchCriteria) {
            case "Titre":
                // TODO limit the scope to movie title informations
                setYearSearchMode(false);
                break;
            case "Acteur":
                // TODO limit the scope to actors informations  
                setYearSearchMode(false);
                break;
            case "Année":
                setYearSearchMode(true);
                break;
            default:
                break;
        }
    }
    
    // Displays/hides textfields according to the selected combobox search criteria
    private void setYearSearchMode(boolean on) {
        startingYearLabel.setVisible(on);
        startingYearTextField.setVisible(on);
        endingYearLabel.setVisible(on);
        endingYearTextField.setVisible(on);
        searchTextField.setVisible(!on);
    }

    @FXML
    private void getMovieInformations(MouseEvent event) {
        
        Movie movie = (Movie)movieListView.getSelectionModel().getSelectedItem();
        String poster = "Unknown";

        /* ACTORS */
        String[] actorsArray = movie.getActors();
        String actors = "";
        for (int j = 0; j < actorsArray.length; j++) {
            actors += actorsArray[j]+", ";
        }
        actors = actors.substring(0, actors.length()-2);

        /* DIRECTORS */
        String[] directorArray = movie.getDirector();
        String director = "";
        for (int j = 0; j < directorArray.length; j++) {
            director += directorArray[j]+", ";
        }
        director = director.substring(0, director.length()-2);

        /* GENRES */
        String[] genresArray = movie.getGenre();
        String genres = "";
        for (int j = 0; j < genresArray.length; j++) {
            genres += genresArray[j]+", ";
        }
        genres = genres.substring(0, genres.length()-2);

        // Set texts on the labels
        titleValueLabel.setText(movie.getTitle());
        yearValueLabel.setText(movie.getYear());
        synopsisTextArea.setText(movie.getSynopsis());  
        actorsValueLabel.setText(actors);
        genreValueLabel.setText(genres);
        directorsValueLabel.setText(director);

        poster = movie.getPoster();        
        
        // Movie poster handling
        Image moviePoster = new Image("what2watch/images/placeHolder.png");
        if (!poster.equals("Unknown")) {
            moviePoster = new Image("http://image.tmdb.org/t/p/w300" + poster);
        }
        
        movieImageView.setImage(moviePoster);
    }
    
}
