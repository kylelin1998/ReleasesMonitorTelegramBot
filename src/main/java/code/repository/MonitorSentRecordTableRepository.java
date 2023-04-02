package code.repository;

import code.config.Config;
import code.entity.MonitorSentRecordTableEntity;
import code.repository.mapper.TableRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MonitorSentRecordTableRepository extends TableRepository<MonitorSentRecordTableEntity> {

    public MonitorSentRecordTableRepository() {
        super(Config.DBPath);
    }

    public boolean save(MonitorSentRecordTableEntity entity) {
        MonitorSentRecordTableEntity where = new MonitorSentRecordTableEntity();
        where.setMonitorId(entity.getMonitorId());
        where.setVersion(entity.getVersion());
        Integer count = super.selectCount(where);
        if (null == count) {
            return false;
        } else if (count > 0) {
            return true;
        }
        Boolean insert = super.insert(entity);
        return null == insert ? false : insert;
    }

    public List<MonitorSentRecordTableEntity> selectListByMonitorId(String monitorId) {
        MonitorSentRecordTableEntity where = new MonitorSentRecordTableEntity();
        where.setMonitorId(monitorId);
        return super.selectList(where);
    }

}
