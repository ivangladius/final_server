package org.example;


public class Main {

	public static void main(String[] args) {

		Server server = new Server(8888, 3);

		server.start();

		server.loop();

		server.stop();
	}

}
