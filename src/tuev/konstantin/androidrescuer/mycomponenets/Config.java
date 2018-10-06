package tuev.konstantin.androidrescuer.mycomponenets;

import javafx.application.Platform;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Scanner;

public class Config {
    public static final boolean TEST = true;
    public static String root = "https://www.androidrescuer.cf";
    public static String homeUrl = root + "/server/";
    public enum url {
        SENDCONTROLSMS("sendcontrol"),
        GETUSERLOC("getloc");

        public String value;

        url(String value)
        {
            this.value = homeUrl + value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum ResponseJson {
        TEXT("result"),
        ERROR("error");

        private String value;
        ResponseJson(String item) {
            value = item;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public interface onReady {
        void ready(String out);
    }

    public static class CallAPI {
        onReady handler;
        String json;

        public CallAPI(String url, String json, onReady handler) {
            this.handler = handler;
            this.json = json;
            new Thread(() -> doInBackground(url)).start();
        }


        void doInBackground(String... params) {

            String urlString = params[0]; // URL to call

            String resultToDisplay = "";

            InputStream in;
            try {

                URL url = new URL(urlString);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);

                OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());

                String data = "";
                data += URLEncoder.encode("json", "UTF-8") + "="
                        + URLEncoder.encode(json, "UTF-8");


                wr.write(data);
                wr.flush();


                int statusCode = urlConnection.getResponseCode();
                if (statusCode >= 200 && statusCode < 400) {
                    in = new BufferedInputStream(urlConnection.getInputStream());
                } else {
                    in = new BufferedInputStream(urlConnection.getErrorStream());
                }

                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line).append('\n');
                }
                resultToDisplay = total.toString();
            } catch (Exception e) {

                System.out.println(e.getMessage());

                return;

            }
            String finalResultToDisplay = resultToDisplay;
            Platform.runLater(() -> handler.ready(finalResultToDisplay));
        }
    }

    /**
     * types of Operating Systems
     */
    public enum OSType {
        Windows, MacOS, Linux, Other
    };

    // cached result of OS detection
    protected static OSType detectedOS;

    /**
     * detect the operating system from the os.name System property and cache
     * the result
     *
     * @return - the operating system detected
     */
    public static OSType getOperatingSystemType() {
        if (detectedOS == null) {
            String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
            if ((OS.contains("mac")) || (OS.contains("darwin"))) {
                detectedOS = OSType.MacOS;
            } else if (OS.contains("win")) {
                detectedOS = OSType.Windows;
            } else if (OS.contains("nux")) {
                detectedOS = OSType.Linux;
            } else {
                detectedOS = OSType.Other;
            }
        }
        return detectedOS;
    }


    private static String sn = null;

    public static String getSerialNumber() {
        if (sn != null) {
            return sn;
        }
        switch (getOperatingSystemType()) {
            case MacOS:
                return getSerialNumberMac();
            case Windows:
                return getSerialNumberWin();
            case Linux:
                return getSerialNumberLinux();
            case Other:
            default:
                return null;
        }
    }

    private static String getSerialNumberLinux() {

        if (sn == null) {
            readDmidecode();
        }
        if (sn == null) {
            readLshal();
        }
        if (sn == null) {
            System.out.println("Cannot find computer SN");
        }

        return sn;
    }

    private static BufferedReader read(String command) {

        OutputStream os;
        InputStream is;

        Runtime runtime = Runtime.getRuntime();
        Process process;
        try {
            process = runtime.exec(command.split(" "));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        os = process.getOutputStream();
        is = process.getInputStream();

        try {
            os.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new BufferedReader(new InputStreamReader(is));
    }

    private static void readDmidecode() {

        String line;
        String marker = "Serial Number:";
        BufferedReader br = null;

        try {
            br = read("dmidecode -t system");
            while ((line = br.readLine()) != null) {
                if (line.contains(marker)) {
                    sn = line.split(marker)[1].trim();
                    break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void readLshal() {

        String line;
        String marker = "system.hardware.serial =";
        BufferedReader br = null;

        try {
            br = read("lshal");
            while ((line = br.readLine()) != null) {
                if (line.contains(marker)) {
                    sn = line.split(marker)[1].replaceAll("\\(string\\)|(\\')", "").trim();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String getSerialNumberWin() {

        if (sn != null) {
            return sn;
        }

        OutputStream os;
        InputStream is;

        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        try {
            process = runtime.exec(new String[] { "wmic", "bios", "get", "serialnumber" });
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (process == null) {
            return sn;
        }
        os = process.getOutputStream();
        is = process.getInputStream();

        try {
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Scanner sc = new Scanner(is);
        try {
            while (sc.hasNext()) {
                String next = sc.next();
                if ("SerialNumber".equals(next)) {
                    String nextItem = sc.next().trim();
                    if (!nextItem.equals("=")) {
                        sn = nextItem;
                    } else {
                        sn = sc.next().trim();
                    }
                    break;
                }
            }
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (sn == null) {
            System.err.println("Cannot find computer SN");
        }

        return sn;
    }

    private static String getSerialNumberMac() {

        if (sn != null) {
            return sn;
        }

        OutputStream os;
        InputStream is;

        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        try {
            process = runtime.exec(new String[] { "/usr/sbin/system_profiler", "SPHardwareDataType" });
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (process == null) {
            return sn;
        }

        os = process.getOutputStream();
        is = process.getInputStream();

        try {
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        String marker = "Serial Number";
        try {
            while ((line = br.readLine()) != null) {
                if (line.contains(marker)) {
                    sn = line.split(":")[1].trim();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (sn == null) {
            System.err.println("Cannot find computer SN");
        }

        return sn;
    }
}
