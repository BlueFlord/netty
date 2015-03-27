package netty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by schaffer on 15-3-27.
 */
public class MultipleSelectorTimeServer implements Runnable {

    private Selector selector;  //多路复用选择器
    private ServerSocketChannel serverChannel;  //通信通道
    private volatile boolean stop;

    /**
     * 构造函数,初始化channel
     *
     * @param port
     */
    public MultipleSelectorTimeServer(int port) {
        try {
            stop = false;
            selector = Selector.open();   //打开选择器
            serverChannel = ServerSocketChannel.open();//打开通道
            serverChannel.configureBlocking(false); //配置为非阻塞
            serverChannel.bind(new InetSocketAddress(port), 1024);  //通道进行端口绑定
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);   //监听链接
            System.out.println("the server is listening in the port:" + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        this.stop = true;
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                selector.select(1000);  //selector每秒轮寻一次
                Set<SelectionKey> selectionKeys = selector.selectedKeys();  //监听到的keys
                Iterator<SelectionKey> it = selectionKeys.iterator();
                SelectionKey key = null;
                while (it.hasNext()) {
                    key = it.next();
                    it.remove();    //处理完的keys进行移除
                    handleKey(key);
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
            if (key.isAcceptable()) {    //key的类型1
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();  //返回被选中的key对应的channel
                SocketChannel sc = ssc.accept();
                sc.configureBlocking(false);
                sc.register(selector, SelectionKey.OP_READ);
            }

            if (key.isReadable()) {
                SocketChannel sc = (SocketChannel) key.channel();
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                int readBytes = sc.read(buffer);  //将通道中的内容读取到缓存中
                if (readBytes > 0) {
                    buffer.flip();  //就将buffer的position的位置芝为要读取的位置,limit为当前的位置
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);  //将缓冲中的内容放入到bytes中
                    String body = new String(bytes, "UTF-8");
                    System.out.println("The time server receive order:" + body);
                    String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date(System.currentTimeMillis()).toString() : "bad order";
                    doWrite(sc, currentTime);   //数据写到通道中
                } else {
                    key.cancel();
                    sc.close();
                }
            }
        }
    }

    private void doWrite(SocketChannel channel, String response) throws IOException {
        if (response != null || response.trim().length() > 0) {
            byte[] bytes = response.getBytes();
            ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
            byteBuffer.put(bytes);
            byteBuffer.flip();
            channel.write(byteBuffer); //将缓冲中的内容写到buffer中
        }
    }

}
