package com.anbao.controllor;

import com.anbao.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class Server {
    int port;
    //    List<Socket> clients;
    ServerSocket server;
    @Autowired
    DeviceService deviceService;

    public  Server(){
        try {
            port = 9999;
//            clients = new ArrayList<Socket>();
            server = new ServerSocket(9999);

            while (true) {
                Socket socket = server.accept();
//                clients.add(socket);
                Mythread mythread = new Mythread(socket);
                mythread.start();
            }
        } catch (Exception ex) {
        }
    }

    class Mythread extends Thread {
        Socket client;
        private BufferedReader br;
        public String msg;
        private InputStreamReader inSR;
        OutputStreamWriter outSW=new OutputStreamWriter(client.getOutputStream(),"utf-8");
        private ArrayList<String> list;
        Timer timer;
        int i=0;

        //时间线程方法
        //定时方法
        public class TimerTaskTest extends TimerTask {
            @Override
            public void run() {
                if(i>=6){
                    for(Iterator<String> iter=list.iterator();iter.hasNext();){
                        String  temp = iter.next();
                        deviceService.deviceLogin(temp,"0");
                    }
                }

                try {
                    outSW.write("pant");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                i++;

            }
        }

        //线程构造方法
        public Mythread(Socket s) throws IOException {
            client = s;
            list = new ArrayList<>();
            timer=new Timer();
            //20s定时任务
            timer.schedule(new TimerTaskTest(), 0, 2000);
        }


        public void run() {
            try {
                inSR = new InputStreamReader(client.getInputStream(),"utf-8");
                BufferedReader br = new BufferedReader(inSR);
                while ((msg = br.readLine()) != null) {
                    if(msg.equals("pant")){
                        i=0;
                    }
                    deviceService.deviceLogin(msg,"1");
                    System.out.println(msg);
                    list.add(msg);
                }
            } catch (Exception ex) {
                for(Iterator<String> iter=list.iterator();iter.hasNext();){
                    String  temp = iter.next();
                    deviceService.deviceLogin(msg,"0");
                    System.out.println(temp);
                }
            }
        }
    }




}
