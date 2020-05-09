package songbox.house.domain.entity.user;

import lombok.Getter;
import lombok.Setter;
import songbox.house.domain.entity.MusicCollection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import static javax.persistence.CascadeType.DETACH;
import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REFRESH;
import static javax.persistence.FetchType.LAZY;

@Table
@Entity
@Getter
@Setter
public class UserProperty {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column
    private Long userPropId;

    @Column
    private Boolean telegramBotUseGoogleDrive;

    @Column(name = "search_audio_preview_enabled", columnDefinition = "boolean default false", nullable = false)
    private Boolean searchPreviewEnabled = false;

    //TODO remove
    @Column(length = 2048)
    private String vkCookie;

    @OneToOne(cascade = { PERSIST, MERGE, REFRESH, DETACH })
    private MusicCollection defaultCollection;


    //autoSearchReprocessEnabled
    //autoSearchReprocessDownloadEnabled
    //private Long mask;
}
