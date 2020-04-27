package songbox.house.service.impl;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.TextUtils;
import org.springframework.stereotype.Service;
import songbox.house.domain.dto.response.TrackDto;
import songbox.house.domain.entity.MusicCollection;
import songbox.house.domain.entity.Track;
import songbox.house.service.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static java.io.File.createTempFile;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.io.FileUtils.writeByteArrayToFile;

@Slf4j
@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = PRIVATE)
public class GoogleDriveServiceImpl implements GoogleDriveService {
    private static final String FOLDER_NAME = "SongboxHouse";
    private static final String DEFAULT_COLLECTION_FOLDER = "DefaultCollection";
    private static final String MIME_TYPE = "audio/mpeg";
    public static final String GOOGLE_DRIVE_MIMETYPE_FOLDER = "application/vnd.google-apps.folder";

    GoogleAuthenticationService authenticationService;
    UserPropertyService userPropertyService;


    @Override
    public boolean upload(TrackDto track) {
        return upload(track, null, null);
    }

    @Override
    public boolean upload(TrackDto track, String folder, String genreFolder) {
        Drive drive = authenticationService.getDrive();

        try {
            final File fileMetadata = createMetadata(track);
            final FileContent content = createContent(track);

            File initFolder;

            if (!TextUtils.isBlank(genreFolder)) {
                initFolder = getFolderOrCreate(drive, genreFolder, getRootFolder(drive));
            } else {
                initFolder = getRootFolder(drive);
            }

            if (folder != null) {
                initFolder = getFolderOrCreate(drive, folder, initFolder);
            }

            fileMetadata.setParents(Collections.singletonList(initFolder.getId()));
            final File uploadedFile = drive.files().create(fileMetadata, content)
                    .setFields("id, parents")
                    .execute();
            log.info("Uploaded file with id {}", uploadedFile.getId());
            // TODO maybe need to check that files was uploaded. I faced with issue, when I have no free space on my
            // google drive and this flow not throws error
            return true;
        } catch (Exception e) {
            log.error("Can't upload file to the google drive", e);
            return false;
        }
    }

    /**
     *
     * @param drive
     * @param folderName
     * @param parentFolder
     * @return a folder or create if not exist
     * @throws IOException
     */
    private File getFolderOrCreate(Drive drive, String folderName, File parentFolder) throws IOException {
        String escapedFolderName = folderName.replaceAll("'", "").replaceAll("\"", "");
        String q;
        if (parentFolder == null) {
            q = String.format("name = '%s' and mimeType = '%s'",
                    escapedFolderName,
                    GOOGLE_DRIVE_MIMETYPE_FOLDER);
        } else {
            q = String.format("name = '%s' and mimeType = '%s' and parents in '%s'",
                    escapedFolderName,
                    GOOGLE_DRIVE_MIMETYPE_FOLDER,
                    parentFolder.getId());
        }
        FileList fileList = drive.files().list()
                .setQ(q)
                .execute();
        List<File> files = fileList.getFiles();
        if (files.isEmpty()) {
            return createFolder(drive, folderName, parentFolder);
        }

        return files.get(0);
    }

    private File createFolder(Drive drive, String folderName, File parentFolder) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(folderName);
        fileMetadata.setMimeType(GOOGLE_DRIVE_MIMETYPE_FOLDER);
        if (parentFolder != null) {
            fileMetadata.setParents(Collections.singletonList(parentFolder.getId()));
        }

        return drive.files().create(fileMetadata)
                .setFields("id" + (parentFolder != null ? ", parents" : ""))
                .execute();
    }

    private File getRootFolder(Drive drive) throws IOException {
        String collectionFolder;

        MusicCollection defaultCollection = userPropertyService.getCurrentUserProperty().getDefaultCollection();
        if (defaultCollection != null) {
            collectionFolder = defaultCollection.getCollectionName();
        } else {
            collectionFolder = DEFAULT_COLLECTION_FOLDER;
        }

        return getFolderOrCreate(drive, collectionFolder, getFolderOrCreate(drive, FOLDER_NAME, null));
    }

    private File createMetadata(TrackDto track) {
        final File fileMetadata = new File();
        fileMetadata.setName(track.getFileName());
        fileMetadata.setMimeType(MIME_TYPE);
        return fileMetadata;
    }

    private FileContent createContent(TrackDto track) throws IOException {
        //TODO implement without files (with input streams)
        java.io.File file = createTempFile("" + new Random().nextInt(), ".mp3");
        writeByteArrayToFile(file, track.getContent());
        return new FileContent(MIME_TYPE, file);
    }
}
