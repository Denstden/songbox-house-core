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

import static java.util.Objects.isNull;
import static javax.persistence.CascadeType.DETACH;
import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REFRESH;
import static songbox.house.domain.entity.user.UserPropertyMask.AUTO_DOWNLOAD_SEARCH_REPROCESS_ENABLED;
import static songbox.house.domain.entity.user.UserPropertyMask.AUTO_SEARCH_REPROCESS_AFTER_FAIL_ENABLED;
import static songbox.house.domain.entity.user.UserPropertyMask.AUTO_USE_FULL_SEARCH_IF_FAST_NOT_SUCCESS;
import static songbox.house.domain.entity.user.UserPropertyMask.SEARCH_REPROCESS_ENABLED;
import static songbox.house.domain.entity.user.UserPropertyMask.USE_ALWAYS_FULL_SEARCH;
import static songbox.house.util.MaskUtil.hasMask;

@Table
@Entity
@Getter
@Setter
public class UserProperty {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column
    private Long userPropId;

    //TODO remove after clearing DB -> add new bit in the mask
    @Column
    private Boolean telegramBotUseGoogleDrive;

    //TODO remove after clearing DB -> add new bit in the mask
    @Column(name = "search_audio_preview_enabled", columnDefinition = "boolean default false", nullable = false)
    private Boolean searchPreviewEnabled = false;

    //TODO remove after clearing DB(unused)
    @Column(length = 2048)
    private String vkCookie;

    @OneToOne(cascade = { PERSIST, MERGE, REFRESH, DETACH })
    private MusicCollection defaultCollection;

    @Column
    private Long mask = 0L;

    private long getMask() {
        return mask;
    }

    private void setMask(Long mask) {
        this.mask = mask;
    }

    public long getDomainMask() {
        return fromDomainData(mask);
    }

    public void setDomainMask(Long mask) {
        this.mask = toDomainData(mask);
    }

    public boolean isSearchReprocessEnabled() {
        return hasMask(getDomainMask(), SEARCH_REPROCESS_ENABLED);
    }

    public boolean isAutoSearchReprocessAfterFailEnabled() {
        return isSearchReprocessEnabled() && hasMask(getDomainMask(), AUTO_SEARCH_REPROCESS_AFTER_FAIL_ENABLED);
    }

    public boolean isAutoDownloadSearchReprocessEnabled() {
        return isSearchReprocessEnabled() && hasMask(getDomainMask(), AUTO_DOWNLOAD_SEARCH_REPROCESS_ENABLED);
    }

    public boolean isUseFullSearchIfFastFailsEnabled() {
        return hasMask(getDomainMask(), AUTO_USE_FULL_SEARCH_IF_FAST_NOT_SUCCESS);
    }

    public boolean isUseAlwaysFullSearch() {
        return hasMask(getDomainMask(), USE_ALWAYS_FULL_SEARCH);
    }

    private long toDomainData(Long mask) {
        if (isNull(mask)) {
            return 0L;
        }
        return mask ^ INVERSION_MASK;
    }

    private long fromDomainData(Long mask) {
        if (mask == null) {
            return INVERSION_MASK;
        } else {
            return mask ^ INVERSION_MASK;
        }
    }

    private static final long INVERSION_MASK = 0b01011;
}
