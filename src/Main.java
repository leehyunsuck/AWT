import gemini.Gemini;
import tistoy.TistoryWrite;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Gemini gemini = new Gemini();

        TistoryWrite tistoryWrite = new TistoryWrite();

        while(true) {
            System.out.print("[Input] Enter 'stop' to exit or any other input to continue: ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            if(input.toLowerCase().equals("stop")) break;
            tistoryWrite.request(gemini.request(input));
        }
    }
}