import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;


/**
 *
 * @author Сергей
 */
public class JavaApplication2 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
      
        // Последовательность бит
        byte[] bitSequence = {0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x01, 0x03, 0x00, 0x02, 0x00, 0x03};

        // IP-адрес и порт микроконтроллера
        String ipAddress = "192.168.0.100";
        int port = 1234;

        try {
            // Создание сокета и подключение к микроконтроллеру
            Socket socket = new Socket(ipAddress, port);

            // Получение потока вывода для отправки данных
            OutputStream outputStream = socket.getOutputStream();

            // Отправка последовательности бит на микроконтроллер
            outputStream.write(bitSequence);

            // Закрытие соединения
            outputStream.close();
            socket.close();

            System.out.println("Последовательность бит успешно отправлена.");
        } catch (UnknownHostException e) {
            System.err.println("Неизвестный хост: " + ipAddress);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Ошибка ввода/вывода при отправке данных.");
            e.printStackTrace();
        }
    }
    
}
