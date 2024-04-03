package base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <big>实体类的基础类</big>
 *
 * @author 13684
 * @date 2024/4/3
 */
@Data
public abstract class BaseEntity implements Serializable {
    // 主键id，使用@TableId注解标识
    @TableId
    private Long id;

    // 创建时间，使用@TableField注解标识，并设置fill为FieldFill.INSERT，表示在插入时填充
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime created;

    // 更新时间，使用@TableField注解标识，并设置fill为FieldFill.INSERT_UPDATE，表示在插入或更新时填充
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updated;
}
