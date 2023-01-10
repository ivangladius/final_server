package org.example;


public class Main {

	public static void main(String[] args) {

		Server server = new Server(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
//		Server server = new Server(9999, 3);

		server.start();

		server.loop();

		server.stop();
	}

}
