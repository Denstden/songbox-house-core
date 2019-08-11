package songbox.house.domain.entity.user;

import lombok.Getter;
import lombok.Setter;
import songbox.house.domain.entity.MusicCollection;

import javax.persistence.*;

import static javax.persistence.CascadeType.ALL;

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

    @Column
    private String vkCookie;

    @OneToOne(cascade = ALL)
    private MusicCollection defaultCollection;
}
