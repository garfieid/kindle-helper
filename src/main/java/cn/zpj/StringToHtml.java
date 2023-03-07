package cn.zpj;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileSystemView;

/**
 * @author zhangpengji
 */
public class StringToHtml {

    static final Pattern TITLE_PATTERN = Pattern.compile("(.+?)\\(Kindle位置");
    static final Pattern TITLE_PATTERN2 = Pattern.compile("(.+?)\\(p.");

    static final List<String> END_CHARS = Arrays.asList("。", "？", "！", "”", "，", "、", "；", ")", "）");

    String last;
    String half;
    FileOutputStream fos;

    public StringToHtml() {
        half = "";
    }

    public void save(String content, String fileName) throws Exception {
        if (null == content || 0 == content.length()) {
            return;
        }
        if (content.equals(last)) {
            return;
        }
        last = content;
        String[] contents = content.split("\n");
        if (null == fos) {
            String last = contents[contents.length - 1];
            Matcher m = TITLE_PATTERN.matcher(last);
            if (!m.find()) {
                m = TITLE_PATTERN2.matcher(last);
                m.find();
            }
            String name = m.group(1);
            String path = FileSystemView.getFileSystemView().getHomeDirectory().getPath() + File.separator + name;
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdir();
            }
            boolean append = true;
            if (null == fileName || fileName.length() == 0) {
                append = false;
                fileName = contents[0];
                if (fileName.length() > 20) {
                    fileName = fileName.substring(0, 20);
                }
            }
            path = dir.getAbsolutePath() + File.separator + fileName + ".html";
            System.out.println(path);
            if (append) {
                FileInputStream fis = new FileInputStream(path);
                byte[] buf = new byte[100 * 1024]; // 100k
                int len = fis.read(buf);
                String str = new String(buf, 0, len, StandardCharsets.UTF_8);
                if (str.endsWith("</body></html>")) {
                    str = str.substring(0, str.length() - "</body></html>".length());
                }
                fis.close();
                fos = new FileOutputStream(path);
                fos.write(str.getBytes(StandardCharsets.UTF_8));
                System.out.println(str.substring(str.length() - 100));
            } else {
                fos = new FileOutputStream(path);
                fos.write("<html><body style=\"min-width:780px\">".getBytes(StandardCharsets.UTF_8));
            }
        }
        String p = contents[0];
        if (0 == half.length() && p.length() > 60 && !END_CHARS.contains(p.substring(p.length() - 1))) {
            half = p;
        } else {
            p = "<p>" + half + p + "</p>";
            half = "";
            System.out.println(p);
            fos.write(p.getBytes(StandardCharsets.UTF_8));
            fos.flush();
        }
    }

    public void start(String fileName) throws Exception {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        while (true) {
            synchronized (this) {
                try {
                    wait(1000);
                } catch (InterruptedException ignore) {
                    end();
                    break;
                }
            }
            if (!clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                continue;
            }
            String str = clipboard.getData(DataFlavor.stringFlavor).toString();
            str = str.replace(" ", "");
            if (!str.endsWith("Kindle版本.")) {
                continue;
            }
            save(str, fileName);
        }
    }

    public void end() {
        if (null != fos) {
            try {
                fos.write("</body></html>".getBytes(StandardCharsets.UTF_8));
                fos.close();
                fos = null;
            } catch (IOException ignore) {
            }
        }
    }

}
