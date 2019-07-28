package songbox.house.domain.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table
public class TrackListPattern {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column
    private Long patternId;

    @Column
    private String value;

    @Column
    private String example;
}
