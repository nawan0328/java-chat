package chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
	public static void main(String[] args) {
		if(args.length != 2) {
			System.out.println("사용법 : java ChatClient id 접속할 서버 ip");
			System.exit(1);
		}
		Socket sock = null;
		BufferedReader br = null;
		PrintWriter pw = null;
		//boolean endflag = false;
		try {
			sock = new Socket(args[1], 1001);//아아디,포트
			pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));

			pw.println(args[0]);
			pw.flush();
			InputThread it = new InputThread(sock,br);
			it.start();
			String line = null;
			while((line = keyboard.readLine()) != null) {
				pw.println(line);
				pw.flush();
				if(line.equals("/quit")) {
					//endflag = true;
					break;
				}
				if(sock.isConnected() == true && sock.getKeepAlive() == false) {
					sock.setKeepAlive(true);
					if(sock.getKeepAlive() == false) { // Socket 연결이 끊어 졌는지 확인
						System.out.println("KeepAlive : " + sock.getKeepAlive());
						break;
					}
				}
			}
			System.out.println("클라이언트 접속 종료");
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(pw != null) {
					pw.close();
				}
				if(br != null) {
					br.close();
				}
				if(sock != null) {
					sock.close();
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

class InputThread extends Thread{
	    private Socket sock = null;
	    private BufferedReader br = null;
	    public InputThread(Socket sock,BufferedReader br) {
		    this.sock = sock;
		    this.br = br;
	    }
	    public void run() {
		    try {
			    String line = null;
			    while((line = br.readLine()) != null) {
				    System.out.println(line);
				    if (line.indexOf("/quit") == 0){
					    break;
				    }
			    }
		    } catch (Exception e) {
			    e.printStackTrace();
		    }finally {
			    try {
				    if(br != null) {
					    br.close();
				    }
				    if(sock != null) {
					    sock.close();
				    }
			    }catch (Exception e) {
				    e.printStackTrace();
			    }
		    }
	    }
}
