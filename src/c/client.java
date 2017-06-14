package c;
import java.io.*;
import java.nio.channels.*;
import java.util.*;
public final class client implements AutoCloseable{
	public client(final String host,final int port)throws IOException{
		count++;this.host=host;this.port=port;
		selector=Selector.open();	
	}
	public void close(){
		if(conn!=null){
			conn.close();
			conn=null;
		}
//		if(--count==0)
//			on=false;
		on=false;
	}
	public void get(final String uri,final conn.oncontent oncontent,final conn.ongetdone ongetdone)throws Throwable{
		if(conn!=null){
			conn.get(uri,null,oncontent,ongetdone);
			return;
		}
//		conn=new conn(this,host,port,()->conn.get(uri,null,oncontent,ongetdone));
		conn=new conn(this,host,port,new conn.onconnected(){@Override public void onconnected()throws Throwable{
			conn.get(uri,null,oncontent,ongetdone);
		}});
	}
	public void cookie(final String cookie){this.cookie=cookie;}
	public String cookie(){return cookie;}
	public void websock(final String uri,final client.onwebsockconnect onwebsockconnect)throws Throwable{
//		conn=new conn(this,host,port,()->
//			// after connect	
//			conn.get(uri,new kvps().put("Upgrade","websocket").put("Connection","upgrade").put("Sec-WebSocket-Key","x3JJHMbDL1EzLkh9GBhXDw==").put("Sec-WebSocket-Version","13"),null,()->{
//				// after get done
//				conn.mode_websock();//? checkrepliedkey
//				onwebsockconnect.onwebsockconnect();
//			})
//		);
		conn=new conn(this,host,port,new conn.onconnected(){@Override public void onconnected()throws Throwable{
			conn.get(uri,new kvps().put("Upgrade","websocket").put("Connection","upgrade").put("Sec-WebSocket-Key","x3JJHMbDL1EzLkh9GBhXDw==").put("Sec-WebSocket-Version","13"),null,new conn.ongetdone(){@Override public void ongetdone()throws Throwable{
				conn.mode_websock();//? checkrepliedkey
				onwebsockconnect.onwebsockconnect();
			}});
		}});
		// after connect	
		conn.get(uri,new kvps().put("Upgrade","websocket").put("Connection","upgrade").put("Sec-WebSocket-Key","x3JJHMbDL1EzLkh9GBhXDw==").put("Sec-WebSocket-Version","13"),null,new conn.ongetdone(){@Override public void ongetdone()throws Throwable{
			conn.mode_websock();//? checkrepliedkey
			onwebsockconnect.onwebsockconnect();			
		}});
	}
	public void recv(final String s,final conn.onwebsock onwebsockframe)throws Throwable{
		conn.websock_sendframe(s,onwebsockframe);
	}
	public client pl(final String s){c.out.println(s);return this;}

	final private String host;
	final private int port;
	private String cookie;
	private conn conn;

	private static int count;
	public boolean on=true;

	Selector selector;
//	Selector selector(){return sel;}
	public void run()throws Throwable{
		final long t0_ms=System.currentTimeMillis();
		while(on)try{
			selector.select();
			metrs.select++;
			final Iterator<SelectionKey>it=selector.selectedKeys().iterator();
			while(it.hasNext()){
				metrs.ioevent++;
				final SelectionKey sk=it.next();
				it.remove();
				sk.interestOps(0);
				final conn cn=(conn)sk.attachment();
				if(sk.isConnectable()){metrs.iocon++;cn.onconnect();continue;}
				if(sk.isReadable()){metrs.ioread++;cn.onread();continue;}
				if(sk.isWritable()){metrs.iowrite++;cn.onwrite();}
			}
		}catch(final Throwable e){
			if(e instanceof ClosedChannelException)break;
			if(e instanceof ClosedSelectorException)break;
			if(e instanceof CancelledKeyException)break;
			throw new Error(e);
		}
		selector.close();
		final long dt_ms=System.currentTimeMillis()-t0_ms;
		if(c.pstats)System.out.println(c.nclients+" req, "+dt_ms+" ms, "+c.nclients*1000/(dt_ms==0?1:dt_ms)+" req/s, "+(((metrs.output+metrs.input)*1000/(dt_ms==0?1:dt_ms))>>10)+" KB/s");
	}

	public static interface onwebsockconnect{void onwebsockconnect()throws Throwable;}
}