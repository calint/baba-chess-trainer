package c;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.*;
final class kvps{
	final private List<byte[]>ls=new ArrayList<>(8);
	kvps put(final String key,final String value){ls.add((key+": "+value+"\r\n").getBytes());return this;}
	kvps foreach(final Consumer<? super byte[]>x){ls.forEach(x);return this;}
	void put_in(final ByteBuffer bbo){for(final byte[]ba:ls)bbo.put(ba);}
}