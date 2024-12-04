package com.wxxy.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

/**
 * 留言板
 */
@TableName(value = "message_board")
public class MessageBoard implements Serializable {
    /**
     * 留言板唯一 id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 教师队伍 id
     */
    @TableField(value = "teacherId")
    private Long teacherid;

    /**
     * 留言板内容 json 数据
     */
    @TableField(value = "content")
    private String content;

    /**
     * 创建时间
     */
    @TableField(value = "createTime", fill = FieldFill.INSERT)
    private Date createtime;

    /**
     * 更新时间
     */
    @TableField(value = "updateTime", fill = FieldFill.INSERT_UPDATE)
    private Date updatetime;

    /**
     * 是否删除
     */
    @TableField(value = "isDelete")
    private Byte isdelete;

    private static final long serialVersionUID = 1L;

    public static final String COL_ID = "id";

    public static final String COL_TEACHERID = "teacherId";

    public static final String COL_CONTENT = "content";

    public static final String COL_CREATETIME = "createTime";

    public static final String COL_UPDATETIME = "updateTime";

    public static final String COL_ISDELETE = "isDelete";

    /**
     * 获取留言板唯一 id
     *
     * @return id - 留言板唯一 id
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置留言板唯一 id
     *
     * @param id 留言板唯一 id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取教师队伍 id
     *
     * @return teacherId - 教师队伍 id
     */
    public Long getTeacherid() {
        return teacherid;
    }

    /**
     * 设置教师队伍 id
     *
     * @param teacherid 教师队伍 id
     */
    public void setTeacherid(Long teacherid) {
        this.teacherid = teacherid;
    }

    /**
     * 获取留言板内容 json 数据
     *
     * @return content - 留言板内容 json 数据
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置留言板内容 json 数据
     *
     * @param content 留言板内容 json 数据
     */
    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }

    /**
     * 获取创建时间
     *
     * @return createTime - 创建时间
     */
    public Date getCreatetime() {
        return createtime;
    }

    /**
     * 设置创建时间
     *
     * @param createtime 创建时间
     */
    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    /**
     * 获取更新时间
     *
     * @return updateTime - 更新时间
     */
    public Date getUpdatetime() {
        return updatetime;
    }

    /**
     * 设置更新时间
     *
     * @param updatetime 更新时间
     */
    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    /**
     * 获取是否删除
     *
     * @return isDelete - 是否删除
     */
    public Byte getIsdelete() {
        return isdelete;
    }

    /**
     * 设置是否删除
     *
     * @param isdelete 是否删除
     */
    public void setIsdelete(Byte isdelete) {
        this.isdelete = isdelete;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", teacherid=").append(teacherid);
        sb.append(", content=").append(content);
        sb.append(", createtime=").append(createtime);
        sb.append(", updatetime=").append(updatetime);
        sb.append(", isdelete=").append(isdelete);
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        MessageBoard other = (MessageBoard) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getTeacherid() == null ? other.getTeacherid() == null : this.getTeacherid().equals(other.getTeacherid()))
                && (this.getContent() == null ? other.getContent() == null : this.getContent().equals(other.getContent()))
                && (this.getCreatetime() == null ? other.getCreatetime() == null : this.getCreatetime().equals(other.getCreatetime()))
                && (this.getUpdatetime() == null ? other.getUpdatetime() == null : this.getUpdatetime().equals(other.getUpdatetime()))
                && (this.getIsdelete() == null ? other.getIsdelete() == null : this.getIsdelete().equals(other.getIsdelete()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTeacherid() == null) ? 0 : getTeacherid().hashCode());
        result = prime * result + ((getContent() == null) ? 0 : getContent().hashCode());
        result = prime * result + ((getCreatetime() == null) ? 0 : getCreatetime().hashCode());
        result = prime * result + ((getUpdatetime() == null) ? 0 : getUpdatetime().hashCode());
        result = prime * result + ((getIsdelete() == null) ? 0 : getIsdelete().hashCode());
        return result;
    }
}