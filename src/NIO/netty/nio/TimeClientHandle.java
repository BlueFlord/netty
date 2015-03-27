package netty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by schaffer on 15-3-27.
 */
public class TimeClientHandle implements Runnable {

    private String host;
    private int port;
    private Selector selector;
    private SocketChannel socketChannel;
    private volatile boolean stop;

    public TimeClientHandle(String host, int port) {
        this.host = (host == null) ? "localhost" : host;
        this.port = port;
        try {
            this.selector = Selector.open();
            this.socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            doConnect();        //进行链接
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (!stop) {
            try {
                selector.select(1000);  //selector每秒轮寻一次
                Set<SelectionKey> selectionKeys = selector.selectedKeys();  //监听到的keys
                Iterator<SelectionKey> it = selectionKeys.iterator();
                SelectionKey key = null;
                while (it.hasNext()) {
                    key = it.next();
                    it.remove();    //处理完的keys进行移除
                    try {
                        handleKey(key);
                    } catch (IOException e) {
                        e.printStackTrace();
                        if (key != null) {
                            key.cancel();
                        }
                        if (key.channel() != null)
                            key.channel().close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();

            }
        }

        if (selector != null) {    //所有的key处理完成后,关闭掉多路复用器,注册在其上的通道channel等都会被关闭
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleKey(SelectionKey key) throws IOException {
        if (key.isValid()) { //判断key是否有效
            SocketChannel sc = (SocketChannel) key.channel();
            if (key.isConnectable()) {
                if (sc.finishConnect()) {
                    sc.register(selector, SelectionKey.OP_READ);
                    doWrite(sc, "helloworld");
                } else
                    //System.exit(1);
                    doConnect();    //重试链接
            }

            if (key.isReadable()) {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                int readBytes = sc.read(buffer);  //将通道中的内容读取到缓存中
                if (readBytes > 0) {
                    buffer.flip();  //就将buffer的position的位置芝为要读取的位置,limit为当前的位置
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);  //将缓冲中的内容放入到bytes中
                    String body = new String(bytes, "UTF-8");
                    //System.out.println("The time server receive order:" + body);
                    //String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date(System.currentTimeMillis()).toString() : "bad order";
                    //doWrite(sc, currentTime);
                    this.stop = true;
                } else {
                    key.cancel();
                    sc.close();
                }
            }
        }
    }

    private void doConnect() throws IOException {
        if (socketChannel.connect(new InetSocketAddress(host, port))) {   //链接成功
            socketChannel.register(selector, SelectionKey.OP_READ);
            doWrite(socketChannel, "helloworld");
        } else {
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
        }
    }

    private void doWrite(SocketChannel channel, String response) throws IOException {
        if (response == null || response.length() == 0)
            throw new IllegalArgumentException("argumentException");
        byte[] bytes = response.getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
        byteBuffer.put(bytes);
        byteBuffer.flip();
        channel.write(byteBuffer); //将缓冲中的内容写到buffer中

    }
}
