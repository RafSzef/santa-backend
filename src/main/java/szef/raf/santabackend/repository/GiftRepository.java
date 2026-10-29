package szef.raf.santabackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import szef.raf.santabackend.entity.Gift;

@Repository
public interface GiftRepository extends JpaRepository<Gift, Long> {
}
