package co.id.middleware.finnet.repository;

import co.id.middleware.finnet.entity.DataLog;
import org.springframework.stereotype.Component;

/**
 * @author errykistiyanto@gmail.com 2022-09-12
 */
@Component
public interface HistoryService {
    void save(DataLog resp);

}