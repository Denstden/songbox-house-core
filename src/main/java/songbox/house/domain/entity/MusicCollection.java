package songbox.house.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import songbox.house.domain.entity.user.UserInfo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table
@RequiredArgsConstructor
@NoArgsConstructor
public class MusicCollection {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column
    private Long collectionId;

    @Column(unique = true)
    @NonNull
    private String collectionName;

    @OneToOne
    @JsonIgnore
    private UserInfo owner;

    //TODO list users who can see collection
}
