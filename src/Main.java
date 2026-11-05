import java.io.IOException;
import museu.MuseuApplication;
import museu.config.ServerConfig;

public class Main {

    public static void main(String[] args) throws IOException {
        MuseuApplication.start(ServerConfig.fromEnvironment());
    }
}
