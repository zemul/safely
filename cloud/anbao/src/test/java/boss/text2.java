package boss;

import java.io.IOException;


import com.anbao.controllor.WebSocketControllor;
import org.junit.Test;

public class text2 {

	
	@Test
    public void test() throws IOException {

		System.out.println(WebSocketControllor.clients.size());
		System.out.println(WebSocketControllor.clients.get("AAABBBCCC"));
		new WebSocketControllor().singleSend("aaa","AAABBBCCC");


	}
}
