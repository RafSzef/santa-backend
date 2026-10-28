package szef.raf.santabackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "letters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Letter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LetterType type = LetterType.ADULT;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_code")
    @JsonIgnore
    private Page page;

    @OneToMany(mappedBy = "letter", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Gift> giftList = new ArrayList<>();

    public void addGift(Gift gift) {
        if (this.giftList == null) {
            this.giftList = new ArrayList<>();
        }
        this.giftList.add(gift);
        gift.setLetter(this);
    }

    public void removeGift(Gift gift) {
        if (this.giftList != null) {
            this.giftList.remove(gift);
            gift.setLetter(null);
        }
    }

    // Aliases for seamless frontend (Angular) communication
    @JsonProperty("from")
    public String getFrom() {
        return name;
    }

    @JsonProperty("from")
    public void setFrom(String from) {
        this.name = from;
    }

    @JsonProperty("text")
    public String getText() {
        return description;
    }

    @JsonProperty("text")
    public void setText(String text) {
        this.description = text;
    }

    @JsonProperty("gifts")
    public List<Gift> getGifts() {
        return giftList;
    }

    @JsonProperty("gifts")
    public void setGifts(List<Gift> gifts) {
        if (this.giftList == null) {
            this.giftList = new ArrayList<>();
        } else {
            this.giftList.clear();
        }
        if (gifts != null) {
            for (Gift gift : gifts) {
                this.addGift(gift);
            }
        }
    }
}
