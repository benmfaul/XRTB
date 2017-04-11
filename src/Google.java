import javax.xml.bind.DatatypeConverter;

import com.google.openrtb.OpenRtb.BidRequest;
import com.google.openrtb.OpenRtb.BidRequest.Imp;
import com.google.openrtb.OpenRtb.BidRequest.Imp.Video;
import com.google.openrtb.OpenRtb.Protocol;
import com.google.openrtb.OpenRtb.VideoLinearity;
import com.xrtb.exchanges.adx.RealtimeBidding.BidResponse;

public class Google {
	
	public static void main(String [] args) throws Exception {
		// new Google().request();
		new Google().response("ChZXT3VWY0FBS0F5NEtERWZPZnd6QkZREsUICrcIChZXT3VWY0FBS0F5NEtERWZPZnd6QkZREgExGZqZmZmZmbk/IgI1NSpdaHR0cDovLzUyLjg3LjE3OC42MDo4MDgwL3J0Yi93aW4vZ29vZ2xlLyR7QVVDVElPTl9QUklDRX0vMC4wLzAuMC81NS80MS9XT3VWY0FBS0F5NEtERWZPZnd6QkZRMskGPGltZyBzcmM9XCJodHRwOi8vNTIuODcuMTc4LjYwOjgwODAvcnRiL3dpbi9nb29nbGUvNTUvNDEvJSVXSU5OSU5HX1BSSUNFJSUve2xhdH0ve2xvbn0vV091VmNBQUtBeTRLREVmT2Z3ekJGUVwiIGhlaWdodD1cIjFcIiB3aWR0aD1cIjFcIiBzdHlsZT1cImRpc3BsYXk6bm9uZTtcIi8+PGltZyBzcmM9XCJodHRwOi8vNTIuODcuMTc4LjYwOjgwODAvcGl4ZWw/YWRfaWQ9NTUmY3JlYXRpdmVfaWQ9NDFcIiBoZWlnaHQ9XCIxXCIgd2lkdGg9XCIxXCIgc3R5bGU9XCJkaXNwbGF5Om5vbmU7XCIvPjxzY3JpcHQgdHlwZT1cInRleHQvamF2YXNjcmlwdFwiPnZhciBhZG9zID0gYWRvcyB8fCB7fTthZG9zLnJ1biA9IGFkb3MucnVuIHx8IFtdOy8qIGxvYWQgcGxhY2VtZW50IGZvciBhY2NvdW50OiBDMVgsIGNhbXBhaWduOiBCaWRkZXIzX1Rlc3QsIGZsaWdodDogQURYLCBjcmVhdGl2ZTogQmlkZGVyM19BRFhfMzAweDI1MCwgc2l0ZTogRG91YmxlQ2xpY2sgQWRFeGNoYW5nZSwgc2l6ZTogTWVkaXVtIFJlY3RhbmdsZSAtIDMwMHB4IGJ5IDI1MHB4Ki9hZG9zLnJ1bi5wdXNoKGZ1bmN0aW9uKCkge2Fkb3NfYWRkSW5saW5lUGxhY2VtZW50KDk3NDcsIDQyMTMyMSwgNSkuc2V0RmxpZ2h0Q3JlYXRpdmVJZCgzNTY2ODY4KS5zZXRSZWRpcmVjdFVybCgnJSVDTElDS19VUkxfVU5FU0MlJWh0dHA6Ly81Mi44Ny4xNzguNjA6ODA4MC9yZWRpcmVjdD9hZF9pZD01NSZjcmVhdGl2ZV9pZD00MSZ1cmw9JykubG9hZElubGluZSgpO30pOzwvc2NyaXB0PjxzY3JpcHQgdHlwZT1cInRleHQvamF2YXNjcmlwdFwiIHNyYz1cImh0dHBzOi8vc3RhdGljLmFkemVyay5uZXQvYWRvcy5qc1wiPjwvc2NyaXB0PjoOYzFleGNoYW5nZS5jb21CSmh0dHBzOi8vc3RhdGljLmFkemVyay5uZXQvQWR2ZXJ0aXNlcnMvNWMzNGRkNDFhMDU3NDEzZGE3YmM5ODFiZjZjZTAzNDgucG5nSgI1NVICNDESCWdvb2dsZS1pZBoWV091VmNBQUtBeTRLREVmT2Z3ekJGUQ==");
	}
	public Google() {

	}
	
	public void response(String proto) throws Exception  {
		byte[] data = DatatypeConverter.parseBase64Binary(proto);
		BidResponse r = BidResponse.parseFrom(data);
		System.out.println(r);
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
