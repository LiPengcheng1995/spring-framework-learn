package org.springframework;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * Package: org.springframework
 * User: 李鹏程
 * Email:
 * Date: 2019-05-08
 * Time: 20:57
 * Description:
 */
public class MyTest {
	public static void main(String[] args){
		XmlBeanFactory beanFactory = new XmlBeanFactory(new ClassPathResource(""));
		beanFactory.getBean("");
		beanFactory.autowireBean();
	}
}
