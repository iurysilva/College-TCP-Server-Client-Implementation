package Client;

import java.net.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;


public class Client {

    public static void main(String[] args) {
        final File[] fileToSend = new File[1];

        JFrame jFrame = new JFrame("Client");
        jFrame.setSize(450, 450);
        jFrame.setLayout(new BoxLayout(jFrame.getContentPane(), BoxLayout.Y_AXIS));
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel jlTitle = new JLabel("File Sender");
        jlTitle.setFont(new Font("Arial", Font.BOLD, 25));
        jlTitle.setBorder(new EmptyBorder(20, 0, 10, 0));
        jlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel jlFileName = new JLabel("choose a file to send");
        jlFileName.setFont(new Font("Arial", Font.BOLD, 20));
        jlFileName.setBorder(new EmptyBorder(50, 0, 0, 0));
        jlFileName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel jpButton = new JPanel();
        jpButton.setBorder(new EmptyBorder(75, 0, 10, 0));

        JButton jbSendFile = new JButton("Send File");
        jbSendFile.setPreferredSize(new Dimension(150, 75));
        jbSendFile.setFont(new Font("Arial", Font.BOLD, 20));

        JButton jbChooseFile = new JButton("Choose File");
        jbChooseFile.setPreferredSize(new Dimension(150, 75));
        jbChooseFile.setFont(new Font("Arial", Font.BOLD, 20));

        jpButton.add(jbSendFile);
        jpButton.add(jbChooseFile);

        jbChooseFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setDialogTitle("Choose a file to send");

                if (jFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
                    fileToSend[0] = jFileChooser.getSelectedFile();
                    jlFileName.setText("The file you want to send is: " + fileToSend[0].getName());
                }
            }
        });

        jbSendFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Socket socket = null;
                if (fileToSend[0] == null){
                    jlFileName.setText("Please choose a file first.");
                } else{
                    try{
                        FileInputStream fileInputStream = new FileInputStream(fileToSend[0].getAbsolutePath());
                        int serverPort = 7896;
                        socket = new Socket(args[0], serverPort);
                        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                        String fileName = fileToSend[0].getName();
                        byte[] fileNameBytes = fileName.getBytes();
                        byte[] fileContentBytes = new byte[(int)fileToSend[0].length()];
                        fileInputStream.read(fileContentBytes);

                        //dataOutputStream.writeInt(fileNameBytes.length);
                        //dataOutputStream.write(fileNameBytes);

                        dataOutputStream.writeInt(fileContentBytes.length);
                        dataOutputStream.write(fileContentBytes);
                        System.out.println("Image sent to: " + args[0]);

                        int fileContentLength = dataInputStream.readInt();
                        byte[] compressedFileContentBytes = new byte[fileContentLength];
                        dataInputStream.readFully(compressedFileContentBytes, 0, compressedFileContentBytes.length);
                        try {
                            FileOutputStream stream = new FileOutputStream("files/compressed_image.jpg");
                            stream.write(compressedFileContentBytes);
                            stream.close();
                            System.out.println("Compressed image was received");
                        }catch (IOException ex) {
                            ex.printStackTrace();
                        }

                    }catch (IOException error){
                        error.printStackTrace();
                    }finally{
                        if (socket != null){
                            try{
                                socket.close();
                            }catch(IOException error){
                                System.out.println("It was not possible to close the socket");
                            }
                        }
                    }
                }
            }
        });

        jFrame.add(jlTitle);
        jFrame.add(jlFileName);
        jFrame.add(jpButton);
        jFrame.setVisible(true);
    }
}
