package songbox.house.domain.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import java.util.Date;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;
import static javax.persistence.TemporalType.TIMESTAMP;
import static songbox.house.domain.entity.SearchReprocessStatus.NOT_FOUND;

@Getter
@Setter
@Entity
@Table(name = "SEARCH_REPROCESS")
@NoArgsConstructor
public class SearchReprocess {
    @Id
    @GeneratedValue(strategy = AUTO)
    @Column
    Long id;
    @Column(nullable = false)
    String searchQuery;
    @Column(nullable = false)
    Long userId;
    @Column
    Long collectionId;
    @Column
    Integer retries = 0;
    @CreationTimestamp
    @Temporal(TIMESTAMP)
    @Column
    Date createdAt;
    @UpdateTimestamp
    @Temporal(TIMESTAMP)
    @Column
    Date updatedAt;
    @Temporal(TIMESTAMP)
    @Column
    Date downloadedAt;
    @Temporal(TIMESTAMP)
    @Column
    Date foundAt;
    @Column
    @Enumerated(STRING)
    SearchReprocessStatus status = NOT_FOUND;
    @Column
    String genres;
}
