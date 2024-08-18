import gemini.Gemini;
import tistoy.TistoryWrite;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Start Program");

        Gemini gemini = new Gemini();
        String[] aiAnswer = gemini.request("마인크래프트 1.21 버전에서 치즐 모드 사용하는 방법이 있나요?");

        TistoryWrite tistoryWrite = new TistoryWrite();
        tistoryWrite.request(aiAnswer);
    }
}