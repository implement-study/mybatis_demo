package tech.insight.mybatis;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
public class MySqlSessionFactory {

    private static final String JDBCURL = "jdbc:mysql://localhost:3306/mybatis_db";
    private static final String DBUSER = "root";
    private static final String PASSWORD = "123456";


    @SuppressWarnings("all")
    public <T> T getMapper(Class<T> mapperClass) {
        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{mapperClass},
                new MapperInvocationHandler());
    }

    static class MapperInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().startsWith("select")) {
                return invokeSelect(proxy, method, args);
            }
            return null;
        }

        private Object invokeSelect(Object proxy, Method method, Object[] args) {
            String sql = createSelectSql(method);
            System.out.println(sql);
            try (Connection conn = DriverManager.getConnection(JDBCURL, DBUSER, PASSWORD);
                 PreparedStatement statement = conn.prepareStatement(sql)) {
                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    if (arg instanceof Integer) {
                        statement.setInt(i + 1, (int) arg);
                    } else if (arg instanceof String) {
                        statement.setString(i + 1, arg.toString());
                    }
                }
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    return parseResult(rs, method.getReturnType());
                }
            } catch (Exception e) {
            }
            return null;
        }

        private Object parseResult(ResultSet rs, Class<?> returnType) throws Exception {
            Constructor<?> constructor = returnType.getConstructor();
            Object result = constructor.newInstance();
            Field[] declaredFields = returnType.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                Object column = null;
                String name = declaredField.getName();
                if (declaredField.getType() == String.class) {
                    column = rs.getString(name);
                } else if (declaredField.getType() == Integer.class) {
                    column = rs.getInt(name);
                }
                declaredField.setAccessible(true);
                declaredField.set(result, column);
            }
            return result;
        }

        private String createSelectSql(Method method) {
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("select ");
            List<String> selectCols = getSelectCols(method.getReturnType());
            sqlBuilder.append(String.join(",", selectCols));
            sqlBuilder.append(" from ");
            String tableName = getSelectTableName(method.getReturnType());
            sqlBuilder.append(tableName);
            sqlBuilder.append(" where ");
            //  select * from user where id = ?
            String where = getSelectWhere(method);
            sqlBuilder.append(where);
            return sqlBuilder.toString();
        }

        private String getSelectWhere(Method method) {
            return Arrays.stream(method.getParameters())
                    .map((parameter) -> {
                        Param param = parameter.getAnnotation(Param.class);
                        String column = param.value();
                        return column + " = ?";
                    }).collect(Collectors.joining(" and "));
        }

        private String getSelectTableName(Class<?> returnType) {
            Table table = returnType.getAnnotation(Table.class);
            if (table == null) {
                throw new RuntimeException("返回值无法确定查询表");
            }
            return table.tableName();
        }

        private List<String> getSelectCols(Class<?> returnType) {
            Field[] declaredFields = returnType.getDeclaredFields();
            return Arrays.stream(declaredFields).map(Field::getName).toList();
        }

    }

}
