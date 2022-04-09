package grooteogi.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import grooteogi.enums.HashtagType;
import java.sql.Timestamp;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
public class Hashtag {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private HashtagType hashtagType; //concern, personality

  @Column(length = 200, nullable = false)
  private String tag;

  @CreationTimestamp
  private Timestamp registered;

  @Column(length = 100, nullable = false, columnDefinition = "int default 0")
  private int count;

  @OneToMany(mappedBy = "hashtag")
  @JsonManagedReference
  private List<UserHashtag> userHashtag;

  @OneToMany(mappedBy = "hashtag")
  @JsonManagedReference
  private List<PostHashtag> postHashtag;
}
