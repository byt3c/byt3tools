/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.byt3.bytetools;

import com.sun.org.apache.xerces.internal.util.XMLChar;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Iterator;
import java.nio.ByteBuffer;

/**
 *
 * @author byt3
 */
public class TransportClass {

    private byte[] buf = null;
    public String name = "";
    private int size = 0;
    public int position = 0;
    private int chunk = 70;
    public int locked = 0;
    public String LocalName = "";
    public String encoding = "UTF-8";
    public static final int MASK_VALID = 0x01;
    private static final byte[] CHARS = new byte[1 << 16];

    public TransportClass Append(char token) {
        if (buf != null) {
            setLength(1 + size);
            buf[size - 1] = (byte) token;
        }
        return this;
    }

    public void AppendShortString(String s) {
        byte[] bx = s.getBytes();
        byte b = (byte) bx.length;
        Append(b);
        Append(bx);
    }

    public void AppendShortString(TransportClass s) {
        byte b = (byte) s.Length();
        Append(b);
        Append(s);
    }

    public TransportClass extract(int offset, int count) {
        if (offset + count > size) {
            return null;
        } else {
            TransportClass t = new TransportClass();
            t.ensureCapacity(count);
            for (int i = offset; i < offset + count; i++) {
                t.Append(buf[i]);
            }
            return t;
        }
    }

    public TransportClass Part(int offset, char delimiter) {
        return Part(offset, (byte) delimiter);
    }

    public void ensureCapacity(int length) {
        if (buf != null) {
            int ckl;
            if (length > buf.length) {
                byte[] b;
                if (length - buf.length > chunk) {
                    ckl = (int) (chunk + Math.round(length * 0.4));
                    b = new byte[length + ckl];
                    chunk = ckl;
                } else {
                    ckl = chunk + 2 * Math.round(buf.length / chunk);
                    b = new byte[buf.length + ckl];
                    chunk = ckl;
                }
                System.arraycopy(buf, 0, b, 0, buf.length);
                buf = b;
            }
        }
    }

    public TransportClass setLength(int length) {
        if (buf != null) {
            if (length > size) {
                ensureCapacity(length);
            }
            size = length;
            if (position > size) {
                position = size;
            }
            return this;
        }
        return null;
    }

    public TransportClass() {
        clear();
    }

    public TransportClass(String encoding) {
        clear();
        this.encoding = encoding;
    }

    public boolean isEmptyString() {
        return size == 0;
    }

    public boolean isNull() {
        return buf == null;
    }

    public TransportClass Copy() {
        TransportClass t = new TransportClass(encoding);
        t.Append(this);
        t.name = name;
        t.locked = locked;
        t.LocalName = new String().concat(LocalName);
        return t;
    }

    public TransportClass(boolean empty) {
        if (!empty) {
            clear();
        }
    }

    public byte[] toBytes() {
        byte[] b = new byte[size];
        System.arraycopy(buf, 0, b, 0, size);
        return b;
    }

    public byte[] getBuffer() {
        return buf;
    }

    public TransportClass Append(String string) {
        return Append(string.getBytes());
    }

    public TransportClass Append(TransportClass data) {
        if (buf != null) {
            int op = size;
            setLength(data.Length() + size);
            for (int i = op; i < size; i++) {
                buf[i] = data.getBuffer()[i - op];
            }
            return this;
        } else {
            return null;
        }
    }

    public TransportClass Append(byte[] data) {
        if (buf != null) {
            int op = size;
            setLength(data.length + size);
            for (int i = op; i < size; i++) {
                buf[i] = data[i - op];
            }
            return this;
        }
        return null;
    }

    public void Assign(byte[] data) {
        buf = data;
        size = data.length;
        position = 0;
    }

    public void Assign(byte[] data, int sizex) {
        buf = data;
        size = sizex;
        position = 0;
    }

    public void Append(byte[] data, int amount) {
        if (buf != null) {
            int op = size;
            setLength(amount + size);
            for (int i = op; i < size; i++) {
                buf[i] = data[i - op];
            }
        }
    }

    public void Append(ByteBuffer data, int amount) {
        if (buf != null) {
            int op = size;
            setLength(amount + size);
            for (int i = op; i < size; i++) {
                buf[i] = data.get(i - op);
            }
        }
    }

    public void Append(byte[] data, int amount, int offset) {
        if (buf != null) {
            int op = size;
            setLength(amount + size);
            for (int i = op; i < size; i++) {
                buf[i] = data[i - op + offset];
            }
        }
    }

    public void write(byte[] data, int poss) {
        ensureCapacity(position + data.length);
        if (buf != null) {
            for (int i = poss; i < poss + data.length; i++) {
                buf[i] = data[i - poss];
            }
        }
        position = poss + data.length;
    }

    public void write(byte[] data, int index, int count) {
        ensureCapacity(position + count);
        if (buf != null) {
            for (int i = position; i < position + count; i++) {
                buf[i] = data[index + i - position];
            }
        }
        position = index + count;
    }

    public boolean write(byte[] data) {
        ensureCapacity(position + data.length);
        if (buf != null) {
            for (int i = position; i < position + data.length; i++) {
                buf[i] = data[i - position];
            }
            position += data.length;
            return true;
        } else {
            return false;
        }
    }

    public boolean write(byte data, int poss) {
        ensureCapacity(position + 1);
        if (buf != null) {
            buf[poss] = data;
            return true;
        } else {
            return false;
        }
    }

    public boolean write(byte data) {
        if (buf != null) {
            if (size == position) {
                setLength(size + 1);
            }
            buf[position] = data;
            position++;
            return true;
        } else {
            return false;
        }
    }

    public void write(int liczba, int poss) {
        byte[] b = new byte[4];
        b[0] = (byte) (liczba);
        b[1] = (byte) (liczba >>> 8);
        b[2] = (byte) (liczba >>> 16);
        b[3] = (byte) (liczba >>> 24);
        write(b, poss);
    }

    public boolean write(int liczba) {
        byte[] b = new byte[4];
        b[0] = (byte) (liczba);
        b[1] = (byte) (liczba >>> 8);
        b[2] = (byte) (liczba >>> 16);
        b[3] = (byte) (liczba >>> 24);
        return write(b);
    }

    public boolean write(long liczba) {
        byte[] b = new byte[8];
        b[0] = (byte) (liczba);
        b[1] = (byte) (liczba >>> 8);
        b[2] = (byte) (liczba >>> 16);
        b[3] = (byte) (liczba >>> 24);
        b[4] = (byte) (liczba >>> 32);
        b[5] = (byte) (liczba >>> 40);
        b[6] = (byte) (liczba >>> 48);
        b[7] = (byte) (liczba >>> 56);
        return write(b);
    }

    public TransportClass Append(byte data) {
        if (buf != null) {
            setLength(1 + size);
            buf[size - 1] = data;
        }
        return this;
    }

    public void Insert(byte b) {
        if (buf != null) {
            setLength(1 + size);
            for (int i = size; i > 0; i--) {
                buf[i] = buf[i - 1];
            }
            buf[0] = b;
            position++;
        }
    }

    @Override
    public String toString() {
        if (buf != null) {
            try {
                return new String(buf, 0, size, encoding);
            } catch (UnsupportedEncodingException ex) {
                Log.Log(this, "", ex);
            }
        } else {
            return null;
        }
        return null;
    }

    public String toString(String encoding) throws UnsupportedEncodingException {
        if (buf != null) {
            return new String(buf, 0, size, encoding);
        } else {
            return null;
        }
    }

    public TransportClass TranslateFromTo(String encoding) {
        if (buf != null) {
            try {
                //Charset iso88592charset = Charset.forName("ISO-8859-2");
                Charset transferCharset = Charset.forName(encoding);
                //ByteBuffer b = ByteBuffer.wrap(toBytes());
                //CharBuffer d =  iso88592charset.decode(b);
                // ByteBuffer outputBuffer = UTF8charset.encode(d);
                //Assign(outputBuffer.array());
                //String ss = new String(toBytes(), "Windows-1250");
                String ss = new String(buf, 0, size, this.encoding);
                //ss = new String(ss.getBytes(UTF8charset));
                //String outs = "";
                //for(int i=0;i<ss.length();i++){
                //    if(ss.charAt(i)==''){
                //        outs=outs+'ś';
                //        continue;
                //    }
                //    if(ss.charAt(i)=='š'){
                //        outs=outs+'ą';
                //        continue;
                //    }
                //    outs=outs+ss.charAt(i);
                //}
                //Assign(outs.getBytes(UTF8charset));
                Assign(ss.getBytes(transferCharset));
            } catch (UnsupportedEncodingException ex) {
                Log.Log(this, "", ex);
            }
            return this;
        } else {
            return null;
        }
    }

    public Long toStringLong() {
        if (buf != null) {
            String d = "";
            try {
                d = new String(buf, 0, size, encoding);
            } catch (UnsupportedEncodingException ex) {
                Log.Log(this, "Conversion error", ex);
            }
            return Long.valueOf(d);
        } else {
            return null;
        }
    }

    public int Length() {
        if (buf != null) {
            return size;
        } else {
            return -1;
        }
    }

    public final void clear() {
        name = "";
        clearData();
    }

    public final TransportClass clearData() {
        size = 0;
        position = 0;
        if (buf != null) {
            if (buf.length < 2 * chunk) {
                return this;
            }
        }
        buf = new byte[chunk];
        return this;
    }

    public void appendLong(long liczba) {
        byte[] b = new byte[8];
        b[0] = (byte) (liczba);
        b[1] = (byte) (liczba >>> 8);
        b[2] = (byte) (liczba >>> 16);
        b[3] = (byte) (liczba >>> 24);
        b[4] = (byte) (liczba >>> 32);
        b[5] = (byte) (liczba >>> 40);
        b[6] = (byte) (liczba >>> 48);
        b[7] = (byte) (liczba >>> 56);
        Append(b);
    }

    public void appendInt(int liczba) {
        byte[] b = new byte[4];
        b[0] = (byte) (liczba);
        b[1] = (byte) (liczba >>> 8);
        b[2] = (byte) (liczba >>> 16);
        b[3] = (byte) (liczba >>> 24);
        Append(b);
    }

    public TransportClass SqlEscapeStr() {
        TransportClass t = new TransportClass(encoding);
        byte b = 92;
        for (int i = 0; i < size; i++) {
            if ((buf[i] == 39) || (buf[i] == 0) || (buf[i] == 92) || (buf[i] == 13) || (buf[i] == 10) || (buf[i] == 26) || (buf[i] == 34)) {
                t.Append(b);
            }
            t.Append(buf[i]);
        }
        return t;
    }

    public TCList explode(char delimiter) {
        return (explode((byte) delimiter));
    }

    public TCList explode(String delimiter) {
        return (explode(new TransportClass().Append(delimiter)));
    }

    public TCList explode(byte delimiter) {
        TCList outs = new TCList();
        TransportClass t = new TransportClass();
        for (int i = 0; i < size; i++) {
            if (buf[i] == delimiter) {
                outs.add(t);
                t = new TransportClass();
            } else {
                t.Append(buf[i]);
            }
        }
        outs.add(t);
        return outs;
    }

    public TCList explode(char delimiter, int maxcount) {
        return (explode((byte) delimiter, maxcount));
    }

    public TCList explode(byte delimiter, int maxcount) {
        maxcount--;
        TCList outs = new TCList();
        TransportClass t = new TransportClass();
        for (int i = 0; i < size; i++) {
            if ((maxcount != 0) && (buf[i] == delimiter)) {
                outs.add(t);
                maxcount--;
                t = new TransportClass();
            } else {
                t.Append(buf[i]);
            }
        }
        outs.add(t);
        return outs;
    }

    public TCList explodeCSV(char del) { // we cant expolde text fields 
        byte delimiter = (byte) del;
        byte quote = (byte) '"';
        boolean inQuotes = false;
        TCList outs = new TCList();

        TransportClass t = new TransportClass();
        for (int i = 0; i < size; i++) {
            if (buf[i] == quote) {
                inQuotes = inQuotes ? false : true;
            }
            if (buf[i] == delimiter && !inQuotes) {
                if (t.buf[0] == quote && t.buf[t.size - 1] == quote) { // wywalamy cudzyslowia z przodu i z tylu w srodku musza zoastac
                    t = t.extract(1, t.size - 2);
                }
                outs.add(t);
                t = new TransportClass();
            } else {
                t.Append(buf[i]);
            }
        }
        outs.add(t);
        return outs;

    }

    public boolean IsAtPosition(TransportClass delimiter, int pos) {
        if (delimiter == null) {
            return false;
        }
        if (pos + delimiter.Length() > Length()) {
            return false;
        }
        for (int i = pos; i < pos + delimiter.Length(); i++) {
            if (buf[i] != delimiter.getBuffer()[i - pos]) {
                return false;
            }
        }
        return true;
    }

    public TCList explode(TransportClass delimiter) {
        if (delimiter.Length() == 1) {
            return explode(delimiter.getBuffer()[0]);
        }
        TCList outs = new TCList();
        TransportClass t = new TransportClass();
        for (int i = 0; i < size; i++) {
            if (IsAtPosition(delimiter, i)) {
                outs.add(t);
                t = new TransportClass();
                i += delimiter.Length() - 1;
            } else {
                t.Append(buf[i]);
            }
        }
        outs.add(t);
        return outs;
    }

    public TCList explodeByAny(TransportClass delimiters) {
        if (delimiters.Length() == 1) {
            return explode(delimiters.getBuffer()[0]);
        }
        TCList outs = new TCList();
        TransportClass t = new TransportClass();
        boolean found;
        boolean foundLast = false;
        for (int i = 0; i < size; i++) {
            found = false;
            for (int j = 0; j < delimiters.size; j++) {
                if (delimiters.buf[j] == buf[i]) {
                    found = true;
                    break;
                }
            }
            if (found) {
                if (!foundLast) {
                    foundLast = true;
                    outs.add(t);
                    t = new TransportClass();
                }
            } else {
                t.Append(buf[i]);
                foundLast = false;
            }
        }
        outs.add(t);
        return outs;
    }

    public TransportClass Part(int offset, byte b) {
        int cp = -1;
        TransportClass t = null;
        if (offset == 0) {
            t = new TransportClass(encoding);
        } else {
            cp = 0;
        }
        for (int i = 0; i < size; i++) {
            if (buf[i] == b) {
                cp++;
                if (t != null) {
                    return t;
                }
                if (cp == offset) {
                    t = new TransportClass(encoding);
                }
            } else if (t != null) {
                t.Append(buf[i]);
            }
        }
        return t;
    }

    public Integer toStringInt() {
        if (buf != null) {
            StringBuilder s = new StringBuilder();
            s.ensureCapacity(128);
            s.setLength(0);
            String d = "";
            try {
                d = new String(buf, 0, size, encoding);
            } catch (UnsupportedEncodingException ex) {
                Log.Log(this, "Conversion error", ex);
            }
            return Integer.valueOf(d);
        } else {
            return null;
        }
    }

    public byte readByte(int poss) {
        return buf[poss];
    }

    public int readWord(int poss) {
        return (int) (readByte(poss) & 0xff) + (int) ((readByte(poss + 1) & 0xff) << 8);
    }

    public TransportClass readBinary(int poss) {
        if (poss + 4 > size) {
            throw new ArrayIndexOutOfBoundsException("position + 4 > size; p=" + String.valueOf(position + 4) + "; s=" + String.valueOf(size));
        }
        TransportClass wynik = new TransportClass();
        int len = readInt();
        for (int i = 0; i < len; i++) {
            if (size < poss + len) {
                return wynik; // za krotkie dane
            }
            wynik.Append(buf[poss + i]);
        }
        return wynik;
    }

    public TransportClass readBinary() {
        if (position + 4 > size) {
            throw new ArrayIndexOutOfBoundsException("position + 4 > size; p=" + String.valueOf(position + 4) + "; s=" + String.valueOf(size));
        }
        TransportClass wynik = new TransportClass();
        int len = readInt();
        if (size < position + len) {
            throw new ArrayIndexOutOfBoundsException("position + len > size; p=" + String.valueOf(position + len) + "; s=" + String.valueOf(size));
            // return null;
        }
        for (int i = 0; i < len; i++) {
            wynik.Append(buf[position + i]);
        }
        position += wynik.Length();
        return wynik;
    }

    public TransportClass readBytes(int count) {
        if (position + count > size) {
            throw new ArrayIndexOutOfBoundsException("position + 4 > size; p=" + String.valueOf(position + 4) + "; s=" + String.valueOf(size));
        }
        TransportClass wynik = new TransportClass();
        if (size < position + count) {
            throw new ArrayIndexOutOfBoundsException("position + count > size; p=" + String.valueOf(position + count) + "; s=" + String.valueOf(size));
            // return null;
        }
        wynik.ensureCapacity(count);
        for (int i = 0; i < count; i++) {
            wynik.Append(buf[position + i]);
        }
        position += wynik.Length();
        return wynik;
    }

    public TCList toArrayParameter() {
        if ((buf == null) || (size < 5)) {
            return new TCList();
        }
        int poss = 0;
        TransportClass tc;
        TCList tcl = new TCList();
        while (poss < size - 4) {
            tc = readBinary(poss);
            poss += 4 + tc.Length();
            tcl.add(tc);
        }
        return tcl;
    }

    public void writeBinary(TransportClass data) {
        write(data.Length());
        write(data.getBuffer(), 0, data.size);
    }

    public void AppendBinary(TransportClass data) {
        appendInt(data.Length());
        Append(data);
    }

    public void writeShortString(String s) {
        byte b = (byte) s.length();
        write(b);
        write(s.getBytes());
    }

    public void writeLongString(String s) {
        byte[] b = s.getBytes();
        write(b.length);
        write(b);
    }

    public void writeWord(int in) {
        byte[] b = new byte[2];
        b[0] = (byte) (in);
        b[1] = (byte) (in >>> 8);
        Append(b);
    }

    public void AppendWord(int word) {
        int pp = position;
        position = size;
        writeWord(word);
        position = pp;
    }

    public void AppendLongString(String s) {
        byte[] b = s.getBytes();
        appendInt(b.length);
        Append(b);
    }

    public String getString(int poss) {
        TransportClass wynik = new TransportClass();
        int i = 0;
        int accum = 0;
        for (int shiftBy = 0; shiftBy < 16; shiftBy += 8) {
            accum |= ((int) (buf[poss + i] & 0xff)) << shiftBy;
            i++;
        }
        for (i = 0; i < accum; i++) {
            wynik.Append(buf[poss + 2 + i]);
        }
        return wynik.toString();
    }

    public TransportClass readShortString(int poss) {
        TransportClass wynik = new TransportClass();
        int len = DataUtils.byteToInt(buf[poss]);
        for (int i = 0; i < len; i++) {
            wynik.Append(buf[poss + 1 + i]);
        }
        return wynik;
    }

    public TransportClass readShortString() {
        TransportClass wynik = new TransportClass();
        int len = DataUtils.byteToInt(buf[position]);
        for (int i = 0; i < len; i++) {
            wynik.Append(buf[position + 1 + i]);
        }
        position += 1 + wynik.Length();
        return wynik;
    }

    public long readLong(int poss) {
        byte[] b = new byte[8];
        for (int i = 0; i < 8; i++) {
            b[i] = buf[i + poss];
        }
        return DataUtils.bytesToLong(b);
    }

    public long readLong() {
        byte[] b = new byte[8];
        for (int i = 0; i < 8; i++) {
            b[i] = buf[i + position];
        }
        position += 8;
        return DataUtils.bytesToLong(b);
    }

    public byte readByte() {
        position++;
        return buf[position - 1];
    }

    public int readInt(int poss) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = buf[i + poss];
        }
        return DataUtils.bytesToInt(b);
    }

    public int readInt() {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = buf[i + position];
        }
        position += 4;
        return DataUtils.bytesToInt(b);
    }

    public TransportClass replace(char what, char to) {
        for (int i = 0; i < size; i++) {
            if ((char) buf[i] == what) {
                buf[i] = (byte) to;
            }
        }
        return this;
    }

    public int replace(String what, String to) {
        return replace(new TransportClass().Append(what), new TransportClass().Append(what));
    }

    public int replace(TransportClass what, TransportClass to) {
        int res = 0;
        /*for (int i = 0; i < Length(); i++) {
         if (IsAtPosition(what, i)) {
         if (what.Length() == to.Length()) {
         for (int x = i; x < i + what.Length(); x++) {
         buf[x] = to.getBuffer()[x - i];
         }
         res++;
         i += to.Length();
         }
         if (what.Length() > to.Length()) {
         int delta = what.Length()-to.Length();
         for (int x = i + to.Length(); x < size; x++) {
         buf[x] = buf[x+delta];
         }
         for (int x = i; x < i + to.Length(); x++) {
         buf[x] = to.getBuffer()[x - i];
         }
         size = size-delta;
         res++;
         }
         if (what.Length() < to.Length()) {
         ensureCapacity(Length() + insert.Length());
         for (int x = Length() + insert.Length() - 1; x > i + insert.Length() + key.Length() - 1; x--) {
         buf[x] = buf[x - insert.Length()];
         }
         for (int x = i + key.Length(); x < i + insert.Length() + key.Length(); x++) {
         buf[x] = insert.getBuffer()[x - i - key.Length()];
         }
         res++;
         }
         }
         }*/
        String xx = toString().replace(what.toString(), to.toString());
        clearData();
        Append(xx);
        return 1;
    }

    public TransportClass sanitizeURL() {
        removeInvalidStringChars();
        TransportClass tc = new TransportClass();
        tc.ensureCapacity(size);
        boolean isq = false; // was questionmark
        boolean ise = false; // was equals char
        String url = toString();
        for (int i = 0; i < url.length(); i++) {
            Character c = url.charAt(i);
            if (c.equals('|')) {
                tc.Append("%7C");
                continue;
            }
            if (c.equals(' ')) {
                tc.Append("%20");
                continue;
            }
            if (c.equals('^')) {
                tc.Append("%5E");
                continue;
            }
            if (c.equals('`')) {
                tc.Append("%60");
                continue;
            }
            if (c.equals(',')) {
                tc.Append("%2C");
                continue;
            }
            if (c.equals('=')) {
                ise = true;
            }
            if (c.equals('?')) {
                if (isq || ise) {
                    tc.Append("%3f");
                    continue;
                } else {
                    isq = true;
                }
            }
            tc.Append(c.toString());
        }
        Assign(tc.getBuffer(), tc.size);
        return this;
    }

    public TransportClass removeInvalidStringChars() {
        Charset utf8 = Charset.forName(encoding);
        for (int i = 0; i < size; i++) {
            byte b = (byte) buf[i];
            char c;
            if (b < 0) {
                //utf8 - we only use 2bytes max.
                if (i == size - 1) {
                    setLength(size - 1);
                    break;
                }
                i++;
                byte[] bb = new byte[2];
                bb[0] = b;
                bb[1] = buf[i];
                String cstr = new String(bb, utf8);
                c = cstr.charAt(0);
                if (!XMLChar.isValid(c)) {
                    buf[i] = (byte) ' ';
                    buf[i - 1] = (byte) ' ';
                }
            } else if (b < 32) {
                buf[i] = (byte) ' ';
            } else {
                c = (char) buf[i];
                if (!XMLChar.isValid(c)) {
                    buf[i] = (byte) ' ';
                }
            }
        }
        return this;
    }

    public String toValidXMLString() {
        String tts = toString();
        StringBuilder outs = new StringBuilder(tts.length());
        char c;
        for (int i = 0; i < tts.length(); i++) {
            c = tts.charAt(i);
            if (!XMLChar.isValid(c)) {
                outs.append(' ');
            } else {
                outs.append(c);
            }
        }
        return outs.toString();
    }

    public TransportClass replace(char what, char to, int start, int count) {
        for (int i = start; i < start + count; i++) {
            if ((char) buf[i] == what) {
                buf[i] = (byte) to;
            }
        }
        return this;
    }

    public TransportClass StripNumbers() {
        TransportClass t = new TransportClass(encoding);
        for (int i = 0; i < size; i++) {
            if (Character.isDigit(buf[i])) {
                continue;
            }
            if (((char) buf[i] == ',') || ((char) buf[i] == '.')) {
                continue;
            }
            t.Append(buf[i]);
        }
        return t;
    }

    public TransportClass StripNonNumbers() {
        TransportClass t = new TransportClass(encoding);
        for (int i = 0; i < size; i++) {
            if (Character.isDigit(buf[i])) {
                t.Append(buf[i]);
            }
            if (((char) buf[i] == ',') || ((char) buf[i] == '.')) {
                t.Append(buf[i]);
            }
        }
        return t;
    }

    public TransportClass implode(Collection<?> items, String separator) {
        clearData();
        Iterator i = items.iterator();
        int ii = 0;
        while (i.hasNext()) {
            if (ii > 0) {
                Append(separator);
            }
            Append(String.valueOf(i.next()));
            ii++;
        }
        return this;
    }

    public TransportClass ensureIsNumber() {
        try {
            double dd = Double.valueOf(toString());
        } catch (NumberFormatException ex) {
            clearData();
            Append('0');
        }
        return this;
    }

    public boolean contentEquals(TransportClass tc) {
        if (tc == null) {
            return false;
        }
        if (tc.Length() != Length()) {
            return false;
        }
        for (int i = 0; i < Length(); i++) {
            if (buf[i] != tc.getBuffer()[i]) {
                return false;
            }
        }
        return true;
    }

    public Character charAt(int i) {
        if (i >= size) {
            return null;
        }
        return (char) buf[i];
    }

    /**
     *
     * @param f File
     *
     * Reads from file into itself until eof. Data are writed from current
     * position and if necessary extends transport class.
     * @return
     * @throws java.io.FileNotFoundException
     */
    public TransportClass LoadFile(File f) throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(f.getAbsolutePath());
        byte[] buffer = new byte[8192];
        int n;
        ensureCapacity(position + (int) f.length());
        while ((n = fis.read(buffer)) != -1) { // reads until end of transmission
            if (n > 0) {
                Append(buffer, n, 0);
            }
        }
        return this;
    }

    public TCList ExplodeToSimpleConfigs(char c) {
        TCList cfgs = explode('\n');
        for (int i = cfgs.count() - 1; i >= 0; i--) {
            TransportClass cc = cfgs.get(i);
            String line = cc.toString().trim();
            if (line.isEmpty() || line.startsWith("#")) {
                cfgs.delete(i);
                continue;
            }
            TCList kv = new TransportClass().Append(line).explode(c);
            cc.name = kv.get(0).toString().trim();
            if (cc.name.isEmpty()) {
                cfgs.delete(i);
                continue;
            }
            if (kv.count() > 1) {
                cc.setLength(0);
                cc.Append(kv.get(1).toString().trim());
            } else {
                cc.setLength(0);
                cc.Append("undefined");
            }
        }
        return cfgs;
    }

    public void reverse() {
        byte[] newbuff = new byte[buf.length];
        for (int i = size; i > 0; i--) {
            newbuff[size - i] = buf[i - 1];
        }
        buf = newbuff;
    }

    public TransportClass implode(String[] items, String separator) {
        clearData();
        for (int i = 0; i < items.length; i++) {
            if (i > 0) {
                Append(separator);
            }
            Append(String.valueOf(items[i]));
        }
        return this;
    }

    public int getSize() {
        return size;
    }

    public TransportClass toAlphaNumeric() {
        TransportClass t = new TransportClass();

        return t;
    }

    public boolean isValidIP() {
        return DataUtils.isValidIPV4(toString());
    }

    public int InsertAfter(String key, String insert) {
        return InsertAfter(new TransportClass().Append(key), new TransportClass().Append(insert));
    }

    public int InsertAfter(TransportClass key, TransportClass insert) {
        int res = 0;
        for (int i = 0; i < Length(); i++) {
            if (IsAtPosition(key, i)) {
                ensureCapacity(Length() + insert.Length());
                for (int x = Length() + insert.Length() - 1; x > i + insert.Length() + key.Length() - 1; x--) {
                    buf[x] = buf[x - insert.Length()];
                }
                for (int x = i + key.Length(); x < i + insert.Length() + key.Length(); x++) {
                    buf[x] = insert.getBuffer()[x - i - key.Length()];
                }
                res++;
                i += insert.Length();
                setLength(Length() + insert.Length());
            }
        }
        return res;
    }

    public boolean contentEquals(String key) {
        if (key == null) {
            return false;
        }
        if (key.length() != Length()) {
            return false;
        }
        for (int i = 0; i < Length(); i++) {
            if (buf[i] != key.getBytes()[i]) {
                return false;
            }
        }
        return true;
    }

    public TransportClass shift_scramble(byte b) {
        if (b > 0) {
            for (int i = 0; i < size; i++) {
                int bb = buf[i];
                bb += b;
                if (bb > 127) {
                    bb = bb - 256;
                }
                buf[i] = (byte) bb;
            }
        } else {
            for (int i = 0; i < size; i++) {
                int bb = buf[i];
                bb += b;
                if (bb < -128) {
                    bb = bb + 256;
                }
                buf[i] = (byte) bb;
            }
        }
        return this;
    }

    public int pos(char chunkDelimiter) {
        return pos(chunkDelimiter, 0);
    }

    public int pos(char chunkDelimiter, int offset) {
        for (int i = offset; i < size; i++) {
            if ((byte) buf[i] == (byte) chunkDelimiter) {
                return i;
            }
        }
        return -1;
    }

    public int pos(byte[] filter) {
        return pos(filter, 0);
    }

    public int pos(byte[] filter, int offset) {
        return pos(filter, offset, size - offset);
    }

    public int pos(byte[] filter, int offset, int maxCount) {
        if (filter.length > maxCount) {
            return -1;
        }
        if (filter.length == 0) {
            return -1;
        }
        for (int i = offset; i < offset + maxCount; i++) {
            if (buf[i] == filter[0]) {
                for (int x = 1; x < filter.length; x++) {
                    if (buf[i + x] != filter[x]) {
                        break;
                    }
                    if (x == filter.length - 1) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    public TransportClass cutFront(int p) {
        for (int i = p; i < size; i++) {
            buf[i - p] = buf[i];
        }
        size = size - p;
        return this;
    }

    public boolean contains(String filter) {
        return contains(filter.getBytes());
    }

    public boolean contains(byte[] filter) {
        return pos(filter) >= 0;
    }
}