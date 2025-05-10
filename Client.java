import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.io.StringReader;

public class Client {

  private String host;
  private int port;

  public static void main(String[] args) throws UnknownHostException, IOException {
    new Client("127.0.0.1", 12345).run();
  }

  //host kismi localhosta göre ayarli, sunucuda calismasi icin sunucu ip gir.

  public Client(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public void run() throws UnknownHostException, IOException {
    // User sunucu baglanma
    Socket client = new Socket(host, port);
    System.out.println("Kullanici basariyla sunucuya baglandi.!");

    // Socket output stream port cekme
    PrintStream output = new PrintStream(client.getOutputStream());

    // nick sorgu
    Scanner sc = new Scanner(System.in);
    System.out.print("Bir kullanici adi girin: ");
    String nickname = sc.nextLine();

    // sunucu nick gönder
    output.println(nickname);

    // server msg hndl yeni thread yarat
    new Thread(new ReceivedMessagesHandler(client.getInputStream())).start();

    // msg oku sunucuya yolla
    System.out.println("Mesajlar: \n");

    // while new messages
    while (sc.hasNextLine()) {
      output.println(sc.nextLine());
    }

    // kapatma: ctrl + D
    output.close();
    sc.close();
    client.close();
  }
}

class ReceivedMessagesHandler implements Runnable {

  private InputStream server;

  public ReceivedMessagesHandler(InputStream server) {
    this.server = server;
  }

  public void run() {
    // mesaj al ekrana yazdir
    Scanner s = new Scanner(server);
    String tmp = "";
    while (s.hasNextLine()) {
      tmp = s.nextLine();
      if (tmp.charAt(0) == '[') {
        tmp = tmp.substring(1, tmp.length()-1);
        System.out.println(
            "\nKULLANICI LISTESI: " +
            new ArrayList<String>(Arrays.asList(tmp.split(","))) + "\n"
            );
      }else{
        try {
          System.out.println("\n" + getTagValue(tmp));
          // System.out.println(tmp);
        } catch(Exception ignore){}
      }
    }
    s.close();
  }

  // javax.xml.parsers da kullanilabilir (sadelik icin kullanmadim)
  public static String getTagValue(String xml){
    return  xml.split(">")[2].split("<")[0] + xml.split("<span>")[1].split("</span>")[0];
  }

}
