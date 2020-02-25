package songbox.house.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTube.Builder;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import songbox.house.domain.entity.user.GoogleApiToken;
import songbox.house.domain.entity.user.UserInfo;
import songbox.house.repository.GoogleApiTokenRepository;
import songbox.house.service.GoogleAuthenticationService;
import songbox.house.service.UserService;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import static com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets.load;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static songbox.house.util.Constants.APP_NAME;

@Service
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class GoogleAuthenticationServiceImpl implements GoogleAuthenticationService {
    private static final String YOUTUBE_TOKEN_URL = "/api/google/token";
    private static final List<String> SCOPES = Lists.newArrayList("https://www.googleapis.com/auth/youtube",
            "https://www.googleapis.com/auth/drive");

    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JacksonFactory JACKSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static final GoogleClientSecrets GOOGLE_CLIENT_SECRETS = loadClientSecrets();
    private static final GoogleAuthorizationCodeFlow GOOGLE_AUTHORIZATION_CODE_FLOW = initFlow();

    @Autowired
    public GoogleAuthenticationServiceImpl(GoogleApiTokenRepository googleApiTokenRepository, UserService userService) {
        this.googleApiTokenRepository = googleApiTokenRepository;
        this.userService = userService;
    }

    private static GoogleClientSecrets loadClientSecrets() {
        try {
            Reader clientSecretReader = new InputStreamReader(GoogleAuthenticationServiceImpl.class.getResourceAsStream("/client_secrets.json"));
            return load(JACKSON_FACTORY, clientSecretReader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static GoogleAuthorizationCodeFlow initFlow() {
        return new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT,
                JACKSON_FACTORY,
                GOOGLE_CLIENT_SECRETS, SCOPES)
                .setApprovalPrompt("force")
                .setAccessType("offline").build();
    }

    GoogleApiTokenRepository googleApiTokenRepository;
    UserService userService;

    @Override
    public YouTube getYouTube() {
        UserInfo currentUser = userService.getCurrentUser();

        GoogleApiToken token = googleApiTokenRepository.findByUserId(currentUser.getUserId());

        return ofNullable(token)
                .map(this::initCredential)
                .map(this::initYouTube)
                .orElseThrow(() -> new RuntimeException("Google API credentials not found"));
    }

    @Override
    public Drive getDrive() {
        UserInfo currentUser = userService.getCurrentUser();

        GoogleApiToken token = googleApiTokenRepository.findByUserId(currentUser.getUserId());

        return ofNullable(token)
                .map(this::initCredential)
                .map(this::initDrive)
                .orElseThrow(() -> new RuntimeException("Google API credentials not found"));
    }

    @Override
    public String getRequestAccessUrl(String requestUrl) {
        UserInfo currentUser = userService.getCurrentUser();

        return GOOGLE_AUTHORIZATION_CODE_FLOW
                .newAuthorizationUrl()
                .setRedirectUri(getRedirectUrl(requestUrl))
                .setState(currentUser.getUserId().toString())
                .build();
    }

    @Override
    public void getTokenAndSave(String code, String state, String requestUrl) {
        Long userId = Long.valueOf(state);
        log.info("UserId for token {}", userId);

        getToken(code, requestUrl).ifPresent(token -> saveToken(token, userId));
    }

    private YouTube initYouTube(GoogleCredential googleCredential) {
        return new Builder(HTTP_TRANSPORT, JACKSON_FACTORY, googleCredential)
                .setApplicationName(APP_NAME)
                .build();
    }

    private Drive initDrive(GoogleCredential googleCredential) {
        return new Drive.Builder(HTTP_TRANSPORT, JACKSON_FACTORY, googleCredential)
                .setApplicationName(APP_NAME)
                .build();
    }

    private GoogleCredential initCredential(GoogleApiToken token) {
        GoogleCredential googleCredential = new GoogleCredential.Builder()
                .setJsonFactory(JACKSON_FACTORY)
                .setTransport(HTTP_TRANSPORT)
                .setClientSecrets(getClientSecrets())
                .build();

        googleCredential.setAccessToken(token.getAccessToken());
        googleCredential.setRefreshToken(token.getRefreshToken());

        return googleCredential;
    }

    private GoogleClientSecrets getClientSecrets() {
        return GOOGLE_CLIENT_SECRETS;
    }

    private Optional<GoogleTokenResponse> getToken(String code, String requestUrl) {
        try {
            return of(GOOGLE_AUTHORIZATION_CODE_FLOW
                    .newTokenRequest(code)
                    .setRedirectUri(getRedirectUrl(requestUrl))
                    .execute());
        } catch (IOException e) {
            log.error("Exception during getting access token", e);
            return empty();
        }
    }

    private void saveToken(GoogleTokenResponse googleTokenResponse, Long userId) {
        GoogleApiToken googleApiToken = createToken(googleTokenResponse, userId);

        // Delete
        ofNullable(googleApiTokenRepository.findByUserId(userId)).ifPresent(googleApiTokenRepository::delete);

        googleApiTokenRepository.save(googleApiToken);
    }

    private GoogleApiToken createToken(GoogleTokenResponse googleTokenResponse, Long userId) {
        GoogleApiToken googleApiToken = new GoogleApiToken();
        googleApiToken.setAccessToken(googleTokenResponse.getAccessToken());
        googleApiToken.setRefreshToken(googleTokenResponse.getRefreshToken());
        googleApiToken.setUserId(userId);
        return googleApiToken;
    }

    // localhost:8080/superserver
    // localhost:8080
    private String getRedirectUrl(String requestUrl) {
        GenericUrl url = new GenericUrl(requestUrl);
        // Already contains YOUTUBE_TOKEN_URL
        if (requestUrl.endsWith(YOUTUBE_TOKEN_URL)
                || requestUrl.substring(0, requestUrl.length() - 1).endsWith(YOUTUBE_TOKEN_URL)) {
            url.setRawPath(YOUTUBE_TOKEN_URL);
        } else {
            url.appendRawPath(YOUTUBE_TOKEN_URL);
        }

        return url.build();
    }
}
