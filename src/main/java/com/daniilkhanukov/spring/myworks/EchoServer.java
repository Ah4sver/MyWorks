package com.daniilkhanukov.spring.myworks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/// Для проверки работоспособности класса я запустил main метод тут,
/// потом включил telnet на ПК, открыл три командных строки
/// и запустил в каждой из них команду "telnet localhost 8888".
/// В каждой из них я вводил символы и мне сразу же возвращало эхо-ответ.
/// В консоли среды разработки получил следующее (запуск telnet в каждой консоли и последующий выход из них):
/// <p>Echo Server started on port 8888</p>
/// <p>New client connected: /127.0.0.1</p>
/// <p>New client connected: /127.0.0.1</p>
/// <p>New client connected: /127.0.0.1</p>
/// <p>Client disconnected</p>
/// <p>Client disconnected</p>
/// <p>Client disconnected</p>
public class EchoServer {
    private static final int PORT = 8888;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Echo Server started on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                new Thread(new EchoHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class EchoHandler implements Runnable {
        private final Socket clientSocket;

        EchoHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (InputStream in = clientSocket.getInputStream();
                 OutputStream out = clientSocket.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Client disconnected");
        }
    }
}
