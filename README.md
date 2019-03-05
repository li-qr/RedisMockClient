# RedisMockClient

解析Redis协议字符串.

使用方法：
```$shell
javac CommandParser.java
cd ../../../
java net.leezw.RedisMockClient.CommandParser '*5\r\n:1\r\n:2\r\n:3\r\n:4\r\n$6\r\nfoobar\r\n'
```

参考文档：
http://redisdoc.com/topic/protocol.htmlç
