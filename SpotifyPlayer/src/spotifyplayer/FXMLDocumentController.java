/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spotifyplayer;

import com.sun.javafx.collections.ObservableListWrapper;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Callback;
import javafx.util.Duration;

/**
 *
 * @author bergeron
 */
public class FXMLDocumentController implements Initializable {
    @FXML
    Label artistLabel;
    
    @FXML
    Label albumLabel;
    
    @FXML
    TextField searchInput;
    
    @FXML
    TableView tracksTableView;
    
    @FXML
    Slider trackSlider;
    
    @FXML
    ImageView imageView;
    
    @FXML
    Button rightClick;
    
    @FXML
    Button leftClick;
    
    @FXML
    ProgressIndicator progressIndicator;
    
    
    private int currentAlbumNumber = 0;

    // Other Fields...
    ScheduledExecutorService sliderExecutor = null;
    MediaPlayer mediaPlayer = null;
    boolean isSliderAnimationActive = false;
    Button lastPlayButtonPressed = null;
    
    ArrayList<Album> albums = null;
    int currentAlbumIndex = 0;
    


    private void startMusic(String url) {
        lastPlayButtonPressed.setText("Pause");
        trackSlider.setDisable(false);

        
        if (mediaPlayer != null)
        {
            stopMusic();
        }
        
        mediaPlayer = new MediaPlayer(new Media(url));
        mediaPlayer.setOnReady(() -> {
            mediaPlayer.play();
            isSliderAnimationActive = true;
            trackSlider.setValue(0);
            trackSlider.setMax(30.0);
            
            mediaPlayer.setOnEndOfMedia(() -> {
                mediaPlayer.pause();
                mediaPlayer.seek(Duration.ZERO);
                
                isSliderAnimationActive = true;
                trackSlider.setValue(0);
            });
        });
    }
    
    public void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
    }

    public void playPauseMusic() {
        try
        {
            if (lastPlayButtonPressed != null && lastPlayButtonPressed.getText().equals("Play"))
            {
                lastPlayButtonPressed.setText("Pause");
                
                if (mediaPlayer != null)
                {
                    mediaPlayer.play();
                }
                trackSlider.setValue(mediaPlayer.getCurrentTime().toSeconds());
                isSliderAnimationActive = true;
            }
            else
            {
                lastPlayButtonPressed.setText("Play");
                if (mediaPlayer != null)
                {
                    mediaPlayer.pause();
                }
                isSliderAnimationActive = false;
            }
        }
        catch(Exception e)
        {
            System.err.println("Error playing/pausing song...");
        }        
    }
    
    private void displayAlbum(int albumNumber)
    {
        // TODO - Display all the informations about the album
        //
        //        Artist Name 
        //        Album Name
        //        Album Cover Image
        //        Enable next/previous album buttons, if there is more than one album
        
        
        // Display Tracks for the album passed as parameter
        if (albumNumber >=0 && albumNumber < albums.size())
        {
            currentAlbumIndex = albumNumber;
            Album album = albums.get(albumNumber);
            
            // Set tracks
            ArrayList<TrackForTableView> tracks = new ArrayList<>();
            for (int i=0; i<album.getTracks().size(); ++i)
            {
                TrackForTableView trackForTable = new TrackForTableView();
                Track track = album.getTracks().get(i);
                trackForTable.setTrackNumber(track.getNumber());
                trackForTable.setTrackTitle(track.getTitle());
                trackForTable.setTrackPreviewUrl(track.getUrl());
                tracks.add(trackForTable);
            }
            tracksTableView.setItems(new ObservableListWrapper(tracks));

            trackSlider.setDisable(true);
            trackSlider.setValue(0.0);                       
        }
    }
    
    private void searchAlbumsFromArtist(String artistName)
    {
        // TODO - Make sure this is not blocking the UI
        currentAlbumIndex = 0;
        String artistId = SpotifyController.getArtistId(artistName);
        albums = SpotifyController.getAlbumDataFromArtist(artistId);        
    }
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        progressIndicator.setVisible(false);
        // Setup Table View
        TableColumn<TrackForTableView, Number> trackNumberColumn = new TableColumn("#");
        trackNumberColumn.setCellValueFactory(new PropertyValueFactory("trackNumber"));
        
        TableColumn trackTitleColumn = new TableColumn("Title");
        trackTitleColumn.setCellValueFactory(new PropertyValueFactory("trackTitle"));
        trackTitleColumn.setPrefWidth(325);
        
        TableColumn playColumn = new TableColumn("Preview");
        playColumn.setCellValueFactory(new PropertyValueFactory("trackPreviewUrl"));
        Callback<TableColumn<TrackForTableView, String>, TableCell<TrackForTableView, String>> cellFactory = new Callback<TableColumn<TrackForTableView, String>, TableCell<TrackForTableView, String>>(){
            @Override
            public TableCell<TrackForTableView, String> call(TableColumn<TrackForTableView, String> param) {
                final TableCell<TrackForTableView, String> cell = new TableCell<TrackForTableView, String>(){
                    final Button playButton = new Button("Play");

                    @Override
                    public void updateItem(String item, boolean empty)
                    {
                        if (item != null && item.equals("") == false){
                            playButton.setOnAction(event -> {
                                if (playButton.getText().equals("Pause") || (mediaPlayer != null && mediaPlayer.getMedia().getSource().equals(item)))
                                {
                                    playPauseMusic();
                                }
                                else
                                {
                                    if (lastPlayButtonPressed != null)
                                    {
                                        lastPlayButtonPressed.setText("Play");
                                    }

                                    lastPlayButtonPressed = playButton;
                                    startMusic(item);
                                }
                            });
    
                            setGraphic(playButton);
                        }
                        else{                        
                            setGraphic(null);
                        }

                        setText(null);                        
                    }
                };
                
                return cell;
            }
        };
        playColumn.setCellFactory(cellFactory);
        tracksTableView.getColumns().setAll(trackNumberColumn, trackTitleColumn, playColumn);

        // When slider is released, we must seek in the song
        trackSlider.setOnMouseReleased(new EventHandler() {
            @Override
            public void handle(Event event) {
                if (mediaPlayer != null)
                {
                    mediaPlayer.seek(Duration.seconds(trackSlider.getValue()));
                }
            }
        });

        // Schedule the slider to move right every second
        // Set boolean flag to activate/deactivate the animation
        sliderExecutor = Executors.newSingleThreadScheduledExecutor();
        sliderExecutor.scheduleAtFixedRate(new Runnable(){
            @Override
            public void run() {
                // We can't update the GUI elements on a separate thread... 
                // Let's call Platform.runLater to do it in main thread!!
                Platform.runLater(new Runnable(){
                    @Override
                    public void run() {
                        // Move slider
                        if (isSliderAnimationActive)
                        {
                            trackSlider.setValue(trackSlider.getValue() + 1.0);
                        }
                    }
                });
            }
        }, 1, 1, TimeUnit.SECONDS);

        
        // Initialize GUI
        searchAlbumsFromArtist("Metallica");
        displayAlbum(currentAlbumNumber);
        imageView.setImage(new Image(albums.get(currentAlbumNumber).getImageURL()));
        artistLabel.setText(albums.get(currentAlbumNumber).getArtistName());
        albumLabel.setText(albums.get(currentAlbumNumber).getAlbumName());
    }
    
    public void rightClicked(ActionEvent e)
    {
//        if(isSliderAnimationActive)
//        {
//            playPauseMusic();
//        }
        try
        {
            currentAlbumNumber++;
            
            displayAlbum(currentAlbumNumber);
            imageView.setImage(new Image(albums.get(currentAlbumNumber).getImageURL()));
        }
        catch(Exception y)
        {
            currentAlbumNumber = 0;
            displayAlbum(currentAlbumNumber);
            imageView.setImage(new Image(albums.get(currentAlbumNumber).getImageURL()));
        } 
        albumLabel.setText(albums.get(currentAlbumNumber).getAlbumName());
    }
    
    
    public void leftClicked(ActionEvent e)
    {
//        if(isSliderAnimationActive)
//        {
//            playPauseMusic();
//        }
        
        try
        {
            currentAlbumNumber--;
            displayAlbum(currentAlbumNumber);
            imageView.setImage(new Image(albums.get(currentAlbumNumber).getImageURL()));
        }
        catch(Exception y)
        {
            currentAlbumNumber = albums.size()-1;
            displayAlbum(currentAlbumNumber);
            imageView.setImage(new Image(albums.get(currentAlbumNumber).getImageURL()));
        }
        albumLabel.setText(albums.get(currentAlbumNumber).getAlbumName());
    }
    
    public void enterPressed(KeyEvent e)
    {
        if(e.getCode() == KeyCode.ENTER)
        {
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            String input = searchInput.getText();
            progressIndicator.setVisible(true);
            try
            {
                executor.submit(new Task<Void>(){
                    @Override
                    protected Void call() throws Exception 
                    {
                        searchAlbumsFromArtist(input);
                        return null;
                    }
                    
                    @Override
                    protected void succeeded()
                    {
                        progressIndicator.setVisible(false);
                        displayAlbum(currentAlbumNumber); 
                        imageView.setImage(new Image(albums.get(currentAlbumNumber).getImageURL()));
                        artistLabel.setText(albums.get(currentAlbumNumber).getArtistName());
                        albumLabel.setText(albums.get(currentAlbumNumber).getAlbumName());
                    }
                });
                
                if(albums.size() == 1)
                {
                    leftClick.setDisable(true);
                }
            }
            catch(Exception y)
            {
                artistLabel.setText("ERROR");
                albumLabel.setText("Artist not found... Please try again!");
            }
        }
    }
        
    // This will get called automatically when window is closed
    // See spotifyPlayer.java for details about the setup!
    public void shutdown()
    {
        if (sliderExecutor != null)
        {
            sliderExecutor.shutdown();
        }
        
        Platform.exit();
    }
}
