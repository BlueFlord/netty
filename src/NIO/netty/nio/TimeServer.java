package netty.nio;

/**
 * Created by schaffer on 15-3-27.
 */
public class TimeServer {
    public static void main(String args[]) {
        int port = 8080;
        MultipleSelectorTimeServer selectorTimeServer = new MultipleSelectorTimeServer(port);
        new Thread(selectorTimeServer).start();
    }

}
