package songbox.house.converter;

import org.springframework.stereotype.Component;
import songbox.house.domain.entity.Track;
import songbox.house.domain.entity.VkAudio;

@Component
public class VkAudioToTrackConverter {
    public Track convert(final VkAudio audio) {
        final Track track = new Track();

        track.setAuthorsStr(audio.getArtist().trim());
        track.setTitle(audio.getTitle().trim());
        track.setDuration(audio.getDuration());
        track.setBitRate(audio.getBitRate());
        track.setSizeMb(audio.getSizeMb());
        track.setFileName(audio.getFilename().trim());

        return track;
    }
}
