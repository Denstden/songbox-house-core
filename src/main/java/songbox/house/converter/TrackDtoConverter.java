package songbox.house.converter;

import org.springframework.stereotype.Component;
import songbox.house.domain.dto.response.TrackDto;
import songbox.house.domain.entity.Track;
import songbox.house.domain.entity.TrackContent;

@Component
public class TrackDtoConverter implements Converter<Track, TrackDto> {
    @Override
    public Track toEntity(TrackDto dto) {
        Track track = new Track();
        track.setAuthorsStr(dto.getArtists());
        track.setTitle(dto.getTitle());
        track.setBitRate(dto.getBitRate());
        track.setDuration(dto.getDurationSec());
        track.setSizeMb(dto.getSizeBytes() / 1024. / 1024);
        track.setExtension(dto.getExtension());
        track.setFileName(dto.getFileName());
        TrackContent content = new TrackContent();
        content.setContent(dto.getContent());
        track.setContent(content);
        return track;
    }

    @Override
    public TrackDto toDto(Track entity) {
        TrackDto dto = new TrackDto();
        //TODO maybe need parse authors
        dto.setArtists(entity.getAuthorsStr());
        dto.setTitle(entity.getTitle());
        dto.setBitRate(entity.getBitRate());
        dto.setDurationSec(entity.getDuration());
        dto.setSizeBytes(Double.valueOf(entity.getSizeMb() * 1024 * 1024).longValue());
        dto.setExtension(entity.getExtension());
        dto.setFileName(entity.getFileName());
        dto.setContent(entity.getContent().getContent());
        return dto;
    }
}
