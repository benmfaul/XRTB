package tools;
import java.util.concurrent.CountDownLatch;

import org.redisson.Redisson;
import org.redisson.core.MessageListener;
import org.redisson.core.RTopic;

import com.xrtb.commands.BasicCommand;
import com.xrtb.commands.Echo;

public class RedissonTopicTest {

	public static void main(String args[])throws Exception  {
		new RedissonTopicTest().test();
	}


    public void test() throws InterruptedException {
        final CountDownLatch messageRecieved = new CountDownLatch(2);

        Redisson redisson1 = Redisson.create();
        RTopic<BasicCommand> topic1 = redisson1.getTopic("responses");
        topic1.addListener(new MessageListener<BasicCommand>() {
            @Override
            public void onMessage(BasicCommand msg) {
                System.out.println(msg);
                messageRecieved.countDown();
            }
        });

        Redisson redisson2 = Redisson.create();
        RTopic<BasicCommand> topic2 = redisson2.getTopic("responses");
        topic2.addListener(new MessageListener<BasicCommand>() {
            @Override
            public void onMessage(BasicCommand msg) {
                System.out.println(msg);
                messageRecieved.countDown();
            }
        });
        topic2.publish(new Echo());

        messageRecieved.await();

        redisson1.shutdown();
        redisson2.shutdown();
    }

}

class Message {
	String name;
	public Message() {
		
	}
	
	public Message(String name) {
		this.name = name;
	}
}
