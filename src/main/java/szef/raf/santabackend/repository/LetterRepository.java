package szef.raf.santabackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import szef.raf.santabackend.entity.Letter;

import java.util.List;
import java.util.Optional;

@Repository
public interface LetterRepository extends JpaRepository<Letter, Long> {

    List<Letter> findByPage_Code(String pageCode);

    @Query("SELECT l FROM Letter l LEFT JOIN FETCH l.giftList WHERE l.id = :id AND l.page.code = :pageCode")
    Optional<Letter> findByIdAndPageCode(@Param("id") Long id, @Param("pageCode") String pageCode);

    @Query("SELECT DISTINCT l FROM Letter l LEFT JOIN FETCH l.giftList WHERE l.page.code = :pageCode")
    List<Letter> findAllByPageCodeWithGifts(@Param("pageCode") String pageCode);
}
