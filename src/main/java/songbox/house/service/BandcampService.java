package songbox.house.service;

import songbox.house.domain.dto.response.SearchAndDownloadResponseDto;
//import songbox.house.util.Measurable;

import java.util.Set;

public interface BandcampService {
//    @Measurable
    SearchAndDownloadResponseDto searchAndDownload(String link, Long collectionId, Set<String> genres, boolean only320);
}
