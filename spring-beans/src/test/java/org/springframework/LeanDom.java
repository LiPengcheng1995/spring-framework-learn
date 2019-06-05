package org.springframework;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/**
 * Package: org.springframework
 * User: 李鹏程
 * Email: lipengcheng3@jd.com
 * Date: 2019-06-05
 * Time: 15:07
 * Description:
 */
public class LeanDom {

	public static void main(String arge[]) {

		long lasting = System.currentTimeMillis();

		try {
			File f = new File("log4j2-test.xml");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(f);
			Element element =doc.getDocumentElement();
			for (int i=0;i<element.getChildNodes().getLength();i++){
				System.out.println(element.getChildNodes().item(i).getPrefix());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
