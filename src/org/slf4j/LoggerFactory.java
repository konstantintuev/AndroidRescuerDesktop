package org.slf4j;

public class LoggerFactory {
    public static Logger getLogger(Class<?> clas) {
        return new Logger() {
            @Override
            public void info(String message) {
                System.out.println("[INFO] "+ clas.getSimpleName() + " "+message);
            }
            @Override
            public void error(String message) {
                System.err.println("[ERR] "+ clas.getSimpleName() + " "+message);
            }
        };
    }
}
