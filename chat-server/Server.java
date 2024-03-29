package chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class Server {
	public static void main(String[] args) {
		try {
			ServerSocket server = new ServerSocket(1001);
			HashMap<String, Object> hm = new HashMap<String, Object>();
			while(true) {
				System.out.println("접속을 기다립니다.");
				Socket sock = server.accept();
				ChatThread chatThread = new ChatThread(sock, hm);
				chatThread.start();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class ChatThread extends Thread{
	private Socket sock;
	private String id;
	private BufferedReader br;
	private HashMap<String, Object> hm;
	private boolean duplFlag = true;

	public ChatThread(Socket sock, HashMap<String,Object> hm) {

		this.sock = sock;
		this.hm = hm;
		    try {
		            PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
		            br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		            id = br.readLine();
			    Object obj = hm.get(id);
			    if(obj != null) {
				    pw.println("/quit already exist id");
				    pw.flush();
				    System.out.println("중복아이디 퇴장처리 : "+id);
				    duplFlag = false;
				    return;
			    }
		            synchronized (hm) {
				    hm.put(this.id, pw);
			    }
		            broadcast(id + "님이 접속하셨습니다.");
		            System.out.println("접속한 사용자의 아이디 : "+id);
			    duplFlag = true;
		    } catch (Exception e) {
			    e.printStackTrace();
		    }
	}
	public void run() {
			if (duplFlag == false) {//중복 ID 체크 후 메서드 종료
				return;
			}
		try {
				String line = null;
				while((line = br.readLine()) != null) {
					if(line.equals("/quit")) {
						break;
					}
					if(line.indexOf("/to") == 0) {
						sendmsg(line);
					}else {
						broadcast(id+" : "+line);
					}
				}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			synchronized (hm) {
				hm.remove(id);
			}
			broadcast(id+"님이 접속을 종료했습니다.");
			try {
				if(sock != null) {
					sock.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
	public void sendmsg(String msg) {
		int idStart = msg.indexOf(" ") + 1;
		int idEnd = msg.indexOf(" ",idStart);
		if(idEnd != -1) {
			String to = msg.substring(idStart, idEnd);
			String msg2 = msg.substring(idEnd +1);
			Object obj = hm.get(to);
			if(obj != null) {
				PrintWriter pw = (PrintWriter)obj;
				pw.println(id + "님이 다음의 귓속말을 보냈습니다. : " + msg2);
				pw.flush();
			}
		}else if(idEnd == -1){
			Object obj = hm.get(id);
			PrintWriter pw = (PrintWriter)obj;
			pw.println("귓속말 실패 하였습니다. you should check the arguments.");
			pw.flush();
		}
	}
	public void broadcast(String msg) {
		synchronized (hm) {
			Collection<Object> collection = hm.values();
			Iterator<?> iter = collection.iterator();
			while(iter.hasNext()) {
				PrintWriter pw = (PrintWriter)iter.next();
				pw.println(msg);
				pw.flush();
			}
		}
		System.out.println(msg);
	}
}
