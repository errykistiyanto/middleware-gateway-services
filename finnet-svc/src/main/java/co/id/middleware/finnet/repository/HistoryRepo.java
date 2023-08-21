package co.id.middleware.finnet.repository;

import co.id.middleware.finnet.postgre.DataLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author errykistiyanto@gmail.com 2020-03-05
 */
@Repository
public interface HistoryRepo extends JpaRepository<DataLog, Long> {

}
