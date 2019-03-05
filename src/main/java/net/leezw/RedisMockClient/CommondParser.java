package net.leezw.RedisMockClient;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by 李泉 on 2019-03-05.
 */
public class CommondParser {

  private static final char STATUS = '+';
  private static final char ERROR = '-';
  private static final char INTEGER = ':';
  private static final char BULK = '$';
  private static final char MULTI = '*';

  private static final char CR = '\r';
  private static final char LF = '\n';

  private static StringBuilder commond= null;
  private static int index = 0;


  public static void main(String ... args) {
      if(args.length<1) {
        throw new IllegalArgumentException("需要1个参数");
      }
      commond = new StringBuilder(args[0]
          .replaceAll("\\\\r","\r")
          .replaceAll("\\\\n","\n"));
      System.out.println(process());
  }

  private static Object process(){
    char f = commond.charAt(index++);
    if(f == STATUS){
      return processStatusReply();
    }else if(f == ERROR){
      return processErrorReply();
    }else if(f == INTEGER){
      return processIntegerReply();
    }else if(f == BULK){
      return processBulkReply();
    }else if(f == MULTI){
      return processMultiReply();
    }else {
      throw new RuntimeException("未知应答："+commond.toString());
    }
  }

  private static Object processMultiReply() {
    int length = processIntegerReply();
    if(length<0){
      return null;
    }
    List<Object> reply = new ArrayList<>(length);
    for(int i=0;i<length;i++){
      reply.add(process());
    }
    return reply;
  }

  private static Object processBulkReply() {
    int length =  processIntegerReply();
    if(length<0){
      return null;
    }
    if (index + length >= commond.length()) {
      throw new RuntimeException("格式错误");
    }
    String reply = commond.substring(index, (index + length));
    index = index + length;
    if (index >= commond.length()) {
      throw new RuntimeException("格式错误");
    }
    char c = commond.charAt(index++);
    if (c == CR) {
      if (index >= commond.length()
          || commond.charAt(index++) != LF) {
        throw new RuntimeException("格式错误");
      }
    }
    return reply;
  }

  private static Integer processIntegerReply() {
    if (index >= commond.length()) {
      throw new RuntimeException("格式错误");
    }
    final boolean isNeg = commond.charAt(index) == '-';
    if (isNeg) {
      ++index;
    }

    int reply = 0;
    boolean isNull = true;
    while (true) {
      if (index >= commond.length()) {
        throw new RuntimeException("格式错误");
      }
      char c = commond.charAt(index++);
      if (c == CR) {
        if (index >= commond.length()
            || commond.charAt(index++) != LF) {
          throw new RuntimeException("格式错误");
        } else {
          break;
        }
      }
      isNull = false;
      reply = (reply * 10) + (c - '0');
    }
    return isNull ? null : (isNeg ? -reply : reply);
  }

  private static Object processErrorReply() {
    int offseta = commond.substring(index-1).indexOf(" ");
    int offsetb = commond.substring(index-1).indexOf("\r\n");
    String reply = "错误类型：" + commond.substring(index,offseta)
        +" 详情："+commond.substring(offseta+1,offsetb);
    index = offsetb+2;
    return reply;
  }

  private static Object processStatusReply() {
    int offset = index;
    while (true){
      if(offset>=commond.length()){
        throw new RuntimeException("格式错误");
      }
      if(commond.charAt(offset++)==CR){
        if(offset>=commond.length()
        || commond.charAt(offset++)!=LF){
          throw new RuntimeException("格式错误");
        }else {
          break;
        }
      }
    }
    return commond.substring(index,offset-2);
  }
}
