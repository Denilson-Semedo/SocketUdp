package socketudp;

import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.sql.*;
import java.util.Calendar;
import javax.xml.*;
//import javax.xml.bind.DatatypeConverter;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class UDPServer {
    
    public static String generateHash(String password, String salt){
        String hash = null;
        
        try 
        {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt.getBytes());
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16)
                        .substring(1));
            }
            hash = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }        
        return hash;
    }
    
    private static String getSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt.toString();
    }

    public static void main(String args[]) throws Exception {

        int porta = 9876;
        int numConn = 1;

        DatagramSocket serverSocket = new DatagramSocket(porta);

        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];
        
        System.out.println(". . . Servidor disponivel. . .\n");

        while (true) {

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            //System.out.println("Esperando por datagrama UDP com nome na porta " + porta);
            serverSocket.receive(receivePacket);
           // System.out.print("Datagrama UDP [" + numConn + "] recebido...");

            String sentence = new String(receivePacket.getData());
            String[] dados = sentence.split(";");
            String username = dados[0].trim();
            String password = dados[1].trim();

            //System.out.println(sentence);

            InetAddress IPAddress = receivePacket.getAddress();

            int port = receivePacket.getPort();
            
            
           

            System.out.print("Username: " + username);
            System.out.print("  Password: " + password);
            
            password = generateHash(password,getSalt());
            
            String db_user = System.getenv("BD_USER");
            String db_pass = System.getenv("BD_PASS");
            
            try {
                // create a mysql database connection
                String myUrl = "jdbc:mysql://localhost:3306/teste?useTimezone=true&serverTimezone=UTC";
                //Class.forName(myDriver);
                Connection conn = DriverManager.getConnection(myUrl, db_user, db_pass);
                
                 

                // the mysql insert statement
                String query = "insert into user (username,password,password2) values (?,?)";

                // create the mysql insert preparedstatement
                PreparedStatement preparedStmt = conn.prepareStatement(query);
                preparedStmt.setString(1, username);
                preparedStmt.setString(2, password);

                // execute the preparedstatement
                preparedStmt.execute();

                conn.close();
            } catch (Exception e) {
                System.err.println("Got an exception!" + e.getMessage());
                System.err.println(e.getMessage());
            }

            /*String capitalizedSentence = "Olá " + sentence.toUpperCase();

            sendData = capitalizedSentence.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);

            System.out.print("Enviando " + capitalizedSentence + "...");

            serverSocket.send(sendPacket);
            System.out.println("OK\n");*/
        }
    }
}
