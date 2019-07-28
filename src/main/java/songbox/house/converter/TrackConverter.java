package songbox.house.converter;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import songbox.house.domain.dto.response.ArtistDto;
import songbox.house.domain.dto.response.GenreDto;
import songbox.house.domain.dto.response.PageDto;
import songbox.house.domain.dto.response.TrackInfoDto;
import songbox.house.domain.dto.response.TracksDto;
import songbox.house.domain.entity.Author;
import songbox.house.domain.entity.Genre;
import songbox.house.domain.entity.Track;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class TrackConverter implements Converter<Track, TrackInfoDto> {
    @Override
    public Track toEntity(final TrackInfoDto dto) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TrackInfoDto toDto(final Track entity) {
        final TrackInfoDto dto = new TrackInfoDto();

        dto.setTrackId(entity.getTrackId());
        dto.setTitle(entity.getTitle());
        dto.setArtists(convertArtists(entity.getAuthors()));
        dto.setGenres(convertGenres(entity.getGenres()));
        dto.setBitRate(entity.getBitRate());
        dto.setDurationSec(entity.getDuration().shortValue());
        dto.setSizeMb(entity.getSizeMb());
        dto.setFormat(entity.getExtension());

        return dto;
    }

    public TracksDto toDto(final Page<Track> trackPage) {
        final TracksDto tracksDto = new TracksDto();

        tracksDto.setPageDto(createPageDto(trackPage));
        tracksDto.setTracks(toDtos(trackPage));

        return tracksDto;
    }

    private PageDto createPageDto(final Page<Track> trackPage) {
        final PageDto pageDto = new PageDto();

        pageDto.setPageNumber(trackPage.getNumber());
        pageDto.setPageSize(trackPage.getSize());
        pageDto.setTotalPages(trackPage.getTotalPages());
        pageDto.setTotalElements(trackPage.getTotalElements());

        return pageDto;
    }

    private List<GenreDto> convertGenres(final Set<Genre> genres) {
        final List<GenreDto> dtos = new ArrayList<>();

        if (genres != null) {
            genres.forEach(genre -> dtos.add(convertGenre(genre)));
        }

        return dtos;
    }

    private GenreDto convertGenre(final Genre genre) {
        return new GenreDto(genre.getGenreId(), genre.getName());
    }

    private List<ArtistDto> convertArtists(final Set<Author> authors) {
        final List<ArtistDto> dtos = new ArrayList<>();

        if (authors != null) {
            authors.forEach(author -> {
                dtos.add(convertArtist(author));
            });
        }

        return dtos;
    }

    private ArtistDto convertArtist(final Author author) {
        return new ArtistDto(author.getId(), author.getName());
    }
}
