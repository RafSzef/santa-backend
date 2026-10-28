package szef.raf.santabackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "gifts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2048)
    private String link;

    @Builder.Default
    private Boolean isSelected = false;

    @Builder.Default
    private Boolean isClaimed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "letter_id")
    @JsonIgnore
    private Letter letter;

    // Helper property aliases for frontend compatibility
    @JsonProperty("url")
    public String getUrl() {
        return link;
    }

    @JsonProperty("url")
    public void setUrl(String url) {
        this.link = url;
    }

    @JsonProperty("claimed")
    public Boolean getClaimed() {
        return isClaimed;
    }

    @JsonProperty("claimed")
    public void setClaimed(Boolean claimed) {
        this.isClaimed = claimed;
    }

    @JsonProperty("selected")
    public Boolean getSelected() {
        return isSelected;
    }

    @JsonProperty("selected")
    public void setSelected(Boolean selected) {
        this.isSelected = selected;
    }
}
