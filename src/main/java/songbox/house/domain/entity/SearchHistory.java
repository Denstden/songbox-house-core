package songbox.house.domain.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import songbox.house.domain.SearchStatus;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Getter
@Setter
@Entity
@Table
public class SearchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column
    private Long searchHistoryId;

    @Column
    private String artists;

    @Column
    private String title;

    @Column
    @Enumerated(EnumType.STRING)
    private SearchStatus searchStatus;

    @Column
    private boolean existsInDb = false;

    @OneToOne(fetch = FetchType.LAZY)
    private Track track;

    @Column(length = 4096)
    private String uri;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date dateAdded;
}
