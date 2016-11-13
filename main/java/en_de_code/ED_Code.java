package en_de_code;

import java.io.UnsupportedEncodingException;

/**
 * ED_Code 不同字符编码转换器
 * @author zhangxinming
 * @version 1.0.0
 *
 */
public class ED_Code {
	
	public ED_Code() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * ISO_To_UTF8 将iso编码转成utf-8
	 * @param iso_s
	 * @return
	 */
	public String ISO_To_UTF8(String iso_s){
		String str_UTF = null;
		if (iso_s == null) {
			return null;
		}
		try {
			str_UTF = new String(iso_s.getBytes("iso-8859-1"),"utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//将
		return str_UTF;
	}
	
	/**
	 * GBK_To_UTF8 将gbk编码转成utf-8
	 * @param gbk_s
	 * @return
	 */
	public String GBK_To_UTF8(String gbk_s){
		String str_UTF = null;
		String str_GBK = null;
		byte[] b_utf = null;
		try {
			b_utf = gbk_s.getBytes("utf-8");
			str_UTF = new String(b_utf,"utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str_UTF;
	}
	
	/**
	 * printHexString 查看字符串的字节编码
	 * @category debug 
	 * @param b
	 */
	public static void printHexString( byte[] b){
        for (int i = 0; i < b.length; i++)
        {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1)
            {
                hex = '0' + hex;
            }
            System.out.print(hex.toUpperCase() + " ");
        }
        System.out.println("");
    }
	
	public static int ByteArray_Act_Len( byte[] b){
		int len = 0;
		
		for (int i = 0; i < b.length; i++) {
			if (b[i] != 0x00) {
				len++;
			}
			else
				break;
		}
		
		return len;
	}
}
