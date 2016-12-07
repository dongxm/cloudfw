package com.dxmcloudfw.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 *
 * @author dongxm
 */
public class XmlTempletUtil {

    public static String createXML(Map<String, String> map) {//map里存的是你要构建的元素

        String strXml = null;
        if (map.size() > 0) {
            try {
                //1,构造空的Document
                Document doc = DocumentHelper.createDocument();
                //2,构造根元素
                Element rootElmt = doc.addElement("root");
                //3,遍历构造子元素
                Element parentsElmt = rootElmt.addElement("QueryCondition");
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    Element childElmt = parentsElmt.addElement(entry.getKey());
                    childElmt.addAttribute("des", "vt测试");
                    childElmt.addCDATA(entry.getValue());
                    
                    Element childElmt2 = parentsElmt.addElement(entry.getKey());
                    childElmt2.addAttribute("des", "vt测试");
                    childElmt2.addCDATA(entry.getValue());
                }
                doc.setXMLEncoding("utf-8");
                strXml = doc.asXML();
                System.out.println(strXml);

                System.out.println(" - - - - - -  begin parse xml ");
                parserXml(doc);

                //  outputXML(doc);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return strXml;
    }

    public static void parserXml(Document document) {

        try {
            Element employees = document.getRootElement();
            for (Iterator i = employees.elementIterator(); i.hasNext();) {
                Element employee = (Element) i.next();
                System.out.println("  e : value : "+employee.getText());
                
                for (Iterator j = employee.elementIterator(); j.hasNext();) {
                    Element node = (Element) j.next();
                    System.out.println(node.getName() + ":" + node.getText());
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("dom4j parserXml");
    }

    public static void main(String[] dd) {

        Map data = new HashMap();
        data.put("a", "a1");
        createXML(data);
        
        Integer.toBinaryString(150);
        
        System.out.println("  int 150 to b : "+Integer.toBinaryString(-150));
        

    }

}
