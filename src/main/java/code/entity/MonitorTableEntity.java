package code.entity;

import code.repository.mapper.TableEntity;
import code.repository.mapper.TableField;
import code.repository.mapper.TableName;
import lombok.Data;

@TableName(name = "monitor_table")
@Data
public class MonitorTableEntity implements TableEntity {

    @TableField(name = "id", sql = "id varchar(55) primary key")
    private String id;

    @TableField(name = "monitor_name", sql = "monitor_name varchar(50)")
    private String monitorName;

    @TableField(name = "create_time", sql = "create_time timestamp")
    private Long createTime;
    @TableField(name = "update_time", sql = "update_time timestamp")
    private Long updateTime;

    @TableField(name = "chat_id", sql = "chat_id varchar(88)")
    private String chatId;

    @TableField(name = "platform", sql = "platform varchar(88)")
    private String platform;

    @TableField(name = "monitor_on", sql = "monitor_on integer(2)")
    private Integer monitorOn;

    @TableField(name = "git_owner", sql = "git_owner varchar(88)")
    private String gitOwner;

    @TableField(name = "git_repo", sql = "git_repo varchar(88)")
    private String gitRepo;

    @TableField(name = "template", sql = "template text")
    private String template;

    @TableField(name = "version", sql = "version varchar(30)")
    private String version;

    @TableField(name = "chat_id_array_json", sql = "chat_id_array_json text")
    private String chatIdArrayJson;

    @TableField(name = "web_page_preview", sql = "web_page_preview integer(2)")
    private Integer webPagePreview;

}
