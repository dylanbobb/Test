/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spotifyplayer;

import java.util.ArrayList;

/**
 *
 * @author cstuser
 */
public class Json 
{
    public static void main(String[] args)
    {
        String artistId = SpotifyController.getArtistId("Metallica");
        String artistId2 = SpotifyController.getArtistId("EXO");
        System.out.println(artistId + "  " + artistId2);
    }
}
