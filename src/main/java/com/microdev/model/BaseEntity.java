package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.Version;
import com.baomidou.mybatisplus.enums.FieldStrategy;
import com.baomidou.mybatisplus.enums.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;


import java.io.Serializable;
import java.time.OffsetDateTime;


/**
 * @author liutf
 */
@Data
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value ="id",type = IdType.UUID)
    private String pid;
    /**
     * 创建时间
     */
    //@TableField(validate= FieldStrategy.IGNORED)
    @TableField(value="create_time",validate = FieldStrategy.NOT_EMPTY)
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private OffsetDateTime createTime;
    /**
     * 最后一次更新时间
     */
    @Version
    //@TableField(validate= FieldStrategy.IGNORED)
    @TableField(value="modify_time",validate = FieldStrategy.NOT_EMPTY)
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private OffsetDateTime modifyTime;
    /**
     * 逻辑删除  默认：false，true为删除
     */
    private Boolean deleted = false;

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    public OffsetDateTime getCreateTime() {
        return createTime;
    }
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    public void setCreateTime(OffsetDateTime createTime) {
        this.createTime = createTime;
    }
}
