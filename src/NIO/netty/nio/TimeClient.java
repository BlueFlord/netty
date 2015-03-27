package netty.nio;

/**
 * Created by schaffer on 15-3-27.
 */
public class TimeClient {
    public static void main(String args[]) {
        TimeClientHandle handle = new TimeClientHandle("localhost", 8080);
        new Thread(handle).start();
    }
}
