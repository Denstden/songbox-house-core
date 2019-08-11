package songbox.house.service;

import com.google.api.services.drive.Drive;
import com.google.api.services.youtube.YouTube;

public interface GoogleAuthenticationService {
    YouTube getYouTube();

    Drive getDrive();

    String getRequestAccessUrl();

    void getTokenAndSave(String code, String state, String requestUrl);
}
