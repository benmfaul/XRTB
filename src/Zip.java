import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.GZIPOutputStream;

public class Zip {
	
	public static void main(String args[]) throws Exception  {
		String content = new String(Files.readAllBytes(Paths.get("SampleBids/nexage.txt")), StandardCharsets.UTF_8);
		byte [] bytes  = compressString(content);
		Files.write(Paths.get("SampleBids/nexage.gz"), bytes);
		System.out.println("Wrote " + bytes.length + " bytes");
	}

	public static byte[]  compressString(String str) throws IOException{
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(str.getBytes(StandardCharsets.UTF_8));
        gzip.close();
        return out.toByteArray();
		}
}
