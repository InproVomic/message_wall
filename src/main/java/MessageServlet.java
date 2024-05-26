import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


class Message{
    public String from;
    public String to;
    public String message;

    @Override
    public String toString() {
        return "Message{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}

@WebServlet("/message")
public class MessageServlet extends HttpServlet {
    private ObjectMapper objectMapper = new ObjectMapper();

    private DataSource dataSource = new MysqlDataSource();

    @Override
    public void init() throws ServletException {
        ((MysqlDataSource)dataSource).setURL("jdbc:mysql://127.0.0.1:3306/message_wall?characterEncoding=utf8&useSSL=false");
        ((MysqlDataSource)dataSource).setUser("root");
        ((MysqlDataSource)dataSource).setPassword("15016246620");
    }

    private List<Message> load() throws SQLException {
        //1.建立连接
        Connection connection = (Connection) dataSource.getConnection();

        //2.构造sql
        String sql = "select * from message";
        PreparedStatement statement = connection.prepareStatement(sql);

        //3.执行sql
        ResultSet resultSet = statement.executeQuery();

        //4.遍历结果集
        List<Message> messageList = new ArrayList<>();
        while(resultSet.next()){
            Message message = new Message();
            message.from = resultSet.getString("from");
            message.to = resultSet.getString("to");
            message.message = resultSet.getString("message");
            messageList.add(message);
        }

        //5.回收资源
        connection.close();
        statement.close();
        resultSet.close();

        //6.返回结果
        return messageList;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(200);
        resp.setContentType("application/json; charset=utf8");
        //查询数据库
        List<Message> messageList = null;
        System.out.println("查询啦！");
        try {
            messageList = load();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        String respJson = objectMapper.writeValueAsString(messageList);//转成json格式
        resp.getWriter().write(respJson);
    }



    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //1.拿到json数据并转化成Message类
        Message message = objectMapper.readValue(req.getInputStream(),Message.class);
        System.out.println(message);

        //2.保存数据
        try {
            save(message);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        //3.返回响应
        resp.setStatus(200);
        resp.getWriter().write("ok");
    }

    private void save(Message message) throws SQLException {
        //1.与数据库建立连接
        Connection connection = (Connection) dataSource.getConnection();

        //2.构造sql
        String sql = "insert into message values(?,?,?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1,message.from);
        statement.setString(2,message.to);
        statement.setString(3,message.message);

        //3.执行sql
        statement.executeUpdate();

        //4.回收资源
        connection.close();
        statement.close();
    }
}
