package songbox.house.service.impl;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import songbox.house.domain.entity.Track;
import songbox.house.service.GoogleAuthenticationService;
import songbox.house.service.GoogleDriveService;

import java.io.IOException;
import java.util.Random;

import static java.io.File.createTempFile;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.io.FileUtils.writeByteArrayToFile;

@Slf4j
@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = PRIVATE)
public class GoogleDriveServiceImpl implements GoogleDriveService {

    private static final String MIME_TYPE = "audio/mpeg";

    GoogleAuthenticationService authenticationService;

    @Override
    public void upload(Track track) {
        final Drive drive = authenticationService.getDrive();

        try {
            final File fileMetadata = createMetadata(track);
            final FileContent content = createContent(track);

            final File uploadedFile = drive.files().create(fileMetadata, content)
                    .setFields("id")
                    .execute();
            log.info("Uploaded file with id {}", uploadedFile.getId());
        } catch (Exception e) {
            log.error("Can't upload file to the google drive", e);
        }
    }

    private File createMetadata(Track track) {
        final File fileMetadata = new File();
        fileMetadata.setName(track.getFileName());
        fileMetadata.setMimeType(MIME_TYPE);
        return fileMetadata;
    }

    private FileContent createContent(Track track) throws IOException {
        //TODO implement without files (with input streams)
        java.io.File file = createTempFile("" + new Random().nextInt(), ".mp3");
        writeByteArrayToFile(file, track.getContent().getContent());
        return new FileContent(MIME_TYPE, file);
    }
}
