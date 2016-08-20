package util;

import java.sql.*;  

  
public class Jdbc_Util {  
    private Connection conn =null;  
    private Statement statement =null;  
    private ResultSet result=null;  
    private boolean b;  
    private int a;  
      
    public Jdbc_Util(){  
        try {  
            Class.forName("com.mysql.jdbc.Driver");//加载驱动  
  
            conn = (Connection) DriverManager.getConnection(  
                        "jdbc:mysql://127.0.0.1:3306/movie_recommendation?characterEncoding=utf8",   
                        "root", "");//获得连接  
                  
            System.out.println("加载驱动成功！！");  
        } catch (ClassNotFoundException e) {  
            System.out.println("加载驱动失败！！");  
            e.printStackTrace();  
        }catch (SQLException e) {  
            System.out.println("获取数据库连接失败！！");  
            e.printStackTrace();  
        }  
    }  
    //增加数据  
    public int add(String sql){  
        try {  
            statement =conn.createStatement();  
            a=statement.executeUpdate(sql);       
        } catch (SQLException e) {        
         e.printStackTrace();  
        }  
        return a;  
    }  
  
    //删除数据  
    public int delete(String sql){  
        try {  
            statement =conn.createStatement();  
            a=statement.executeUpdate(sql);  
        } catch (SQLException e) {  
            e.printStackTrace();  
        }  
        return a;  
    }  
      
    //查询数据  
    public ResultSet select(String sql){  
        try {  
            Statement statement =conn.createStatement(  
                    ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);  
            result=statement.executeQuery(sql);  
        } catch (SQLException e) {  
            e.printStackTrace();  
        }  
        return result;        
    }  
      
    //改数据  
    public int update(String sql){  
        try {  
            statement =conn.createStatement();  
            a=statement.executeUpdate(sql);       
        } catch (SQLException e) {        
         e.printStackTrace();  
        }  
        return a;  
    }  
      
    //关闭资源  
    public boolean close(){  
        try {  
            result.close();  
            statement.close();  
            conn.close();  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return b;  
    }  
}  
