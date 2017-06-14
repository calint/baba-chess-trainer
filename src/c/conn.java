package c;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
public final class conn{
	conn(final client cl,final String host,final int port,final onconnected onconnected)throws IOException{
		this.cl=cl;
		this.onconnected=onconnected;
		this.host=host;
		sc=SocketChannel.open();
		sc.configureBlocking(false);
		sc.setOption(StandardSocketOptions.TCP_NODELAY,true);
		sc.connect(new InetSocketAddress(host,port));
		sk=sc.register(cl.selector,SelectionKey.OP_CONNECT,this);
	}
	void close(){try{sc.close();}catch(final IOException ok){}}
	void get(final String path,final kvps hd,final oncontent oncontent,final ongetdone ongetdone)throws Throwable{
		this.oncontent=oncontent;
		this.ongetdone=ongetdone;
		final ByteBuffer bbo=ByteBuffer.allocate(512);
		bbo.put(b_get).put(path.getBytes()).put(b_prothost).put(host.getBytes()).put(b_rnl);
		final String cookie=cl.cookie();
		if(cookie!=null&&!cookie.isEmpty())
			bbo.put(b_hk_cookie).put(cookie.getBytes()).put(b_rnl);
//		if(hd!=null)
//			hd.foreach(bbo::put);
		if(hd!=null)
			hd.put_in(bbo);
		bbo.put(b_rnl);
		bbo.flip();
		bboa=new ByteBuffer[]{bbo};onwrite();
	}
	void onconnect()throws Throwable{
		if(!sc.finishConnect())throw new Error();
		bbi.position(bbi.limit());
		if(onconnected!=null){onconnected.onconnected();onconnected=null;}
	}
	void onwrite()throws Throwable{
		if(c.preq)for(final ByteBuffer b:bboa)System.out.print(c.tostr(b));
		final long n=sc.write(bboa);
		metrs.output+=n;
		for(final ByteBuffer b:bboa)
			if(b.hasRemaining()){sk.interestOps(SelectionKey.OP_WRITE);return;}
		bboa=null;
		sk.interestOps(SelectionKey.OP_READ);//? whatif bbi.hasremaining and no read avail
	}
	void onread()throws Throwable{
		while(true){
			bbi.clear();
			final int n=sc.read(bbi);
			if(n==-1){close();return;}
			if(n==0){sk.interestOps(SelectionKey.OP_READ);return;}
			metrs.input+=n;
			bbi.flip();
			while(true){
				final byte b=bbi.get();
				final char c=(char)b;
				switch(st){default:throw new Error();
				case newreq:prot.setLength(0);st=state.prot;
				case prot:
					switch(c){
					case' ':statuscode.setLength(0);st=state.statuscode;break;
					default:prot.append(c);break;
					}
					break;
				case statuscode:
					switch(c){
					case' ':statusline.setLength(0);st=state.statusline;break;
					case'\n':statusline.setLength(0);hdrs.clear();st=state.headername;break;
					default:statuscode.append(c);break;
					}
					break;
				case statusline:
					switch(c){
					case'\n':headername.setLength(0);hdrs.clear();st=state.headername;break;
					default:statusline.append(c);break;
					}
					break;
				case headername:
					switch(c){
					case':':headervalue.setLength(0);st=state.headervalue;break;
					case'\n':afterheader();break;
					default:headername.append(c);break;
					}
					break;
				case headervalue:
					switch(c){
					case'\n':hdrs.put(headername.toString().trim().toLowerCase(),headervalue.toString().trim());headername.setLength(0);st=state.headername;break;
					default:headervalue.append(c);break;
					}
					break;
				case chunksize:
					switch(c){
					case'\n':
						final String chnksz=chunksizehex.toString().trim();
						chunkrem=Integer.parseInt(chnksz,16);
						chunksizehex.setLength(0);
						if(chunkrem==0){
							st=state.chunkeddone;
							break;
						}
						st=state.chunkdata;
						onchunkdata();
						break;
					default:chunksizehex.append(c);break;
					}
					break;
				case chunkdata:bbi.position(bbi.position()-1);onchunkdata();break;
				case chunkend:if(c=='\n')st=state.chunksize;break;
				case chunkeddone:if(c=='\n'){st=state.newreq;ondone();}break;
				case content:bbi.position(bbi.position()-1);oncontent();break;
				case websock:
					if(b!=(byte)129)throw new Error("unexpected first byte in websocket frame "+b);
					final byte b2=bbi.get();
					if(b2<=125){
						payloadrem=b2;
					}else if(b2==126){
						final int by2=(((int)bbi.get()&0xff)<<8);
						final int by1= ((int)bbi.get()&0xff);
						payloadrem=by2|by1;
					}else if(b2==127){
						bbi.get();bbi.get();bbi.get();bbi.get();
						final int by4=(((int)bbi.get()&0xff)<<24);
						final int by3=(((int)bbi.get()&0xff)<<16);
						final int by2=(((int)bbi.get()&0xff)<<8);
						final int by1= ((int)bbi.get()&0xff);
						payloadrem=by4|by3|by2|by1;
					}else throw new Error();
					final int bbirem=bbi.remaining();
					onwebsock.onwebsock(payloadrem,bbi);
					bbi.position(bbi.limit());//? bug payloadrem<limit
					payloadrem-=bbirem;
					if(payloadrem!=0)
						st=state.websock_more;
					break;
				case websock_more:
					bbi.position(bbi.position()-1);
					final int bbirem2=bbi.remaining();
					onwebsock.onwebsock(payloadrem,bbi);
					//? whatif bbirem2>payloadrem
					bbi.position(bbi.limit());
					payloadrem-=bbirem2;
					if(payloadrem==0)
						st=state.websock;
				}
				if(!bbi.hasRemaining())break;
			}
		}
	}
	void mode_websock(){st=state.websock;}
	private void afterheader()throws Throwable{
		if(c.presph)System.out.println(prot.toString().trim()+' '+statuscode.toString().trim()+' '+statusline.toString().trim()+' '+hdrs);
		final String cookie=hdrs.get("set-cookie");if(cookie!=null){final String[]cookieparts=cookie.split(";");cl.cookie(cookieparts[0]);}//?. key value path expiration
		if("chunked".equals(hdrs.get("transfer-encoding"))){st=state.chunksize;return;}
		final String contentlenstr=hdrs.get("content-length");
		if(contentlenstr==null){st=state.newreq;ondone();return;}
		contentlen=Long.parseLong(contentlenstr);
		if(contentlen==0){st=state.newreq;ondone();return;}
		oncontent();
	}
	private void oncontent()throws Throwable{
		final int datalen=(int)(bbi.remaining()>contentlen?contentlen:bbi.remaining());
		if(oncontent!=null)oncontent.oncontent(bbi.array(),bbi.position(),datalen);
		bbi.position(bbi.position()+datalen);
		contentlen-=datalen;
		if(contentlen==0){st=state.newreq;ondone();return;}
		st=state.content;
	}
	private void onchunkdata()throws Throwable{
		final int datalen=bbi.remaining()>chunkrem?chunkrem:bbi.remaining();
		if(oncontent!=null)oncontent.oncontent(bbi.array(),bbi.position(),datalen);
		bbi.position(bbi.position()+datalen);
		chunkrem-=datalen;
		if(chunkrem==0)st=state.chunkend;
	}
	private void ondone(){if(ongetdone!=null)try{ongetdone.ongetdone();}catch(final Throwable t){throw new Error(t);}}
	void websock_sendframe(final String s,final onwebsock onwebsockframe)throws Throwable{
		this.onwebsock=onwebsockframe;
		final ByteBuffer bb=ByteBuffer.wrap(s.getBytes("utf8"));
		final int bblen=bb.remaining();
		final ByteBuffer bbh=ByteBuffer.allocate(10);
		final byte b0=(byte)(1|128);// text frame fin
		bbh.put(b0);
		if(bblen<=126){final byte b1=(byte)(bblen|128);bbh.put(b1);}// masked small payload
		else if(bblen<=0x10000){final byte b1=(byte)(126|128);// masked medium payload
			//TODO
			bbh.put(b1);
		}else{final byte b1=(byte)(127|128);// masked large payload
			//TODO
			bbh.put(b1);
		}
		bbh.put(new byte[]{0,0,0,0});// mask
		bbh.flip();
		bboa=new ByteBuffer[]{bbh,bb};onwrite();
	}

	private final client cl;
	private final SocketChannel sc;
	private final SelectionKey sk;
	private final String host;
	private ByteBuffer[]bboa;
	private final ByteBuffer bbi=ByteBuffer.allocate(1024);
	private state st=state.newreq;
	private final StringBuilder prot=new StringBuilder(8);
	private final StringBuilder statuscode=new StringBuilder(4);
	private final StringBuilder statusline=new StringBuilder(128);
	private final StringBuilder headername=new StringBuilder(32);
	private final StringBuilder headervalue=new StringBuilder(64);
	private final Map<String,String>hdrs=new HashMap<>(8);
	private long contentlen;
	private final StringBuilder chunksizehex=new StringBuilder(8);
	private int chunkrem;
	private int payloadrem;
	private onconnected onconnected;
	private oncontent oncontent;
	private ongetdone ongetdone;
	private onwebsock onwebsock;
	private static enum state{newreq,prot,statuscode,statusline,headername,headervalue,content,chunksize,chunkdata,chunkend,chunkeddone,websock,websock_more};

	private static final byte[]b_get="GET ".getBytes();
	private static final byte[]b_prothost=" HTTP/1.1\r\nHost: ".getBytes();
	private static final byte[]b_rnl="\r\n".getBytes();
	private static final byte[]b_hk_cookie="Cookie: ".getBytes();

	public static interface onconnected{void onconnected()throws Throwable;}
	public static interface oncontent{void oncontent(final byte[]data,final int offset,final int len)throws Throwable;}
	public static interface ongetdone{void ongetdone()throws Throwable;}
	public static interface onwebsock{void onwebsock(final int nbytesleftinframe,final ByteBuffer bbi)throws Throwable;}
}
