package co.id.middleware.finnet.repository;

import co.id.middleware.finnet.entity.DataLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author errykistiyanto@gmail.com 2022-09-12
 */
@Service
public class HistoryImpl implements HistoryService {

    @Autowired(required = false)
    private HistoryRepo service;

    @Override
    public void save(DataLog resp) {service.save(resp);}

}
