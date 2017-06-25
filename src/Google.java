import java.net.URLEncoder;
import java.util.Scanner;

import javax.xml.bind.DatatypeConverter;


import com.google.openrtb.OpenRtb.BidRequest;
import com.google.openrtb.OpenRtb.BidRequest.Imp;
import com.google.openrtb.OpenRtb.BidRequest.Imp.Video;
import com.google.openrtb.OpenRtb.BidResponse;
import com.google.openrtb.OpenRtb.Protocol;
import com.google.openrtb.OpenRtb.VideoLinearity;

public class Google {
	
	public static void main(String [] args) throws Exception {
		
		String id = "W3kk3k3k3\\x";
		System.out.println("B4:"  + id);
		id = URLEncoder.encode(id, "UTF-8");
		System.out.println("AFTER: " + id);
		Scanner sc = new Scanner(System.in);
		while(true) {
			System.out.print(">>>");;
			String line = sc.nextLine();
			new Google().response(line);
		}
		//new Google().response("ChZXUEFzekFBSU1TWUtUN05HZFFWdVFBEtUICscIChZXUEFzekFBSU1TWUtUN05HZFFWdVFBEgExGZqZmZmZmbk/IgI1NSpfaHR0cDovLzU0LjIzNC4yNTIuMjE2OjgwODAvcnRiL3dpbi9nb29nbGUvJHtBVUNUSU9OX1BSSUNFfS8wLjAvMC4wLzU1LzQxL1dQQXN6QUFJTVNZS1Q3TkdkUVZ1UUEyzwY8aW1nIHNyYz1cImh0dHA6Ly81NC4yMzQuMjUyLjIxNjo4MDgwL3J0Yi93aW4vZ29vZ2xlLzU1LzQxLyUlV0lOTklOR19QUklDRSUlL3tsYXR9L3tsb259L1dQQXN6QUFJTVNZS1Q3TkdkUVZ1UUFcIiBoZWlnaHQ9XCIxXCIgd2lkdGg9XCIxXCIgc3R5bGU9XCJkaXNwbGF5Om5vbmU7XCIvPjxpbWcgc3JjPVwiaHR0cDovLzU0LjIzNC4yNTIuMjE2OjgwODAvcGl4ZWw/YWRfaWQ9NTUmY3JlYXRpdmVfaWQ9NDFcIiBoZWlnaHQ9XCIxXCIgd2lkdGg9XCIxXCIgc3R5bGU9XCJkaXNwbGF5Om5vbmU7XCIvPjxzY3JpcHQgdHlwZT1cInRleHQvamF2YXNjcmlwdFwiPnZhciBhZG9zID0gYWRvcyB8fCB7fTthZG9zLnJ1biA9IGFkb3MucnVuIHx8IFtdOy8qIGxvYWQgcGxhY2VtZW50IGZvciBhY2NvdW50OiBDMVgsIGNhbXBhaWduOiBCaWRkZXIzX1Rlc3QsIGZsaWdodDogQURYLCBjcmVhdGl2ZTogQmlkZGVyM19BRFhfMzAweDI1MCwgc2l0ZTogRG91YmxlQ2xpY2sgQWRFeGNoYW5nZSwgc2l6ZTogTWVkaXVtIFJlY3RhbmdsZSAtIDMwMHB4IGJ5IDI1MHB4Ki9hZG9zLnJ1bi5wdXNoKGZ1bmN0aW9uKCkge2Fkb3NfYWRkSW5saW5lUGxhY2VtZW50KDk3NDcsIDQyMTMyMSwgNSkuc2V0RmxpZ2h0Q3JlYXRpdmVJZCgzNTY2ODY4KS5zZXRSZWRpcmVjdFVybCgnJSVDTElDS19VUkxfVU5FU0MlJWh0dHA6Ly81NC4yMzQuMjUyLjIxNjo4MDgwL3JlZGlyZWN0P2FkX2lkPTU1JmNyZWF0aXZlX2lkPTQxJnVybD0nKS5sb2FkSW5saW5lKCk7fSk7PC9zY3JpcHQ+PHNjcmlwdCB0eXBlPVwidGV4dC9qYXZhc2NyaXB0XCIgc3JjPVwiaHR0cHM6Ly9zdGF0aWMuYWR6ZXJrLm5ldC9hZG9zLmpzXCI+PC9zY3JpcHQ+Og5jMWV4Y2hhbmdlLmNvbUJKaHR0cHM6Ly9zdGF0aWMuYWR6ZXJrLm5ldC9BZHZlcnRpc2Vycy81YzM0ZGQ0MWEwNTc0MTNkYTdiYzk4MWJmNmNlMDM0OC5wbmdKAjU1UgI0MYABrAKIAfoBEglnb29nbGUtaWQaFldQQXN6QUFJTVNZS1Q3TkdkUVZ1UUEiA1VTRA==");
	}

	public Google() {

	}
	
	public void response(String proto) throws Exception  {
		byte[] data = DatatypeConverter.parseBase64Binary(proto);
		BidResponse r = BidResponse.parseFrom(data);
		System.out.println(r.toString());
	}

	public void request() {
		BidRequest request = BidRequest.newBuilder()
		    .setId("1")
		    .addBcat("IAB1")
		    .addImp(Imp.newBuilder()
		        .setId("1")
		        .setBidfloor(4000)
		        .setVideo(Video.newBuilder()
		            .setLinearity(VideoLinearity.LINEAR)
		            .addProtocols(Protocol.VAST_3_0)
		            .setW(640)
		            .setH(480)))
		    .build();
	}
}
