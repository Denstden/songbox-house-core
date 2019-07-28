package songbox.house.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import songbox.house.domain.DownloadStatus;
import songbox.house.util.compare.BitRateDuration;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;

@Data
@EqualsAndHashCode(of = { "id", "ownerId" })
@NoArgsConstructor

@Entity
public class VkAudio implements Serializable, BitRateDuration {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column
    private Long id;

    @Column
    private Long vkAudioId;

    @Column
    private Long ownerId;

    @Column(nullable = false)
    private String artist;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer duration;

    @Column(nullable = true)
    private String artworkSrc;

/*    @Column(nullable = false)
    private Integer position;*/

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DownloadStatus status = DownloadStatus.NEW;

    @Transient
    private Short bitRate;

    @Transient
    private Double sizeMb;

    @Transient
    private String url;


    public VkAudio(@NonNull Long id, @NonNull Long ownerId, @NonNull String artist, @NonNull String title,
            @NonNull Integer duration) {
        this.vkAudioId = id;
        this.ownerId = ownerId;
        this.artist = artist;
        this.title = title;
        this.duration = duration;
    }

    public String getFilename() {
        StringBuilder sb = new StringBuilder();

        String formattedArtist = getArtist().trim().replaceAll("[!\"#$%&'()*+,\\-/:;<=>?@\\[\\]^_`{|}~]", "");
        String formattedTitle = getTitle().trim().replaceAll("[!\"#$%&'()*+,\\-/:;<=>?@\\[\\]^_`{|}~]", "");

        sb.append(StringUtils.substring(formattedArtist, 0, 40));
        sb.append(" - ");
        sb.append(StringUtils.substring(formattedTitle, 0, 50));

        sb.append(".mp3");

        return sb.toString();
    }

    public boolean isM3U8() {
        return getUrl().contains(".m3u8");
    }
}
