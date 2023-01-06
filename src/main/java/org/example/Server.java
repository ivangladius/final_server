
package org.example;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

import org.json.JSONObject;

public class Server {

    private Database db;

    private int port;
    private ServerSocket serverSocket;
    private ThreadPoolExecutor executor;

    public Server(int port, int threadPoolSize) {

        try {
            db = HSQLDatabase.getInstance();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Database running...");

        this.port = port;
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadPoolSize);
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started...");
        } catch (IOException e) {
            stop();
            e.printStackTrace();
        }
    }

    public void loop() {
        for (; ; ) {
            try {
                System.out.println("Waiting for Connections..");
                Socket clientSocket = serverSocket.accept();
                executor.execute(new MyRunnable(this, clientSocket));
            } catch (IOException ignore) {
            }
        }
    }

    public void handle_connection(Socket cSocket) {

        System.out.println("\nClient connected: " + cSocket.getRemoteSocketAddress().toString());

        PrintWriter out;
        BufferedReader in;
        try {
            out = new PrintWriter(cSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(cSocket.getInputStream()));

            String result = in.readLine();

            int i = result.indexOf("{");
            result = result.substring(i);
            JSONObject json = new JSONObject(result.trim());
            System.out.println(json.toString(4));

//
            System.out.println("operation: " + json.get("operation"));

            // answer
            JSONObject reply = new JSONObject();
            reply.put("key", json.get("key"));
            reply.put("operation", json.get("operation"));

            String operation = json.get("operation").toString();

            if (operation.equals("getUsername"))
                reply.put("payload", getUsername(json.get("key").toString()));

            if (operation.equals("createUser"))
                reply.put("payload", createUser(json.get("payload").toString()));

            else
                reply.put("payload", "");

            out.println(reply.toString());
            System.out.println("data send back");

            out.close();
            in.close();

            ResultSet result2 = db.executeQuery("SELECT * FROM users;");

            String id, username, email, password;
            id = username = email = password = null;
            while (result2.next()) {

                id = result2.getString(1);
                username = result2.getString(2);
                email = result2.getString(3);
                password = result2.getString(4);

                System.out.println(id + ", " + username + " " + email + " " + password);

            }

            System.out.println
                    ("new user: " + id + ", " + username + " " + email + " " + password);

            result2.close();


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        closeClient(cSocket);
    }

    private void closeClient(Socket cSocket) {
        try {
            cSocket.close();
        } catch (IOException ignore) {
        }
    }

    public void stop() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public String getUsername(String key) {

        System.out.println("Username requested\n");

        return "Hans Peter";
    }

    public String createUser(String payload) {
        System.out.println("client payload: " + payload);
        String[] data = payload.split(" ");
        String userId = db.queryInsertUser(data[0], data[1], data[2]);
        if (userId == null)
            System.out.println("user already exist");
        else
            System.out.println("user " + data[0] + "created");

        System.out.println("### USERID: " + userId);
        return userId;
    }

    public void replyError(PrintWriter out) {
        out.print("Error");
    }
}

class MyRunnable implements Runnable {

    private Server server;
    private Socket client;

    // passing original server object to this constructor
    public MyRunnable(Server server, Socket client) {
        this.server = server;
        this.client = client;
    }

    public void run() {
        this.server.handle_connection(this.client);
        // long threadId = Thread.currentThread().getId();
        // System.out.print("ID: " + threadId + " ");
    }
}
