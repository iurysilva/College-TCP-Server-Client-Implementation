package Server;

import java.net.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.*;
import javax.imageio.stream.*;
import java.util.Iterator;

public class Server {

    public static void main(String[] args){
        try{
            int serverPort = 7896;
            ServerSocket listenSocket = new ServerSocket(serverPort);

            while (true){
                Socket clientSocket = listenSocket.accept();
                Connection c = new Connection(clientSocket);
            }
        }
        catch(IOException e){
            System.out.println("Listen: " + e.getMessage());
        }
    }
}

class Connection extends Thread{
    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;

    public Connection(Socket aClientSocket){
        try{
            clientSocket = aClientSocket;
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            this.start();
        }
        catch (IOException e){
            System.out.println("Server.Connection:" + e.getMessage());
        }
    }

    public void run(){
        String imageName = "files/image.jpg";
        try{
            int fileContentLength = in.readInt();
            byte[] fileContentBytes = new byte[fileContentLength];
            in.readFully(fileContentBytes, 0, fileContentBytes.length);
            try{
                FileOutputStream stream = new FileOutputStream(imageName);
                stream.write(fileContentBytes);
                stream.close();

                File input = new File(imageName);
                BufferedImage image = ImageIO.read(input);
                File compressedImageFile = new File("files/compressed_image.jpg");
                OutputStream compressionStream = new FileOutputStream(compressedImageFile);
                Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
                ImageWriter writer = (ImageWriter) writers.next();
                ImageOutputStream ios = ImageIO.createImageOutputStream(compressionStream);
                writer.setOutput(ios);
                ImageWriteParam param = writer.getDefaultWriteParam();
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(0.15f);  // Change the quality value you prefer
                writer.write(null, new IIOImage(image, null, null), param);
                compressionStream.close();
                ios.close();
                writer.dispose();
                System.out.println("The image was compressed, sending result to client...");

                File input2 = new File("files/compressed_image.jpg");
                FileInputStream fileInputStream = new FileInputStream(input2.getAbsolutePath());
                byte[] compressedFileContentBytes = new byte[(int)input2.length()];
                fileInputStream.read(compressedFileContentBytes);
                out.writeInt(compressedFileContentBytes.length);
                out.write(compressedFileContentBytes);

            }catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        catch (EOFException e){
            System.out.println("EOF: " + e.getMessage());
        }
        catch (IOException e){
            System.out.println("Server.Connection:" + e.getMessage());
        }
        finally {
            try{
                clientSocket.close();
            }
            catch (IOException e){
                System.out.println("It was not possible to close the socket");
            }
        }
    }
}