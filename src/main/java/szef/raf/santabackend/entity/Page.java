package szef.raf.santabackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Page {

    @Id
    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(length = 255)
    @JsonIgnore
    private String passwordHash;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Letter> letterList = new ArrayList<>();

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addLetter(Letter letter) {
        if (this.letterList == null) {
            this.letterList = new ArrayList<>();
        }
        this.letterList.add(letter);
        letter.setPage(this);
    }

    public void removeLetter(Letter letter) {
        if (this.letterList != null) {
            this.letterList.remove(letter);
            letter.setPage(null);
        }
    }

    @JsonProperty("letters")
    public List<Letter> getLetters() {
        return letterList;
    }

    @JsonProperty("letters")
    public void setLetters(List<Letter> letters) {
        if (this.letterList == null) {
            this.letterList = new ArrayList<>();
        } else {
            this.letterList.clear();
        }
        if (letters != null) {
            for (Letter letter : letters) {
                this.addLetter(letter);
            }
        }
    }
}
