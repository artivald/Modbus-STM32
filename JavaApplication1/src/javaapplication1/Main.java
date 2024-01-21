import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Main {
    private static JTextField entryIP;
    private static JTextField entryPort;
    private static JTextField entryFilePath;
    private static JTextField entryData;
    private static JTextArea outputText;
    private static JProgressBar progressBar;
    private static JFrame window; // Added window reference

    public static void main(String[] args) {
        // Создание графического интерфейса
        window = new JFrame("Прошивка");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(500, 400);
        window.setLayout(new BorderLayout());

        JPanel frame = new JPanel();
        frame.setLayout(new FlowLayout());
        window.add(frame, BorderLayout.NORTH);

        JLabel labelIP = new JLabel("IP-адрес устройства:");
        frame.add(labelIP);

        entryIP = new JTextField(20);
        frame.add(entryIP);

        JLabel labelPort = new JLabel("Порт соединения:");
        frame.add(labelPort);

        entryPort = new JTextField(20);
        frame.add(entryPort);

        JLabel labelFilePath = new JLabel("Выберите файл прошивки:");
        frame.add(labelFilePath);

        entryFilePath = new JTextField(20);
        frame.add(entryFilePath);

        JButton buttonSelect = new JButton("Выбрать файл");
        buttonSelect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectFile();
            }
        });
        frame.add(buttonSelect);

        JLabel labelData = new JLabel("Введите данные (в битах):");
        frame.add(labelData);

        entryData = new JTextField(20);
        frame.add(entryData);

        JButton buttonSendFirmware = new JButton("Отправить прошивку");
        buttonSendFirmware.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendFirmware();
            }
        });
        frame.add(buttonSendFirmware);

        JButton buttonSendBits = new JButton("Отправить биты");
        buttonSendBits.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendBits();
            }
        });
        frame.add(buttonSendBits);

        JPanel progressFrame = new JPanel();
        window.add(progressFrame, BorderLayout.CENTER);

        JLabel progressLabel = new JLabel("Прогресс:");
        progressFrame.add(progressLabel);

        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(300, 20));
        progressFrame.add(progressBar);

        JPanel outputFrame = new JPanel();
        outputFrame.setLayout(new BorderLayout());
        window.add(outputFrame, BorderLayout.SOUTH);

        JLabel outputLabel = new JLabel("Вывод информации:");
        outputFrame.add(outputLabel, BorderLayout.NORTH);

        outputText = new JTextArea(10, 40);
        outputText.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputText);
        outputFrame.add(scrollPane, BorderLayout.CENTER);

        window.setVisible(true);
    }

    private static void selectFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("/"));
        fileChooser.setDialogTitle("Select Firmware File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Hex Files", "hex"));

        int result = fileChooser.showOpenDialog(window); // Use the window reference
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
entryFilePath.setText(selectedFile.getAbsolutePath());
        }
    }

    private static void sendFirmware() {
        appendText("Отправка прошивки...");

        String host = entryIP.getText(); // IP-адрес устройства
        int port = Integer.parseInt(entryPort.getText()); // Порт для соединения

        String filepath = entryFilePath.getText();

        try {
            // Создание TCP-сокета и подключение к устройству
            Socket sock = new Socket(host, port);

            // Определение размера файла
            File file = new File(filepath);
            long fileSize = file.length();

            // Отправка размера файла
            OutputStream outputStream = sock.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeLong(fileSize);

            // Отправка файла
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytesSent = 0;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesSent += bytesRead;
                double progress = (double) totalBytesSent / fileSize * 100;
                progressBar.setValue((int) progress);
                window.revalidate();
                window.repaint();
            }

            appendText("Файл прошивки успешно отправлен!");

            // Вычисление хэша файла
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            FileInputStream fileInputStreamHash = new FileInputStream(file);
            byte[] dataBuffer = new byte[8192];
            int bytesReadHash;
            while ((bytesReadHash = fileInputStreamHash.read(dataBuffer)) != -1) {
                md.update(dataBuffer, 0, bytesReadHash);
            }
            byte[] fileHash = md.digest();

            // Отправка хэша файла
            outputStream.write(fileHash);
            appendText("Хэш файла успешно отправлен!");

            // Получение ответа от устройства
            InputStream inputStream = sock.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String response = reader.readLine();
            appendText("Получен ответ от устройства: " + response);

            sock.close();

        } catch (Exception e) {
            appendText("Произошла ошибка при отправке файла: " + e.getMessage());
        }
    }
    private static void sendBits() {
    appendText("Отправка битов...");

    String host = entryIP.getText(); // IP-адрес устройства
    int port = Integer.parseInt(entryPort.getText()); // Порт для соединения

    String dataBits = entryData.getText(); // Последовательность битов, введенная пользователем

    try {
        // Создание TCP-сокета и подключение к устройству
        Socket sock = new Socket(host, port);

        // Преобразование последовательности битов в целое число
        String bits = dataBits.replace(" ", ""); // Удаление пробелов
        byte[] dataBytes = new byte[(bits.length() + 7) / 8]; // Размер байтового массива
        for (int i = 0; i < bits.length(); i++) {
            if (bits.charAt(i) == '1') {
                int byteIndex = i / 8;
                int bitIndex = i % 8;
                dataBytes[byteIndex] |= (1 << (7 - bitIndex));
            }
        }

        // Отправка данных (битового массива)
        OutputStream outputStream = sock.getOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.write(dataBytes);
        appendText("Биты успешно отправлены!");

        // Получение ответа от устройства
        InputStream inputStream = sock.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String response = reader.readLine();
        appendText("Получен ответ от устройства: " + response);

        sock.close();

    } catch (Exception e) {
        appendText("Произошла ошибка при отправке битов: " + e.getMessage());
    }
}
