package toolkit.test;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author simetrias
 */
public class TestRunnerClient {

    private Integer port;

    public TestRunnerClient(Integer port) {
        this.port = port;
    }

    protected void execute(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setDoInput(true);
            connection.setDoOutput(false);
            int responseCode = connection.getResponseCode();
            if (responseCode > 200) {
                throw new Exception(connection.getResponseMessage());
            }
        } finally {
            connection.disconnect();
        }
    }

    public void execute() throws Exception {
        execute("http://localhost:" + port + "/test");
    }

    public void execute(Class testClass) throws Exception {
        execute("http://localhost:" + port + "/test?testClass=" + testClass.getName());
    }
}
