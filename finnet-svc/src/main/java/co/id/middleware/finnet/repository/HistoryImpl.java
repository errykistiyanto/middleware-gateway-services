package co.id.middleware.finnet.repository;

import co.id.middleware.finnet.postgre.DataLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author errykistiyanto@gmail.com 2020-03-05
 */
@Service
public class HistoryImpl implements HistoryService {

    @Autowired(required = false)
    private HistoryRepo service;

    @Override
    public void save(DataLog resp) {service.save(resp);}

}
