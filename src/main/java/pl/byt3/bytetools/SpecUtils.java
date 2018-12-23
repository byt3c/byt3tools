/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.byt3.bytetools;

/**
 *
 * @author Tantal
 */
public class SpecUtils {

    public static int[] backTempB20(float tem) {
        double te;
        int thib, tlob;
        te = Math.floor(tem);
        thib = (int) te;
        tlob = (int) Math.round((tem - te) * 16);
        int dint0 = tlob | ((thib & 0x0F) << 4);
        int dint1 = (thib & 0xF0) >>> 4;
        int[] res = new int[2];
        res[0] = dint0;
        res[1] = dint1;
        return res;
    }

    public static double tempB20(int hib, int lob) { // dla DS18B20
        double ter, tem;
        int thib, tlob;
        tlob = lob;
        tlob = tlob & 0xF0;
        tlob = tlob >>> 4;
        thib = hib;
        thib = thib & 0x07;
        thib = thib << 4;
        thib = thib | tlob;
        ter = thib;
        tlob = lob & 0x0F;
        ter = ter + tlob * 0.0625;
        return (ter);
    }

    public static String toHex(byte x) {
        String str = Integer.toHexString(DataUtils.byteToInt(x)).toUpperCase();
        if ((str.length() % 2) > 0) {
            str = '0' + str;
        }
        return (str);
    }

    public static String toDHex(byte h, byte l) {
        return (SpecUtils.toHex(h) + SpecUtils.toHex(l));
    }

    public static String arrToStr(int[] tab) {
        String st = "";
        int i, len;
        len = tab.length;
        for (i = 0; i < len; i++) {
            st = st + (char) (tab[i]);
        }
        return (st);
    }

    public static int[] strToArr(String st) {
        int i, len;
        len = st.length();
        int[] ar = new int[len];
        for (i = 0; i < len; i++) {
            ar[i] = st.charAt(i);
        }
        return (ar);

    }

    public static String toBinary(int li) {
        String st;
        st = Integer.toBinaryString(li);
        while (st.length() < 8) {
            st = "0" + st;
        }
        return (st);
    }

}
