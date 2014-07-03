package toolkit.derby;

import org.apache.derby.drda.NetworkServerControl;

import java.io.File;
import java.util.logging.Logger;

/**
 * @author simetrias
 */

public class DerbyUtils {

    private static final Logger logger = Logger.getLogger(DerbyUtils.class.getName());

    public static void startDerbyServer() throws Exception {
        System.setProperty("derby.drda.startNetworkServer", "true");
        System.setProperty("derby.system.home", System.getProperty("user.home") + "/.netbeans-derby/");
        System.out.println("Starting Derby Network Server");
        String property = System.getProperty("derby.system.home");
        System.out.println("derby.system.home=" + new File(property).getAbsolutePath());
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
        NetworkServerControl server = new NetworkServerControl();

        System.out.println("Pinging Network Server ...");

        for (int i = 0; i < 10; i++) {
            try {
                server.ping();
                break;
            } catch (Exception e) {
                System.out.println("Try #" + i + ": " + e.toString());
                if (i == 9) {
                    System.out.println("Network Server does *not* respond.");
                    throw e;
                } else {
                    Thread.sleep(5000);
                }
            }
        }

        System.out.println("Derby Network Server OK.");
    }

}
