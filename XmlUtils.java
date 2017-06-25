import android.support.annotation.NonNull;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by Administrator on 2017-06-22.
 * 负责xml文件到类的双向转换
 */

public final class XmlUtls {
    private static String Ecoding = "UTF-8";
    private static String divideChar = "_";
    private static String ERRORTAG="error";
    private static XmlPullParser xpp;
    private static XmlSerializer xmlSerializer;
    private static TreeNode root;
    private XmlUtls()
    {

    }
    /**
     * 设置分隔符,xml文件的节点以此为标志分层，xml文件对应的类中属性名也根据这个命名。如userdata节点下的net节点下的name属性就被命名为userdata_net_name
     *
     * @param divide
     */
    public static void setDivideChar(String divide) {
        divideChar = divide;
    }

    /**
     * 设置xml文件编码
     *
     * @param ecoding
     */
    public static void setEcoding(String ecoding) {
        Ecoding = ecoding;
    }

    /**
     * 解析xml文件为对应的类,className为对应的类全路径名称,inputStream是文件的输入流。
     *
     * @param className
     * @param inputStream
     * @return
     * @throws ClassNotFoundException
     * @throws XmlPullParserException
     * @throws IOException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static Object pull(String className, InputStream inputStream) {
        Object o =null;
        try {
            Class css = Class.forName(className);
            o = css.newInstance();
            Field[] fields = css.getFields();
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            xpp = factory.newPullParser();
            xpp.setInput(inputStream, Ecoding);
            int eventType = xpp.getEventType();
            String propertyName = "";
            String foraheadpropertyName="";
            String arrayNodeTag="";
            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_DOCUMENT) {

                } else if (eventType == XmlPullParser.START_TAG) {
                    foraheadpropertyName = propertyName;


                    if (propertyName.equals("")) {
                        propertyName += xpp.getName();
                    } else {
                        propertyName += divideChar + xpp.getName();
                    }
                    Log.v("ssss","现在"+propertyName);
                    dealArrayResult(propertyName,arrayNodeTag,o);
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (propertyName.contains(divideChar)) {
                        propertyName = propertyName.replace(divideChar + xpp.getName(), "");
                    } else
                        propertyName = "";
                } else if (eventType == XmlPullParser.TEXT) {
                    if(xpp.getText().trim().equals("")) {

                    }
                    else {
                        for (Field f : fields) {
                            if (f.getName().equals(propertyName) && !f.getType().isArray()) {
                                f.set(o, xpp.getText());

                            } else if (f.getName().equals(foraheadpropertyName)) {
                                Log.v("ssss",foraheadpropertyName);
                                String[] middle = propertyName.split(divideChar);
                                arrayNodeTag = middle[middle.length - 2];
                                String arrayNodePropertyName = middle[middle.length - 1];

                                dealArrayNode(f, o, arrayNodeTag, arrayNodePropertyName, xpp);

                                propertyName = foraheadpropertyName.replace("_" + arrayNodeTag, "");
                                Log.v("ssss", "dealArrayNode后" + propertyName);
                                break;
                            }

                        }
                    }
                }

                eventType = xpp.next();
                Log.v("ssss",eventType+"");
            }

            dealArrayResult(propertyName,arrayNodeTag,o);
           xpp = null;
            return o;
        }
         catch (IOException e) {
            e.printStackTrace();
             Log.e(ERRORTAG,"输入输出流异常");
        } catch (InstantiationException e) {
            e.printStackTrace();
            Log.e(ERRORTAG,"反射对象实例化异常");
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            Log.e(ERRORTAG,"创建XmlPullParser异常");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e(ERRORTAG,"XmlPullParser调用next异常");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Log.e(ERRORTAG,"className非法");
        }
       return  o;
    }

    /**
     * 处理数组字段赋值
     * @param propertyName
     * @param arrayNodeTag
     * @param o
     * @throws IllegalAccessException
     */
    private static void dealArrayResult(String propertyName, String arrayNodeTag, Object o) throws IllegalAccessException {
        //如果只有1个数组，不会经过startTag赋值,mArrayListsize>0所以要再次处理。
        if(mArrayList.size()>0)
        {
            //数组节点遍历已结束
            if(!propertyName.contains(arrayNodeTag))
            {
                arrayField.setAccessible(true);
                Object oa =Array.newInstance(arrayField.getType().getComponentType(),mArrayList.size());
                for(int i=0;i<mArrayList.size();i++)
                    Array.set(oa,i,mArrayList.get(i));
                arrayField.set(o,oa);
                mArrayList.clear();
            }
        }
    }

    private static ArrayList mArrayList = new ArrayList();
    private static Field arrayField;

    /**
     * 处理类中数组类型的字段中的元素赋值
     * @param f
     * @param o
     * @param arrayNodeTag
     * @param arrayNodePropertyName
     * @param xpp
     */
    private static void dealArrayNode(Field f, Object o, String arrayNodeTag,String arrayNodePropertyName,XmlPullParser xpp) {
        Class c = f.getType();
        Class arrayType = null;
        Object oo=null;
        String propertyName=arrayNodePropertyName;
        String foraheadpropertyName="";
        String arryNodeTagInner="";
        if(c.isArray())
        {
           arrayType =  c.getComponentType();
            arrayField = f;
        }
        try {
           oo = arrayType.newInstance();
        while(true) {

            int eventType = 0;
            try {
                eventType = xpp.getEventType();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
             Log.v("ssss","zi"+xpp.getName());
            if (eventType == XmlPullParser.START_TAG) {
                foraheadpropertyName = propertyName;



                if (propertyName.equals("")) {
                    propertyName += xpp.getName();
                } else {
                    propertyName += divideChar + xpp.getName();
                }
                //数组节点遍历已结束
                if(!propertyName.contains(arrayNodeTag))
                {
                    if(mArrayList.size()>0) {
                        arrayField.set(o,mArrayList.toArray());
                        mArrayList.clear();
                    }
                }
            } else if (eventType == XmlPullParser.END_TAG) {

                if (xpp.getName().equals(arrayNodeTag)) {

                    mArrayList.add(oo);

                    break;
                }
                if (propertyName.contains(divideChar)) {
                    propertyName = propertyName.replace(divideChar + xpp.getName(), "");
                } else
                    propertyName = "";
            } else if (eventType == XmlPullParser.TEXT) {
                     if(xpp.getText().trim().equals("")) {
                     }
                     else {
                         Field[] fields = arrayType.getFields();
                         for (Field ff : fields) {

                             if (ff.getName().equals(propertyName) && !ff.getType().isArray()) {

                                 ff.set(oo, xpp.getText());
                             } else if (f.getName().equals(foraheadpropertyName)) {
                                 String[] middle = propertyName.split(divideChar);
                                 arryNodeTagInner = middle[middle.length - 2];
                                 arrayNodePropertyName = middle[middle.length - 1];
                                 dealArrayNode(f, o, arryNodeTagInner, arrayNodePropertyName, xpp);
                                 propertyName = foraheadpropertyName.replace("_" + arryNodeTagInner, "");
                                 break;
                             }
                         }
                     }
            }
            eventType = xpp.next();
        }
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    /**
     * 将类文件序列化为xml文件
     * @param o
     * @param savePath
     * @return
     */
    public static boolean serialize(Object o,String savePath)
    {
        promitPathOK(savePath);
        File file = new File(savePath);
        if(!file.exists())
        {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();

            }
        }

        Class css =o.getClass();
        Field[]fields = css.getFields();
        XmlPullParserFactory factory = null;
        try {
           TreeNode root = buildTreeNode(fields,o);

            FileOutputStream fileOutputStream = new FileOutputStream(file);

            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            xmlSerializer = factory.newSerializer();
            xmlSerializer.setOutput(fileOutputStream,Ecoding);
            xmlSerializer.startDocument(Ecoding,true);
            Stack stack = new Stack();
            TreeNode t = root;
            stack.push(t);

            xmlSerializer.startTag("",t.name);
            TreeNode tss = (TreeNode) t.mTreeNodes.get(0);
//            for (Object s:tss.mTreeNodes
//                 ) {
//                Main2Activity.call("父"+tss.name+((TreeNode)s).name);
//            }
            buildXmlNode(t,stack);

            xmlSerializer.endDocument();
            xmlSerializer = null;
            return  true;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            return false;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 根据树结构构造xml文件
     * @param t
     * @param stack
     * @throws IOException
     */
    private static void buildXmlNode(TreeNode t, Stack stack) throws IOException {
    if(t.mTreeNodes.size()>0) {
        for (Object item:t.mTreeNodes
                ) {
           TreeNode treeNode = (TreeNode) item;
            stack.push(treeNode);

            xmlSerializer.startTag("",treeNode.name);
            if(treeNode.value!=null)
                xmlSerializer.text(treeNode.value);
            buildXmlNode(treeNode,stack);
        }
        //子节点遍历完，结束父节点。
        TreeNode t1= (TreeNode) stack.pop();
        xmlSerializer.endTag("", t1.name);
    }
    else {
        TreeNode t1= (TreeNode) stack.pop();
        xmlSerializer.endTag("", t1.name);
    }
}

    /**
     * 将类转换成树结构
     * @param fields
     * @param o
     * @return
     * @throws IllegalAccessException
     */
    private static TreeNode buildTreeNode(Field[] fields,Object o) throws IllegalAccessException {
        root = null;
        TreeNode treeNode = null;

        for(Field f:fields)
        {
            String fieldName = f.getName();
          if(f.getType().isArray())
          {
              Class css =f.getType().getComponentType();
              Field []fieldss = css.getFields();
              Object array = f.get(o);
              Object oiterator;
              int length =Array.getLength(array);
              for(int i=0;i<length;i++) {
                 oiterator = Array.get(array,i);
                  for(Field ff:fieldss)
                  {
                      buildTreeArrayNodePartial(oiterator,ff,fieldName+divideChar+ff.getName());
                  }
              }
          }
          else {
             buildTreeNodePartial(o,  f, fieldName);
          }
        }
        return root;
    }

    /**
     * 对于类中数组需要特殊处理出现重复的节点。构建数组节点子树
     * @param o
     * @param f
     * @param fieldName
     * @throws IllegalAccessException
     */
    private static void buildTreeArrayNodePartial(Object o,Field f,String fieldName) throws IllegalAccessException {
        TreeNode treeNode;

        String[] tagNames = fieldName.split(divideChar);
        if (root == null)
            root = new TreeNode(null, tagNames[0]);
        TreeNode iterator = root;

        for (int i = 1; i < tagNames.length; i++) {
            if(i==tagNames.length-2)
            {
                treeNode = new TreeNode(null,tagNames[i]);
                iterator.add(treeNode);
                iterator = treeNode;
                continue;
            }
            boolean once=false;
            for(Object os:iterator.mTreeNodes) {
                if(((TreeNode)(os)).name.equals(tagNames[i])&&((TreeNode)(os)).value==null)
                {
                    once = true;
                    iterator = ((TreeNode)(os));
                    break;
                }
            }
            if(!once)
            {
                if(i==tagNames.length-1)
                    treeNode = new TreeNode((String) f.get(o),tagNames[i]);
                else
                {
                    treeNode = new TreeNode(null,tagNames[i]);
                }

                iterator.add(treeNode);
                iterator = treeNode;
            }
        }
    }

    /**
     * 建立树的非数组节点
     * @param o
     * @param f
     * @param fieldName
     * @throws IllegalAccessException
     */
    @NonNull
    private static void buildTreeNodePartial(Object o,  Field f, String fieldName) throws IllegalAccessException {
        TreeNode treeNode;

        String[] tagNames = fieldName.split(divideChar);
        if (root == null)
            root = new TreeNode(null, tagNames[0]);
        TreeNode iterator = root;

        for (int i = 1; i < tagNames.length; i++) {
                boolean once=false;
            for(Object os:iterator.mTreeNodes) {
                if(((TreeNode)(os)).name.equals(tagNames[i])&&((TreeNode)(os)).value==null)
                {
                  once = true;
                    iterator = ((TreeNode)(os));
                    break;
                }
            }
            if(!once)
            {
                if(i==tagNames.length-1)
                    treeNode = new TreeNode((String) f.get(o),tagNames[i]);
                else
                {
                    treeNode = new TreeNode(null,tagNames[i]);
                }

                iterator.add(treeNode);
                iterator = treeNode;
            }

        }

    }

    /**
     * 保证序列化路径可用
     * @param savePath
     */
    private static void promitPathOK(String savePath)
    {
        String []lujing = savePath.split("\\/");
        File file =null;
        StringBuilder sb= new StringBuilder();
        sb.append(lujing[0]);
        for(int i=1;i<lujing.length-1;i++)
        {
            sb.append("/");
            sb.append(lujing[i]);
            file = new File(sb.toString());

            if(!file.exists())
            {
               file.mkdirs();
            }
        }
    }

    /**
     * 树节点结构
     */
    private static class TreeNode{
        public String value;
        public String name;
        public ArrayList  mTreeNodes;
        public TreeNode parentNode=null;
        public TreeNode(String value,String name)
        {
            this.value = value;
            this.name = name;
            mTreeNodes = new ArrayList();
        }
        public void add(TreeNode treeNode)
        {
            mTreeNodes.add(treeNode);

            treeNode.parentNode = this;
        }
    }
    }

