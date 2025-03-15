package tech.insight.mybatis;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
public interface UserMapper {
    
    User selectById(@Param("id") int id);
    
    User selectByName(@Param("name") String name);
    
    User selectByNameAndAge(@Param("name") String name,@Param("age") int age);
    
}
