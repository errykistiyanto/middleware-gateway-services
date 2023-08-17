package co.id.middleware.finnet.repository;

import co.id.middleware.finnet.entity.DataLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author errykistiyanto@gmail.com 2022-09-12
 */
@Repository
public interface HistoryRepo extends JpaRepository<DataLog, Long> {

}