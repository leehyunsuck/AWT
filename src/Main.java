import gemini.Gemini;
import tistoy.TistoryWrite;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Gemini gemini = new Gemini();

        TistoryWrite tistoryWrite = new TistoryWrite();

        while(true) {
            System.out.print("[Input] Enter 'SS#' to exit or any other input to continue and Enter 'S#' : ");
            Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
            StringBuilder input = new StringBuilder();

            while (true) {
                String line = scanner.nextLine();
                if (line.trim().equals("SS#")) return;
                if (line.trim().equalsIgnoreCase("S#")) break;
                input.append(line).append("\n");
            }
            tistoryWrite.request(gemini.request(input.toString(), " ", " "));
        }
    }
}