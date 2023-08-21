package co.id.middleware.finnet.repository;

import co.id.middleware.finnet.postgre.DataLog;
import org.springframework.stereotype.Component;

/**
 * @author errykistiyanto@gmail.com 2020-03-05
 */
@Component
public interface HistoryService {
    void save(DataLog resp);
}


