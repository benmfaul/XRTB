package test.java;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xrtb.bidder.MimeTypes;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.Node;
import com.xrtb.db.User;
import com.xrtb.pojo.BidRequest;

/**
 * Tests the constraint node processing.
 * @author Ben M. Faul
 *
 */
public class TestMimeTypes {

	@Test
	public void testScript() {
		String data = "<script type=\"text/javascript\" src=\"http://us.mobrt.com/unit/player.aspx?sga=152&size=320x50&banner=skywars_320x50.jpg&publisher_id=7702&subid=[REPLACE-SUBID]&idfa=[REPLACE-IDFA]\"></script>";
		String type = MimeTypes.determineType(data);
		assertTrue(type.equals("text/javascript"));
		type = MimeTypes.determineType("<!-- 320x50 --><div id='hjsdtu533'></div><script type='text/javascript'>;var ppp='819f5e9e-69ef-11e6-8b77-86f30ca893d3';var rand=Math.round(new Date().getTime());var vl1='108622_{app_id}', vl3='22';document.write('<scri'+'pt type=\"text/javascript\" src=\"http://sv.rtbcon.com/sv?vv=hjsdtu533&ppp='+ppp+'&vl1='+vl1+'&vl3='+vl3+'&rand='+rand+ '&mmt=1\"></scri'+'pt>');</script>");
		assertTrue(type.equals("text/javascript"));
		System.out.println(type);
	}
}
