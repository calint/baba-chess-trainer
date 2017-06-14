package a;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

final public class pgnreader{
		private PushbackReader pr;
		public pgnreader(final Reader r){pr=new PushbackReader(r,1);}
		public pgnreader(final String s){pr=new PushbackReader(new StringReader(s),1);}
		/**@return null when end of file otherwise proceed with next_move()*/
		public Map<String,String>next_tags()throws IOException{
			final LinkedHashMap<String,String>tags=new LinkedHashMap<String,String>();
			while(true){
				skip_white_space(pr);
				final int ch=pr.read();
				if(ch==-1){return null;}
				if(ch!='['){pr.unread(ch);break;}//no headers
//				if(ch!='[')throw new Error("expected [ for tag start");
				//read tagname
				final tag_name tn=new tag_name(pr);
				skip_white_space(pr);
				final tag_value tv=new tag_value(pr);
				tags.put(tn.toString(),tv.toString());
			}
			return tags;
		}
		/**@return null when end of game, proceed with next_tags()*/
		public String next_move()throws IOException{
			skip_white_space(pr);
			if(!blkmv){
				final StringBuilder sb=new StringBuilder();
				while(true){
					final int ch=pr.read();
					if(ch==-1)break;
					if(Character.isWhitespace(ch))break;
					sb.append((char)ch);
				}
				final String s=sb.toString();
				if(is_end_of_game(s)){
					blkmv=false;
					return null;//end of game
				}
				if(!s.endsWith(".")){// unnumbered moves, consider white move
					blkmv=!blkmv;
					final move mv=new move(new StringReader(s));
					skip_white_space(pr);
					final int ch=pr.read();
					if(ch=='{')new comment(pr);
					else if(ch!=-1)pr.unread(ch);
					final String ss=mv.toString();
					if(is_end_of_game(ss)){
						blkmv=false;
						return null;//end of game
					}
					return ss;				
				}
				skip_white_space(pr);
			}
			blkmv=!blkmv;
			final move mv=new move(pr);
			skip_white_space(pr);
			final int ch=pr.read();
			if(ch=='{')new comment(pr);
			else if(ch!=-1)pr.unread(ch);
			final String s=mv.toString();
			if(is_end_of_game(s)){
				blkmv=false;
				return null;//end of game
			}
			return s;
		}

		private boolean blkmv;

		static private boolean is_end_of_game(final String s){return s.equals("1-0")||s.equals("0-1")||s.equals("1/2-1/2");}

		private static void skip_white_space(PushbackReader pr)throws IOException{
			while(true){
				final int ch=pr.read();
				if(ch==-1)break;
				if(Character.isWhitespace(ch))continue;
				pr.unread(ch);
				break;
			}
		}
		private static class tag_name{
			final String s;
			tag_name(Reader r)throws IOException{
				final StringBuilder sb=new StringBuilder();
				while(true){
					final int ch=r.read();
					if(ch==-1)break;
					if(ch==' ')break;
					sb.append((char)ch);
				}
				s=sb.toString();
			}
			public String toString(){return s;}
		}	
		private static class tag_value{
			final String s;
			tag_value(Reader r)throws IOException{
				final StringBuilder sb=new StringBuilder();
				if(r.read()!='"')throw new Error("expected \"");
				while(true){
					final int ch=r.read();
					if(ch==-1)break;
					if(ch=='\"')break;
					sb.append((char)ch);
				}
				s=sb.toString();
				if(r.read()!=']')throw new Error("expected ]");
			}
			public String toString(){return s;}
		}
		private static class move{
			final String s;
			move(Reader r)throws IOException{
				final StringBuilder sb=new StringBuilder();
				while(true){
					final int ch=r.read();
					if(ch==-1)break;
					if(Character.isWhitespace(ch))break;
					sb.append((char)ch);
				}
				s=sb.toString();
			}
			public String toString(){return s;}
		}
		private static class comment{
			final String s;
			comment(Reader r)throws IOException{
				final StringBuilder sb=new StringBuilder();
				while(true){
					final int ch=r.read();
					if(ch==-1)break;
					if(ch=='}')break;
					sb.append((char)ch);
				}
				s=sb.toString();
			}
			public String toString(){return s;}
		}

}