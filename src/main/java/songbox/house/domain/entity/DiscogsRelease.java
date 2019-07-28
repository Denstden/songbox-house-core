package songbox.house.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.CascadeType.ALL;

@Setter
@Getter
@Entity
@Table
@RequiredArgsConstructor
public class DiscogsRelease {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column
    private Long id;

    @Column
    private String thumbnail;
    @Column
    private String artist;
    @Column
    private String title;
    @Column
    private String audioLabel; /* RORA */
    @Column
    private String audioLabelReleaseName; /* RORA 18*/
    @Column
    private String country; /* Switzerland */

    @Column
    @ManyToMany(cascade = ALL)
    @JoinTable(name = "DISCOGS_RELEASE_GENRE", joinColumns = @JoinColumn(name = "DISCOGS_RELEASE_ID"), inverseJoinColumns = @JoinColumn(name = "GENRE_ID"))
    private Set<Genre> genres; /* Minimal */

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "DISCOGS_RELEASE_COLLECTION", joinColumns = @JoinColumn(name = "DISCOGS_RELEASE_ID"), inverseJoinColumns = @JoinColumn(name = "COLLECTION_ID"))
    @JsonIgnore
    private Set<MusicCollection> collections = new HashSet<>();

    @Column(unique = true)
    private String discogsLink; /* https://www.discogs.com/IulyB-Systematic-EP/release/13370327 */

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date dateAdded;

}
