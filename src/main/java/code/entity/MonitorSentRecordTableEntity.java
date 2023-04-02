package code.entity;

import code.repository.mapper.TableEntity;
import code.repository.mapper.TableField;
import code.repository.mapper.TableName;
import lombok.Data;

@TableName(name = "monitor_sent_record_table")
@Data
public class MonitorSentRecordTableEntity implements TableEntity {

    @TableField(name = "monitor_id", sql = "monitor_id varchar(55)")
    private String monitorId;

    @TableField(name = "version", sql = "version varchar(30)")
    private String version;

}
