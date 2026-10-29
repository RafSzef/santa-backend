package szef.raf.santabackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import szef.raf.santabackend.entity.Page;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<Page, String> {

    @Query("SELECT p FROM Page p LEFT JOIN FETCH p.letterList WHERE p.code = :code")
    Optional<Page> findByCodeWithLetters(@Param("code") String code);

    @Modifying
    @Query("UPDATE Page p SET p.updatedAt = :now WHERE p.code = :code")
    void touchPage(@Param("code") String code, @Param("now") LocalDateTime now);
}
