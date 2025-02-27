import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class SlackUploader {
    private static final String SLACK_TOKEN = System.getenv("SLACK_BOT_TOKEN"); // GitHub Secrets 사용
    private static final String SLACK_CHANNEL = "C08E97AN0R3"; // 실제 채널 ID 입력

    public static void uploadFileToSlack(File file, String message) {
        try {
            String url = "https://slack.com/api/files.upload";
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + SLACK_TOKEN);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=boundary");
            conn.setDoOutput(true);

            // 파일 읽기
            byte[] fileBytes = Files.readAllBytes(Path.of(file.getAbsolutePath()));

            // Slack 파일 업로드 요청 데이터 생성
            String payload = "--boundary\r\n"
                    + "Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n"
                    + "Content-Type: application/octet-stream\r\n\r\n";
            String endPayload = "\r\n--boundary--";

            // 전송
            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes());
                os.write(fileBytes);
                os.write(endPayload.getBytes());
            }

            // 응답 확인
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                System.out.println("✅ Slack 파일 업로드 성공: " + file.getName());
            } else {
                System.out.println("❌ Slack 파일 업로드 실패: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        File folder = new File("."); // 현재 디렉토리 탐색
        File latestTxt = null;
        File latestJpg = null;

        // 최근 생성된 파일 찾기
        for (File file : folder.listFiles()) {
            if (file.getName().endsWith(".txt") && (latestTxt == null || file.lastModified() > latestTxt.lastModified())) {
                latestTxt = file;
            }
            if (file.getName().endsWith(".jpg") && (latestJpg == null || file.lastModified() > latestJpg.lastModified())) {
                latestJpg = file;
            }
        }

        // 파일 업로드 실행
        if (latestTxt != null) {
            uploadFileToSlack(latestTxt, "📝 최신 텍스트 파일 업로드");
        }
        if (latestJpg != null) {
            uploadFileToSlack(latestJpg, "🖼️ 최신 이미지 파일 업로드");
        }
    }
}
