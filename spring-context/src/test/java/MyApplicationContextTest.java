import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Package: org.springframework
 * User: 李鹏程
 * Email:
 * Date: 2019-06-17
 * Time: 10:33
 * Description:
 */
public class MyApplicationContextTest {
	public static void main(String[] args){
		ApplicationContext context = new ClassPathXmlApplicationContext("");
	}
}
