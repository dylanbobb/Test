package spotifyplayer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SpotifyController{
    final static private String SPOTIFY_CLIENT_ID     = "3f23b85754384bcfaa3a8d2cbd90bd47";
    final static private String SPOTIFY_CLIENT_SECRET = "10de1ddac7264fb88a36796d22af3180";
    
    public static String getArtistId(String artistNameQuery)
    {
        String artistId = "";
        try
        {
            String endpoint = "https://api.spotify.com/v1/search";
            String params = "type=artist&q=" + artistNameQuery;
            String jsonOutput = spotifyEndpointToJson(endpoint, params);
            JsonElement rootElement = new JsonParser().parse(jsonOutput);
            JsonObject rootObject = rootElement.getAsJsonObject();
            JsonObject artists= rootObject.get("artists").getAsJsonObject();
            JsonArray items = artists.get("items").getAsJsonArray();
            
            artistId = items.get(0).getAsJsonObject().get("id").getAsString();
        }
        catch(Exception e)
        {
           // e.printStackTrace();
        }

        return artistId;
    }
    
    public static ArrayList<String> getAlbumIdsFromArtist(String artistId)
    {
        ArrayList<String> albumIds = new ArrayList<>();
        String endpoint = "https://api.spotify.com/v1/artists/" + artistId + "/albums";
        String params = "market=CA&limit=50";
        
        String jsonOutput = spotifyEndpointToJson(endpoint, params);
        JsonElement rootElement = new JsonParser().parse(jsonOutput);
        JsonObject rootObject = rootElement.getAsJsonObject();
        JsonArray items = rootObject.get("items").getAsJsonArray();
        
        for(JsonElement x : items)
        {
            albumIds.add(x.getAsJsonObject().get("id").getAsString());
        }

        return albumIds;
    }
    
    public static ArrayList<Album> getAlbumDataFromArtist(String artistId)
    {
        ArrayList<String> albumIds = getAlbumIdsFromArtist(artistId);
        ArrayList<Album> albums = new ArrayList<>();
        String params = "market=CA";

        for(String albumId : albumIds)
        {
            String endpoint = "https://api.spotify.com/v1/albums/" + albumId;
            String jsonOutput = spotifyEndpointToJson(endpoint,params);
            
            
            JsonElement rootElement = new JsonParser().parse(jsonOutput);
            JsonObject rootObject = rootElement.getAsJsonObject();
            
            JsonArray artists = rootObject.get("artists").getAsJsonArray();
            String artistName = artists.get(0).getAsJsonObject().get("name").getAsString();
            
            String albumName = rootObject.get("name").getAsString();
            
            JsonArray images = rootObject.get("images").getAsJsonArray();
            String coverURL = images.get(0).getAsJsonObject().get("url").getAsString();
            
            JsonObject tracks = rootObject.get("tracks").getAsJsonObject();
            JsonArray items = tracks.get("items").getAsJsonArray();
            
            ArrayList<Track> tracksArr = new ArrayList<>();

            for(JsonElement x : items)
            {
                String title = x.getAsJsonObject().get("name").getAsString();
                int durationInSeconds = x.getAsJsonObject().get("duration_ms").getAsInt() / 1000;
                String url = x.getAsJsonObject().get("uri").getAsString();
                System.out.println(url);
                int number  = x.getAsJsonObject().get("track_number").getAsInt();
                tracksArr.add(new Track(number,title,durationInSeconds,url));
            }   
            albums.add(new Album(artistName,albumName,coverURL,tracksArr));
        }
        return albums;
    }


    // This code will help you retrieve the JSON data from a spotify end point
    // It takes care of the complicated parts such as the authentication and 
    // connection to the Web API
    // 
    // You shouldn't have to modify any of the code...
    private static String spotifyEndpointToJson(String endpoint, String params)
    {
        params = params.replace(' ', '+');

        try
        {
            String fullURL = endpoint;
            if (params.isEmpty() == false)
            {
                fullURL += "?"+params;
            }
            
            URL requestURL = new URL(fullURL);
            
            HttpURLConnection connection = (HttpURLConnection)requestURL.openConnection();
            String bearerAuth = "Bearer " + getSpotifyAccessToken();
            connection.setRequestProperty ("Authorization", bearerAuth);
            connection.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;
            String jsonOutput = "";
            while((inputLine = in.readLine()) != null)
            {
                jsonOutput += inputLine;
            }
            in.close();
            
            return jsonOutput;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        return "";
    }


    // This implements the Client Credentials Authorization Flows
    // Based on the Spotify API documentation
    // 
    // It retrieves the Access Token based on the client ID and client Secret  
    //
    // You shouldn't have to modify any of this code...          
    private static String getSpotifyAccessToken()
    {
        try
        {
            URL requestURL = new URL("https://accounts.spotify.com/api/token");
            
            HttpURLConnection connection = (HttpURLConnection)requestURL.openConnection();
            String keys = SPOTIFY_CLIENT_ID+":"+SPOTIFY_CLIENT_SECRET;
            String postData = "grant_type=client_credentials";
            
            String basicAuth = "Basic " + new String(Base64.getEncoder().encode(keys.getBytes()));
            
            // Send header parameter
            connection.setRequestProperty ("Authorization", basicAuth);
            
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // Send body parameters
            OutputStream os = connection.getOutputStream();
            os.write( postData.getBytes() );
            os.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            
            String inputLine;
            String jsonOutput = "";
            while((inputLine = in.readLine()) != null)
            {
                jsonOutput += inputLine;
            }
            in.close();
            
            JsonElement jelement = new JsonParser().parse(jsonOutput);
            JsonObject rootObject = jelement.getAsJsonObject();
            String token = rootObject.get("access_token").getAsString();

            return token;
        }
        catch(Exception e)
        {
            System.out.println("Something wrong here... make sure you set your Client ID and Client Secret properly!");
            e.printStackTrace();
        }
        
        return "";
    }
}
