package tech.insight.mybatis;

import lombok.Data;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
@Data
@Table(tableName = "user")
public class User {
    private Integer id;
    private String name;
    private Integer age;
}
