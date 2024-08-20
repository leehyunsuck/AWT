import gemini.Gemini;
import tistoy.TistoryWrite;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Start Program");

        Gemini gemini = new Gemini();

        TistoryWrite tistoryWrite = new TistoryWrite();
        tistoryWrite.request(gemini.request("러시아 우크라이나 전쟁 관련"));
    }
}