package code.repository;

import code.config.Config;
import code.entity.MonitorTableEntity;
import code.repository.mapper.TableRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MonitorTableRepository extends TableRepository<MonitorTableEntity> {

    public MonitorTableRepository() {
        super(Config.DBPath);
    }

    public boolean save(MonitorTableEntity entity) {
        MonitorTableEntity where = new MonitorTableEntity();
        where.setMonitorName(entity.getMonitorName());
        where.setChatId(entity.getChatId());
        Integer count = super.selectCount(where);
        if (null == count) {
            return false;
        } else if (count > 0) {
            return true;
        }
        Boolean insert = super.insert(entity);
        return null == insert ? false : insert;
    }

    public boolean update(MonitorTableEntity update) {
        MonitorTableEntity where = new MonitorTableEntity();
        where.setId(update.getId());
        Boolean aBoolean = super.update(update, where);
        return null == aBoolean ? false : aBoolean;
    }

    public List<MonitorTableEntity> selectListByChatId(String chatId) {
        MonitorTableEntity where = new MonitorTableEntity();
        where.setChatId(chatId);
        return super.selectList(where);
    }

    public MonitorTableEntity selectOne(String id, String chatId) {
        MonitorTableEntity where = new MonitorTableEntity();
        where.setId(id);
        where.setChatId(chatId);
        return super.selectOne(where);
    }

    public Integer selectCount(String chatId, String name) {
        MonitorTableEntity where = new MonitorTableEntity();
        where.setChatId(chatId);
        where.setMonitorName(name);
        return super.selectCount(where);
    }

}
