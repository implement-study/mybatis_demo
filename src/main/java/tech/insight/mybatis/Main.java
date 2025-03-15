package tech.insight.mybatis;


/**
 * @author gongxuanzhangmelt@gmail.com
 **/
public class Main {

    public static void main(String[] args) throws Exception {
        MySqlSessionFactory mySqlSessionFactory = new MySqlSessionFactory();
        UserMapper mapper = mySqlSessionFactory.getMapper(UserMapper.class);
        User user = mapper.selectById(2);
        System.out.println(user);
        System.out.println(mapper.selectByName("jerry"));
        System.out.println(mapper.selectByNameAndAge("jerry",10));
    }


}
