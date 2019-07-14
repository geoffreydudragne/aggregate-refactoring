package kata.dbtestutil;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MemoryDbTestContext {

    private MemoryDbTestContext(Jdbi jdbi, Handle handle) {
        this.jdbi = jdbi;
        this.handle = handle;
    }

    private static String loadSetupSql(String file) throws URISyntaxException, IOException {
        Path sqlPath = Paths.get(MemoryDbTestContext.class.getResource(file).toURI());
        return new String(Files.readAllBytes(sqlPath));
    }

    public static MemoryDbTestContext openWithSql(String resourcePath) throws IOException, URISyntaxException {
        String sqlScript = loadSetupSql(resourcePath);
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test");
        Handle handle = jdbi.open();
        handle.createScript(sqlScript).execute();
        return new MemoryDbTestContext(jdbi, handle);
    }

    public void close() {
        handle.close();
    }

    private Jdbi jdbi;

    private final Handle handle;

    public Jdbi getJdbi() {
        return jdbi;
    }
}
