package netty.bio;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author schaffer
 * @version 1.0
 * @data 2015.3.27
 */
public class TimeServer {
    public static void main(String args[]) {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        ServerSocket server = null;
        try {
            System.out.println("开始监听:");
            server = new ServerSocket(port);
            Socket socket = null;
            //TODO Timeerver的改进,添加线程池
            TimeServerHandlerExecutePool timeServerHandlerExecutePool = new TimeServerHandlerExecutePool(50, 1000);
            while (true) {
                socket = server.accept();
                //TODO 线程池调用
                timeServerHandlerExecutePool.execute(new TimeServerHandler(socket));
                //new Thread(new netty.BIO.TimeServerHandler(socket)).start();  //每个用户启动一个线程进行通信
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (server != null) {
                    System.out.println("The Time server is closed");
                    server.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}