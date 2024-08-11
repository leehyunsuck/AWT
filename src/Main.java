import gemini.Gemini;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Start Program");

        String test = """
                너의 역할은 블로거야.
                
                주제를 기반으로 MARKDOWN 형식으로 블로그 글을 작성해.
                
                참고로 블로그 형식으로 해야하니 소제목, 내용, 요약 형식으로 작성해.
                
                글에 맞는 내용과 해시태그10개도 작성해
                
                답변 형식:
                {
                    \\"head\\" : \\"제목\\"
                    \\"content\\" : \\"내용\\"
                    \\"hashTag\\" : [\\"태그1\\", \\"태그2\\", ..., \\"태그10\\"]
                }
                
                주제:
                아무리 해도 질리지 않는 존잼 모바일 게임 추천해주세용
                요즘 방학이라 시간이 많이 나서 평일엔 게임하면서 지내는데 계속 같은 게임만 하니까 질리고 재미도 없늬ㅣ요 진짜 갓겜 추천해주세요 액션 게임이면 더 좋고요 멀티 플레이가 되면 좋습니다 제 취향은 그래픽 좋고 타격감 ㅈ되는 게임입니다 추천 ㄱㄱ
                """;

        Gemini gemini = new Gemini();
        gemini.request(test);
    }
}