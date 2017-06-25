# Android-XmlFactory
一个解析所有的xml文件通用的工具类。
使用说明:
建立与Xml文件对应的数据类，字段名称为完整的标签内容名称。层次之间用分隔符隔开，默认是“_”,可以自行设置，
如userdata_server_uid表示userdata标签下的server标签下的uid内容
调用XmlUtils的Pull方法完成解析。同理调用serialize方法完成序列化。
