package songbox.house.domain.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import static javax.persistence.GenerationType.AUTO;

@Setter
@Getter
@Entity
@Table
@NoArgsConstructor
public class Like {

    @Id
    @GeneratedValue(strategy = AUTO)
    @Column
    Long id;
    @Column
    String userId;
    @Column
    String artists;
    @Column
    String title;
    @Column
    Integer rating;
    @Column
    String meta;

    //TODO add hashtags


    public Like(String userId, String artists, String title, Integer rating, String meta) {
        this.userId = userId;
        this.artists = artists;
        this.title = title;
        this.rating = rating;
        this.meta = meta;
    }
}
