package songbox.house.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.util.CollectionUtils;
import songbox.house.domain.TrackSource;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;
import static songbox.house.domain.TrackSource.FILESYSTEM;

@Getter
@Setter
@Entity
@Table(name = "TRACK")
@NoArgsConstructor
@EqualsAndHashCode(of = { "title", "authorsStr" })
public class Track implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column
    private Long trackId;

    @Column
    private String fileName;

    @Column
    private String title;

    @Column
    private Integer duration;

    @Column
    private Short bpm;

    @Column
    private Short bitRate;

    @Column
    private String key;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date dateAdded;

    @JsonIgnore
    @OneToOne(cascade = ALL, fetch = LAZY)
    private TrackContent content;

    @Column(columnDefinition = "Decimal(10,2)")
    private Double sizeMb;

    @Column
    @Enumerated(EnumType.STRING)
    private TrackSource trackSource = FILESYSTEM;

    @Column
    private String extension;

//    private Album album;

    @Column
    private String authorsStr;

    @Column
    private Short year;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "AUTHOR_TRACK", joinColumns = @JoinColumn(name = "TRACK_ID"), inverseJoinColumns = @JoinColumn(name = "AUTHOR_ID"))
    private Set<Author> authors = new HashSet<>();

    @Column
    private String mainGenre;

    @ManyToMany(cascade = ALL)
    @JoinTable(name = "TRACK_GENRE", joinColumns = @JoinColumn(name = "TRACK_ID"), inverseJoinColumns = @JoinColumn(name = "GENRE_ID"))
    private Set<Genre> genres = new HashSet<>();

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "TRACK_COLLECTION", joinColumns = @JoinColumn(name = "TRACK_ID"), inverseJoinColumns = @JoinColumn(name = "COLLECTION_ID"))
    @JsonIgnore
    private Set<MusicCollection> collections = new HashSet<>();

    public void setGenres(final Set<Genre> genres) {
        this.genres = genres;

        if (!CollectionUtils.isEmpty(genres)) {
            mainGenre = genres.iterator().next().getName();
        }
    }
}
